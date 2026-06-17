# Motor Shop POS Mobile

Android scaffold that uses the existing Google Sheets + Apps Script POS backend.

## What this mobile app covers

- Android camera barcode scanning with `CameraX` + `ML Kit`
- Inventory intake flow:
  - scan barcode
  - lookup barcode template in Apps Script
  - prefill product name, brand, category, description
  - save stock item back into Google Sheets
- Cashier POS scan flow:
  - scan barcode
  - confirm stock availability
  - show price from inventory
- Sample role dashboard templates:
  - `admin`
  - `cashier`
  - `mechanic`

## Backend dependency

This app expects the Apps Script web app in this repo to be deployed with the new mobile JSON API routes:

- `mobile/login`
- `mobile/bootstrap`
- `mobile/dashboard`
- `mobile/inventory/barcode-template`
- `mobile/inventory/save-template`
- `mobile/inventory/add-item`
- `mobile/pos/scan`
- `mobile/mechanic/jobs`

## Important notes

- Update `BuildConfig.APPS_SCRIPT_URL` in [app/build.gradle.kts](app/build.gradle.kts) if your deployment URL changes.
- The current backend still uses the existing `Wood_Stocks` sheet as the main inventory stock table.
- Barcode template metadata is stored in the new `Barcode_Templates` sheet.
- For production, replace plaintext login with stronger auth and token revocation rules.

## Recommended next steps

1. Open this folder in Android Studio.
2. Sync Gradle.
3. Deploy the latest Apps Script version.
4. Test login, barcode scan, inventory template lookup, and inventory save.
5. Expand screens into full admin/cashier/mechanic workflows.
