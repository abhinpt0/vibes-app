// ============================================================
// DSR App — Google Apps Script Web App
// Paste this in: Google Sheet → Extensions → Apps Script
// Deploy as Web App → Execute as: Me → Who has access: Anyone
// ============================================================

// Sheet names for the 4-section data model
var SHEET_RETAILER_TXN = "Retailer_Txn";
var SHEET_CREDIT_TXN   = "Credit_Txn";
var SHEET_DEBIT_TXN    = "Debit_Txn";
var SHEET_REMARK_TXN   = "Remark_Txn";
var RETAILER_SHEET_NAME = "Retailers";

// ─────────────────────────────────────────────
// GET  →  /exec?action=getDSRList&limit=30
//         /exec?action=getRetailers
//         /exec?action=getSummary&date=2026-09-05
// ─────────────────────────────────────────────
function doGet(e) {
  var action = (e && e.parameter && e.parameter.action) ? e.parameter.action : "getDSRList";
  try {
    if (action === "getRetailers") {
      return jsonResponse(getRetailers());
    } else if (action === "getDSRList") {
      var limit = parseInt((e.parameter && e.parameter.limit) || 30);
      return jsonResponse(getDSRList(limit));
    } else if (action === "getSummary") {
      return jsonResponse(getSummary(e.parameter.date));
    } else {
      return jsonResponse({ error: "Unknown action: " + action });
    }
  } catch (err) {
    return jsonResponse({ error: err.message, stack: err.stack });
  }
}

// ─────────────────────────────────────────────
// POST →  { action:"submitDSR", dsr_date, retailer_txns[], credit_txns[], debit_txns[], remark_txns[] }
// ─────────────────────────────────────────────
function doPost(e) {
  try {
    if (!e || !e.postData || !e.postData.contents) {
      return jsonResponse({ error: "No POST body received" });
    }
    var payload = JSON.parse(e.postData.contents);
    var action = payload.action || "submitDSR";

    if (action === "submitDSR") {
      return jsonResponse(submitDSR(payload));
    } else {
      return jsonResponse({ error: "Unknown action: " + action });
    }
  } catch (err) {
    return jsonResponse({ error: err.message, stack: err.stack });
  }
}

// ─────────────────────────────────────────────
// SUBMIT — writes all 4 transaction arrays to their sheets
// ─────────────────────────────────────────────
function submitDSR(p) {
  var dsrDate = p.dsr_date || "";
  if (!dsrDate) return { error: "dsr_date is required" };

  var result = {
    status: "success",
    dsr_date: dsrDate,
    retailer_rows: 0,
    credit_rows: 0,
    debit_rows: 0,
    remark_rows: 0
  };

  if (Array.isArray(p.retailer_txns) && p.retailer_txns.length > 0) {
    writeRetailerTxns(dsrDate, p.retailer_txns);
    result.retailer_rows = p.retailer_txns.length;
  }
  if (Array.isArray(p.credit_txns) && p.credit_txns.length > 0) {
    writeCreditTxns(dsrDate, p.credit_txns);
    result.credit_rows = p.credit_txns.length;
  }
  if (Array.isArray(p.debit_txns) && p.debit_txns.length > 0) {
    writeDebitTxns(dsrDate, p.debit_txns);
    result.debit_rows = p.debit_txns.length;
  }
  if (Array.isArray(p.remark_txns) && p.remark_txns.length > 0) {
    writeRemarkTxns(dsrDate, p.remark_txns);
    result.remark_rows = p.remark_txns.length;
  }

  return result;
}

// ─────────────────────────────────────────────
// Retailer_Txn  — id | dsr_date | time | retailer | forward | reverse | pg_stock | credit
// ─────────────────────────────────────────────
function writeRetailerTxns(dsrDate, rows) {
  var sheet = getOrCreateSheet(SHEET_RETAILER_TXN,
    ["id", "dsr_date", "time", "retailer", "forward", "reverse", "pg_stock", "credit"]);
  rows.forEach(function(r) {
    sheet.appendRow([
      r.id    || "",
      dsrDate,
      r.time  || "",
      r.retailer || "",
      Number(r.forward) || 0,
      Number(r.reverse) || 0,
      Number(r.pgStock) || 0,
      Number(r.credit)  || 0
    ]);
  });
}

// ─────────────────────────────────────────────
// Credit_Txn  — id | dsr_date | time | particular | amount
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
      Number(r.amount) || 0
    ]);
  });
}

// ─────────────────────────────────────────────
// Debit_Txn  — id | dsr_date | time | particular | amount
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
      Number(r.amount) || 0
    ]);
  });
}

// ─────────────────────────────────────────────
// Remark_Txn  — id | dsr_date | time | remark | amount
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
      Number(r.amount) || 0
    ]);
  });
}

// ─────────────────────────────────────────────
// GET summary for a date
// ─────────────────────────────────────────────
function getSummary(date) {
  if (!date) return { error: "date parameter required" };
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  return {
    dsr_date:       date,
    retailer_count: countRowsByDate(ss, SHEET_RETAILER_TXN, date),
    credit_total:   sumAmountByDate(ss, SHEET_CREDIT_TXN, date),
    debit_total:    sumAmountByDate(ss, SHEET_DEBIT_TXN, date),
    remark_count:   countRowsByDate(ss, SHEET_REMARK_TXN, date)
  };
}

function countRowsByDate(ss, sheetName, date) {
  var sheet = ss.getSheetByName(sheetName);
  if (!sheet || sheet.getLastRow() < 2) return 0;
  var col = sheet.getRange(2, 2, sheet.getLastRow() - 1, 1).getValues();
  return col.filter(function(r) { return String(r[0]) === String(date); }).length;
}

function sumAmountByDate(ss, sheetName, date) {
  var sheet = ss.getSheetByName(sheetName);
  if (!sheet || sheet.getLastRow() < 2) return 0;
  var rows = sheet.getRange(2, 1, sheet.getLastRow() - 1, 5).getValues();
  return rows
    .filter(function(r) { return String(r[1]) === String(date); })
    .reduce(function(sum, r) { return sum + (parseFloat(r[4]) || 0); }, 0);
}

// ─────────────────────────────────────────────
// GET last N unique DSR dates
// ─────────────────────────────────────────────
function getDSRList(limit) {
  var ss   = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName(SHEET_RETAILER_TXN);
  if (!sheet || sheet.getLastRow() < 2) return [];
  var col   = sheet.getRange(2, 2, sheet.getLastRow() - 1, 1).getValues();
  var dates = [], seen = {};
  for (var i = col.length - 1; i >= 0; i--) {
    var d = String(col[i][0]);
    if (d && !seen[d]) { seen[d] = true; dates.push(d); if (dates.length >= limit) break; }
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
  var ss    = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName(name);
  if (!sheet) {
    sheet = ss.insertSheet(name);
    sheet.appendRow(headers);
    // Freeze header row
    sheet.setFrozenRows(1);
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
