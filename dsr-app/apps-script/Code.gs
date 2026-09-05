// ============================================================
// DSR App — Google Apps Script Web App
// Paste this in: Google Sheet → Extensions → Apps Script
// Deploy as Web App → Execute as: Me → Who has access: Anyone
// ============================================================

var SPREADSHEET_ID = SpreadsheetApp.getActiveSpreadsheet().getId();

// Sheet names for the new 4-section data model
var SHEET_RETAILER_TXN = "Retailer_Txn";
var SHEET_CREDIT_TXN   = "Credit_Txn";
var SHEET_DEBIT_TXN    = "Debit_Txn";
var SHEET_REMARK_TXN   = "Remark_Txn";

// Kept for backward-compatible GET actions
var RETAILER_SHEET_NAME = "Retailers";

// ─────────────────────────────────────────────
// GET  →  /exec?action=getDSRList&limit=30
//         /exec?action=getRetailers
//         /exec?action=getSummary&date=2026-09-05
// ─────────────────────────────────────────────
function doGet(e) {
  var action = e.parameter.action || "getDSRList";

  try {
    if (action === "getRetailers") {
      return jsonResponse(getRetailers());
    } else if (action === "getDSRList") {
      var limit = parseInt(e.parameter.limit) || 30;
      return jsonResponse(getDSRList(limit));
    } else if (action === "getSummary") {
      return jsonResponse(getSummary(e.parameter.date));
    } else {
      return jsonResponse({ error: "Unknown action: " + action });
    }
  } catch (err) {
    return jsonResponse({ error: err.message });
  }
}

// ─────────────────────────────────────────────
// POST →  body: { action: "submitDSR", dsr_date, retailer_txns, credit_txns, debit_txns, remark_txns }
// ─────────────────────────────────────────────
function doPost(e) {
  try {
    var payload = JSON.parse(e.postData.contents);
    var action = payload.action || "submitDSR";

    if (action === "submitDSR") {
      return jsonResponse(submitDSR(payload));
    } else {
      return jsonResponse({ error: "Unknown action: " + action });
    }
  } catch (err) {
    return jsonResponse({ error: err.message });
  }
}

// ─────────────────────────────────────────────
// SUBMIT DSR — writes all 4 arrays to their sheets
// ─────────────────────────────────────────────
function submitDSR(p) {
  var dsrDate = p.dsr_date || "";

  if (p.retailer_txns && Array.isArray(p.retailer_txns)) {
    writeRetailerTxns(dsrDate, p.retailer_txns);
  }
  if (p.credit_txns && Array.isArray(p.credit_txns)) {
    writeCreditTxns(dsrDate, p.credit_txns);
  }
  if (p.debit_txns && Array.isArray(p.debit_txns)) {
    writeDebitTxns(dsrDate, p.debit_txns);
  }
  if (p.remark_txns && Array.isArray(p.remark_txns)) {
    writeRemarkTxns(dsrDate, p.remark_txns);
  }

  return { status: "success", message: "DSR submitted for " + dsrDate };
}

// ─────────────────────────────────────────────
// Write Retailer_Txn rows
// Columns: id, dsr_date, time, retailer, forward, reverse, pg_stock, credit
// ─────────────────────────────────────────────
function writeRetailerTxns(dsrDate, rows) {
  var sheet = getOrCreateSheet(SHEET_RETAILER_TXN,
    ["id", "dsr_date", "time", "retailer", "forward", "reverse", "pg_stock", "credit"]);

  rows.forEach(function(r) {
    sheet.appendRow([
      r.id || "",
      dsrDate,
      r.time || "",
      r.retailer || "",
      r.forward || 0,
      r.reverse || 0,
      r.pgStock || 0,
      r.credit || 0
    ]);
  });
}

// ─────────────────────────────────────────────
// Write Credit_Txn rows
// Columns: id, dsr_date, time, particular, amount
// ─────────────────────────────────────────────
function writeCreditTxns(dsrDate, rows) {
  var sheet = getOrCreateSheet(SHEET_CREDIT_TXN,
    ["id", "dsr_date", "time", "particular", "amount"]);

  rows.forEach(function(r) {
    sheet.appendRow([
      r.id || "",
      dsrDate,
      r.time || "",
      r.particular || "",
      r.amount || 0
    ]);
  });
}

// ─────────────────────────────────────────────
// Write Debit_Txn rows
// Columns: id, dsr_date, time, particular, amount
// ─────────────────────────────────────────────
function writeDebitTxns(dsrDate, rows) {
  var sheet = getOrCreateSheet(SHEET_DEBIT_TXN,
    ["id", "dsr_date", "time", "particular", "amount"]);

  rows.forEach(function(r) {
    sheet.appendRow([
      r.id || "",
      dsrDate,
      r.time || "",
      r.particular || "",
      r.amount || 0
    ]);
  });
}

// ─────────────────────────────────────────────
// Write Remark_Txn rows
// Columns: id, dsr_date, time, remark, amount
// ─────────────────────────────────────────────
function writeRemarkTxns(dsrDate, rows) {
  var sheet = getOrCreateSheet(SHEET_REMARK_TXN,
    ["id", "dsr_date", "time", "remark", "amount"]);

  rows.forEach(function(r) {
    sheet.appendRow([
      r.id || "",
      dsrDate,
      r.time || "",
      r.remark || "",
      r.amount || 0
    ]);
  });
}

// ─────────────────────────────────────────────
// GET summary totals for a given date
// Returns: { dsr_date, retailer_count, credit_total, debit_total, remark_count }
// ─────────────────────────────────────────────
function getSummary(date) {
  if (!date) return { error: "date parameter required" };
  var ss = SpreadsheetApp.getActiveSpreadsheet();

  var retailerCount = countRowsByDate(ss, SHEET_RETAILER_TXN, date);
  var creditTotal   = sumAmountByDate(ss, SHEET_CREDIT_TXN, date);
  var debitTotal    = sumAmountByDate(ss, SHEET_DEBIT_TXN, date);
  var remarkCount   = countRowsByDate(ss, SHEET_REMARK_TXN, date);

  return {
    dsr_date: date,
    retailer_count: retailerCount,
    credit_total: creditTotal,
    debit_total: debitTotal,
    remark_count: remarkCount
  };
}

// Count data rows where column index 1 (dsr_date) matches
function countRowsByDate(ss, sheetName, date) {
  var sheet = ss.getSheetByName(sheetName);
  if (!sheet || sheet.getLastRow() < 2) return 0;
  var col = sheet.getRange(2, 2, sheet.getLastRow() - 1, 1).getValues();
  return col.filter(function(r) { return String(r[0]) === date; }).length;
}

// Sum column index 4 (amount) where column index 1 (dsr_date) matches
function sumAmountByDate(ss, sheetName, date) {
  var sheet = ss.getSheetByName(sheetName);
  if (!sheet || sheet.getLastRow() < 2) return 0;
  var rows = sheet.getRange(2, 1, sheet.getLastRow() - 1, 5).getValues();
  return rows
    .filter(function(r) { return String(r[1]) === date; })
    .reduce(function(sum, r) { return sum + (parseFloat(r[4]) || 0); }, 0);
}

// ─────────────────────────────────────────────
// GET last N DSR dates (unique dates from Retailer_Txn)
// ─────────────────────────────────────────────
function getDSRList(limit) {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName(SHEET_RETAILER_TXN);
  if (!sheet || sheet.getLastRow() < 2) return [];

  var col = sheet.getRange(2, 2, sheet.getLastRow() - 1, 1).getValues();
  var dates = [];
  var seen = {};
  for (var i = col.length - 1; i >= 0; i--) {
    var d = String(col[i][0]);
    if (d && !seen[d]) {
      seen[d] = true;
      dates.push(d);
      if (dates.length >= limit) break;
    }
  }
  return dates.map(function(d) { return { dsr_date: d }; });
}

// ─────────────────────────────────────────────
// GET Retailer master list
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
// Helpers
// ─────────────────────────────────────────────
function getOrCreateSheet(name, headers) {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName(name);
  if (!sheet) {
    sheet = ss.insertSheet(name);
    sheet.appendRow(headers);
  }
  return sheet;
}

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
