package com.vibes.dsrapp.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.vibes.dsrapp.model.CreditTxn

@Dao
interface CreditTxnDao {
    @Insert
    suspend fun insert(txn: CreditTxn)

    @Update
    suspend fun update(txn: CreditTxn)

    @Delete
    suspend fun delete(txn: CreditTxn)

    @Query("SELECT * FROM credit_txn WHERE dsrDate = :dsrDate ORDER BY createdAt DESC")
    fun getByDate(dsrDate: String): LiveData<List<CreditTxn>>

    @Query("SELECT COUNT(*) FROM credit_txn WHERE dsrDate = :dsrDate")
    fun getCountByDate(dsrDate: String): LiveData<Int>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM credit_txn WHERE dsrDate = :dsrDate")
    fun getSumByDate(dsrDate: String): LiveData<Double>

    @Query("SELECT * FROM credit_txn WHERE dsrDate = :dsrDate ORDER BY createdAt DESC")
    suspend fun getAllByDate(dsrDate: String): List<CreditTxn>

    @Query("DELETE FROM credit_txn WHERE dsrDate = :dsrDate")
    suspend fun deleteByDate(dsrDate: String)
}
