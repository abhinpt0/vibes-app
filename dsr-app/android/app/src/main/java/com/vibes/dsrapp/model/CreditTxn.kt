package com.vibes.dsrapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_txn")
data class CreditTxn(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dsrDate: String,
    val time: String,
    val createdAt: Long,
    val particular: String,
    val amount: Double
)
