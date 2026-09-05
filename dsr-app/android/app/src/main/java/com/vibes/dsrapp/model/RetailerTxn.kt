package com.vibes.dsrapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "retailer_txn")
data class RetailerTxn(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dsrDate: String,
    val time: String,
    val createdAt: Long,
    val retailer: String,
    val forward: Double = 0.0,
    val reverse: Double = 0.0,
    val pgStock: Double = 0.0,
    val credit: Double = 0.0
)
