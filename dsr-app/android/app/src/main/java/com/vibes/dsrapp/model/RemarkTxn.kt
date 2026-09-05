package com.vibes.dsrapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remark_txn")
data class RemarkTxn(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dsrDate: String,
    val time: String,
    val createdAt: Long,
    val remark: String,
    val amount: Double = 0.0
)
