package com.vibes.dsrapp.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.vibes.dsrapp.model.DebitTxn

@Dao
interface DebitTxnDao {
    @Insert
    suspend fun insert(txn: DebitTxn)

    @Update
    suspend fun update(txn: DebitTxn)

    @Delete
    suspend fun delete(txn: DebitTxn)

    @Query("SELECT * FROM debit_txn WHERE dsrDate = :dsrDate ORDER BY createdAt DESC")
    fun getByDate(dsrDate: String): LiveData<List<DebitTxn>>

    @Query("SELECT COUNT(*) FROM debit_txn WHERE dsrDate = :dsrDate")
    fun getCountByDate(dsrDate: String): LiveData<Int>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM debit_txn WHERE dsrDate = :dsrDate")
    fun getSumByDate(dsrDate: String): LiveData<Double>

    @Query("SELECT * FROM debit_txn WHERE dsrDate = :dsrDate ORDER BY createdAt DESC")
    suspend fun getAllByDate(dsrDate: String): List<DebitTxn>

    @Query("DELETE FROM debit_txn WHERE dsrDate = :dsrDate")
    suspend fun deleteByDate(dsrDate: String)
}
