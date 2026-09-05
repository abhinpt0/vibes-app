package com.vibes.dsrapp.model

import com.google.gson.annotations.SerializedName

/**
 * One retailer transaction row — maps to a row in the Retailer_Transactions sheet.
 * Multiple rows can be submitted per DSR day.
 */
data class RetailerTransaction(
    @SerializedName("date")     val date: String = "",
    @SerializedName("retailer") val retailer: String = "",
    @SerializedName("forward")  val forward: Double = 0.0,
    @SerializedName("reverse")  val reverse: Double = 0.0,
    @SerializedName("pg_stock") val pgStock: Double = 0.0,
    @SerializedName("credit")   val credit: Double = 0.0
)
