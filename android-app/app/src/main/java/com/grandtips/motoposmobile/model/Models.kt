package com.grandtips.motoposmobile.model

import com.google.gson.annotations.SerializedName

data class ApiEnvelope<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)

data class MobileSession(
    val token: String,
    val user: MobileUser,
    val permissions: Map<String, Boolean> = emptyMap()
)

data class MobileUser(
    @SerializedName("id") val id: Int,
    @SerializedName("full_name") val fullName: String,
    val email: String,
    val role: String,
    val avatar: String = ""
)

data class MobileDashboardTemplate(
    val title: String,
    val description: String
)

data class BarcodeTemplate(
    val barcode: String = "",
    @SerializedName("product_name") val productName: String = "",
    val brand: String = "",
    @SerializedName("category_id") val categoryId: String = "",
    @SerializedName("sub_category_id") val subCategoryId: String = "",
    val description: String = "",
    @SerializedName("default_buy_rate") val defaultBuyRate: Double = 0.0,
    @SerializedName("default_sell_rate") val defaultSellRate: Double = 0.0,
    @SerializedName("default_qty") val defaultQty: Int = 1
)

data class InventoryStock(
    val id: Int,
    val barcode: String,
    val sku: String,
    @SerializedName("inventory_group") val inventoryGroup: String,
    @SerializedName("sub_category_id") val subCategoryId: String = "",
    val qty: Int,
    @SerializedName("sell_price") val sellPrice: Double,
    @SerializedName("sell_rate") val sellRate: Double,
    val width: Double = 0.0,
    val length: Double = 0.0,
    val cft: Double = 0.0,
    val image: String = "",
    val notes: String = "",
    val status: String = "available"
)

data class MobileInventoryLookup(
    val barcode: String,
    val template: BarcodeTemplate? = null,
    val inventory: List<InventoryStock> = emptyList(),
    @SerializedName("available_count") val availableCount: Int = 0
)

data class BarcodeTemplateDraft(
    val barcode: String = "",
    val productName: String = "",
    val brand: String = "",
    val categoryId: String = "",
    val subCategoryId: String = "",
    val description: String = "",
    val buyRate: String = "",
    val sellRate: String = "",
    val qty: String = "1"
)

data class InventorySaveResult(
    @SerializedName("inventory_id") val inventoryId: Int
)

data class MobileCheckoutItem(
    @SerializedName("wood_stock_id") val woodStockId: Int,
    val serial: String,
    val qty: Int,
    val rate: Double,
    val width: Double = 0.0,
    val length: Double = 0.0,
    val cft: Double = 0.0,
    @SerializedName("line_total") val lineTotal: Double
)

data class MobileSaleResult(
    val id: Int,
    @SerializedName("invoice_no") val invoiceNo: String
)

data class MobileCustomerSummary(
    val id: Int,
    val name: String,
    val phone: String = "",
    val address: String = "",
    @SerializedName("total_due") val totalDue: Double = 0.0,
    @SerializedName("created_at") val createdAt: String = ""
)

data class MobileVehicleSummary(
    val id: Int,
    @SerializedName("customer_id") val customerId: Int = 0,
    @SerializedName("customer_name") val customerName: String = "",
    @SerializedName("plate_no") val plateNo: String = "",
    @SerializedName("vehicle_make") val vehicleMake: String = "",
    @SerializedName("vehicle_model") val vehicleModel: String = "",
    @SerializedName("year_model") val yearModel: String = "",
    val color: String = "",
    @SerializedName("engine_no") val engineNo: String = "",
    @SerializedName("chassis_no") val chassisNo: String = "",
    @SerializedName("odo_reading") val odoReading: Double = 0.0,
    val notes: String = "",
    @SerializedName("is_active") val isActive: Int = 1,
    @SerializedName("created_at") val createdAt: String = ""
) {
    val label: String
        get() = listOf(plateNo, vehicleMake, vehicleModel).filter { it.isNotBlank() }.joinToString(" • ")
}

data class MobileVehicleSaveResult(
    val id: Int
)

data class MobileVehicleType(
    val id: Int,
    val brand: String = "",
    val model: String = "",
    val category: String = "",
    val label: String = ""
)

data class MobileWorkOrderSummary(
    val id: Int,
    @SerializedName("work_order_no") val workOrderNo: String = "",
    @SerializedName("customer_id") val customerId: Int = 0,
    @SerializedName("customer_name") val customerName: String = "",
    @SerializedName("vehicle_id") val vehicleId: Int = 0,
    @SerializedName("vehicle_label") val vehicleLabel: String = "",
    val complaint: String = "",
    val diagnosis: String = "",
    @SerializedName("mechanic_id") val mechanicId: Int? = null,
    @SerializedName("mechanic_name") val mechanicName: String = "",
    val priority: String = "normal",
    val status: String = "pending",
    @SerializedName("labor_total") val laborTotal: Double = 0.0,
    @SerializedName("parts_total") val partsTotal: Double = 0.0,
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    @SerializedName("grand_total") val grandTotal: Double = 0.0,
    @SerializedName("paid_amount") val paidAmount: Double = 0.0,
    @SerializedName("due_amount") val dueAmount: Double = 0.0,
    @SerializedName("sale_id") val saleId: Int? = null,
    val notes: String = "",
    @SerializedName("check_in_at") val checkInAt: String = "",
    @SerializedName("created_by") val createdBy: Int = 0
)

data class MobileWorkOrderSaveResult(
    val id: Int
)

data class MobileSaleSummary(
    val id: Int,
    @SerializedName("invoice_no") val invoiceNo: String,
    @SerializedName("customer_name") val customerName: String = "Walk-in",
    @SerializedName("grand_total") val grandTotal: Double = 0.0,
    @SerializedName("paid_amount") val paidAmount: Double = 0.0,
    @SerializedName("due_amount") val dueAmount: Double = 0.0,
    @SerializedName("payment_method") val paymentMethod: String = "cash",
    @SerializedName("sale_date") val saleDate: String = "",
    val status: String = "completed",
    @SerializedName("cashier_name") val cashierName: String = ""
)

data class MobileSaleLine(
    val id: Int = 0,
    val serial: String = "",
    val width: Double = 0.0,
    val length: Double = 0.0,
    val cft: Double = 0.0,
    val rate: Double = 0.0,
    val total: Double = 0.0
)

data class MobilePaymentLine(
    val id: Int = 0,
    val amount: Double = 0.0,
    val method: String = "",
    val reference: String = "",
    val date: String = "",
    @SerializedName("created_by_name") val createdByName: String = ""
)

data class MobileSaleDetail(
    val id: Int,
    @SerializedName("invoice_no") val invoiceNo: String,
    @SerializedName("customer_name") val customerName: String = "Walk-in",
    @SerializedName("total_items") val totalItems: Int = 0,
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    @SerializedName("grand_total") val grandTotal: Double = 0.0,
    @SerializedName("paid_amount") val paidAmount: Double = 0.0,
    @SerializedName("due_amount") val dueAmount: Double = 0.0,
    @SerializedName("payment_method") val paymentMethod: String = "",
    @SerializedName("sale_date") val saleDate: String = "",
    val status: String = "completed",
    val notes: String = "",
    @SerializedName("cashier_name") val cashierName: String = "",
    val items: List<MobileSaleLine> = emptyList(),
    val payments: List<MobilePaymentLine> = emptyList()
)
