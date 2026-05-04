package com.eeman.stockscout.data.repo

import com.eeman.stockscout.data.dao.AliasDao
import com.eeman.stockscout.data.dao.ItemDao
import com.eeman.stockscout.data.mappers.toAliasEntities
import com.eeman.stockscout.data.mappers.toDomain
import com.eeman.stockscout.data.mappers.toItemEntity
import com.eeman.stockscout.data.remote.api.ApiService
import com.eeman.stockscout.data.models.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ItemRepository(
    private val itemDao: ItemDao,
    private val aliasDao: AliasDao,
    private val apiService: ApiService
) {

    /**
     * Pull fresh items from remote and cache locally.
     * Returns Result.success/failure so the ViewModel can surface errors.
     */
    suspend fun syncFromRemote(): Result<Unit> {
        return try {
            val response = apiService.getItems()
            if (response.isSuccessful) {
                val dtos = response.body() ?: emptyList()

                // Wipe old data and re-insert
                itemDao.deleteAll()
                aliasDao.deleteAll()

                val itemEntities = dtos.map { it.toItemEntity() }
                val aliasEntities = dtos.flatMap { it.toAliasEntities() }

                itemDao.insertAll(itemEntities)
                aliasDao.insertAll(aliasEntities)

                Result.success(Unit)
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Observe all items as a reactive Flow for the UI */
    fun observeItems(): Flow<List<Item>> {
        return itemDao.observeAll().map { itemEntities ->
            itemEntities.map { entity ->
                val aliases = aliasDao.getAliasesForItem(entity.itemCode)
                entity.toDomain(aliases)
            }
        }
    }

    /** One-shot read for resolver (doesn't need to be reactive) */
    suspend fun getAllItemsDomain(): List<Item> {
        return itemDao.getAll().map { entity ->
            val aliases = aliasDao.getAliasesForItem(entity.itemCode)
            entity.toDomain(aliases)
        }
    }

//    suspend fun getItemDomain(itemCode: String): Item? {
//        val entity = itemDao.getByCode(itemCode) ?: return null
//        val aliases = aliasDao.getAliasesForItem(itemCode)
//        return entity.toDomain(aliases)
//    }
}

// When user taps Pick:
//suspend fun pickItem(itemCode: String) {
//    // 1. Decrement locally in Room — immediately
//    itemDao.decrementQty(itemCode)
//
//    // 2. Add to pending queue
//    val pick = PendingPickEntity(itemCode, newQty, System.currentTimeMillis())
//    pickDao.insert(pick)
//
//    // 3. Schedule WorkManager sync (with network constraint)
//    val syncWork = OneTimeWorkRequestBuilder<SyncPicksWorker>()
//        .setConstraints(
//            Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.CONNECTED)
//                .build()
//        )
//        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
//        .build()
//
//    WorkManager.getInstance(context).enqueueUniqueWork(
//        "sync_picks",
//        ExistingWorkPolicy.KEEP,  // Don't replace if already scheduled
//        syncWork
//    )
//}
//
//// Worker runs when online:
//class SyncPicksWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
//    override suspend fun doWork(): Result {
//        val pending = pickDao.getAllPending()
//        for (pick in pending) {
//            try {
//                api.postPick(pick.toDto())
//                pickDao.markSynced(pick.id)  // Remove from queue
//            } catch (e: IOException) {
//                return Result.retry()  // WorkManager retries with backoff
//            }
//        }
//        return Result.success()
//    }
//}