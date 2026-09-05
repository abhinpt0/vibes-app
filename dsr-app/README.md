# DSR App — Complete Setup Guide

## Project Structure

```
dsr-app/
├── apps-script/
│   └── Code.gs                   ← Paste into Google Apps Script
└── android/
    └── app/
        ├── build.gradle
        └── src/main/
            ├── AndroidManifest.xml
            ├── java/com/vibes/dsrapp/
            │   ├── MainActivity.kt
            │   ├── adapter/
            │   │   ├── DsrListAdapter.kt
            │   │   └── RetailerAdapter.kt
            │   ├── model/
            │   │   └── DsrEntry.kt           ← Data models
            │   ├── network/
            │   │   └── ApiClient.kt          ← HTTP layer
            │   ├── ui/
            │   │   ├── HomeFragment.kt       ← DSR records list
            │   │   ├── EntryFragment.kt      ← New DSR form
            │   │   └── RetailersFragment.kt  ← Retailer balances
            │   └── viewmodel/
            │       └── DsrViewModel.kt       ← State management
            └── res/
                ├── layout/
                │   ├── activity_main.xml
                │   ├── fragment_home.xml
                │   ├── fragment_entry.xml    ← Full form (all fields)
                │   ├── fragment_retailers.xml
                │   ├── item_dsr.xml
                │   └── item_retailer.xml
                ├── menu/bottom_nav_menu.xml
                ├── navigation/nav_graph.xml
                └── values/
                    ├── colors.xml
                    └── themes.xml
```

---

## Google Sheet Setup (One-time)

### Step 1 — Open Apps Script
In your Google Sheet:
`Extensions → Apps Script → delete default code → paste Code.gs → Save`

### Step 2 — Deploy as Web App
1. Click **Deploy → New Deployment**
2. Type: **Web App**
3. Execute as: **Me**
4. Who has access: **Anyone**
5. Click **Deploy → Authorize → Copy the URL**

### Step 3 — Paste URL in Android
Open `ApiClient.kt` and replace:
```kotlin
private const val WEB_APP_URL = "https://script.google.com/macros/s/YOUR_DEPLOYMENT_ID/exec"
```
with the URL you copied.

### Step 4 — Create Retailer Master Sheet
The script auto-creates `DSR_Data` and `Retailers` sheets the first time you submit.
Or manually add a sheet named **`Retailers`** with these headers in row 1:
```
ret_name | opening | forward | reverse | pg_sock_fwd_cash_paid | credit | balance
```
And add your retailer names (from your sheet) in column A:
```
Prompt, Fantasy, Falcon, Cell city, Fixit, Iswarya statio,
Smartech, SM online, Mobile mart, Vibes (Mobicare),
Sbi Service, Fono, Tk Store, Mobi time, KKM
```

---

## DSR_Data Sheet — Flat Schema

| Column | Field | Type | Description |
|---|---|---|---|
| A | date | String | YYYY-MM-DD |
| B | opening_cash | Number | Opening cash balance |
| C | jio_collection_aswanth | Number | Jio – Aswanth (Onchiam Market) |
| D | jio_collection_tmk_ck | Number | Jio – TMK + CK |
| E | paybingo_collection | Number | PayBingo collection |
| F | pg_charge | Number | PG charges |
| G | paybingo_id_sale | Number | PayBingo ID sale |
| H | paybingo_online_transfer | Number | PayBingo online transfer |
| I | self_deposit | Number | Self deposit (CDM/SBI) |
| J | reverse_from_retailer | Number | Reverse from retailer |
| K | upi_transfer | Number | UPI transfer |
| L | vibes_deposit_bob | Number | Vibes Deposit – BOB |
| M | vibes_deposit_sbi | Number | Vibes Deposit – SBI 5859 |
| N | vibes_deposit_canara | Number | Vibes Deposit – Canara |
| O | paybingo_sbi_deposit_sbi3696 | Number | PayBingo SBI Deposit (3696) |
| P | paybingo_deposit_pnb | Number | PayBingo Deposit – PNB |
| Q | paybingo_sbi_deposit_hdfc | Number | PayBingo SBI Deposit – HDFC |
| R | paybingo_deposit_online_stock | Number | PayBingo Deposit Online Stock |
| S | paybingo_deposit_other | Number | PayBingo Deposit – Other |
| T | cash_paid_against_reverse | Number | Cash paid against reverse |
| U | upi_against_reverse | Number | UPI against reverse |
| V | pg_stock_adjust_vibes | Number | PG stock adjust – Vibes |
| W | notes_500 | Int | Count of ₹500 notes |
| X | notes_200 | Int | Count of ₹200 notes |
| Y | notes_100 | Int | Count of ₹100 notes |
| Z | notes_50 | Int | Count of ₹50 notes |
| AA | notes_20 | Int | Count of ₹20 notes |
| AB | notes_10 | Int | Count of ₹10 notes |
| AC | notes_5 | Int | Count of ₹5 notes |
| AD | od_received | Number | OD received |
| AE | value_received | Number | Value received |
| AF | od_settlement | Number | OD settlement |
| AG | remarks | String | Free text remarks |
| AH | submitted_at | DateTime | Auto: ISO timestamp |

---

## Computed Formulas (add to DSR_Data sheet if needed)

Add these helper columns after AH:

| Column | Formula | Description |
|---|---|---|
| AI | `=B2+C2+D2+E2+F2+G2+H2+I2` | Total Credit |
| AJ | `=J2+K2+L2+M2+N2+O2+P2+Q2+R2+S2` | Total Debit |
| AK | `=AI2-AJ2` | Closing Balance |
| AL | `=W2*500+X2*200+Y2*100+Z2*50+AA2*20+AB2*10+AC2*5` | Notes Total |
| AM | `=AK2-AL2` | Cash Difference |

---

## API Reference

### GET — Read data
| URL | Returns |
|---|---|
| `?action=getDSRList&limit=30` | Last 30 DSR entries as JSON array |
| `?action=getDSR&date=2025-07-10` | Single DSR entry for that date |
| `?action=getRetailers` | All retailers with current balances |

### POST — Write data
Send JSON body:
```json
{
  "action": "submitDSR",
  "date": "2025-07-10",
  "opening_cash": 5000,
  "jio_collection_aswanth": 12000,
  "paybingo_collection": 8000,
  "notes_500": 10,
  "notes_100": 5,
  "remarks": "Normal day",
  "retailer_entries": [
    {
      "ret_name": "Prompt",
      "opening": 1000,
      "forward": 5000,
      "reverse": 200,
      "pg_sock_fwd_cash_paid": 0,
      "credit": 4800,
      "balance": 5800
    }
  ]
}
```

---

## Security (Optional but Recommended)

Add a secret token to prevent unauthorized writes:

**In Code.gs**, add at the top:
```javascript
var SECRET = "your-secret-token-here";
```
In `doPost`, add before processing:
```javascript
if (payload.token !== SECRET) {
  return jsonResponse({ error: "Unauthorized" });
}
```
**In ApiClient.kt**, add to every payload:
```kotlin
put("token", "your-secret-token-here")
```

---

## Build & Run

```bash
# Open in Android Studio
File → Open → dsr-app/android

# Sync Gradle, then Run on device/emulator
# Min SDK: Android 7.0 (API 24)
```

---

## App Screens

| Screen | Tab | What it does |
|---|---|---|
| **DSR Records** | Records | Lists all past entries with totals (pull to refresh) |
| **New Entry** | New Entry | Full form matching the sheet — all sections |
| **Retailers** | Retailers | Shows all retailers with Opening/Forward/Reverse/Balance |
