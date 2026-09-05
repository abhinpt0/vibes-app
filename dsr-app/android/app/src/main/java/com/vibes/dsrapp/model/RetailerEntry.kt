package com.vibes.dsrapp.model

import com.google.gson.annotations.SerializedName

/**
 * Retailer ledger row – mirrors columns N–T of the sheet.
 * Also embedded inside [DsrEntry.retailerEntries] when submitting a full DSR.
 */
data class RetailerEntry(
    @SerializedName("ret_name")              val retName: String = "",
    @SerializedName("opening")               val opening: Double = 0.0,
    @SerializedName("forward")               val forward: Double = 0.0,
    @SerializedName("reverse")               val reverse: Double = 0.0,
    @SerializedName("pg_sock_fwd_cash_paid") val pgSockFwdCashPaid: Double = 0.0,
    @SerializedName("credit")                val credit: Double = 0.0,
    @SerializedName("balance")               val balance: Double = 0.0
)
