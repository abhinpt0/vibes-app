package com.vibes.dsrapp.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.vibes.dsrapp.model.RetailerTxn

@Dao
interface RetailerTxnDao {
    @Insert
    suspend fun insert(txn: RetailerTxn)

    @Update
    suspend fun update(txn: RetailerTxn)

    @Delete
    suspend fun delete(txn: RetailerTxn)

    @Query("SELECT * FROM retailer_txn WHERE dsrDate = :dsrDate ORDER BY createdAt DESC")
    fun getByDate(dsrDate: String): LiveData<List<RetailerTxn>>

    @Query("SELECT COUNT(*) FROM retailer_txn WHERE dsrDate = :dsrDate")
    fun getCountByDate(dsrDate: String): LiveData<Int>

    @Query("SELECT * FROM retailer_txn WHERE dsrDate = :dsrDate ORDER BY createdAt DESC")
    suspend fun getAllByDate(dsrDate: String): List<RetailerTxn>

    @Query("DELETE FROM retailer_txn WHERE dsrDate = :dsrDate")
    suspend fun deleteByDate(dsrDate: String)
}
