package com.eeman.stockscout.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.eeman.stockscout.data.dao.AliasDao
import com.eeman.stockscout.data.dao.ItemDao
import com.eeman.stockscout.data.dao.PendingPickDao
import com.eeman.stockscout.data.local.AliasEntity
import com.eeman.stockscout.data.local.ItemEntity
import com.eeman.stockscout.data.local.PendingPickEntity

@Database(
    entities = [
        ItemEntity::class,
        AliasEntity::class,
        PendingPickEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao
    abstract fun aliasDao(): AliasDao
    abstract fun pendingPickDao(): PendingPickDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stockscout.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}