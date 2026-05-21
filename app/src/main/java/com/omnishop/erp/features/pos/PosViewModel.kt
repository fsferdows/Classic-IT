package com.omnishop.erp.features.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnishop.erp.core.data.local.*
import com.omnishop.erp.core.data.repository.ErpRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PosViewModel(
    private val repository: ErpRepository
) : ViewModel() {

    private val activeShopId: Flow<String> = repository.activeShopIdState

    // Observed product list scoped to active shop
    val productsFlow: StateFlow<List<ProductEntity>> = activeShopId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList())
        else repository.getProductsForActiveShop(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Category list computed dynamically
    val categories: StateFlow<List<String>> = productsFlow.map { list ->
        listOf("All Items") + list.map { it.category }.distinct()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All Items"))

    private val _selectedCategory = MutableStateFlow("All Items")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Reactive filtered products display
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        productsFlow,
        selectedCategory,
        searchQuery
    ) { list, cat, query ->
        list.filter {
            (cat == "All Items" || it.category == cat) &&
            (query.isEmpty() || it.name.contains(query, ignoreCase = true) || it.sku.contains(query, ignoreCase = true) || it.barcode == query)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart Management
    private val _cart = MutableStateFlow<List<CartItemState>>(emptyList())
    val cart: StateFlow<List<CartItemState>> = _cart

    // Discount options
    private val _discountPercent = MutableStateFlow(0)
    val discountPercent: StateFlow<Int> = _discountPercent

    // Loyalty Customer Pick
    val customersFlow: StateFlow<List<CustomerEntity>> = activeShopId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList())
        else repository.getCustomersForActiveShop(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val selectedCustomer: StateFlow<CustomerEntity?> = _selectedCustomer

    // SPLIT payment methods values
    val subtotal: StateFlow<Double> = _cart.map { items ->
        items.sumOf { it.product.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val discountAmount: StateFlow<Double> = combine(subtotal, _discountPercent) { subValue, discPct ->
        subValue * (discPct / 100.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val taxes: StateFlow<Double> = subtotal.map { subValue ->
        subValue * 0.18 // standard 18% GST/VAT Compliant calculation
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val total: StateFlow<Double> = combine(subtotal, discountAmount, taxes) { subVal, discVal, taxVal ->
        (subVal - discVal + taxVal).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Invoice print and success states
    private val _checkoutSuccess = MutableStateFlow<SaleEntity?>(null)
    val checkoutSuccess: StateFlow<SaleEntity?> = _checkoutSuccess

    private val _printerConnectionLog = MutableStateFlow<List<String>>(emptyList())
    val printerConnectionLog: StateFlow<List<String>> = _printerConnectionLog

    fun filterByCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addToCart(product: ProductEntity) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index != -isLessThanZero()) {
            val item = currentList[index]
            if (item.quantity < product.stockQty) {
                currentList[index] = item.copy(quantity = item.quantity + 1)
            }
        } else {
            if (product.stockQty >= 1.0) {
                currentList.add(CartItemState(product, 1))
            }
        }
        _cart.value = currentList
    }

    private fun isLessThanZero() = 1 // Helper for index

    fun updateQuantity(product: ProductEntity, actionAdd: Boolean) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index != -1) {
            val item = currentList[index]
            if (actionAdd) {
                if (item.quantity < product.stockQty) {
                    currentList[index] = item.copy(quantity = item.quantity + 1)
                }
            } else {
                if (item.quantity > 1) {
                    currentList[index] = item.copy(quantity = item.quantity - 1)
                } else {
                    currentList.removeAt(index)
                }
            }
        }
        _cart.value = currentList
    }

    fun removeFromCart(product: ProductEntity) {
        _cart.value = _cart.value.filter { it.product.id != product.id }
    }

    fun applyDiscountPercent(percentage: Int) {
        _discountPercent.value = percentage
    }

    fun selectCustomer(customer: CustomerEntity?) {
        _selectedCustomer.value = customer
    }

    fun simulateBarcodeScanner(barcode: String) {
        viewModelScope.launch {
            val shopId = repository.getActiveShopId()
            val prod = repository.getProductByBarcode(shopId, barcode)
            if (prod != null) {
                addToCart(prod)
                _searchQuery.value = ""
            }
        }
    }

    fun checkoutSplit(
        cashPaid: Double,
        cardPaid: Double,
        upiPaid: Double,
        paymentMethod: String,
        cashierName: String
    ) {
        viewModelScope.launch {
            val shopId = repository.getActiveShopId()
            val totalVal = total.value
            val saleEntity = repository.checkoutSale(
                shopId = shopId,
                customerId = _selectedCustomer.value?.id,
                total = totalVal,
                subTotal = subtotal.value,
                taxes = taxes.value,
                discounts = discountAmount.value,
                paymentMethod = paymentMethod,
                cashPaid = cashPaid,
                cardPaid = cardPaid,
                upiPaid = upiPaid,
                cartItems = _cart.value,
                cashierName = cashierName
            )
            _checkoutSuccess.value = saleEntity
            simulateEscPosPrint(saleEntity)
        }
    }

    fun resetCheckout() {
        _cart.value = emptyList()
        _discountPercent.value = 0
        _selectedCustomer.value = null
        _checkoutSuccess.value = null
    }

    // Hardware Bluetooth ESC/POS Simulating Logs
    private fun simulateEscPosPrint(sale: SaleEntity) {
        val logs = mutableListOf<String>()
        logs.add("[Printer] Initiating Bluetooth Connection to 58mm Thermal Device...")
        logs.add("[Printer] Connected successfully to 'Thermal_POS_80B' [MAC: 00:11:22:33:AA:BB]")
        logs.add("[Printer] Sending Print Queue for Invoice #${sale.invoiceNo}...")
        logs.add("[ESC/POS] ESC @ (Init) | ESC a 1 (Align Center)")
        logs.add("[ESC/POS] DLE EOT 1 (Paper Status OK)")
        logs.add("[Printer] Feed lines & cut printed: SUCCESS")
        _printerConnectionLog.value = logs
    }
}
