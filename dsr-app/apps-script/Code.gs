// ============================================================
// DSR App — Google Apps Script Web App
// Paste this in: Google Sheet → Extensions → Apps Script
// Deploy as Web App → Execute as: Me → Who has access: Anyone
// ============================================================

var SPREADSHEET_ID = SpreadsheetApp.getActiveSpreadsheet().getId();
var DATA_SHEET_NAME = "DSR_Data";                    // flat daily summary
var RETAILER_SHEET_NAME = "Retailers";               // retailer master list
var RETAILER_TXN_SHEET_NAME = "Retailer_Transactions"; // per-retailer daily rows

// ─────────────────────────────────────────────
// GET  →  /exec?action=getDSR&date=2025-07-10
//         /exec?action=getRetailers
//         /exec?action=getDSRList&limit=30
// ─────────────────────────────────────────────
function doGet(e) {
  var action = e.parameter.action || "getDSRList";

  try {
    if (action === "getDSR") {
      return jsonResponse(getDSRByDate(e.parameter.date));
    } else if (action === "getRetailers") {
      return jsonResponse(getRetailers());
    } else if (action === "getDSRList") {
      var limit = parseInt(e.parameter.limit) || 30;
      return jsonResponse(getDSRList(limit));
    } else if (action === "getRetailerTransactions") {
      return jsonResponse(getRetailerTransactions(e.parameter.date));
    } else {
      return jsonResponse({ error: "Unknown action: " + action });
    }
  } catch (err) {
    return jsonResponse({ error: err.message });
  }
}

// ─────────────────────────────────────────────
// POST →  body: { action, ...fields }
//         action = "submitDSR" | "updateRetailer"
// ─────────────────────────────────────────────
function doPost(e) {
  try {
    var payload = JSON.parse(e.postData.contents);
    var action = payload.action || "submitDSR";

    if (action === "submitDSR") {
      return jsonResponse(submitDSR(payload));
    } else if (action === "updateRetailer") {
      return jsonResponse(updateRetailerBalance(payload));
    } else {
      return jsonResponse({ error: "Unknown action: " + action });
    }
  } catch (err) {
    return jsonResponse({ error: err.message });
  }
}

// ─────────────────────────────────────────────
// SUBMIT a new DSR entry
// ─────────────────────────────────────────────
function submitDSR(p) {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName(DATA_SHEET_NAME);

  // Auto-create the sheet + headers if it doesn't exist
  if (!sheet) {
    sheet = ss.insertSheet(DATA_SHEET_NAME);
    sheet.appendRow([
      "date", "opening_cash",
      "jio_collection_aswanth", "jio_collection_tmk_ck",
      "paybingo_collection", "pg_charge",
      "paybingo_id_sale", "paybingo_online_transfer",
      "self_deposit",
      "reverse_from_retailer",
      "upi_transfer",
      "vibes_deposit_bob", "vibes_deposit_sbi", "vibes_deposit_canara",
      "paybingo_sbi_deposit_sbi3696", "paybingo_deposit_pnb",
      "paybingo_sbi_deposit_hdfc", "paybingo_deposit_online_stock",
      "paybingo_deposit_other",
      "cash_paid_against_reverse", "upi_against_reverse",
      "pg_stock_adjust_vibes",
      "notes_500", "notes_200", "notes_100", "notes_50", "notes_20", "notes_10", "notes_5",
      "od_received", "value_received", "od_settlement",
      "remarks",
      "submitted_at"
    ]);
  }

  sheet.appendRow([
    p.date || "",
    p.opening_cash || 0,
    p.jio_collection_aswanth || 0,
    p.jio_collection_tmk_ck || 0,
    p.paybingo_collection || 0,
    p.pg_charge || 0,
    p.paybingo_id_sale || 0,
    p.paybingo_online_transfer || 0,
    p.self_deposit || 0,
    p.reverse_from_retailer || 0,
    p.upi_transfer || 0,
    p.vibes_deposit_bob || 0,
    p.vibes_deposit_sbi || 0,
    p.vibes_deposit_canara || 0,
    p.paybingo_sbi_deposit_sbi3696 || 0,
    p.paybingo_deposit_pnb || 0,
    p.paybingo_sbi_deposit_hdfc || 0,
    p.paybingo_deposit_online_stock || 0,
    p.paybingo_deposit_other || 0,
    p.cash_paid_against_reverse || 0,
    p.upi_against_reverse || 0,
    p.pg_stock_adjust_vibes || 0,
    p.notes_500 || 0,
    p.notes_200 || 0,
    p.notes_100 || 0,
    p.notes_50 || 0,
    p.notes_20 || 0,
    p.notes_10 || 0,
    p.notes_5 || 0,
    p.od_received || 0,
    p.value_received || 0,
    p.od_settlement || 0,
    p.remarks || "",
    new Date().toISOString()
  ]);

  // Write retailer transaction rows if provided
  if (p.retailer_transactions && Array.isArray(p.retailer_transactions)) {
    saveRetailerTransactions(p.date, p.retailer_transactions);
  }

  return { status: "success", message: "DSR saved for " + p.date };
}

// ─────────────────────────────────────────────
// SAVE retailer transaction rows
// ─────────────────────────────────────────────
function saveRetailerTransactions(date, rows) {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName(RETAILER_TXN_SHEET_NAME);

  if (!sheet) {
    sheet = ss.insertSheet(RETAILER_TXN_SHEET_NAME);
    sheet.appendRow(["date", "retailer", "forward", "reverse", "pg_stock", "credit"]);
  }

  rows.forEach(function(r) {
    sheet.appendRow([
      date || r.date || "",
      r.retailer || "",
      r.forward || 0,
      r.reverse || 0,
      r.pg_stock || 0,
      r.credit || 0
    ]);
  });
}

// ─────────────────────────────────────────────
// GET DSR by date
// ─────────────────────────────────────────────
function getDSRByDate(date) {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(DATA_SHEET_NAME);
  if (!sheet) return { error: "DSR_Data sheet not found" };

  var data = sheet.getDataRange().getValues();
  var headers = data[0];
  for (var i = 1; i < data.length; i++) {
    if (String(data[i][0]) === date) {
      return rowToObject(headers, data[i]);
    }
  }
  return { error: "No DSR found for date: " + date };
}

// ─────────────────────────────────────────────
// GET last N DSR entries (for the list screen)
// ─────────────────────────────────────────────
function getDSRList(limit) {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(DATA_SHEET_NAME);
  if (!sheet) return [];

  var data = sheet.getDataRange().getValues();
  if (data.length < 2) return [];

  var headers = data[0];
  var rows = data.slice(1).reverse().slice(0, limit);
  return rows.map(function(row) { return rowToObject(headers, row); });
}

// ─────────────────────────────────────────────
// GET Retailer list with balances
// ─────────────────────────────────────────────
function getRetailers() {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(RETAILER_SHEET_NAME);
  if (!sheet) return [];

  var data = sheet.getDataRange().getValues();
  if (data.length < 2) return [];

  var headers = data[0];
  return data.slice(1).map(function(row) { return rowToObject(headers, row); });
}

// ─────────────────────────────────────────────
// UPDATE retailer balance row
// ─────────────────────────────────────────────
function updateRetailerBalance(r) {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName(RETAILER_SHEET_NAME);

  if (!sheet) {
    sheet = ss.insertSheet(RETAILER_SHEET_NAME);
    sheet.appendRow(["ret_name", "opening", "forward", "reverse", "pg_sock_fwd_cash_paid", "credit", "balance"]);
  }

  var data = sheet.getDataRange().getValues();
  for (var i = 1; i < data.length; i++) {
    if (data[i][0] === r.ret_name) {
      sheet.getRange(i + 1, 2, 1, 6).setValues([[
        r.opening || 0, r.forward || 0, r.reverse || 0,
        r.pg_sock_fwd_cash_paid || 0, r.credit || 0, r.balance || 0
      ]]);
      return { status: "updated", ret_name: r.ret_name };
    }
  }

  // Not found — add new retailer
  sheet.appendRow([r.ret_name, r.opening || 0, r.forward || 0, r.reverse || 0,
    r.pg_sock_fwd_cash_paid || 0, r.credit || 0, r.balance || 0]);
  return { status: "added", ret_name: r.ret_name };
}

// ─────────────────────────────────────────────
// GET retailer transactions (optionally filter by date)
// ─────────────────────────────────────────────
function getRetailerTransactions(date) {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(RETAILER_TXN_SHEET_NAME);
  if (!sheet) return [];

  var data = sheet.getDataRange().getValues();
  if (data.length < 2) return [];

  var headers = data[0];
  var rows = data.slice(1);
  if (date) {
    rows = rows.filter(function(r) { return String(r[0]) === date; });
  }
  return rows.map(function(row) { return rowToObject(headers, row); });
}

// ─────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────
function rowToObject(headers, row) {
  var obj = {};
  headers.forEach(function(h, i) { obj[h] = row[i]; });
  return obj;
}

function jsonResponse(data) {
  return ContentService
    .createTextOutput(JSON.stringify(data))
    .setMimeType(ContentService.MimeType.JSON);
}
