package com.vibes.dsrapp.model

import com.google.gson.annotations.SerializedName

// ─── DSR Entry (maps 1-to-1 to the DSR_Data sheet columns) ──────────────────
data class DsrEntry(
    @SerializedName("date")                          val date: String = "",
    @SerializedName("opening_cash")                  val openingCash: Double = 0.0,

    // Credit
    @SerializedName("jio_collection_aswanth")        val jioCollectionAswanth: Double = 0.0,
    @SerializedName("jio_collection_tmk_ck")         val jioCollectionTmkCk: Double = 0.0,
    @SerializedName("paybingo_collection")           val paybingoCollection: Double = 0.0,
    @SerializedName("pg_charge")                     val pgCharge: Double = 0.0,
    @SerializedName("paybingo_id_sale")              val paybingoIdSale: Double = 0.0,
    @SerializedName("paybingo_online_transfer")      val paybingoOnlineTransfer: Double = 0.0,
    @SerializedName("self_deposit")                  val selfDeposit: Double = 0.0,

    // Debit
    @SerializedName("reverse_from_retailer")         val reverseFromRetailer: Double = 0.0,
    @SerializedName("upi_transfer")                  val upiTransfer: Double = 0.0,
    @SerializedName("vibes_deposit_bob")             val vibesDepositBob: Double = 0.0,
    @SerializedName("vibes_deposit_sbi")             val vibesDepositSbi: Double = 0.0,
    @SerializedName("vibes_deposit_canara")          val vibesDepositCanara: Double = 0.0,
    @SerializedName("paybingo_sbi_deposit_sbi3696")  val paybingoSbiDepositSbi3696: Double = 0.0,
    @SerializedName("paybingo_deposit_pnb")          val paybingoDepositPnb: Double = 0.0,
    @SerializedName("paybingo_sbi_deposit_hdfc")     val paybingoSbiDepositHdfc: Double = 0.0,
    @SerializedName("paybingo_deposit_online_stock") val paybingoDepositOnlineStock: Double = 0.0,
    @SerializedName("paybingo_deposit_other")        val paybingoDepositOther: Double = 0.0,

    // Cash flow adjustments
    @SerializedName("cash_paid_against_reverse")     val cashPaidAgainstReverse: Double = 0.0,
    @SerializedName("upi_against_reverse")           val upiAgainstReverse: Double = 0.0,
    @SerializedName("pg_stock_adjust_vibes")         val pgStockAdjustVibes: Double = 0.0,

    // Cash denomination
    @SerializedName("notes_500")                     val notes500: Int = 0,
    @SerializedName("notes_200")                     val notes200: Int = 0,
    @SerializedName("notes_100")                     val notes100: Int = 0,
    @SerializedName("notes_50")                      val notes50: Int = 0,
    @SerializedName("notes_20")                      val notes20: Int = 0,
    @SerializedName("notes_10")                      val notes10: Int = 0,
    @SerializedName("notes_5")                       val notes5: Int = 0,

    // Stock
    @SerializedName("od_received")                   val odReceived: Double = 0.0,
    @SerializedName("value_received")                val valueReceived: Double = 0.0,
    @SerializedName("od_settlement")                 val odSettlement: Double = 0.0,

    @SerializedName("remarks")                       val remarks: String = "",
    @SerializedName("retailer_entries")              val retailerEntries: List<RetailerEntry> = emptyList(),

    // action field for POST
    @SerializedName("action")                        val action: String = "submitDSR"
) {
    // Computed totals (mirrors the sheet formulas)
    val totalCredit: Double get() =
        openingCash + jioCollectionAswanth + jioCollectionTmkCk +
        paybingoCollection + pgCharge + paybingoIdSale +
        paybingoOnlineTransfer + selfDeposit

    val totalDebit: Double get() =
        reverseFromRetailer + upiTransfer + vibesDepositBob +
        vibesDepositSbi + vibesDepositCanara + paybingoSbiDepositSbi3696 +
        paybingoDepositPnb + paybingoSbiDepositHdfc +
        paybingoDepositOnlineStock + paybingoDepositOther

    val closingBalance: Double get() = totalCredit - totalDebit

    val notesTotal: Double get() =
        notes500 * 500.0 + notes200 * 200.0 + notes100 * 100.0 +
        notes50 * 50.0 + notes20 * 20.0 + notes10 * 10.0 + notes5 * 5.0

    val cashDifference: Double get() = closingBalance - notesTotal
}

