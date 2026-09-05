package com.vibes.dsrapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vibes.dsrapp.model.CreditTxn
import com.vibes.dsrapp.model.DebitTxn
import com.vibes.dsrapp.model.RemarkTxn
import com.vibes.dsrapp.model.RetailerTxn

@Database(
    entities = [RetailerTxn::class, CreditTxn::class, DebitTxn::class, RemarkTxn::class],
    version = 1,
    exportSchema = false
)
abstract class DsrDatabase : RoomDatabase() {
    abstract fun retailerTxnDao(): RetailerTxnDao
    abstract fun creditTxnDao(): CreditTxnDao
    abstract fun debitTxnDao(): DebitTxnDao
    abstract fun remarkTxnDao(): RemarkTxnDao

    companion object {
        @Volatile
        private var INSTANCE: DsrDatabase? = null

        fun getInstance(context: Context): DsrDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DsrDatabase::class.java,
                    "dsr_database"
                ).build().also { INSTANCE = it }
            }
    }
}
