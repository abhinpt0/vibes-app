package com.vibes.dsrapp.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.vibes.dsrapp.model.RemarkTxn

@Dao
interface RemarkTxnDao {
    @Insert
    suspend fun insert(txn: RemarkTxn)

    @Update
    suspend fun update(txn: RemarkTxn)

    @Delete
    suspend fun delete(txn: RemarkTxn)

    @Query("SELECT * FROM remark_txn WHERE dsrDate = :dsrDate ORDER BY createdAt DESC")
    fun getByDate(dsrDate: String): LiveData<List<RemarkTxn>>

    @Query("SELECT COUNT(*) FROM remark_txn WHERE dsrDate = :dsrDate")
    fun getCountByDate(dsrDate: String): LiveData<Int>

    @Query("SELECT * FROM remark_txn WHERE dsrDate = :dsrDate ORDER BY createdAt DESC")
    suspend fun getAllByDate(dsrDate: String): List<RemarkTxn>

    @Query("DELETE FROM remark_txn WHERE dsrDate = :dsrDate")
    suspend fun deleteByDate(dsrDate: String)
}
