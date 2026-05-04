package com.eeman.stockscout.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_picks")
data class PendingPickEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemCode: String,
    val newQty: Int,
    val timestamp: Long,        // epoch millis
    val synced: Boolean = false,
    val retryCount: Int = 0
)