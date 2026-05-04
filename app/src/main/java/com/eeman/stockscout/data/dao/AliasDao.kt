package com.eeman.stockscout.data.dao

import androidx.room.*
import com.eeman.stockscout.data.local.AliasEntity

@Dao
interface AliasDao {

    @Query("SELECT * FROM aliases WHERE itemCode = :itemCode")
    suspend fun getAliasesForItem(itemCode: String): List<AliasEntity>

    @Query("SELECT * FROM aliases WHERE value = :value LIMIT 1")
    suspend fun findByValue(value: String): AliasEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(aliases: List<AliasEntity>)

    @Query("DELETE FROM aliases")
    suspend fun deleteAll()
}