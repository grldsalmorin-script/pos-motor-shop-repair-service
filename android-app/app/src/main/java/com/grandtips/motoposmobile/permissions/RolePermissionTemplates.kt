package com.grandtips.motoposmobile.permissions

import com.grandtips.motoposmobile.model.MobileDashboardTemplate

object RolePermissionTemplates {
    fun templatesForRole(role: String): List<MobileDashboardTemplate> = when (role) {
        "admin" -> listOf(
            MobileDashboardTemplate("Admin Overview", "Full KPI dashboard, inventory intake, barcode template control, pricing and role governance."),
            MobileDashboardTemplate("Inventory Intake", "Scan a barcode, prefill product name, brand, category and push new stock into Google Sheets."),
            MobileDashboardTemplate("POS Control", "Scan parts in-store, confirm stock quantity, sell price and cashier workflow."),
        )
        "mechanic" -> listOf(
            MobileDashboardTemplate("Assigned Jobs", "View only the work orders assigned to this mechanic."),
            MobileDashboardTemplate("Vehicle Queue", "See complaint, diagnosis, priority and due release timing."),
            MobileDashboardTemplate("Work Progress", "Update task context while leaving sales and settings locked down.")
        )
        else -> listOf(
            MobileDashboardTemplate("Cashier POS", "Scan barcode to check price and stock before adding to sale."),
            MobileDashboardTemplate("Quick Customer Flow", "Fast walk-in sale handling with mobile-friendly controls."),
            MobileDashboardTemplate("Recent Sales", "See own activity and payment collection shortcuts.")
        )
    }
}
