package com.eeman.stockscout.data.dao

import androidx.room.*
import com.eeman.stockscout.data.local.PendingPickEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingPickDao {

    @Insert
    suspend fun insert(pick: PendingPickEntity): Long

    @Query("SELECT * FROM pending_picks WHERE synced = 0 ORDER BY timestamp ASC")
    suspend fun getPending(): List<PendingPickEntity>

    @Query("SELECT * FROM pending_picks WHERE synced = 0 ORDER BY timestamp ASC")
    fun observePending(): Flow<List<PendingPickEntity>>

    @Query("UPDATE pending_picks SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)

    @Query("UPDATE pending_picks SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetry(id: Long)

    @Query("SELECT COUNT(*) FROM pending_picks WHERE synced = 0")
    fun observePendingCount(): Flow<Int>
}