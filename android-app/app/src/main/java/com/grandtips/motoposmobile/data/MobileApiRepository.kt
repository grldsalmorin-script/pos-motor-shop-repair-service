package com.grandtips.motoposmobile.data

import com.google.gson.GsonBuilder
import com.grandtips.motoposmobile.BuildConfig
import com.grandtips.motoposmobile.model.ApiEnvelope
import com.grandtips.motoposmobile.model.InventorySaveResult
import com.grandtips.motoposmobile.model.MobileCustomerSummary
import com.grandtips.motoposmobile.model.MobileInventoryLookup
import com.grandtips.motoposmobile.model.MobileSaleDetail
import com.grandtips.motoposmobile.model.MobileCheckoutItem
import com.grandtips.motoposmobile.model.MobileSaleResult
import com.grandtips.motoposmobile.model.MobileSaleSummary
import com.grandtips.motoposmobile.model.MobileSession
import com.grandtips.motoposmobile.model.InventoryStock
import com.grandtips.motoposmobile.model.MobileVehicleSaveResult
import com.grandtips.motoposmobile.model.MobileVehicleSummary
import com.grandtips.motoposmobile.model.MobileVehicleType
import com.grandtips.motoposmobile.model.MobileWorkOrderSaveResult
import com.grandtips.motoposmobile.model.MobileWorkOrderSummary
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface MobileApiRepository {
    @POST
    suspend fun postRoute(@Url url: String, @Body body: Map<String, @JvmSuppressWildcards Any>): ApiEnvelope<Any>

    companion object {
        fun create(): MobileApiRepository {
            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            val client = OkHttpClient.Builder().addInterceptor(logger).build()
            return Retrofit.Builder()
                .baseUrl("https://script.google.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .build()
                .create(MobileApiRepository::class.java)
        }
    }
}

class AppsScriptRepository(private val api: MobileApiRepository) {
    suspend fun login(email: String, password: String): Result<MobileSession> = runCatching {
        val raw = api.postRoute(BuildConfig.APPS_SCRIPT_URL, mapOf("route" to "mobile/login", "email" to email, "password" to password))
        if (!raw.success || raw.data == null) error(raw.message ?: "Login failed")
        GsonHolder.gson.fromJson(GsonHolder.gson.toJson(raw.data), MobileSession::class.java)
    }

    suspend fun lookupBarcodeTemplate(token: String, barcode: String): Result<MobileInventoryLookup> = runLookup(
        route = "mobile/inventory/barcode-template",
        token = token,
        barcode = barcode
    )

    suspend fun lookupPosBarcode(token: String, barcode: String): Result<MobileInventoryLookup> = runLookup(
        route = "mobile/pos/scan",
        token = token,
        barcode = barcode
    )

    suspend fun addInventoryItem(token: String, draft: com.grandtips.motoposmobile.model.BarcodeTemplateDraft): Result<InventorySaveResult> = runCatching {
        val payload = mapOf(
            "route" to "mobile/inventory/add-item",
            "token" to token,
            "barcode" to draft.barcode,
            "product_name" to draft.productName,
            "brand" to draft.brand,
            "category_id" to draft.categoryId,
            "sub_category_id" to draft.subCategoryId,
            "description" to draft.description,
            "buy_rate" to draft.buyRate,
            "sell_rate" to draft.sellRate,
            "qty" to draft.qty
        )
        val raw = api.postRoute(BuildConfig.APPS_SCRIPT_URL, payload)
        if (!raw.success || raw.data == null) error(raw.message ?: "Inventory add failed")
        GsonHolder.gson.fromJson(GsonHolder.gson.toJson(raw.data), InventorySaveResult::class.java)
    }

    suspend fun updateInventoryItem(
        token: String,
        inventoryId: Int,
        draft: com.grandtips.motoposmobile.model.BarcodeTemplateDraft
    ): Result<InventorySaveResult> = runCatching {
        val payload = mapOf(
            "route" to "mobile/inventory/update-item",
            "token" to token,
            "id" to inventoryId,
            "barcode" to draft.barcode,
            "product_name" to draft.productName,
            "brand" to draft.brand,
            "category_id" to draft.categoryId,
            "sub_category_id" to draft.subCategoryId,
            "description" to draft.description,
            "buy_rate" to draft.buyRate,
            "sell_rate" to draft.sellRate,
            "qty" to draft.qty
        )
        val raw = api.postRoute(BuildConfig.APPS_SCRIPT_URL, payload)
        if (!raw.success || raw.data == null) error(raw.message ?: "Inventory update failed")
        GsonHolder.gson.fromJson(GsonHolder.gson.toJson(raw.data), InventorySaveResult::class.java)
    }

    suspend fun submitMobileSale(
        token: String,
        items: List<MobileCheckoutItem>,
        customerName: String,
        paidAmount: String,
        paymentMethod: String,
        notes: String,
        status: String
    ): Result<MobileSaleResult> = runCatching {
        val payload = mutableMapOf<String, Any>(
            "route" to "mobile/pos/checkout",
            "token" to token,
            "customer_name" to customerName,
            "paid_amount" to paidAmount,
            "payment_method" to paymentMethod.lowercase(),
            "notes" to notes,
            "status" to status
        )
        if (items.size == 1) {
            val item = items.first()
            payload["stock_id"] = item.woodStockId
            payload["qty"] = item.qty
            payload["rate"] = item.rate
        }
        payload["items"] = items.map {
            mapOf(
                "wood_stock_id" to it.woodStockId,
                "serial" to it.serial,
                "qty" to it.qty,
                "rate" to it.rate,
                "width" to it.width,
                "length" to it.length,
                "cft" to it.cft,
                "line_total" to it.lineTotal
            )
        }
        val raw = api.postRoute(BuildConfig.APPS_SCRIPT_URL, payload)
        if (!raw.success || raw.data == null) error(raw.message ?: "Sale submission failed")
        GsonHolder.gson.fromJson(GsonHolder.gson.toJson(raw.data), MobileSaleResult::class.java)
    }

    suspend fun getCustomers(token: String): Result<List<MobileCustomerSummary>> = runCatching {
        val raw = api.postRoute(BuildConfig.APPS_SCRIPT_URL, mapOf("route" to "mobile/customers/list", "token" to token))
        if (!raw.success || raw.data == null) error(raw.message ?: "Customers load failed")
        GsonHolder.gson.fromJson(
            GsonHolder.gson.toJson(raw.data),
            com.google.gson.reflect.TypeToken.getParameterized(List::class.java, MobileCustomerSummary::class.java).type
        )
    }

    suspend fun addCustomer(
        token: String,
        name: String,
        phone: String,
        address: String
    ): Result<MobileCustomerSummary> = runCatching {
        val raw = api.postRoute(
            BuildConfig.APPS_SCRIPT_URL,
            mapOf(
                "route" to "mobile/customers/create",
                "token" to token,
                "name" to name,
                "phone" to phone,
                "address" to address
            )
        )
        if (!raw.success || raw.data == null) error(raw.message ?: "Customer add failed")
        GsonHolder.gson.fromJson(GsonHolder.gson.toJson(raw.data), MobileCustomerSummary::class.java)
    }

    suspend fun getInventoryItems(token: String): Result<List<InventoryStock>> = runCatching {
        val raw = api.postRoute(BuildConfig.APPS_SCRIPT_URL, mapOf("route" to "mobile/inventory/list", "token" to token))
        if (!raw.success || raw.data == null) error(raw.message ?: "Inventory load failed")
        GsonHolder.gson.fromJson(
            GsonHolder.gson.toJson(raw.data),
            com.google.gson.reflect.TypeToken.getParameterized(List::class.java, InventoryStock::class.java).type
        )
    }

    suspend fun getSales(token: String): Result<List<MobileSaleSummary>> = runCatching {
        val raw = api.postRoute(BuildConfig.APPS_SCRIPT_URL, mapOf("route" to "mobile/sales/list", "token" to token))
        if (!raw.success || raw.data == null) error(raw.message ?: "Sales load failed")
        GsonHolder.gson.fromJson(
            GsonHolder.gson.toJson(raw.data),
            com.google.gson.reflect.TypeToken.getParameterized(List::class.java, MobileSaleSummary::class.java).type
        )
    }

    suspend fun getSaleDetail(token: String, saleId: Int): Result<MobileSaleDetail> = runCatching {
        val raw = api.postRoute(BuildConfig.APPS_SCRIPT_URL, mapOf("route" to "mobile/sales/detail", "token" to token, "sale_id" to saleId))
        if (!raw.success || raw.data == null) error(raw.message ?: "Sale detail failed")
        GsonHolder.gson.fromJson(GsonHolder.gson.toJson(raw.data), MobileSaleDetail::class.java)
    }

    suspend fun getVehicles(token: String): Result<List<MobileVehicleSummary>> = runCatching {
        val raw = api.postRoute(BuildConfig.APPS_SCRIPT_URL, mapOf("route" to "mobile/vehicles/list", "token" to token))
        if (!raw.success || raw.data == null) error(raw.message ?: "Vehicle load failed")
        GsonHolder.gson.fromJson(
            GsonHolder.gson.toJson(raw.data),
            com.google.gson.reflect.TypeToken.getParameterized(List::class.java, MobileVehicleSummary::class.java).type
        )
    }

    suspend fun getVehicleTypes(token: String): Result<List<MobileVehicleType>> = runCatching {
        val raw = api.postRoute(BuildConfig.APPS_SCRIPT_URL, mapOf("route" to "mobile/vehicle-types/list", "token" to token))
        if (!raw.success || raw.data == null) error(raw.message ?: "Vehicle types load failed")
        GsonHolder.gson.fromJson(
            GsonHolder.gson.toJson(raw.data),
            com.google.gson.reflect.TypeToken.getParameterized(List::class.java, MobileVehicleType::class.java).type
        )
    }

    suspend fun addVehicle(
        token: String,
        customerId: Int,
        plateNo: String,
        vehicleMake: String,
        vehicleModel: String,
        yearModel: String,
        color: String,
        engineNo: String,
        chassisNo: String,
        odoReading: String,
        notes: String
    ): Result<MobileVehicleSaveResult> = runCatching {
        val raw = api.postRoute(
            BuildConfig.APPS_SCRIPT_URL,
            mapOf(
                "route" to "mobile/vehicles/create",
                "token" to token,
                "customer_id" to customerId,
                "plate_no" to plateNo,
                "vehicle_make" to vehicleMake,
                "vehicle_model" to vehicleModel,
                "year_model" to yearModel,
                "color" to color,
                "engine_no" to engineNo,
                "chassis_no" to chassisNo,
                "odo_reading" to odoReading,
                "notes" to notes
            )
        )
        if (!raw.success || raw.data == null) error(raw.message ?: "Vehicle create failed")
        GsonHolder.gson.fromJson(GsonHolder.gson.toJson(raw.data), MobileVehicleSaveResult::class.java)
    }

    suspend fun updateVehicle(
        token: String,
        id: Int,
        customerId: Int,
        plateNo: String,
        vehicleMake: String,
        vehicleModel: String,
        yearModel: String,
        color: String,
        engineNo: String,
        chassisNo: String,
        odoReading: String,
        notes: String,
        isActive: Int
    ): Result<MobileVehicleSaveResult> = runCatching {
        val raw = api.postRoute(
            BuildConfig.APPS_SCRIPT_URL,
            mapOf(
                "route" to "mobile/vehicles/update",
                "token" to token,
                "id" to id,
                "customer_id" to customerId,
                "plate_no" to plateNo,
                "vehicle_make" to vehicleMake,
                "vehicle_model" to vehicleModel,
                "year_model" to yearModel,
                "color" to color,
                "engine_no" to engineNo,
                "chassis_no" to chassisNo,
                "odo_reading" to odoReading,
                "notes" to notes,
                "is_active" to isActive
            )
        )
        if (!raw.success || raw.data == null) error(raw.message ?: "Vehicle update failed")
        GsonHolder.gson.fromJson(GsonHolder.gson.toJson(raw.data), MobileVehicleSaveResult::class.java)
    }

    suspend fun getWorkOrders(token: String): Result<List<MobileWorkOrderSummary>> = runCatching {
        val raw = api.postRoute(BuildConfig.APPS_SCRIPT_URL, mapOf("route" to "mobile/work-orders/list", "token" to token))
        if (!raw.success || raw.data == null) error(raw.message ?: "Work orders load failed")
        GsonHolder.gson.fromJson(
            GsonHolder.gson.toJson(raw.data),
            com.google.gson.reflect.TypeToken.getParameterized(List::class.java, MobileWorkOrderSummary::class.java).type
        )
    }

    suspend fun createWorkOrder(
        token: String,
        customerId: Int,
        vehicleId: Int,
        complaint: String,
        diagnosis: String,
        priority: String,
        notes: String
    ): Result<MobileWorkOrderSaveResult> = runCatching {
        val raw = api.postRoute(
            BuildConfig.APPS_SCRIPT_URL,
            mapOf(
                "route" to "mobile/work-orders/create",
                "token" to token,
                "customer_id" to customerId,
                "vehicle_id" to vehicleId,
                "complaint" to complaint,
                "diagnosis" to diagnosis,
                "priority" to priority,
                "notes" to notes
            )
        )
        if (!raw.success || raw.data == null) error(raw.message ?: "Work order create failed")
        GsonHolder.gson.fromJson(GsonHolder.gson.toJson(raw.data), MobileWorkOrderSaveResult::class.java)
    }

    suspend fun updateWorkOrder(
        token: String,
        id: Int,
        customerId: Int,
        vehicleId: Int,
        complaint: String,
        diagnosis: String,
        priority: String,
        notes: String
    ): Result<MobileWorkOrderSaveResult> = runCatching {
        val raw = api.postRoute(
            BuildConfig.APPS_SCRIPT_URL,
            mapOf(
                "route" to "mobile/work-orders/update",
                "token" to token,
                "id" to id,
                "customer_id" to customerId,
                "vehicle_id" to vehicleId,
                "complaint" to complaint,
                "diagnosis" to diagnosis,
                "priority" to priority,
                "notes" to notes
            )
        )
        if (!raw.success || raw.data == null) error(raw.message ?: "Work order update failed")
        GsonHolder.gson.fromJson(GsonHolder.gson.toJson(raw.data), MobileWorkOrderSaveResult::class.java)
    }

    suspend fun addWorkOrderItem(
        token: String,
        workOrderId: Int,
        stockId: Int,
        qty: Int,
        notes: String
    ): Result<MobileWorkOrderSaveResult> = runCatching {
        val raw = api.postRoute(
            BuildConfig.APPS_SCRIPT_URL,
            mapOf(
                "route" to "mobile/work-orders/add-item",
                "token" to token,
                "work_order_id" to workOrderId,
                "wood_stock_id" to stockId,
                "qty" to qty,
                "notes" to notes
            )
        )
        if (!raw.success || raw.data == null) error(raw.message ?: "Work order item add failed")
        GsonHolder.gson.fromJson(GsonHolder.gson.toJson(raw.data), MobileWorkOrderSaveResult::class.java)
    }

    suspend fun checkoutWorkOrder(
        token: String,
        workOrderId: Int,
        paidAmount: String,
        paymentMethod: String,
        notes: String
    ): Result<MobileSaleResult> = runCatching {
        val raw = api.postRoute(
            BuildConfig.APPS_SCRIPT_URL,
            mapOf(
                "route" to "mobile/work-orders/checkout",
                "token" to token,
                "work_order_id" to workOrderId,
                "paid_amount" to paidAmount,
                "payment_method" to paymentMethod.lowercase(),
                "notes" to notes
            )
        )
        if (!raw.success || raw.data == null) error(raw.message ?: "Work order checkout failed")
        GsonHolder.gson.fromJson(GsonHolder.gson.toJson(raw.data), MobileSaleResult::class.java)
    }

    private suspend fun runLookup(route: String, token: String, barcode: String): Result<MobileInventoryLookup> = runCatching {
        val raw = api.postRoute(BuildConfig.APPS_SCRIPT_URL, mapOf("route" to route, "token" to token, "barcode" to barcode))
        if (!raw.success || raw.data == null) error(raw.message ?: "Lookup failed")
        GsonHolder.gson.fromJson(GsonHolder.gson.toJson(raw.data), MobileInventoryLookup::class.java)
    }
}

private object GsonHolder {
    val gson = GsonBuilder().create()
}
