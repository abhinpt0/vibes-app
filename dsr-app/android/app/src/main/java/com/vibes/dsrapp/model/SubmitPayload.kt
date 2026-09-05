package com.vibes.dsrapp.model

import com.vibes.dsrapp.model.CreditTxn
import com.vibes.dsrapp.model.DebitTxn
import com.vibes.dsrapp.model.RemarkTxn
import com.vibes.dsrapp.model.RetailerTxn

data class SubmitPayload(
    val action: String = "submitDSR",
    val dsr_date: String,
    val retailer_txns: List<RetailerTxn>,
    val credit_txns: List<CreditTxn>,
    val debit_txns: List<DebitTxn>,
    val remark_txns: List<RemarkTxn>
)
