package com.omnishop.erp.features.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnishop.erp.core.data.local.StaffEntity
import com.omnishop.erp.core.data.repository.ErpRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class StaffViewModel(
    private val repository: ErpRepository
) : ViewModel() {

    private val activeShopId: Flow<String> = repository.activeShopIdState

    val staffList: StateFlow<List<StaffEntity>> = activeShopId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList())
        else repository.getStaffMembers(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rolePermissionsMatrix = mapOf(
        "Owner" to listOf("POS Checkout", "Manage Inventory", "Modify Stores", "Journal Accounting", "Modify Staff"),
        "Manager" to listOf("POS Checkout", "Manage Inventory", "Modify Stores", "Journal Accounting"),
        "Cashier" to listOf("POS Checkout"),
        "Accountant" to listOf("Journal Accounting"),
        "InventoryManager" to listOf("Manage Inventory")
    )

    fun addStaffMember(
        name: String,
        email: String,
        role: String,
        phone: String
    ) {
        viewModelScope.launch {
            val shopId = repository.getActiveShopId()
            val computedPermissions = when (role) {
                "Owner" -> "POS,INVENTORY,ACCOUNTING,STAFF"
                "Manager" -> "POS,INVENTORY,ACCOUNTING"
                "Cashier" -> "POS"
                "Accountant" -> "ACCOUNTING"
                "InventoryManager" -> "INVENTORY"
                else -> "POS"
            }

            val member = StaffEntity(
                id = UUID.randomUUID().toString(),
                shopId = shopId,
                name = name,
                email = email,
                role = role,
                phoneNumber = phone,
                permissions = computedPermissions
            )
            repository.insertStaffMember(member)
        }
    }

    fun removeStaffMember(id: String) {
        viewModelScope.launch {
            val shopId = repository.getActiveShopId()
            repository.deleteStaff(shopId, id)
        }
    }
}
