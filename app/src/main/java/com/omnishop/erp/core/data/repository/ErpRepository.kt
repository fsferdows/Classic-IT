package com.omnishop.erp.core.data.repository

import com.omnishop.erp.core.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID

class ErpRepository(
    private val appDatabase: AppDatabase,
    private val prefs: PreferencesManager
) {
    // Current Active Shop ID Observable
    val activeShopIdState: Flow<String> = prefs.selectedShopIdState

    suspend fun getActiveShopId(): String {
        var id = prefs.getSelectedShopId()
        if (id.isEmpty()) {
            val firstShop = appDatabase.shopDao().getAllShops().map { it.firstOrNull() }.firstOrNull()
            if (firstShop != null) {
                prefs.setSelectedShopId(firstShop.id)
                id = firstShop.id
            }
        }
        return id
    }

    // ----------------------------------------------------
    // SHOPS (TENANTS)
    // ----------------------------------------------------
    fun getAllShops(): Flow<List<ShopEntity>> = appDatabase.shopDao().getAllShops()

    suspend fun insertShop(shop: ShopEntity) {
        appDatabase.shopDao().insertShop(shop)
        if (prefs.getSelectedShopId().isEmpty()) {
            prefs.setSelectedShopId(shop.id)
        }
    }

    suspend fun switchShop(shopId: String) {
        prefs.setSelectedShopId(shopId)
    }

    // ----------------------------------------------------
    // PRODUCTS (INVENTORY)
    // ----------------------------------------------------
    fun getProductsForActiveShop(shopId: String): Flow<List<ProductEntity>> {
        return appDatabase.productDao().getProductsByShop(shopId)
    }

    fun getLowStockProducts(shopId: String): Flow<List<ProductEntity>> {
        return appDatabase.productDao().getLowStockProducts(shopId)
    }

    suspend fun getProductById(shopId: String, id: String): ProductEntity? {
        return appDatabase.productDao().getProductById(shopId, id)
    }

    suspend fun getProductByBarcode(shopId: String, barcode: String): ProductEntity? {
        return appDatabase.productDao().getProductByBarcode(shopId, barcode)
    }

    suspend fun insertProduct(product: ProductEntity) {
        appDatabase.productDao().insertProduct(product)
    }

    suspend fun updateStock(shopId: String, id: String, qty: Double) {
        appDatabase.productDao().updateStock(shopId, id, qty)
    }

    suspend fun deleteProductLogically(shopId: String, id: String) {
        appDatabase.productDao().deleteProductLogically(shopId, id)
    }

    // ----------------------------------------------------
    // CUSTOMERS (CRM)
    // ----------------------------------------------------
    fun getCustomersForActiveShop(shopId: String): Flow<List<CustomerEntity>> {
        return appDatabase.customerDao().getCustomersByShop(shopId)
    }

    suspend fun insertCustomer(customer: CustomerEntity) {
        appDatabase.customerDao().insertCustomer(customer)
    }

    suspend fun updateCustomerBalance(shopId: String, id: String, newBalance: Double) {
        appDatabase.customerDao().updateOutstandingBalance(shopId, id, newBalance)
    }

    // ----------------------------------------------------
    // SALES & POS
    // ----------------------------------------------------
    fun getSalesHistory(shopId: String): Flow<List<SaleEntity>> {
        return appDatabase.saleDao().getSalesByShop(shopId)
    }

    suspend fun getSaleItems(saleId: String): List<SaleItemEntity> {
        return appDatabase.saleDao().getItemsForSale(saleId)
    }

    suspend fun checkoutSale(
        shopId: String,
        customerId: String?,
        total: Double,
        subTotal: Double,
        taxes: Double,
        discounts: Double,
        paymentMethod: String,
        cashPaid: Double,
        cardPaid: Double,
        upiPaid: Double,
        cartItems: List<com.omnishop.erp.features.pos.CartItemState>,
        cashierName: String
    ): SaleEntity {
        val saleId = UUID.randomUUID().toString()
        val invoiceNo = "INV-${System.currentTimeMillis() % 10000000}"

        val saleEntity = SaleEntity(
            id = saleId,
            shopId = shopId,
            invoiceNo = invoiceNo,
            customerId = customerId,
            timestamp = System.currentTimeMillis(),
            subTotal = subTotal,
            taxAmount = taxes,
            discountAmount = discounts,
            totalAmount = total,
            paymentMethod = paymentMethod,
            cashPaid = cashPaid,
            cardPaid = cardPaid,
            upiPaid = upiPaid,
            isSynced = false,
            cashierName = cashierName
        )

        val saleItems = cartItems.map {
            SaleItemEntity(
                id = UUID.randomUUID().toString(),
                saleId = saleId,
                productId = it.product.id,
                productName = it.product.name,
                price = it.product.price,
                quantity = it.quantity.toDouble(),
                totalAmount = it.product.price * it.quantity
            )
        }

        // Complete database transaction
        appDatabase.saleDao().insertCompleteSale(saleEntity, saleItems)

        // Deduct quantities in stock
        cartItems.forEach {
            val originalProduct = appDatabase.productDao().getProductById(shopId, it.product.id)
            if (originalProduct != null) {
                val updatedQty = (originalProduct.stockQty - it.quantity).coerceAtLeast(0.0)
                appDatabase.productDao().updateStock(shopId, it.product.id, updatedQty)
            }
        }

        // Update double-entry accounting automatically
        val journalEntry = JournalEntity(
            id = UUID.randomUUID().toString(),
            shopId = shopId,
            date = System.currentTimeMillis(),
            description = "POS Sales standard Invoice: $invoiceNo",
            accountFrom = "Inventory Revenue Account",
            accountTo = when (paymentMethod) {
                "CASH" -> "Cash Drawer Desk"
                "CARD" -> "Standard Bank Account"
                "UPI" -> "Digital Wallet Asset"
                else -> "Split Accounts Cash/Card/UPI"
            },
            amount = total,
            category = "REVENUE"
        )
        appDatabase.journalDao().insertJournalEntry(journalEntry)

        // If customer exists, credit points
        if (customerId != null) {
            val customer = appDatabase.customerDao().getCustomerById(shopId, customerId)
            if (customer != null) {
                val addedPoints = (total / 10).toInt() // 1 point per 10 currency spent
                val updatedCustomer = customer.copy(
                    loyaltyPoints = customer.loyaltyPoints + addedPoints,
                    outstandingBalance = if (paymentMethod == "CREDIT") {
                        customer.outstandingBalance + total
                    } else {
                        customer.outstandingBalance
                    }
                )
                appDatabase.customerDao().insertCustomer(updatedCustomer)
            }
        }

        return saleEntity
    }

    // ----------------------------------------------------
    // ACCOUNTING (DOUBLE ENTRY)
    // ----------------------------------------------------
    fun getJournalEntries(shopId: String): Flow<List<JournalEntity>> {
        return appDatabase.journalDao().getJournalEntries(shopId)
    }

    suspend fun insertJournalEntry(entry: JournalEntity) {
        appDatabase.journalDao().insertJournalEntry(entry)
    }

    fun getTotalRevenueFlow(shopId: String): Flow<Double> {
        return appDatabase.journalDao().getTotalAmountByCategory(shopId, "REVENUE").map { it ?: 0.0 }
    }

    fun getTotalExpensesFlow(shopId: String): Flow<Double> {
        return appDatabase.journalDao().getTotalAmountByCategory(shopId, "EXPENSE").map { it ?: 0.0 }
    }

    // ----------------------------------------------------
    // STAFF & SECURITY (RBAC)
    // ----------------------------------------------------
    fun getStaffMembers(shopId: String): Flow<List<StaffEntity>> {
        return appDatabase.staffDao().getStaffByShop(shopId)
    }

    suspend fun insertStaffMember(staff: StaffEntity) {
        appDatabase.staffDao().insertStaff(staff)
    }

    suspend fun deleteStaff(shopId: String, staffId: String) {
        appDatabase.staffDao().deleteStaff(shopId, staffId)
    }
}
