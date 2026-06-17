package com.grandtips.motoposmobile.data

import com.google.gson.GsonBuilder
import com.grandtips.motoposmobile.BuildConfig
import com.grandtips.motoposmobile.model.ApiEnvelope
import com.grandtips.motoposmobile.model.InventorySaveResult
import com.grandtips.motoposmobile.model.MobileCustomerSummary
import com.grandtips.motoposmobile.model.MobileInventoryLookup
import com.grandtips.motoposmobile.model.MobileSaleDetail
import com.grandtips.motoposmobile.model.MobileSaleResult
import com.grandtips.motoposmobile.model.MobileSaleSummary
import com.grandtips.motoposmobile.model.MobileSession
import com.grandtips.motoposmobile.model.InventoryStock
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
        stockId: Int,
        qty: Int,
        customerName: String,
        paidAmount: String,
        paymentMethod: String,
        notes: String,
        status: String
    ): Result<MobileSaleResult> = runCatching {
        val payload = mapOf(
            "route" to "mobile/pos/checkout",
            "token" to token,
            "stock_id" to stockId,
            "qty" to qty,
            "customer_name" to customerName,
            "paid_amount" to paidAmount,
            "payment_method" to paymentMethod.lowercase(),
            "notes" to notes,
            "status" to status
        )
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

    private suspend fun runLookup(route: String, token: String, barcode: String): Result<MobileInventoryLookup> = runCatching {
        val raw = api.postRoute(BuildConfig.APPS_SCRIPT_URL, mapOf("route" to route, "token" to token, "barcode" to barcode))
        if (!raw.success || raw.data == null) error(raw.message ?: "Lookup failed")
        GsonHolder.gson.fromJson(GsonHolder.gson.toJson(raw.data), MobileInventoryLookup::class.java)
    }
}

private object GsonHolder {
    val gson = GsonBuilder().create()
}
