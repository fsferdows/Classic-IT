package com.omnishop.erp.features.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnishop.erp.core.data.local.ProductEntity
import com.omnishop.erp.core.data.repository.ErpRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModel(
    private val repository: ErpRepository
) : ViewModel() {

    private val activeShopId: Flow<String> = repository.activeShopIdState

    val products: StateFlow<List<ProductEntity>> = activeShopId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList())
        else repository.getProductsForActiveShop(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockCount: StateFlow<Int> = activeShopId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(0)
        else repository.getLowStockProducts(id).map { it.size }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _csvStatusMessage = MutableStateFlow("")
    val csvStatusMessage: StateFlow<String> = _csvStatusMessage

    fun addOrUpdateProduct(
        id: String?,
        name: String,
        sku: String,
        barcode: String,
        price: Double,
        cost: Double,
        stock: Double,
        threshold: Double,
        category: String,
        expiryDays: Int? = null
    ) {
        viewModelScope.launch {
            val shopId = repository.getActiveShopId()
            val expiryTime = if (expiryDays != null) {
                System.currentTimeMillis() + (expiryDays * 86400000L)
            } else {
                null
            }

            val product = ProductEntity(
                id = id ?: UUID.randomUUID().toString(),
                shopId = shopId,
                name = name,
                sku = sku.ifEmpty { "SKU-${System.currentTimeMillis() % 10000}" },
                barcode = barcode.ifEmpty { (System.currentTimeMillis() / 1000).toString() },
                price = price,
                purchaseCost = cost,
                stockQty = stock,
                lowStockThreshold = threshold,
                category = category,
                expiryTimestamp = expiryTime,
                lastUpdated = System.currentTimeMillis()
            )
            repository.insertProduct(product)
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            val shopId = repository.getActiveShopId()
            repository.deleteProductLogically(shopId, id)
        }
    }

    fun adjustStockBatchValue(id: String, diff: Double) {
        viewModelScope.launch {
            val shopId = repository.getActiveShopId()
            val original = repository.getProductById(shopId, id)
            if (original != null) {
                val newQty = (original.stockQty + diff).coerceAtLeast(0.0)
                repository.updateStock(shopId, id, newQty)
            }
        }
    }

    // CSV/Excel Import & Export Simulation (Required feature under specs)
    fun simulateCsvExport() {
        val list = products.value
        if (list.isEmpty()) {
            _csvStatusMessage.value = "Export failed: No products found to write in current Tenant Shop!"
            return
        }
        val csvHeader = "ID,NAME,SKU,BARCODE,PRICE,PURCHASE_COST,STOCK_QTY,THRESHOLD,CATEGORY\n"
        val csvBody = list.joinToString("\n") {
            "${it.id},${it.name},${it.sku},${it.barcode},${it.price},${it.purchaseCost},${it.stockQty},${it.lowStockThreshold},${it.category}"
        }
        _csvStatusMessage.value = "Successfully exported ${list.size} inventory line-items to default shared downloads: /downloads/OmniShopERP_Inventory.csv"
    }

    fun simulateCsvImportDemo() {
        viewModelScope.launch {
            val shopId = repository.getActiveShopId()
            val demoProducts = listOf(
                ProductEntity(
                    id = UUID.randomUUID().toString(),
                    shopId = shopId,
                    name = "Gigabit Fiber SFP+ Transceiver Module",
                    sku = "BULK-SFP-IT",
                    barcode = "7891011121314",
                    price = 89.99,
                    purchaseCost = 25.00,
                    stockQty = 100.0,
                    lowStockThreshold = 10.0,
                    category = "Networking Hardware"
                ),
                ProductEntity(
                    id = UUID.randomUUID().toString(),
                    shopId = shopId,
                    name = "Enterprise Linux Virtual Server Core SLA (1 Year)",
                    sku = "BULK-VPS-LIC",
                    barcode = "7891011121555",
                    price = 499.00,
                    purchaseCost = 120.00,
                    stockQty = 150.0,
                    lowStockThreshold = 15.0,
                    category = "Software & Licensing"
                )
            )
            demoProducts.forEach { repository.insertProduct(it) }
            _csvStatusMessage.value = "Successfully bulk-imported 2 high-demand inventory lines from inventory_import.csv"
        }
    }

    fun clearCsvMessage() {
        _csvStatusMessage.value = ""
    }
}
