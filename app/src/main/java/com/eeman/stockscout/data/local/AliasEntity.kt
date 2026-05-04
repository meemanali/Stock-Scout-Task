package com.eeman.stockscout.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "aliases",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["itemCode"],
            childColumns = ["itemCode"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("itemCode"), Index("value")]
)
data class AliasEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemCode: String,
    val type: String,   // matches AliasType enum name
    val value: String
)