package com.eeman.stockscout.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.eeman.stockscout.data.db.AppDatabase
import com.eeman.stockscout.data.remote.api.RetrofitClient
import com.eeman.stockscout.data.remote.dto.PickRequestDto
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SyncPicksWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val pendingPickDao = AppDatabase.getInstance(context).pendingPickDao()
    private val apiService = RetrofitClient.apiService
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override suspend fun doWork(): Result {
        val pending = pendingPickDao.getPending()
        if (pending.isEmpty()) return Result.success()

        var anyFailed = false

        for (pick in pending) {
            try {
                val dto = PickRequestDto(
                    itemCode = pick.itemCode,
                    newQty = pick.newQty,
                    timestamp = isoFormat.format(Date(pick.timestamp))
                )
                val response = apiService.postPick(dto)
                if (response.isSuccessful) {
                    pendingPickDao.markSynced(pick.id)
                } else {
                    pendingPickDao.incrementRetry(pick.id)
                    anyFailed = true
                }
            } catch (_: IOException) {
                // Network error — retry the whole worker
                pendingPickDao.incrementRetry(pick.id)
                return Result.retry()
            }
        }

        return if (anyFailed) Result.retry() else Result.success()
    }

    companion object {
        const val WORK_NAME = "sync_picks_worker"
    }
}