package com.omnishop.erp.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnishop.erp.core.data.local.ProductEntity
import com.omnishop.erp.core.data.local.ShopEntity
import com.omnishop.erp.core.data.repository.ErpRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val repository: ErpRepository
) : ViewModel() {

    // Shop observation list
    val allShops: StateFlow<List<ShopEntity>> = repository.getAllShops()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Shop Id
    val activeShopId: StateFlow<String> = repository.activeShopIdState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // Selected Shop Object
    val activeShop: StateFlow<ShopEntity?> = activeShopId.flatMapLatest { shopId ->
        allShops.map { list -> list.find { it.id == shopId } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active Shop Products
    val products: StateFlow<List<ProductEntity>> = activeShopId.flatMapLatest { shopId ->
        if (shopId.isEmpty()) flowOf(emptyList())
        else repository.getProductsForActiveShop(shopId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Low stock items
    val lowStockProducts: StateFlow<List<ProductEntity>> = activeShopId.flatMapLatest { shopId ->
        if (shopId.isEmpty()) flowOf(emptyList())
        else repository.getLowStockProducts(shopId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Shop Customers
    val customersCount: StateFlow<Int> = activeShopId.flatMapLatest { shopId ->
        if (shopId.isEmpty()) flowOf(0)
        else repository.getCustomersForActiveShop(shopId).map { it.size }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Active Shop accounting summary
    val totalRevenue: StateFlow<Double> = activeShopId.flatMapLatest { shopId ->
        if (shopId.isEmpty()) flowOf(0.0)
        else repository.getTotalRevenueFlow(shopId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpenses: StateFlow<Double> = activeShopId.flatMapLatest { shopId ->
        if (shopId.isEmpty()) flowOf(0.0)
        else repository.getTotalExpensesFlow(shopId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalSalesHistoryCount: StateFlow<Int> = activeShopId.flatMapLatest { shopId ->
        if (shopId.isEmpty()) flowOf(0)
        else repository.getSalesHistory(shopId).map { it.size }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun switchActiveShop(shopId: String) {
        viewModelScope.launch {
            repository.switchShop(shopId)
        }
    }
}
