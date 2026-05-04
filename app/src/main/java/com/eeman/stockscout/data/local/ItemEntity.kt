package com.eeman.stockscout.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey
    val itemCode: String,
    val name: String,
    val uom: String,
    val onHandQty: Int
)