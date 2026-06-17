<<<<<<< HEAD
# pos-motor-shop-repair-service
Motor Shop and Repair Service POS
=======
# Motor Shop & Repair POS

Google Apps Script POS system for a motor shop and repair shop, with an Android mobile client that shares the same Google Sheets database.

## Overview

This project contains two parts:

1. `Code.gs` + `index.html`
   Apps Script web app for the desktop/browser POS.
2. `android-app/`
   Android app built with Kotlin + Jetpack Compose for mobile cashier and inventory workflows.

Both clients use the same Google Sheet-backed database through Apps Script.

## Current Features

### Desktop / Apps Script POS

- login and role-based access
- POS / repair sale workflow
- parts and services inventory
- customers, vehicles, work orders
- purchases, suppliers, payments, reports
- demo dataset setup for motor shop / repair shop

### Android App

- login against Apps Script mobile API
- drawer-based navigation
- sales dashboard
- ticket / cashier workflow
- barcode scanning with CameraX + ML Kit
- live customer lookup from Google Sheets
- add new customer from mobile
- receipts list and receipt detail
- items list with paging
- add inventory item
- edit existing inventory item
- barcode template auto-prefill for item creation
- custom numeric keypad for paid amount

## Project Structure

```text
.
├─ Code.gs
├─ index.html
├─ index.txt
├─ old build.txt
└─ android-app/
   ├─ app/
   │  ├─ build.gradle.kts
   │  └─ src/main/java/com/grandtips/motoposmobile/
   │     ├─ MainActivity.kt
   │     ├─ data/
   │     ├─ model/
   │     ├─ permissions/
   │     └─ scanner/
   ├─ build.gradle.kts
   └─ settings.gradle.kts
```

## Apps Script Setup

### 1. Create the spreadsheet

Create or choose the Google Sheet that will hold the POS data.

### 2. Add the Apps Script files

In your Apps Script project:

- copy `Code.gs`
- copy `index.html`

### 3. Update the spreadsheet ID

Inside `Code.gs`, set:

```javascript
const SPREADSHEET_ID = 'YOUR_SPREADSHEET_ID';
```

### 4. Seed demo data

Run these functions in Apps Script as needed:

- `setupDemoData()`
- `seedMobileBarcodeTemplatesDemo()`

This creates the sample motor shop / repair shop dataset and barcode template records.

### 5. Deploy the web app

Deploy as:

- `Execute as`: Me
- `Who has access`: Anyone

After every API change in `Code.gs`, create a **new deployment version** and update the Android app URL if needed.

## Mobile API Routes

The Android app currently uses these Apps Script routes:

- `mobile/login`
- `mobile/bootstrap`
- `mobile/dashboard`
- `mobile/customers/list`
- `mobile/customers/create`
- `mobile/inventory/list`
- `mobile/inventory/barcode-template`
- `mobile/inventory/save-template`
- `mobile/inventory/add-item`
- `mobile/inventory/update-item`
- `mobile/pos/scan`
- `mobile/pos/checkout`
- `mobile/sales/list`
- `mobile/sales/detail`
- `mobile/mechanic/jobs`

## Android App Setup

### Requirements

- Android Studio
- JDK 17
- Android SDK 34
- internet access for Gradle dependency download

### 1. Open the project

Open:

```text
android-app
```

### 2. Update the Apps Script URL

In [android-app/app/build.gradle.kts](android-app/app/build.gradle.kts), update:

```kotlin
buildConfigField("String", "APPS_SCRIPT_URL", "\"YOUR_DEPLOYED_WEB_APP_URL\"")
```

### 3. Sync and build

Let Android Studio sync Gradle and download dependencies.

### 4. Camera permission

The Android app needs camera permission for barcode scanning.

## Main Mobile Flows

### Ticket

- search/select existing customer
- add new customer
- scan barcode
- load item and price
- set quantity
- enter paid amount using numeric keypad
- hold or complete sale with confirmation popup
- reset ticket form after successful hold/complete

### Items

- tap floating `+` to open add-item flow
- scan barcode
- auto-prefill create fields from `Barcode_Templates` when available
- add item to inventory
- tap existing item row to edit
- paged list, 10 items per page

## Notes

- The Android app is not standalone. It depends on the deployed Apps Script web app.
- If the mobile app suddenly stops working after backend changes, first redeploy Apps Script and verify the `APPS_SCRIPT_URL`.
- If barcode scanning works on Android but not in the browser web app, that is expected. The mobile app uses native Android camera access, while Apps Script web apps are more limited in browser camera behavior.

## Recommended GitHub Notes

Before pushing this repo publicly:

- remove or replace any real production spreadsheet IDs
- remove or replace any real deployed Apps Script URLs
- verify there is no private business data in the seeded sheet

## Next Improvements

- item search/filter in the paged items list
- cancel edit button in the items form
- mobile customer dropdown with stronger autocomplete UI
- mobile work order creation/editing
- mobile image upload for inventory items
- receipt printing / PDF export
>>>>>>> 3cc6fad (Initial commit: motor shop repair POS with Android app)
