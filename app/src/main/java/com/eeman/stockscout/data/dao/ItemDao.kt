package com.eeman.stockscout.data.dao

import androidx.room.*
import com.eeman.stockscout.data.local.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM items")
    fun observeAll(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items")
    suspend fun getAll(): List<ItemEntity>

    @Query("SELECT * FROM items WHERE itemCode = :code")
    suspend fun getByCode(code: String): ItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemEntity>)

    @Query("UPDATE items SET onHandQty = onHandQty - 1 WHERE itemCode = :itemCode AND onHandQty > 0")
    suspend fun decrementQty(itemCode: String)

    @Query("SELECT onHandQty FROM items WHERE itemCode = :itemCode")
    suspend fun getQty(itemCode: String): Int?

    @Query("DELETE FROM items")
    suspend fun deleteAll()
}