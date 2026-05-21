package com.omnishop.erp.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.omnishop.erp.core.data.local.PreferencesManager
import com.omnishop.erp.core.data.repository.ErpRepository
import com.omnishop.erp.features.accounting.AccountingViewModel
import com.omnishop.erp.features.auth.AuthViewModel
import com.omnishop.erp.features.dashboard.DashboardViewModel
import com.omnishop.erp.features.inventory.InventoryViewModel
import com.omnishop.erp.features.pos.PosViewModel
import com.omnishop.erp.features.staff.StaffViewModel

class ViewModelFactory(
    private val repository: ErpRepository,
    private val prefs: PreferencesManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(repository, prefs) as T
            }
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(repository) as T
            }
            modelClass.isAssignableFrom(PosViewModel::class.java) -> {
                PosViewModel(repository) as T
            }
            modelClass.isAssignableFrom(InventoryViewModel::class.java) -> {
                InventoryViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AccountingViewModel::class.java) -> {
                AccountingViewModel(repository) as T
            }
            modelClass.isAssignableFrom(StaffViewModel::class.java) -> {
                StaffViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
