package com.grandtips.motoposmobile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grandtips.motoposmobile.data.AppsScriptRepository
import com.grandtips.motoposmobile.data.MobileApiRepository
import com.grandtips.motoposmobile.model.BarcodeTemplateDraft
import com.grandtips.motoposmobile.model.InventoryStock
import com.grandtips.motoposmobile.model.MobileCustomerSummary
import com.grandtips.motoposmobile.model.MobileDashboardTemplate
import com.grandtips.motoposmobile.model.MobileCheckoutItem
import com.grandtips.motoposmobile.model.MobileInventoryLookup
import com.grandtips.motoposmobile.model.MobileSaleDetail
import com.grandtips.motoposmobile.model.MobileSaleSummary
import com.grandtips.motoposmobile.model.MobileSaleResult
import com.grandtips.motoposmobile.model.MobileSession
import com.grandtips.motoposmobile.model.MobileUser
import com.grandtips.motoposmobile.model.MobileVehicleSummary
import com.grandtips.motoposmobile.model.MobileVehicleType
import com.grandtips.motoposmobile.model.MobileWorkOrderSummary
import com.grandtips.motoposmobile.permissions.RolePermissionTemplates
import com.grandtips.motoposmobile.scanner.BarcodeScannerView
import java.util.Locale
import kotlinx.coroutines.launch

private val NavyDark = Color(0xFF072745)
private val Navy = Color(0xFF0A2F56)
private val Blue = Color(0xFF1776D2)
private val BlueSoft = Color(0xFFEAF3FF)
private val SurfaceTint = Color(0xFFF4F7FB)
private val Border = Color(0xFFD8E2EE)
private val TextPrimary = Color(0xFF1F2937)
private val TextMuted = Color(0xFF6B7280)
private val Success = Color(0xFF157347)
private val Warning = Color(0xFFD97706)
private val Danger = Color(0xFFC44536)

private data class NavItem(
    val key: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private data class TicketCartItem(
    val stockId: Int,
    val barcode: String,
    val title: String,
    val subtitle: String,
    val qty: Int,
    val rate: Double,
    val width: Double,
    val length: Double,
    val cft: Double,
    val lineTotal: Double
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val factory = remember {
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        val repo = AppsScriptRepository(MobileApiRepository.create())
                        return MainViewModel(repo) as T
                    }
                }
            }
            val vm: MainViewModel = viewModel(factory = factory)
            MotorShopPosMobileApp(vm)
        }
    }
}

class MainViewModel(private val repo: AppsScriptRepository) : ViewModel() {
    var session by mutableStateOf<MobileSession?>(null)
    var dashboard by mutableStateOf<List<MobileDashboardTemplate>>(emptyList())
    var lastLookup by mutableStateOf<MobileInventoryLookup?>(null)
    var inventoryItems by mutableStateOf<List<InventoryStock>>(emptyList())
    var customers by mutableStateOf<List<MobileCustomerSummary>>(emptyList())
    var vehicles by mutableStateOf<List<MobileVehicleSummary>>(emptyList())
    var vehicleTypes by mutableStateOf<List<MobileVehicleType>>(emptyList())
    var workOrders by mutableStateOf<List<MobileWorkOrderSummary>>(emptyList())
    var sales by mutableStateOf<List<MobileSaleSummary>>(emptyList())
    var selectedSaleDetail by mutableStateOf<MobileSaleDetail?>(null)
    var message by mutableStateOf("")
    var loading by mutableStateOf(false)

    suspend fun login(email: String, password: String) {
        loading = true
        val result = repo.login(email, password)
        result.onSuccess {
            session = it
            dashboard = RolePermissionTemplates.templatesForRole(it.user.role)
            message = "Logged in as ${it.user.fullName}"
            refreshWorkspace()
        }.onFailure {
            message = it.message ?: "Login failed"
        }
        loading = false
    }

    suspend fun refreshWorkspace() {
        val token = session?.token ?: return
        repo.getInventoryItems(token).onSuccess { inventoryItems = it }.onFailure { message = it.message ?: "Inventory load failed" }
        repo.getCustomers(token).onSuccess { customers = it }.onFailure { message = it.message ?: "Customer load failed" }
        repo.getVehicles(token).onSuccess { vehicles = it }.onFailure { message = it.message ?: "Vehicle load failed" }
        repo.getVehicleTypes(token).onSuccess { vehicleTypes = it }.onFailure { message = it.message ?: "Vehicle type load failed" }
        repo.getWorkOrders(token).onSuccess { workOrders = it }.onFailure { message = it.message ?: "Work order load failed" }
        repo.getSales(token).onSuccess { sales = it }.onFailure { message = it.message ?: "Receipts load failed" }
    }

    suspend fun loadSaleDetail(id: Int) {
        val token = session?.token ?: return
        loading = true
        val result = repo.getSaleDetail(token, id)
        loading = false
        result.onSuccess {
            selectedSaleDetail = it
        }.onFailure {
            message = it.message ?: "Failed to load receipt"
        }
    }

    fun clearSaleDetail() {
        selectedSaleDetail = null
    }

    suspend fun scanForInventory(code: String) {
        val token = session?.token ?: return
        loading = true
        val result = repo.lookupBarcodeTemplate(token, code)
        loading = false
        result.onSuccess {
            lastLookup = it
            message = if (it.template != null) "Template found for ${it.barcode}" else "No template yet for ${it.barcode}"
        }.onFailure { message = it.message ?: "Lookup failed" }
    }

    suspend fun scanForInventoryResult(code: String): MobileInventoryLookup? {
        val token = session?.token ?: return null
        loading = true
        val result = repo.lookupBarcodeTemplate(token, code)
        loading = false
        return result.onSuccess {
            lastLookup = it
            message = if (it.template != null) "Template found for ${it.barcode}" else "No template yet for ${it.barcode}"
        }.onFailure {
            message = it.message ?: "Lookup failed"
        }.getOrNull()
    }

    suspend fun scanForPos(code: String) {
        val token = session?.token ?: return
        loading = true
        val result = repo.lookupPosBarcode(token, code)
        loading = false
        result.onSuccess {
            lastLookup = it
            message = if (it.inventory.isNotEmpty()) "Item available at ${formatMoney(it.inventory.first().sellRate)}" else "No available stock for ${it.barcode}"
        }.onFailure { message = it.message ?: "POS scan failed" }
    }

    suspend fun saveInventoryDraft(draft: BarcodeTemplateDraft) {
        val current = session ?: return
        loading = true
        val result = repo.addInventoryItem(current.token, draft)
        loading = false
        result.onSuccess {
            message = "Inventory saved. Stock ID ${it.inventoryId}"
            refreshWorkspace()
        }.onFailure { message = it.message ?: "Inventory save failed" }
    }

    suspend fun submitSale(
        items: List<MobileCheckoutItem>,
        customerName: String,
        paidAmount: String,
        paymentMethod: String,
        notes: String,
        status: String
    ): Boolean {
        val current = session ?: return false
        loading = true
        val result: Result<MobileSaleResult> = repo.submitMobileSale(current.token, items, customerName, paidAmount, paymentMethod, notes, status)
        loading = false
        return result.onSuccess {
            message = "Sale saved: ${it.invoiceNo}"
            lastLookup = null
            refreshWorkspace()
        }.onFailure { message = it.message ?: "Sale submission failed" }.isSuccess
    }

    suspend fun createCustomer(
        name: String,
        phone: String,
        address: String
    ): MobileCustomerSummary? {
        val current = session ?: return null
        loading = true
        val result = repo.addCustomer(current.token, name, phone, address)
        loading = false
        return result.onSuccess {
            message = "Customer added: ${it.name}"
            refreshWorkspace()
        }.onFailure {
            message = it.message ?: "Customer save failed"
        }.getOrNull()
    }

    suspend fun createVehicle(
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
    ): Int? {
        val current = session ?: return null
        loading = true
        val result = repo.addVehicle(current.token, customerId, plateNo, vehicleMake, vehicleModel, yearModel, color, engineNo, chassisNo, odoReading, notes)
        loading = false
        return result.onSuccess {
            message = "Vehicle added: $plateNo"
            refreshWorkspace()
        }.onFailure {
            message = it.message ?: "Vehicle save failed"
        }.getOrNull()?.id
    }

    suspend fun updateVehicle(
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
    ): Int? {
        val current = session ?: return null
        loading = true
        val result = repo.updateVehicle(current.token, id, customerId, plateNo, vehicleMake, vehicleModel, yearModel, color, engineNo, chassisNo, odoReading, notes, isActive)
        loading = false
        return result.onSuccess {
            message = "Vehicle updated: $plateNo"
            refreshWorkspace()
        }.onFailure {
            message = it.message ?: "Vehicle update failed"
        }.getOrNull()?.id
    }

    suspend fun createWorkOrder(
        customerId: Int,
        vehicleId: Int,
        complaint: String,
        diagnosis: String,
        priority: String,
        notes: String
    ): Int? {
        val current = session ?: return null
        loading = true
        val result = repo.createWorkOrder(current.token, customerId, vehicleId, complaint, diagnosis, priority, notes)
        loading = false
        return result.onSuccess {
            message = "Work order created"
            refreshWorkspace()
        }.onFailure {
            message = it.message ?: "Work order save failed"
        }.getOrNull()?.id
    }

    suspend fun updateWorkOrder(
        id: Int,
        customerId: Int,
        vehicleId: Int,
        complaint: String,
        diagnosis: String,
        priority: String,
        notes: String
    ): Int? {
        val current = session ?: return null
        loading = true
        val result = repo.updateWorkOrder(current.token, id, customerId, vehicleId, complaint, diagnosis, priority, notes)
        loading = false
        return result.onSuccess {
            message = "Work order updated"
            refreshWorkspace()
        }.onFailure {
            message = it.message ?: "Work order update failed"
        }.getOrNull()?.id
    }

    suspend fun addItemToWorkOrder(
        workOrderId: Int,
        stockId: Int,
        qty: Int,
        notes: String
    ): Boolean {
        val current = session ?: return false
        loading = true
        val result = repo.addWorkOrderItem(current.token, workOrderId, stockId, qty, notes)
        loading = false
        return result.onSuccess {
            message = "Item added to work order"
            refreshWorkspace()
        }.onFailure {
            message = it.message ?: "Failed to add work-order item"
        }.isSuccess
    }

    suspend fun checkoutWorkOrder(
        workOrderId: Int,
        paidAmount: String,
        paymentMethod: String,
        notes: String
    ): Boolean {
        val current = session ?: return false
        loading = true
        val result = repo.checkoutWorkOrder(current.token, workOrderId, paidAmount, paymentMethod, notes)
        loading = false
        return result.onSuccess {
            message = "Service checked out: ${it.invoiceNo}"
            lastLookup = null
            refreshWorkspace()
        }.onFailure {
            message = it.message ?: "Work order checkout failed"
        }.isSuccess
    }

    suspend fun saveInventoryDraft(
        draft: BarcodeTemplateDraft,
        inventoryId: Int?
    ): Boolean {
        val current = session ?: return false
        loading = true
        val result = if (inventoryId == null) {
            repo.addInventoryItem(current.token, draft)
        } else {
            repo.updateInventoryItem(current.token, inventoryId, draft)
        }
        loading = false
        return result.onSuccess {
            message = if (inventoryId == null) {
                "Inventory saved. Stock ID ${it.inventoryId}"
            } else {
                "Inventory updated. Stock ID ${it.inventoryId}"
            }
            refreshWorkspace()
        }.onFailure {
            message = it.message ?: if (inventoryId == null) "Inventory save failed" else "Inventory update failed"
        }.isSuccess
    }

    fun clearMessage() {
        message = ""
    }

    fun clearTicketLookup() {
        lastLookup = null
    }

    fun previewInventoryItem(item: InventoryStock) {
        lastLookup = MobileInventoryLookup(
            barcode = item.barcode,
            template = null,
            inventory = listOf(item),
            availableCount = item.qty
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MotorShopPosMobileApp(vm: MainViewModel) {
    var email by remember { mutableStateOf("admin@motorshop.demo") }
    var password by remember { mutableStateOf("admin123") }
    var barcode by remember { mutableStateOf("") }
    var draft by remember { mutableStateOf(BarcodeTemplateDraft()) }
    var editingInventoryId by remember { mutableStateOf<Int?>(null) }
    var showItemEditor by remember { mutableStateOf(false) }
    var cameraAllowed by remember { mutableStateOf(false) }
    var activeNav by remember { mutableStateOf("home") }
    var ticketMode by remember { mutableStateOf("sale") }
    var customerQuery by remember { mutableStateOf("Walk-in customer") }
    var selectedCustomerId by remember { mutableStateOf<Int?>(null) }
    var showNewCustomerForm by remember { mutableStateOf(false) }
    var newCustomerName by remember { mutableStateOf("") }
    var newCustomerPhone by remember { mutableStateOf("") }
    var newCustomerAddress by remember { mutableStateOf("") }
    var vehicleQuery by remember { mutableStateOf("") }
    var selectedVehicleId by remember { mutableStateOf<Int?>(null) }
    var showNewVehicleForm by remember { mutableStateOf(false) }
    var editingVehicleId by remember { mutableStateOf<Int?>(null) }
    var vehicleIsActive by remember { mutableStateOf(1) }
    var vehiclePlateNo by remember { mutableStateOf("") }
    var vehicleMake by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }
    var vehicleYearModel by remember { mutableStateOf("") }
    var vehicleColor by remember { mutableStateOf("") }
    var vehicleEngineNo by remember { mutableStateOf("") }
    var vehicleChassisNo by remember { mutableStateOf("") }
    var vehicleOdoReading by remember { mutableStateOf("") }
    var vehicleNotes by remember { mutableStateOf("") }
    var selectedWorkOrderId by remember { mutableStateOf<Int?>(null) }
    var editingWorkOrderId by remember { mutableStateOf<Int?>(null) }
    var workComplaint by remember { mutableStateOf("") }
    var workDiagnosis by remember { mutableStateOf("") }
    var workPriority by remember { mutableStateOf("normal") }
    var saleQty by remember { mutableStateOf("1") }
    var paidAmount by remember { mutableStateOf("") }
    var payMethod by remember { mutableStateOf("Cash") }
    var notes by remember { mutableStateOf("") }
    var ticketItems by remember { mutableStateOf(listOf<TicketCartItem>()) }
    var showPosScanner by remember { mutableStateOf(false) }
    var showInventoryScanner by remember { mutableStateOf(false) }
    var showHoldConfirm by remember { mutableStateOf(false) }
    var showCompleteConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        cameraAllowed = it
    }

    LaunchedEffect(Unit) {
        cameraAllowed = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(vm.message) {
        if (vm.message.isNotBlank()) {
            snackbarHostState.showSnackbar(vm.message)
            vm.clearMessage()
        }
    }

    MaterialTheme {
        if (vm.session == null) {
            LoginScreen(
                email = email,
                password = password,
                loading = vm.loading,
                message = vm.message,
                onEmailChange = { email = it },
                onPasswordChange = { password = it },
                onLogin = { scope.launch { vm.login(email, password) } }
            )
        } else {
            val session = vm.session!!
            val navItems = remember {
                listOf(
                    NavItem("home", "Sales", Icons.Filled.PointOfSale),
                    NavItem("pos", "Ticket", Icons.Filled.Sell),
                    NavItem("inventory", "Items", Icons.Filled.Warehouse),
                    NavItem("workorders", "Work Orders", Icons.AutoMirrored.Filled.Assignment),
                    NavItem("vehicles", "Vehicle Registry", Icons.Filled.DirectionsCar),
                    NavItem("receipts", "Receipts", Icons.AutoMirrored.Filled.ReceiptLong),
                    NavItem("about", "About", Icons.Filled.Info)
                )
            }
            val selectedInventoryItem = vm.lastLookup?.inventory?.firstOrNull()
            val currentPrice = selectedInventoryItem?.sellRate ?: 0.0
            val qtyAvailable = selectedInventoryItem?.qty ?: 0
            val selectedQty = (saleQty.toIntOrNull() ?: 1).coerceAtLeast(1).let { if (qtyAvailable > 0) minOf(it, qtyAvailable) else it }
            val totalPrice = currentPrice * selectedQty
            val cartTotal = ticketItems.sumOf { it.lineTotal }
            val dueAmount = (cartTotal - (paidAmount.toDoubleOrNull() ?: 0.0)).coerceAtLeast(0.0)
            val filteredTicketInventory = remember(barcode, vm.inventoryItems) {
                val q = barcode.trim().lowercase()
                if (q.isBlank()) emptyList() else vm.inventoryItems.filter {
                    val hay = listOf(it.barcode, it.sku, it.inventoryGroup, it.notes).joinToString(" ").lowercase()
                    hay.contains(q)
                }.take(12)
            }
            val filteredCustomers = remember(customerQuery, vm.customers) {
                val q = customerQuery.trim().lowercase()
                if (q.isBlank() || q == "walk-in customer") emptyList() else vm.customers.filter {
                    it.name.lowercase().contains(q) || it.phone.lowercase().contains(q)
                }.take(8)
            }
            val filteredVehicles = remember(vehicleQuery, vm.vehicleTypes) {
                val q = vehicleQuery.trim().lowercase()
                val pool = vm.vehicleTypes
                if (q.isBlank()) emptyList() else pool.filter {
                    it.brand.lowercase().contains(q) ||
                        it.model.lowercase().contains(q) ||
                        it.category.lowercase().contains(q) ||
                        it.label.lowercase().contains(q)
                }.take(20)
            }
            val visibleWorkOrders = remember(selectedCustomerId, selectedVehicleId, vm.workOrders) {
                vm.workOrders.filter {
                    (selectedCustomerId == null || it.customerId == selectedCustomerId) &&
                        (selectedVehicleId == null || it.vehicleId == selectedVehicleId)
                }.take(12)
            }
            val resetTicketForm = {
                ticketMode = "sale"
                barcode = ""
                customerQuery = "Walk-in customer"
                selectedCustomerId = null
                showNewCustomerForm = false
                newCustomerName = ""
                newCustomerPhone = ""
                newCustomerAddress = ""
                vehicleQuery = ""
                selectedVehicleId = null
                showNewVehicleForm = false
                editingVehicleId = null
                vehicleIsActive = 1
                vehiclePlateNo = ""
                vehicleMake = ""
                vehicleModel = ""
                vehicleYearModel = ""
                vehicleColor = ""
                vehicleEngineNo = ""
                vehicleChassisNo = ""
                vehicleOdoReading = ""
                vehicleNotes = ""
                selectedWorkOrderId = null
                editingWorkOrderId = null
                workComplaint = ""
                workDiagnosis = ""
                workPriority = "normal"
                saleQty = "1"
                paidAmount = ""
                payMethod = "Cash"
                notes = ""
                ticketItems = emptyList()
                showPosScanner = false
                showHoldConfirm = false
                showCompleteConfirm = false
                vm.clearTicketLookup()
            }
            val applyLookupToDraft = { lookup: MobileInventoryLookup?, fallbackQty: String? ->
                val template = lookup?.template
                if (template != null) {
                    draft = draft.copy(
                        barcode = template.barcode,
                        productName = template.productName,
                        brand = template.brand,
                        categoryId = template.categoryId,
                        subCategoryId = template.subCategoryId,
                        description = template.description,
                        buyRate = template.defaultBuyRate.toString(),
                        sellRate = template.defaultSellRate.toString(),
                        qty = fallbackQty ?: template.defaultQty.toString()
                    )
                } else if (lookup != null) {
                    draft = draft.copy(barcode = lookup.barcode, qty = fallbackQty ?: draft.qty)
                }
            }
            val addSelectedItemToTicket = {
                val item = selectedInventoryItem
                if (item != null) {
                    val existing = ticketItems.firstOrNull { it.stockId == item.id }
                    val addedQty = selectedQty
                    val newQty = (existing?.qty ?: 0) + addedQty
                    val safeQty = if (item.qty > 0) minOf(newQty, item.qty) else newQty
                    val next = TicketCartItem(
                        stockId = item.id,
                        barcode = item.barcode,
                        title = item.sku,
                        subtitle = item.inventoryGroup,
                        qty = safeQty,
                        rate = item.sellRate,
                        width = item.width,
                        length = item.length,
                        cft = item.cft,
                        lineTotal = item.sellRate * safeQty
                    )
                    ticketItems = if (existing == null) {
                        ticketItems + next
                    } else {
                        ticketItems.map { if (it.stockId == item.id) next else it }
                    }
                    barcode = ""
                    saleQty = "1"
                    vm.clearTicketLookup()
                }
            }

            suspend fun ensureServiceVehicleId(): Int? {
                selectedVehicleId?.let { return it }
                val customerId = selectedCustomerId ?: return null
                if (vehicleMake.isBlank() || vehicleModel.isBlank()) return null
                val generatedPlate = if (vehiclePlateNo.isNotBlank()) vehiclePlateNo else "TEMP-" + System.currentTimeMillis().toString().takeLast(6)
                val createdId = vm.createVehicle(
                    customerId = customerId,
                    plateNo = generatedPlate,
                    vehicleMake = vehicleMake,
                    vehicleModel = vehicleModel,
                    yearModel = vehicleYearModel,
                    color = vehicleColor,
                    engineNo = vehicleEngineNo,
                    chassisNo = vehicleChassisNo,
                    odoReading = vehicleOdoReading,
                    notes = vehicleNotes
                ) ?: return null
                selectedVehicleId = createdId
                if (vehiclePlateNo.isBlank()) vehiclePlateNo = generatedPlate
                vehicleQuery = listOf(vehicleMake, vehicleModel).filter { it.isNotBlank() }.joinToString(" • ")
                return createdId
            }

            suspend fun ensureServiceWorkOrderId(): Int? {
                return selectedWorkOrderId ?: vm.createWorkOrder(
                    customerId = selectedCustomerId ?: return null,
                    vehicleId = ensureServiceVehicleId() ?: return null,
                    complaint = workComplaint,
                    diagnosis = workDiagnosis,
                    priority = workPriority,
                    notes = notes
                )?.also { selectedWorkOrderId = it }
            }

            if (showHoldConfirm) {
                SaleConfirmDialog(
                    title = if (ticketMode == "sale") "Hold Ticket?" else "Save Service Ticket?",
                    message = if (ticketMode == "sale") "This will save the current ticket as pending." else "This will save the current service ticket as a work order.",
                    confirmLabel = "Yes, Hold",
                    onDismiss = { showHoldConfirm = false },
                    onConfirm = {
                        showHoldConfirm = false
                        scope.launch {
                            if (ticketMode == "sale") {
                                val items = ticketItems.map {
                                    MobileCheckoutItem(it.stockId, it.barcode, it.qty, it.rate, it.width, it.length, it.cft, it.lineTotal)
                                }
                                if (items.isNotEmpty() && vm.submitSale(items, customerQuery, "0", payMethod, notes, "pending")) {
                                    resetTicketForm()
                                }
                            } else {
                                val workOrderId = ensureServiceWorkOrderId() ?: return@launch
                                val ok = ticketItems.all { vm.addItemToWorkOrder(workOrderId, it.stockId, it.qty, notes) }
                                if (ok) resetTicketForm()
                            }
                        }
                    }
                )
            }

            if (showCompleteConfirm) {
                SaleConfirmDialog(
                    title = if (ticketMode == "sale") "Complete Sale?" else "Complete Service?",
                    message = if (ticketMode == "sale") "This will finalize the ticket and record the payment." else "This will checkout the work order and record the payment.",
                    confirmLabel = if (ticketMode == "sale") "Yes, Complete" else "Yes, Checkout",
                    onDismiss = { showCompleteConfirm = false },
                    onConfirm = {
                        showCompleteConfirm = false
                        scope.launch {
                            if (ticketMode == "sale") {
                                val items = ticketItems.map {
                                    MobileCheckoutItem(it.stockId, it.barcode, it.qty, it.rate, it.width, it.length, it.cft, it.lineTotal)
                                }
                                if (items.isNotEmpty() && vm.submitSale(items, customerQuery, paidAmount, payMethod, notes, "completed")) {
                                    resetTicketForm()
                                }
                            } else {
                                val workOrderId = ensureServiceWorkOrderId() ?: return@launch
                                val added = ticketItems.all { vm.addItemToWorkOrder(workOrderId, it.stockId, it.qty, notes) }
                                if (added && vm.checkoutWorkOrder(workOrderId, paidAmount, payMethod, notes)) {
                                    resetTicketForm()
                                }
                            }
                        }
                    }
                )
            }

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {
                        DrawerPanel(
                            user = session.user,
                            items = navItems,
                            activeKey = activeNav,
                            onSelect = { key ->
                                activeNav = key
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Column {
                                        Text(screenTitle(activeNav), color = Color.White, fontWeight = FontWeight.SemiBold)
                                        Text("Grand Tips MotoPH", color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                navigationIcon = {
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                                    }
                                },
                                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = Navy)
                            )
                        },
                        containerColor = SurfaceTint,
                        floatingActionButton = {
                            if (activeNav == "inventory") {
                                FloatingActionButton(
                                    onClick = {
                                        editingInventoryId = null
                                        barcode = ""
                                        draft = BarcodeTemplateDraft()
                                        showItemEditor = true
                                        if (!cameraAllowed) cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        showInventoryScanner = true
                                    },
                                    containerColor = Blue,
                                    contentColor = Color.White
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Add item")
                                }
                            }
                        },
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                    ) { padding ->
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(padding),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                MobileHeaderCard(user = session.user, dashboard = vm.dashboard)
                            }
                            item {
                                when (activeNav) {
                                "home" -> HomeDashboardPanel(
                                    sales = vm.sales,
                                    inventoryCount = vm.inventoryItems.size,
                                    customerCount = vm.customers.size,
                                    onOpenPos = { activeNav = "pos" },
                                    onOpenInventory = { activeNav = "inventory" },
                                    onOpenReceipts = { activeNav = "receipts" }
                                )

                                "about" -> AboutPanel(user = session.user, cards = vm.dashboard)

                                "vehicles" -> VehicleRegistryPanel(
                                    vehicles = vm.vehicles,
                                    customerQuery = customerQuery,
                                    customers = filteredCustomers,
                                    selectedCustomerId = selectedCustomerId,
                                    showNewVehicleForm = showNewVehicleForm,
                                    editingVehicleId = editingVehicleId,
                                    vehicleIsActive = vehicleIsActive,
                                    vehiclePlateNo = vehiclePlateNo,
                                    vehicleMake = vehicleMake,
                                    vehicleModel = vehicleModel,
                                    vehicleYearModel = vehicleYearModel,
                                    vehicleColor = vehicleColor,
                                    vehicleEngineNo = vehicleEngineNo,
                                    vehicleChassisNo = vehicleChassisNo,
                                    vehicleOdoReading = vehicleOdoReading,
                                    vehicleNotes = vehicleNotes,
                                    loading = vm.loading,
                                    onCustomerQueryChange = {
                                        customerQuery = it
                                        if (it.isBlank()) selectedCustomerId = null
                                    },
                                    onPickCustomer = {
                                        selectedCustomerId = it.id
                                        customerQuery = it.name
                                    },
                                    onToggleVehicleForm = {
                                        if (showNewVehicleForm) {
                                            editingVehicleId = null
                                            vehicleIsActive = 1
                                            vehiclePlateNo = ""
                                            vehicleMake = ""
                                            vehicleModel = ""
                                            vehicleYearModel = ""
                                            vehicleColor = ""
                                            vehicleEngineNo = ""
                                            vehicleChassisNo = ""
                                            vehicleOdoReading = ""
                                            vehicleNotes = ""
                                        }
                                        showNewVehicleForm = !showNewVehicleForm
                                    },
                                    onVehiclePlateChange = { vehiclePlateNo = it },
                                    onVehicleMakeChange = { vehicleMake = it },
                                    onVehicleModelChange = { vehicleModel = it },
                                    onVehicleYearChange = { vehicleYearModel = it },
                                    onVehicleColorChange = { vehicleColor = it },
                                    onVehicleEngineChange = { vehicleEngineNo = it },
                                    onVehicleChassisChange = { vehicleChassisNo = it },
                                    onVehicleOdoChange = { vehicleOdoReading = it },
                                    onVehicleNotesChange = { vehicleNotes = it },
                                    onEditVehicle = { vehicle ->
                                        editingVehicleId = vehicle.id
                                        selectedCustomerId = vehicle.customerId
                                        customerQuery = vehicle.customerName
                                        vehicleIsActive = vehicle.isActive
                                        vehiclePlateNo = vehicle.plateNo
                                        vehicleMake = vehicle.vehicleMake
                                        vehicleModel = vehicle.vehicleModel
                                        vehicleYearModel = vehicle.yearModel
                                        vehicleColor = vehicle.color
                                        vehicleEngineNo = vehicle.engineNo
                                        vehicleChassisNo = vehicle.chassisNo
                                        vehicleOdoReading = vehicle.odoReading.toString()
                                        vehicleNotes = vehicle.notes
                                        showNewVehicleForm = true
                                    },
                                    onSaveVehicle = {
                                        scope.launch {
                                            val customerId = selectedCustomerId ?: return@launch
                                            val savedId = if (editingVehicleId == null) {
                                                vm.createVehicle(customerId, vehiclePlateNo, vehicleMake, vehicleModel, vehicleYearModel, vehicleColor, vehicleEngineNo, vehicleChassisNo, vehicleOdoReading, vehicleNotes)
                                            } else {
                                                vm.updateVehicle(editingVehicleId!!, customerId, vehiclePlateNo, vehicleMake, vehicleModel, vehicleYearModel, vehicleColor, vehicleEngineNo, vehicleChassisNo, vehicleOdoReading, vehicleNotes, vehicleIsActive)
                                            }
                                            if (savedId != null) {
                                                showNewVehicleForm = false
                                                editingVehicleId = null
                                                vehicleIsActive = 1
                                                vehiclePlateNo = ""
                                                vehicleMake = ""
                                                vehicleModel = ""
                                                vehicleYearModel = ""
                                                vehicleColor = ""
                                                vehicleEngineNo = ""
                                                vehicleChassisNo = ""
                                                vehicleOdoReading = ""
                                                vehicleNotes = ""
                                            }
                                        }
                                    }
                                )

                                "workorders" -> WorkOrdersPanel(
                                    workOrders = vm.workOrders,
                                    customerQuery = customerQuery,
                                    customers = filteredCustomers,
                                    vehicleQuery = vehicleQuery,
                                    vehicles = filteredVehicles,
                                    selectedCustomerId = selectedCustomerId,
                                    selectedVehicleId = selectedVehicleId,
                                    editingWorkOrderId = editingWorkOrderId,
                                    complaint = workComplaint,
                                    diagnosis = workDiagnosis,
                                    priority = workPriority,
                                    notes = notes,
                                    loading = vm.loading,
                                    onCustomerQueryChange = {
                                        customerQuery = it
                                        if (it.isBlank()) selectedCustomerId = null
                                    },
                                    onPickCustomer = {
                                        selectedCustomerId = it.id
                                        customerQuery = it.name
                                        selectedVehicleId = null
                                        vehicleQuery = ""
                                    },
                                    onVehicleQueryChange = {
                                        vehicleQuery = it
                                        selectedVehicleId = null
                                    },
                                    onPickVehicle = {
                                        vehicleQuery = it.label
                                        vehicleMake = it.brand
                                        vehicleModel = it.model
                                        showNewVehicleForm = true
                                        selectedVehicleId = null
                                    },
                                    onEditWorkOrder = { workOrder ->
                                        editingWorkOrderId = workOrder.id
                                        selectedWorkOrderId = workOrder.id
                                        selectedCustomerId = workOrder.customerId
                                        customerQuery = workOrder.customerName
                                        selectedVehicleId = workOrder.vehicleId
                                        vehicleQuery = workOrder.vehicleLabel
                                        workComplaint = workOrder.complaint
                                        workDiagnosis = workOrder.diagnosis
                                        workPriority = workOrder.priority
                                        notes = workOrder.notes
                                    },
                                    onComplaintChange = { workComplaint = it },
                                    onDiagnosisChange = { workDiagnosis = it },
                                    onPriorityChange = { workPriority = it },
                                    onNotesChange = { notes = it },
                                    onSaveWorkOrder = {
                                        scope.launch {
                                            val customerId = selectedCustomerId ?: return@launch
                                            val vehicleId = ensureServiceVehicleId() ?: return@launch
                                            val savedId = if (editingWorkOrderId == null) {
                                                vm.createWorkOrder(customerId, vehicleId, workComplaint, workDiagnosis, workPriority, notes)
                                            } else {
                                                vm.updateWorkOrder(editingWorkOrderId!!, customerId, vehicleId, workComplaint, workDiagnosis, workPriority, notes)
                                            }
                                            if (savedId != null) {
                                                selectedWorkOrderId = savedId
                                                editingWorkOrderId = null
                                                workComplaint = ""
                                                workDiagnosis = ""
                                                workPriority = "normal"
                                                notes = ""
                                            }
                                        }
                                    }
                                )

                                "inventory" -> InventoryIntakePanel(
                                    items = vm.inventoryItems,
                                    barcode = barcode,
                                    draft = draft,
                                    lookup = vm.lastLookup,
                                    loading = vm.loading,
                                    showEditor = showItemEditor,
                                    editingInventoryId = editingInventoryId,
                                    showScanner = showInventoryScanner,
                                    cameraAllowed = cameraAllowed,
                                    onToggleScanner = {
                                        showItemEditor = true
                                        if (!cameraAllowed) cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        showInventoryScanner = !showInventoryScanner
                                    },
                                    onBarcodeDetected = {
                                        barcode = it
                                        draft = draft.copy(barcode = it)
                                        showItemEditor = true
                                        showInventoryScanner = false
                                        scope.launch {
                                            val lookup = vm.scanForInventoryResult(it)
                                            applyLookupToDraft(lookup, draft.qty)
                                        }
                                    },
                                    onBarcodeChange = {
                                        barcode = it
                                        draft = draft.copy(barcode = it)
                                        showItemEditor = true
                                    },
                                    onLookup = {
                                        showItemEditor = true
                                        scope.launch {
                                            val lookup = vm.scanForInventoryResult(barcode)
                                            applyLookupToDraft(lookup, draft.qty)
                                        }
                                    },
                                    onPrefill = { applyLookupToDraft(vm.lastLookup, draft.qty) },
                                    onDraftChange = { draft = it },
                                    onEditItem = { item ->
                                        editingInventoryId = item.id
                                        showItemEditor = true
                                        showInventoryScanner = false
                                        barcode = item.barcode
                                        draft = draft.copy(
                                            barcode = item.barcode,
                                            productName = item.sku,
                                            brand = "",
                                            categoryId = "",
                                            subCategoryId = item.subCategoryId,
                                            description = item.notes,
                                            buyRate = "",
                                            sellRate = item.sellRate.toString(),
                                            qty = item.qty.toString()
                                        )
                                        scope.launch {
                                            val lookup = vm.scanForInventoryResult(item.barcode)
                                            val template = lookup?.template
                                            draft = draft.copy(
                                                barcode = item.barcode,
                                                productName = template?.productName ?: item.sku,
                                                brand = template?.brand ?: "",
                                                categoryId = template?.categoryId ?: "",
                                                subCategoryId = template?.subCategoryId ?: item.subCategoryId,
                                                description = template?.description ?: item.notes,
                                                buyRate = template?.defaultBuyRate?.toString() ?: draft.buyRate,
                                                sellRate = item.sellRate.toString(),
                                                qty = item.qty.toString()
                                            )
                                        }
                                    },
                                    onSave = {
                                        scope.launch {
                                            if (vm.saveInventoryDraft(draft, editingInventoryId)) {
                                                editingInventoryId = null
                                                showItemEditor = false
                                                showInventoryScanner = false
                                                barcode = ""
                                                draft = BarcodeTemplateDraft()
                                                vm.clearTicketLookup()
                                            }
                                        }
                                    }
                                )

                                "receipts" -> ReceiptsPanel(
                                    receipts = vm.sales,
                                    detail = vm.selectedSaleDetail,
                                    loading = vm.loading,
                                    onSelectReceipt = { scope.launch { vm.loadSaleDetail(it.id) } },
                                    onBackToList = { vm.clearSaleDetail() }
                                )

                                else -> PosScanPanel(
                                    ticketMode = ticketMode,
                                    barcode = barcode,
                                    lookup = vm.lastLookup,
                                    loading = vm.loading,
                                    customerQuery = customerQuery,
                                    customers = filteredCustomers,
                                    selectedCustomerId = selectedCustomerId,
                                    showNewCustomerForm = showNewCustomerForm,
                                    newCustomerName = newCustomerName,
                                    newCustomerPhone = newCustomerPhone,
                                    newCustomerAddress = newCustomerAddress,
                                    vehicleQuery = vehicleQuery,
                                    vehicles = filteredVehicles,
                                    selectedVehicleId = selectedVehicleId,
                                    showNewVehicleForm = showNewVehicleForm,
                                    vehiclePlateNo = vehiclePlateNo,
                                    vehicleMake = vehicleMake,
                                    vehicleModel = vehicleModel,
                                    vehicleYearModel = vehicleYearModel,
                                    workComplaint = workComplaint,
                                    workDiagnosis = workDiagnosis,
                                    workPriority = workPriority,
                                    selectedWorkOrderId = selectedWorkOrderId,
                                    visibleWorkOrders = visibleWorkOrders,
                                    saleQty = saleQty,
                                    ticketItems = ticketItems,
                                    searchResults = filteredTicketInventory,
                                    paidAmount = paidAmount,
                                    payMethod = payMethod,
                                    notes = notes,
                                    currentPrice = currentPrice,
                                    totalPrice = totalPrice,
                                    selectedQty = selectedQty,
                                    qtyAvailable = qtyAvailable,
                                    dueAmount = dueAmount,
                                    showScanner = showPosScanner,
                                    cameraAllowed = cameraAllowed,
                                    onTicketModeChange = {
                                        ticketMode = it
                                        selectedWorkOrderId = null
                                    },
                                    onToggleScanner = {
                                        if (!cameraAllowed) cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        showPosScanner = !showPosScanner
                                    },
                                    onBarcodeDetected = {
                                        barcode = it
                                        showPosScanner = false
                                        scope.launch { vm.scanForPos(it) }
                                    },
                                    onBarcodeChange = { barcode = it },
                                    onLookup = {
                                        val firstLocal = filteredTicketInventory.firstOrNull()
                                        if (firstLocal != null) {
                                            vm.previewInventoryItem(firstLocal)
                                        } else {
                                            scope.launch { vm.scanForPos(barcode) }
                                        }
                                    },
                                    onPickSearchResult = { vm.previewInventoryItem(it) },
                                    onCustomerQueryChange = {
                                        customerQuery = it
                                        if (it.isBlank()) selectedCustomerId = null
                                    },
                                    onPickCustomer = {
                                        selectedCustomerId = it.id
                                        customerQuery = it.name
                                        showNewCustomerForm = false
                                        selectedVehicleId = null
                                        vehicleQuery = ""
                                    },
                                    onToggleNewCustomer = { showNewCustomerForm = !showNewCustomerForm },
                                    onNewCustomerNameChange = { newCustomerName = it },
                                    onNewCustomerPhoneChange = { newCustomerPhone = it },
                                    onNewCustomerAddressChange = { newCustomerAddress = it },
                                    onSaveNewCustomer = {
                                        scope.launch {
                                            val created = vm.createCustomer(newCustomerName, newCustomerPhone, newCustomerAddress)
                                            if (created != null) {
                                                selectedCustomerId = created.id
                                                customerQuery = created.name
                                                showNewCustomerForm = false
                                                newCustomerName = ""
                                                newCustomerPhone = ""
                                                newCustomerAddress = ""
                                            }
                                        }
                                    },
                                    onVehicleQueryChange = {
                                        vehicleQuery = it
                                        selectedVehicleId = null
                                    },
                                    onPickVehicle = {
                                        vehicleQuery = it.label
                                        vehicleMake = it.brand
                                        vehicleModel = it.model
                                        showNewVehicleForm = true
                                        selectedVehicleId = null
                                    },
                                    onToggleNewVehicle = { showNewVehicleForm = !showNewVehicleForm },
                                    onVehiclePlateChange = { vehiclePlateNo = it },
                                    onVehicleMakeChange = { vehicleMake = it },
                                    onVehicleModelChange = { vehicleModel = it },
                                    onVehicleYearChange = { vehicleYearModel = it },
                                    onWorkComplaintChange = { workComplaint = it },
                                    onWorkDiagnosisChange = { workDiagnosis = it },
                                    onWorkPriorityChange = { workPriority = it },
                                    onSelectWorkOrder = { workOrder ->
                                        selectedWorkOrderId = workOrder.id
                                        selectedCustomerId = workOrder.customerId
                                        customerQuery = workOrder.customerName
                                        selectedVehicleId = workOrder.vehicleId
                                        vehicleQuery = workOrder.vehicleLabel
                                        workComplaint = workOrder.complaint
                                        workDiagnosis = workOrder.diagnosis
                                        workPriority = workOrder.priority
                                        notes = workOrder.notes
                                    },
                                    onCreateVehicle = {
                                        scope.launch {
                                            val customerId = selectedCustomerId ?: return@launch
                                            val createdId = vm.createVehicle(customerId, vehiclePlateNo, vehicleMake, vehicleModel, vehicleYearModel, vehicleColor, vehicleEngineNo, vehicleChassisNo, vehicleOdoReading, vehicleNotes)
                                            if (createdId != null) {
                                                val createdVehicle = vm.vehicles.firstOrNull { it.id == createdId }
                                                selectedVehicleId = createdId
                                                vehicleQuery = createdVehicle?.label ?: vehiclePlateNo
                                                showNewVehicleForm = false
                                                vehiclePlateNo = ""
                                                vehicleMake = ""
                                                vehicleModel = ""
                                                vehicleYearModel = ""
                                                vehicleColor = ""
                                                vehicleEngineNo = ""
                                                vehicleChassisNo = ""
                                                vehicleOdoReading = ""
                                                vehicleNotes = ""
                                            }
                                        }
                                    },
                                    onCreateWorkOrder = {
                                        scope.launch {
                                            val customerId = selectedCustomerId ?: return@launch
                                            val vehicleId = ensureServiceVehicleId() ?: return@launch
                                            val createdId = vm.createWorkOrder(customerId, vehicleId, workComplaint, workDiagnosis, workPriority, notes)
                                            if (createdId != null) selectedWorkOrderId = createdId
                                        }
                                    },
                                    onSaleQtyChange = { saleQty = it },
                                    onAddCurrentItem = addSelectedItemToTicket,
                                    onRemoveTicketItem = { stockId -> ticketItems = ticketItems.filterNot { it.stockId == stockId } },
                                    onPaidAmountChange = { paidAmount = it },
                                    onPayMethodChange = { payMethod = it },
                                    onNotesChange = { notes = it },
                                    onHold = { showHoldConfirm = true },
                                    onCompleteSale = { showCompleteConfirm = true },
                                    onAddScannedItemToWorkOrder = {
                                        addSelectedItemToTicket()
                                    }
                                )
                                }
                            }
                        }
                    }
                    if (activeNav == "pos" && showPosScanner) {
                        FullScreenScannerOverlay(
                            cameraAllowed = cameraAllowed,
                            onClose = { showPosScanner = false },
                            onBarcodeDetected = {
                                barcode = it
                                showPosScanner = false
                                scope.launch { vm.scanForPos(it) }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginScreen(
    email: String,
    password: String,
    loading: Boolean,
    message: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(NavyDark, Navy)))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier
                        .size(126.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("GTM\nPH", color = Color.White, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Text("Motor Shop & Repair POS", style = MaterialTheme.typography.headlineSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("Sign in to continue to your sales floor", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                OutlinedTextField(value = email, onValueChange = onEmailChange, modifier = Modifier.fillMaxWidth(), label = { Text("Email") }, shape = RoundedCornerShape(14.dp))
                OutlinedTextField(value = password, onValueChange = onPasswordChange, modifier = Modifier.fillMaxWidth(), label = { Text("Password") }, shape = RoundedCornerShape(14.dp), visualTransformation = PasswordVisualTransformation())
                Button(
                    onClick = onLogin,
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) {
                    Text(if (loading) "Signing in..." else "Sign In", fontWeight = FontWeight.Bold)
                }
                if (message.isNotBlank()) {
                    Surface(color = if (message.contains("Logged in")) BlueSoft else Color(0xFFFDECEC), shape = RoundedCornerShape(12.dp)) {
                        Text(text = message, modifier = Modifier.padding(12.dp), color = if (message.contains("Logged in")) Success else Danger)
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerPanel(
    user: MobileUser,
    items: List<NavItem>,
    activeKey: String,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.linearGradient(listOf(NavyDark, Navy)))
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(user.fullName, color = Color.White, style = MaterialTheme.typography.headlineSmall)
                Text("POS 1", color = Color.White.copy(alpha = 0.9f))
                Text("Grand Tips MotoPH", color = Color.White.copy(alpha = 0.9f))
            }
        }
        items.forEach { item ->
            val active = item.key == activeKey
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (active) BlueSoft else Color.White)
                    .clickable { onSelect(item.key) }
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(item.icon, contentDescription = item.label, tint = if (active) Blue else TextMuted)
                Spacer(modifier = Modifier.width(16.dp))
                Text(item.label, color = if (active) Blue else TextPrimary, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text("v1.0.0", modifier = Modifier.padding(20.dp), color = TextMuted)
    }
}

@Composable
private fun MobileHeaderCard(
    user: MobileUser,
    dashboard: List<MobileDashboardTemplate>
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp),
        colors = CardDefaults.cardColors(containerColor = Navy),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(user.fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(user.fullName, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(user.role.replaceFirstChar { it.uppercase() }, color = Color.White.copy(alpha = 0.88f))
                }
                Surface(shape = RoundedCornerShape(30.dp), color = Color.White.copy(alpha = 0.18f)) {
                    Text("${dashboard.size} views", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = Color.White)
                }
            }
            Text("Sales, receipts, items, and customer flow tuned for quick cashier use.", color = Color.White.copy(alpha = 0.92f))
        }
    }
}

@Composable
private fun HomeDashboardPanel(
    sales: List<MobileSaleSummary>,
    inventoryCount: Int,
    customerCount: Int,
    onOpenPos: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenReceipts: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        DashboardActionTile("Sales", "Open active ticket and start scanning parts", Navy, onOpenPos)
        DashboardActionTile("Receipts", "Review closed sales and payment history", Blue, onOpenReceipts)
        DashboardActionTile("Items", "Check stock, prices, and add inventory by barcode", Color(0xFF0E4F8A), onOpenInventory)
        StatStrip(
            stats = listOf(
                "Receipts" to sales.size.toString(),
                "Items" to inventoryCount.toString(),
                "Customers" to customerCount.toString(),
                "Status" to "Live"
            )
        )
    }
}

@Composable
private fun AboutPanel(
    user: MobileUser,
    cards: List<MobileDashboardTemplate>
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionCard("About", "Role access and mobile workspace details.") {
            StatStrip(
                stats = listOf(
                    "Role" to user.role.replaceFirstChar { it.uppercase() },
                    "Views" to cards.size.toString(),
                    "Platform" to "Android",
                    "Source" to "Apps Script"
                )
            )
        }
        cards.forEach { card ->
            Surface(shape = RoundedCornerShape(20.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(card.title, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text(card.description, color = TextMuted)
                }
            }
        }
    }
}

@Composable
private fun PosScanPanel(
    ticketMode: String,
    barcode: String,
    lookup: MobileInventoryLookup?,
    loading: Boolean,
    customerQuery: String,
    customers: List<MobileCustomerSummary>,
    selectedCustomerId: Int?,
    showNewCustomerForm: Boolean,
    newCustomerName: String,
    newCustomerPhone: String,
    newCustomerAddress: String,
    vehicleQuery: String,
    vehicles: List<MobileVehicleType>,
    selectedVehicleId: Int?,
    showNewVehicleForm: Boolean,
    vehiclePlateNo: String,
    vehicleMake: String,
    vehicleModel: String,
    vehicleYearModel: String,
    workComplaint: String,
    workDiagnosis: String,
    workPriority: String,
    selectedWorkOrderId: Int?,
    visibleWorkOrders: List<MobileWorkOrderSummary>,
    saleQty: String,
    ticketItems: List<TicketCartItem>,
    searchResults: List<InventoryStock>,
    paidAmount: String,
    payMethod: String,
    notes: String,
    currentPrice: Double,
    totalPrice: Double,
    selectedQty: Int,
    qtyAvailable: Int,
    dueAmount: Double,
    showScanner: Boolean,
    cameraAllowed: Boolean,
    onTicketModeChange: (String) -> Unit,
    onToggleScanner: () -> Unit,
    onBarcodeDetected: (String) -> Unit,
    onBarcodeChange: (String) -> Unit,
    onLookup: () -> Unit,
    onPickSearchResult: (InventoryStock) -> Unit,
    onCustomerQueryChange: (String) -> Unit,
    onPickCustomer: (MobileCustomerSummary) -> Unit,
    onToggleNewCustomer: () -> Unit,
    onNewCustomerNameChange: (String) -> Unit,
    onNewCustomerPhoneChange: (String) -> Unit,
    onNewCustomerAddressChange: (String) -> Unit,
    onSaveNewCustomer: () -> Unit,
    onVehicleQueryChange: (String) -> Unit,
    onPickVehicle: (MobileVehicleType) -> Unit,
    onToggleNewVehicle: () -> Unit,
    onVehiclePlateChange: (String) -> Unit,
    onVehicleMakeChange: (String) -> Unit,
    onVehicleModelChange: (String) -> Unit,
    onVehicleYearChange: (String) -> Unit,
    onWorkComplaintChange: (String) -> Unit,
    onWorkDiagnosisChange: (String) -> Unit,
    onWorkPriorityChange: (String) -> Unit,
    onSelectWorkOrder: (MobileWorkOrderSummary) -> Unit,
    onCreateVehicle: () -> Unit,
    onCreateWorkOrder: () -> Unit,
    onSaleQtyChange: (String) -> Unit,
    onAddCurrentItem: () -> Unit,
    onRemoveTicketItem: (Int) -> Unit,
    onPaidAmountChange: (String) -> Unit,
    onPayMethodChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onHold: () -> Unit,
    onCompleteSale: () -> Unit,
    onAddScannedItemToWorkOrder: () -> Unit
) {
    val selected = lookup?.inventory?.firstOrNull()
    var showPaidAmountPad by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 4.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(shape = RoundedCornerShape(18.dp), color = BlueSoft) {
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CHARGE", color = Navy, style = MaterialTheme.typography.titleMedium)
                            Text(formatMoney(ticketItems.sumOf { it.lineTotal }), color = Navy, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        SummaryChip("Mode", if (ticketMode == "sale") "Sale" else "Service")
                        SummaryChip("Due", formatMoney(dueAmount), if (dueAmount > 0.0) Danger else Success)
                    }
                }
            }

            SectionCard("Ticket Type", "Choose whether this ticket is for retail sale or service work.") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    PaymentChip("Sale", ticketMode == "sale") { onTicketModeChange("sale") }
                    PaymentChip("Service", ticketMode == "service") { onTicketModeChange("service") }
                }
            }

            SectionCard("Add Customer To Ticket", "Select an existing customer from Google Sheets or add a new one.") {
                val showCustomerMatches = customerQuery.isNotBlank() && customerQuery.trim().lowercase() != "walk-in customer"
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Search, contentDescription = "Search", tint = TextMuted)
                    Text("Existing customers", color = TextMuted)
                }
                Spacer(modifier = Modifier.height(8.dp))
                DraftField("Search customer dropdown", customerQuery, onCustomerQueryChange)
                if (showCustomerMatches) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(16.dp), color = BlueSoft, border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
                        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
                            if (customers.isEmpty()) {
                                Text("No matching customer found.", color = TextMuted, modifier = Modifier.padding(vertical = 10.dp))
                            } else {
                                customers.forEach { customer ->
                                    SimpleListTile(
                                        title = customer.name,
                                        subtitle = listOf(customer.phone, customer.address).filter { it.isNotBlank() }.joinToString("  "),
                                        trailing = "Select",
                                        onClick = { onPickCustomer(customer) }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (selectedCustomerId != null) {
                    Surface(shape = RoundedCornerShape(14.dp), color = BlueSoft) {
                        Text("Selected customer is linked to service/work-order flow.", modifier = Modifier.padding(12.dp), color = Navy)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Button(
                    onClick = onToggleNewCustomer,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue)
                ) {
                    Text(if (showNewCustomerForm) "Cancel New Customer" else "New Customer")
                }
                if (showNewCustomerForm) {
                    Spacer(modifier = Modifier.height(12.dp))
                    DraftField("Customer Name", newCustomerName, onNewCustomerNameChange)
                    Spacer(modifier = Modifier.height(8.dp))
                    DraftField("Phone", newCustomerPhone, onNewCustomerPhoneChange)
                    Spacer(modifier = Modifier.height(8.dp))
                    DraftField("Address", newCustomerAddress, onNewCustomerAddressChange)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onSaveNewCustomer,
                        enabled = !loading && newCustomerName.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy)
                    ) {
                        Text(if (loading) "Saving Customer..." else "Save Customer")
                    }
                }
            }

            if (ticketMode == "service") {
                SectionCard("Vehicle & Work Order", "Pick a vehicle, create one if needed, then create or attach a work order.") {
                    DraftField("Search vehicle type", vehicleQuery, onVehicleQueryChange)
                    if (vehicleQuery.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(shape = RoundedCornerShape(16.dp), color = BlueSoft, border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
                            Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
                                if (vehicles.isEmpty()) {
                                    Text("No matching vehicle type found.", color = TextMuted, modifier = Modifier.padding(vertical = 10.dp))
                                } else {
                                    vehicles.forEach { vehicle ->
                                        SimpleListTile(
                                            title = vehicle.label,
                                            subtitle = vehicle.category,
                                            trailing = "Select",
                                            onClick = { onPickVehicle(vehicle) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onToggleNewVehicle,
                        enabled = selectedCustomerId != null,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue)
                    ) {
                        Text(if (showNewVehicleForm) "Cancel New Vehicle" else "New Vehicle")
                    }
                    if (showNewVehicleForm) {
                        Spacer(modifier = Modifier.height(12.dp))
                        DraftField("Plate Number", vehiclePlateNo, onVehiclePlateChange)
                        Spacer(modifier = Modifier.height(8.dp))
                        DraftField("Make", vehicleMake, onVehicleMakeChange)
                        Spacer(modifier = Modifier.height(8.dp))
                        DraftField("Model", vehicleModel, onVehicleModelChange)
                        Spacer(modifier = Modifier.height(8.dp))
                        DraftField("Year Model", vehicleYearModel, onVehicleYearChange)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onCreateVehicle,
                            enabled = !loading && selectedCustomerId != null && vehiclePlateNo.isNotBlank() && vehicleMake.isNotBlank() && vehicleModel.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Navy)
                        ) {
                            Text(if (loading) "Saving Vehicle..." else "Save Vehicle")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    DraftField("Complaint", workComplaint, onWorkComplaintChange)
                    Spacer(modifier = Modifier.height(8.dp))
                    DraftField("Diagnosis", workDiagnosis, onWorkDiagnosisChange)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Priority", color = TextMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        PaymentChip("Low", workPriority == "low") { onWorkPriorityChange("low") }
                        PaymentChip("Normal", workPriority == "normal") { onWorkPriorityChange("normal") }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        PaymentChip("High", workPriority == "high") { onWorkPriorityChange("high") }
                        PaymentChip("Urgent", workPriority == "urgent") { onWorkPriorityChange("urgent") }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onCreateWorkOrder,
                        enabled = !loading && selectedCustomerId != null && (selectedVehicleId != null || (vehicleMake.isNotBlank() && vehicleModel.isNotBlank())) && workComplaint.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy)
                    ) {
                        Text(if (selectedWorkOrderId == null) "Create Work Order" else "Create Another Work Order")
                    }
                    if (visibleWorkOrders.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Existing work orders", color = TextMuted)
                        visibleWorkOrders.forEach { workOrder ->
                            SimpleListTile(
                                title = workOrder.workOrderNo.ifBlank { "Work Order #${workOrder.id}" },
                                subtitle = "${workOrder.vehicleLabel} • ${workOrder.status}",
                                trailing = if (selectedWorkOrderId == workOrder.id) "Active" else "Use",
                                onClick = { onSelectWorkOrder(workOrder) }
                            )
                        }
                    }
                }
            }

            SectionCard("Scan Item", if (ticketMode == "sale") "Use barcode scan or SKU search to add multiple sale items." else "Use barcode, job code, or description search to add multiple parts and services.") {
                ActionHeaderRow(
                    title = "Barcode scanner",
                    buttonLabel = if (showScanner) "Close Scanner" else "Open Scanner",
                    icon = Icons.Filled.QrCodeScanner,
                    onClick = onToggleScanner
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = barcode,
                    onValueChange = onBarcodeChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (ticketMode == "sale") "Search SKU / Barcode" else "Search barcode / job code / description") },
                    shape = RoundedCornerShape(14.dp)
                )
                if (barcode.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(shape = RoundedCornerShape(16.dp), color = BlueSoft, border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
                        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
                            if (searchResults.isEmpty()) {
                                Text("No matching inventory item found.", color = TextMuted, modifier = Modifier.padding(vertical = 10.dp))
                            } else {
                                searchResults.forEach { result ->
                                    SimpleListTile(
                                        title = result.sku,
                                        subtitle = listOf(result.inventoryGroup, result.notes).filter { it.isNotBlank() }.joinToString(" • "),
                                        trailing = formatMoney(result.sellRate),
                                        onClick = { onPickSearchResult(result) }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onLookup,
                    enabled = !loading && barcode.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) {
                    Text(if (loading) "Checking stock..." else "Check Inventory & Price", fontWeight = FontWeight.Bold)
                }
            }

            SectionCard("Items", "Active ticket lines with quantity and pricing.") {
                TicketItemRow(
                    title = selected?.sku ?: "No scanned item yet",
                    subtitle = selected?.inventoryGroup ?: "Scan barcode to start",
                    price = formatMoney(currentPrice)
                )
                Spacer(modifier = Modifier.height(10.dp))
                DraftField("Quantity", saleQty, onSaleQtyChange)
                Spacer(modifier = Modifier.height(8.dp))
                StatStrip(
                    stats = listOf(
                        "Stock" to qtyAvailable.toString(),
                        "Qty" to selectedQty.toString(),
                        "Unit" to formatMoney(currentPrice)
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(shape = RoundedCornerShape(16.dp), color = BlueSoft) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", color = Navy, fontWeight = FontWeight.SemiBold)
                        Text(formatMoney(totalPrice), color = Navy, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onAddCurrentItem,
                    enabled = selected != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Text(if (ticketMode == "sale") "Add Item To Ticket" else "Add Item To Service Ticket")
                }
                if (ticketMode == "service") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onAddScannedItemToWorkOrder,
                        enabled = selected != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue)
                    ) {
                        Text("Quick Add Current Item")
                    }
                }
                if (ticketItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Ticket Cart", color = TextMuted)
                    ticketItems.forEach { item ->
                        SimpleListTile(
                            title = "${item.title} x${item.qty}",
                            subtitle = item.subtitle,
                            trailing = "Remove",
                            onClick = { onRemoveTicketItem(item.stockId) }
                        )
                    }
                }
            }

            SectionCard("Payment", "Fast payment block for the cashier.") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    PaymentChip("Cash", payMethod == "Cash") { onPayMethodChange("Cash") }
                    PaymentChip("Bank", payMethod == "Bank") { onPayMethodChange("Bank") }
                    PaymentChip("Credit", payMethod == "Credit") { onPayMethodChange("Credit") }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPaidAmountPad = !showPaidAmountPad }
                ) {
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        Text("Paid Amount", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (paidAmount.isBlank()) "Tap to enter amount" else paidAmount,
                            color = if (paidAmount.isBlank()) TextMuted else TextPrimary,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (paidAmount.isBlank()) FontWeight.Normal else FontWeight.SemiBold
                        )
                    }
                }
                if (showPaidAmountPad) {
                    Spacer(modifier = Modifier.height(10.dp))
                    NumberPad(
                        value = paidAmount,
                        onValueChange = onPaidAmountChange,
                        onDone = { showPaidAmountPad = false }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (dueAmount > 0.0) Color(0xFFFFF3E0) else BlueSoft,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (dueAmount > 0.0) Color(0xFFFFD08A) else Color(0xFFA7D7B5))
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (dueAmount > 0.0) "Balance Due" else "Fully Paid", color = if (dueAmount > 0.0) Danger else Success, fontWeight = FontWeight.Bold)
                        Text(formatMoney(dueAmount), color = if (dueAmount > 0.0) Danger else Success, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            SectionCard("Finish Ticket", "Save now or hold this sale for later.") {
                DraftField("Notes", notes, onNotesChange)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onHold, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Warning)) {
                        Text(if (ticketMode == "sale") "Hold" else "Save WO")
                    }
                    Button(onClick = onCompleteSale, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Navy)) {
                        Text(if (ticketMode == "sale") "Complete Sale" else "Checkout Service")
                    }
                }
            }

            LookupSummaryCard(lookup = lookup, showTemplate = false)
        }
    }
}

@Composable
private fun NumberPad(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit
) {
    fun appendToken(current: String, token: String): String {
        return when (token) {
            "." -> if (current.contains(".")) current else if (current.isBlank()) "0." else current + "."
            else -> if (current == "0") token else current + token
        }
    }

    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "00")
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = BlueSoft,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { token ->
                        Button(
                            onClick = { onValueChange(appendToken(value, token)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Navy)
                        ) {
                            Text(token, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onValueChange("") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5E7EB), contentColor = TextPrimary)
                ) {
                    Text("Clear")
                }
                Button(
                    onClick = {
                        if (value.isNotEmpty()) onValueChange(value.dropLast(1))
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Warning)
                ) {
                    Text("Back")
                }
                Button(
                    onClick = onDone,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
private fun InventoryIntakePanel(
    items: List<InventoryStock>,
    barcode: String,
    draft: BarcodeTemplateDraft,
    lookup: MobileInventoryLookup?,
    loading: Boolean,
    showEditor: Boolean,
    editingInventoryId: Int?,
    showScanner: Boolean,
    cameraAllowed: Boolean,
    onToggleScanner: () -> Unit,
    onBarcodeDetected: (String) -> Unit,
    onBarcodeChange: (String) -> Unit,
    onLookup: () -> Unit,
    onPrefill: () -> Unit,
    onDraftChange: (BarcodeTemplateDraft) -> Unit,
    onEditItem: (InventoryStock) -> Unit,
    onSave: () -> Unit
) {
    var currentPage by remember(items) { mutableStateOf(0) }
    val pageSize = 10
    val totalPages = maxOf(1, ((items.size + pageSize - 1) / pageSize))
    val safePage = currentPage.coerceIn(0, totalPages - 1)
    val visibleItems = items.drop(safePage * pageSize).take(pageSize)
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionCard("Add Item", "Tap the barcode icon, scan, then continue with the create item form.") {
            ActionHeaderRow(
                title = "Scan barcode",
                buttonLabel = if (showScanner) "Hide Scanner" else "Open Scanner",
                icon = Icons.Filled.QrCodeScanner,
                onClick = onToggleScanner
            )
            if (showScanner) {
                Spacer(modifier = Modifier.height(10.dp))
                CameraPanel(cameraAllowed = cameraAllowed, onBarcodeDetected = onBarcodeDetected)
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = barcode,
                onValueChange = onBarcodeChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Barcode / SKU") },
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onLookup,
                    enabled = !loading && barcode.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue)
                ) { Text("Prefill") }
                Button(
                    onClick = onPrefill,
                    enabled = lookup?.template != null,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) { Text("Use Template") }
            }
        }

        if (showEditor || barcode.isNotBlank() || lookup?.template != null || editingInventoryId != null) {
            LookupSummaryCard(lookup = lookup, showTemplate = true)
            SectionCard(if (editingInventoryId == null) "Create Item" else "Edit Item", "The scanned barcode stays here while you finish the item fields.") {
                DraftField("Barcode", draft.barcode) { onDraftChange(draft.copy(barcode = it)) }
                DraftField("Product Name", draft.productName) { onDraftChange(draft.copy(productName = it)) }
                DraftField("Brand", draft.brand) { onDraftChange(draft.copy(brand = it)) }
                DraftField("Category ID", draft.categoryId) { onDraftChange(draft.copy(categoryId = it)) }
                DraftField("Sub Category ID", draft.subCategoryId) { onDraftChange(draft.copy(subCategoryId = it)) }
                DraftField("Description", draft.description) { onDraftChange(draft.copy(description = it)) }
                DraftField("Buy Rate", draft.buyRate) { onDraftChange(draft.copy(buyRate = it)) }
                DraftField("Sell Rate", draft.sellRate) { onDraftChange(draft.copy(sellRate = it)) }
                DraftField("Qty", draft.qty) { onDraftChange(draft.copy(qty = it)) }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onSave,
                    enabled = !loading && draft.barcode.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Text(if (loading) "Saving..." else if (editingInventoryId == null) "Add Inventory Item" else "Update Inventory Item", fontWeight = FontWeight.Bold)
                }
            }
        }

        SectionCard("All Items", "Live inventory list from your Apps Script POS. 10 items per page.") {
            visibleItems.forEach { item ->
                SimpleListTile(
                    title = item.sku,
                    subtitle = "${item.qty} in stock - ${item.inventoryGroup}",
                    trailing = "Edit",
                    onClick = { onEditItem(item) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { currentPage = (safePage - 1).coerceAtLeast(0) },
                    enabled = safePage > 0,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9CA3AF))
                ) { Text("Prev") }
                Text("Page ${safePage + 1} of $totalPages", color = TextMuted, fontWeight = FontWeight.Medium)
                Button(
                    onClick = { currentPage = (safePage + 1).coerceAtMost(totalPages - 1) },
                    enabled = safePage < totalPages - 1,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) { Text("Next") }
            }
        }
    }
}

@Composable
private fun VehicleRegistryPanel(
    vehicles: List<MobileVehicleSummary>,
    customerQuery: String,
    customers: List<MobileCustomerSummary>,
    selectedCustomerId: Int?,
    showNewVehicleForm: Boolean,
    editingVehicleId: Int?,
    vehicleIsActive: Int,
    vehiclePlateNo: String,
    vehicleMake: String,
    vehicleModel: String,
    vehicleYearModel: String,
    vehicleColor: String,
    vehicleEngineNo: String,
    vehicleChassisNo: String,
    vehicleOdoReading: String,
    vehicleNotes: String,
    loading: Boolean,
    onCustomerQueryChange: (String) -> Unit,
    onPickCustomer: (MobileCustomerSummary) -> Unit,
    onToggleVehicleForm: () -> Unit,
    onVehiclePlateChange: (String) -> Unit,
    onVehicleMakeChange: (String) -> Unit,
    onVehicleModelChange: (String) -> Unit,
    onVehicleYearChange: (String) -> Unit,
    onVehicleColorChange: (String) -> Unit,
    onVehicleEngineChange: (String) -> Unit,
    onVehicleChassisChange: (String) -> Unit,
    onVehicleOdoChange: (String) -> Unit,
    onVehicleNotesChange: (String) -> Unit,
    onEditVehicle: (MobileVehicleSummary) -> Unit,
    onSaveVehicle: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionCard("Vehicle Registry", "Register customer vehicles that can be attached to service tickets and work orders.") {
            DraftField("Search customer", customerQuery, onCustomerQueryChange)
            if (customerQuery.isNotBlank() && customerQuery.lowercase() != "walk-in customer") {
                Spacer(modifier = Modifier.height(8.dp))
                customers.forEach { customer ->
                    SimpleListTile(
                        title = customer.name,
                        subtitle = listOf(customer.phone, customer.address).filter { it.isNotBlank() }.joinToString("  "),
                        trailing = if (selectedCustomerId == customer.id) "Selected" else "Use",
                        onClick = { onPickCustomer(customer) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onToggleVehicleForm,
                enabled = selectedCustomerId != null,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue)
            ) {
                Text(if (showNewVehicleForm) "Cancel Vehicle Form" else if (editingVehicleId == null) "Add Vehicle" else "Edit Vehicle")
            }
            if (showNewVehicleForm) {
                Spacer(modifier = Modifier.height(12.dp))
                DraftField("Plate Number", vehiclePlateNo, onVehiclePlateChange)
                DraftField("Make", vehicleMake, onVehicleMakeChange)
                DraftField("Model", vehicleModel, onVehicleModelChange)
                DraftField("Year Model", vehicleYearModel, onVehicleYearChange)
                DraftField("Color", vehicleColor, onVehicleColorChange)
                DraftField("Engine No", vehicleEngineNo, onVehicleEngineChange)
                DraftField("Chassis No", vehicleChassisNo, onVehicleChassisChange)
                DraftField("Odo Reading", vehicleOdoReading, onVehicleOdoChange)
                DraftField("Notes", vehicleNotes, onVehicleNotesChange)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Status: ${if (vehicleIsActive == 1) "Active" else "Inactive"}", color = TextMuted)
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onSaveVehicle,
                    enabled = !loading && selectedCustomerId != null && vehiclePlateNo.isNotBlank() && vehicleMake.isNotBlank() && vehicleModel.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) {
                    Text(if (loading) "Saving Vehicle..." else if (editingVehicleId == null) "Save Vehicle" else "Update Vehicle")
                }
            }
        }
        SectionCard("All Vehicles", "Existing vehicle records from Google Sheets.") {
            vehicles.take(20).forEach { vehicle ->
                SimpleListTile(
                    title = vehicle.label,
                    subtitle = "${vehicle.customerName} • ${vehicle.yearModel.ifBlank { "No year" }}",
                    trailing = "Edit",
                    onClick = { onEditVehicle(vehicle) }
                )
            }
        }
    }
}

@Composable
private fun WorkOrdersPanel(
    workOrders: List<MobileWorkOrderSummary>,
    customerQuery: String,
    customers: List<MobileCustomerSummary>,
    vehicleQuery: String,
    vehicles: List<MobileVehicleType>,
    selectedCustomerId: Int?,
    selectedVehicleId: Int?,
    editingWorkOrderId: Int?,
    complaint: String,
    diagnosis: String,
    priority: String,
    notes: String,
    loading: Boolean,
    onCustomerQueryChange: (String) -> Unit,
    onPickCustomer: (MobileCustomerSummary) -> Unit,
    onVehicleQueryChange: (String) -> Unit,
    onPickVehicle: (MobileVehicleType) -> Unit,
    onEditWorkOrder: (MobileWorkOrderSummary) -> Unit,
    onComplaintChange: (String) -> Unit,
    onDiagnosisChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSaveWorkOrder: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionCard("Create Work Order", "Start a repair/service job from the phone and sync it to the shared sheet.") {
            DraftField("Search customer", customerQuery, onCustomerQueryChange)
            if (customerQuery.isNotBlank() && customerQuery.lowercase() != "walk-in customer") {
                Spacer(modifier = Modifier.height(8.dp))
                customers.forEach { customer ->
                    SimpleListTile(
                        title = customer.name,
                        subtitle = listOf(customer.phone, customer.address).filter { it.isNotBlank() }.joinToString("  "),
                        trailing = if (selectedCustomerId == customer.id) "Selected" else "Use",
                        onClick = { onPickCustomer(customer) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            DraftField("Search vehicle type", vehicleQuery, onVehicleQueryChange)
            if (vehicleQuery.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                vehicles.forEach { vehicle ->
                    SimpleListTile(
                        title = vehicle.label,
                        subtitle = vehicle.category,
                        trailing = if (vehicleQuery == vehicle.label) "Selected" else "Use",
                        onClick = { onPickVehicle(vehicle) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            DraftField("Complaint", complaint, onComplaintChange)
            DraftField("Diagnosis", diagnosis, onDiagnosisChange)
            Text("Priority", color = TextMuted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                PaymentChip("Low", priority == "low") { onPriorityChange("low") }
                PaymentChip("Normal", priority == "normal") { onPriorityChange("normal") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                PaymentChip("High", priority == "high") { onPriorityChange("high") }
                PaymentChip("Urgent", priority == "urgent") { onPriorityChange("urgent") }
            }
            DraftField("Notes", notes, onNotesChange)
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onSaveWorkOrder,
                enabled = !loading && selectedCustomerId != null && vehicleQuery.isNotBlank() && complaint.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy)
            ) {
                Text(if (loading) "Saving Work Order..." else if (editingWorkOrderId == null) "Save Work Order" else "Update Work Order")
            }
        }
        SectionCard("Open Work Orders", "Pending and active repair jobs.") {
            workOrders.take(20).forEach { workOrder ->
                SimpleListTile(
                    title = workOrder.workOrderNo.ifBlank { "Work Order #${workOrder.id}" },
                    subtitle = "${workOrder.customerName} • ${workOrder.vehicleLabel} • ${workOrder.status}",
                    trailing = "Edit",
                    onClick = { onEditWorkOrder(workOrder) }
                )
            }
        }
    }
}

@Composable
private fun ReceiptsPanel(
    receipts: List<MobileSaleSummary>,
    detail: MobileSaleDetail?,
    loading: Boolean,
    onSelectReceipt: (MobileSaleSummary) -> Unit,
    onBackToList: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (detail == null) {
            SectionCard("Receipts", "Live sales history from the shared Google Sheets database.") {
                receipts.take(20).forEach { receipt ->
                    ReceiptListTile(receipt = receipt, onClick = { onSelectReceipt(receipt) })
                }
            }
        } else {
            SectionCard(detail.invoiceNo, "Receipt detail") {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.clickable { onBackToList() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Navy)
                    Text("Back to receipts", color = Navy, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(18.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(formatMoney(detail.grandTotal), color = TextPrimary, style = MaterialTheme.typography.headlineLarge)
                        Text("Customer: ${detail.customerName}", color = TextPrimary)
                        Text("Cashier: ${detail.cashierName}", color = TextPrimary)
                        Text("Payment: ${detail.paymentMethod}", color = TextPrimary)
                        Text("Date: ${detail.saleDate}", color = TextMuted)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                detail.items.forEach { item ->
                    SimpleListTile(
                        title = item.serial.ifBlank { "Item" },
                        subtitle = "${formatMoney(item.rate)} x ${if (item.cft > 0) item.cft else 1.0}",
                        trailing = formatMoney(item.total)
                    )
                }
                if (loading) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Loading...", color = TextMuted)
                }
            }
        }
    }
}

@Composable
private fun CameraPanel(
    cameraAllowed: Boolean,
    onBarcodeDetected: (String) -> Unit
) {
    Surface(shape = RoundedCornerShape(20.dp), color = BlueSoft, border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Mobile Camera Scanner", color = TextPrimary, fontWeight = FontWeight.Bold)
            if (!cameraAllowed) {
                Text("Allow camera permission to use barcode scan on this device.", color = TextMuted)
            } else {
                BarcodeScannerView(
                    modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(18.dp)),
                    onBarcodeDetected = onBarcodeDetected
                )
            }
        }
    }
}

@Composable
private fun FullScreenScannerOverlay(
    cameraAllowed: Boolean,
    onClose: () -> Unit,
    onBarcodeDetected: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NavyDark.copy(alpha = 0.98f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Scan Barcode", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Button(
                    onClick = onClose,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Navy)
                ) {
                    Text("Close")
                }
            }
            Text(
                "Point the camera at the barcode. After a successful scan, this screen will close automatically.",
                color = Color.White.copy(alpha = 0.82f)
            )
            Surface(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.06f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
            ) {
                if (!cameraAllowed) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Camera permission is required to scan.", color = Color.White)
                    }
                } else {
                    BarcodeScannerView(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp)),
                        onBarcodeDetected = onBarcodeDetected
                    )
                }
            }
        }
    }
}

@Composable
private fun LookupSummaryCard(
    lookup: MobileInventoryLookup?,
    showTemplate: Boolean
) {
    if (lookup == null) return
    SectionCard("Lookup Result", "Live data returned from the shared Google Sheets database.") {
        StatStrip(
            stats = listOf(
                "Barcode" to lookup.barcode,
                "Available" to lookup.availableCount.toString(),
                "Matches" to lookup.inventory.size.toString()
            )
        )
        if (showTemplate && lookup.template != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFF4F8FD), border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(lookup.template.productName.ifBlank { "Unnamed template" }, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("Brand: ${lookup.template.brand.ifBlank { "-" }}")
                    Text("Description: ${lookup.template.description.ifBlank { "-" }}")
                    Text("Buy/Sell: ${lookup.template.defaultBuyRate} / ${lookup.template.defaultSellRate}")
                }
            }
        }
        if (lookup.inventory.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            lookup.inventory.take(3).forEach { item ->
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(item.sku, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text("Inventory Group: ${item.inventoryGroup}")
                        Text("Qty Available: ${item.qty}")
                        Text("Sell Price: ${formatMoney(item.sellRate)}", color = Success, fontWeight = FontWeight.Bold)
                        Text("Retail Value: ${formatMoney(item.sellPrice)}", color = TextMuted)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun DashboardActionTile(
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(tint.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                Text(title.take(1), color = tint, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = TextMuted)
            }
            Text("Open", color = tint, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActionHeaderRow(
    title: String,
    buttonLabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = BlueSoft,
            modifier = Modifier.clickable { onClick() }
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = buttonLabel, tint = Navy)
                Text(buttonLabel, color = Navy, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun RowScope.SummaryChip(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    Surface(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label, color = TextMuted, style = MaterialTheme.typography.bodySmall)
            Text(value, color = valueColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ReceiptListTile(
    receipt: MobileSaleSummary,
    onClick: () -> Unit
) {
    SimpleListTile(
        title = formatMoney(receipt.grandTotal),
        subtitle = receipt.saleDate,
        trailing = receipt.invoiceNo,
        onClick = onClick
    )
}

@Composable
private fun RowScope.PaymentChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.weight(1f).clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (active) Navy else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (active) Navy else Border)
    ) {
        Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(label, color = if (active) Color.White else TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
private fun StatStrip(stats: List<Pair<String, String>>) {
    val rows = stats.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        rows.forEach { rowStats ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                rowStats.forEach { (label, value) ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = BlueSoft,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(value, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
                            Text(label, color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                if (rowStats.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DraftField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(), label = { Text(label) }, shape = RoundedCornerShape(14.dp))
}

@Composable
private fun SimpleListTile(
    title: String,
    subtitle: String,
    trailing: String,
    onClick: (() -> Unit)? = null
) {
    val modifier = if (onClick != null) Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { onClick() } else Modifier.fillMaxWidth()
    Row(modifier = modifier.padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextMuted)
        }
        Text(trailing, color = Navy, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TicketItemRow(
    title: String,
    subtitle: String,
    price: String
) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(54.dp).clip(RoundedCornerShape(12.dp)).background(BlueSoft))
            Spacer(modifier = Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Medium)
                Text(subtitle, color = TextMuted)
            }
            Text(price, color = TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SaleConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = { Text(message, color = TextMuted) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Navy)
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9CA3AF))
            ) {
                Text("Cancel")
            }
        }
    )
}

private fun screenTitle(key: String): String {
    return when (key) {
        "pos" -> "Ticket"
        "inventory" -> "All Items"
        "workorders" -> "Work Orders"
        "vehicles" -> "Vehicle Registry"
        "receipts" -> "Receipts"
        "about" -> "About"
        else -> "Sales"
    }
}

private fun formatMoney(amount: Double): String {
    return "PHP " + String.format(Locale.getDefault(), "%,.2f", amount)
}
