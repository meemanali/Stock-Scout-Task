package com.eeman.stockscout.data.repo

import android.content.Context
import androidx.work.*
import com.eeman.stockscout.data.dao.ItemDao
import com.eeman.stockscout.data.dao.PendingPickDao
import com.eeman.stockscout.data.local.PendingPickEntity
import com.eeman.stockscout.data.worker.SyncPicksWorker
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class PickRepository(
    private val context: Context,
    private val itemDao: ItemDao,
    private val pendingPickDao: PendingPickDao
) {

    val pendingCount: Flow<Int> = pendingPickDao.observePendingCount()

    suspend fun pick(itemCode: String) {
        // 1. Decrement locally — immediate, survives restart
        itemDao.decrementQty(itemCode)
        val newQty = itemDao.getQty(itemCode) ?: 0

        // 2. Queue the pick for remote sync
        pendingPickDao.insert(
            PendingPickEntity(
                itemCode = itemCode,
                newQty = newQty,
                timestamp = System.currentTimeMillis()
            )
        )

        // 3. Schedule WorkManager with network constraint + exponential backoff
        scheduleSyncWorker()
    }

    fun scheduleSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncPicksWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            SyncPicksWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,  // Don't cancel if already queued
            syncRequest
        )
    }
}