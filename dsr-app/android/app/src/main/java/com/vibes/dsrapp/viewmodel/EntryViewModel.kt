package com.vibes.dsrapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.vibes.dsrapp.db.DsrDatabase
import com.vibes.dsrapp.model.CreditTxn
import com.vibes.dsrapp.model.DebitTxn
import com.vibes.dsrapp.model.RemarkTxn
import com.vibes.dsrapp.model.RetailerTxn
import com.vibes.dsrapp.model.SubmitPayload
import com.vibes.dsrapp.network.ApiClient
import kotlinx.coroutines.launch

class EntryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DsrDatabase.getInstance(application)
    private val retailerDao = db.retailerTxnDao()
    private val creditDao = db.creditTxnDao()
    private val debitDao = db.debitTxnDao()
    private val remarkDao = db.remarkTxnDao()

    // ── Retailer ─────────────────────────────────────────────────────────────

    fun getRetailerTxns(date: String): LiveData<List<RetailerTxn>> = retailerDao.getByDate(date)
    fun getRetailerCount(date: String): LiveData<Int> = retailerDao.getCountByDate(date)

    fun insertRetailer(txn: RetailerTxn) = viewModelScope.launch { retailerDao.insert(txn) }
    fun updateRetailer(txn: RetailerTxn) = viewModelScope.launch { retailerDao.update(txn) }
    fun deleteRetailer(txn: RetailerTxn) = viewModelScope.launch { retailerDao.delete(txn) }

    // ── Credit ────────────────────────────────────────────────────────────────

    fun getCreditTxns(date: String): LiveData<List<CreditTxn>> = creditDao.getByDate(date)
    fun getCreditCount(date: String): LiveData<Int> = creditDao.getCountByDate(date)
    fun getCreditSum(date: String): LiveData<Double> = creditDao.getSumByDate(date)

    fun insertCredit(txn: CreditTxn) = viewModelScope.launch { creditDao.insert(txn) }
    fun updateCredit(txn: CreditTxn) = viewModelScope.launch { creditDao.update(txn) }
    fun deleteCredit(txn: CreditTxn) = viewModelScope.launch { creditDao.delete(txn) }

    // ── Debit ─────────────────────────────────────────────────────────────────

    fun getDebitTxns(date: String): LiveData<List<DebitTxn>> = debitDao.getByDate(date)
    fun getDebitCount(date: String): LiveData<Int> = debitDao.getCountByDate(date)
    fun getDebitSum(date: String): LiveData<Double> = debitDao.getSumByDate(date)

    fun insertDebit(txn: DebitTxn) = viewModelScope.launch { debitDao.insert(txn) }
    fun updateDebit(txn: DebitTxn) = viewModelScope.launch { debitDao.update(txn) }
    fun deleteDebit(txn: DebitTxn) = viewModelScope.launch { debitDao.delete(txn) }

    // ── Remarks ───────────────────────────────────────────────────────────────

    fun getRemarkTxns(date: String): LiveData<List<RemarkTxn>> = remarkDao.getByDate(date)
    fun getRemarkCount(date: String): LiveData<Int> = remarkDao.getCountByDate(date)

    fun insertRemark(txn: RemarkTxn) = viewModelScope.launch { remarkDao.insert(txn) }
    fun deleteRemark(txn: RemarkTxn) = viewModelScope.launch { remarkDao.delete(txn) }

    // ── Submit ────────────────────────────────────────────────────────────────

    suspend fun submitAndClear(date: String): Result<String> {
        val retailers = retailerDao.getAllByDate(date)
        val credits = creditDao.getAllByDate(date)
        val debits = debitDao.getAllByDate(date)
        val remarks = remarkDao.getAllByDate(date)

        val payload = SubmitPayload(
            dsr_date = date,
            retailer_txns = retailers,
            credit_txns = credits,
            debit_txns = debits,
            remark_txns = remarks
        )

        val result = ApiClient.submitAll(payload)
        if (result.isSuccess) {
            retailerDao.deleteByDate(date)
            creditDao.deleteByDate(date)
            debitDao.deleteByDate(date)
            remarkDao.deleteByDate(date)
        }
        return result
    }
}
