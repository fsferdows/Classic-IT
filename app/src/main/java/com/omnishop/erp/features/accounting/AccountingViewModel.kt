package com.omnishop.erp.features.accounting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnishop.erp.core.data.local.JournalEntity
import com.omnishop.erp.core.data.repository.ErpRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class AccountingViewModel(
    private val repository: ErpRepository
) : ViewModel() {

    private val activeShopId: Flow<String> = repository.activeShopIdState

    val journalEntries: StateFlow<List<JournalEntity>> = activeShopId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList())
        else repository.getJournalEntries(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalRevenue: StateFlow<Double> = activeShopId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(0.0)
        else repository.getTotalRevenueFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpenses: StateFlow<Double> = activeShopId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(0.0)
        else repository.getTotalExpensesFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val netProfitLoss: StateFlow<Double> = combine(totalRevenue, totalExpenses) { rev, exp ->
        rev - exp
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Chart of Accounts structures
    val chartOfAccounts = listOf(
        "Cash Drawer Desk" to "Current Asset",
        "Digital Wallet Asset" to "Current Asset",
        "Standard Bank Account" to "Current Asset",
        "Inventory Asset Account" to "Inventory Asset",
        "Inventory Revenue Account" to "Revenue Source",
        "Sales Revenue Account" to "Revenue Source",
        "Store Rent Utility Account" to "Operating Expense",
        "Staff Salaries Operating" to "Personnel Expense",
        "Local Supplier Account Payable" to "Current Liability",
        "Cash Capital Reserves" to "Owner's Equity"
    )

    fun recordDoubleEntryJournal(
        description: String,
        accountFrom: String,
        accountTo: String,
        amount: Double,
        category: String
    ) {
        viewModelScope.launch {
            val shopId = repository.getActiveShopId()
            val entry = JournalEntity(
                id = UUID.randomUUID().toString(),
                shopId = shopId,
                date = System.currentTimeMillis(),
                description = description,
                accountFrom = accountFrom,
                accountTo = accountTo,
                amount = amount,
                category = category
            )
            repository.insertJournalEntry(entry)
        }
    }
}
