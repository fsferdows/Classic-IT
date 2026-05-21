package com.omnishop.erp.features.accounting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.omnishop.erp.core.data.local.JournalEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountingScreen(
    viewModel: AccountingViewModel
) {
    val entries by viewModel.journalEntries.collectAsState()
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val netProfit by viewModel.netProfitLoss.collectAsState()
    val chartOfAccounts = viewModel.chartOfAccounts

    var showAddJournalDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // High-polish Financial Indicators Panel
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("Accumulated Revenue", style = MaterialTheme.typography.labelSmall)
                    Text("$${String.format("%.2f", totalRevenue)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
                VerticalDivider(modifier = Modifier.height(36.dp).width(1.dp).align(Alignment.CenterVertically), color = MaterialTheme.colorScheme.outlineVariant)
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("Operating Expenses", style = MaterialTheme.typography.labelSmall)
                    Text("$${String.format("%.2f", totalExpenses)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                }
                VerticalDivider(modifier = Modifier.height(36.dp).width(1.dp).align(Alignment.CenterVertically), color = MaterialTheme.colorScheme.outlineVariant)
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("Net Store Balance", style = MaterialTheme.typography.labelSmall)
                    val color = if (netProfit >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                    Text("$${String.format("%.2f", netProfit)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = color)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "Chart of Accounts Reference",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Button(
                onClick = { showAddJournalDialog = true },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Voucher")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid/horizontal row for Chart of Accounts categories
        LazyColumn(modifier = Modifier.height(115.dp).fillMaxWidth()) {
            items(chartOfAccounts.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pair.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            tonalElevation = 1.dp,
                            modifier = Modifier.weight(1f).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.first, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f))
                                Text(
                                    text = item.second,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(0.4f),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "General Double Entry Ledger Logs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            val context = androidx.compose.ui.platform.LocalContext.current
            OutlinedButton(
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                onClick = {
                    com.omnishop.erp.core.common.PdfGenerator.downloadShareAccountingReportPdf(
                        context = context,
                        totalRevenue = totalRevenue,
                        totalExpenses = totalExpenses,
                        netProfitLoss = netProfit,
                        entries = entries
                    )
                }
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Download Ledger PDF", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        if (entries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No double-entry logs registered", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries) { entry ->
                    DebitCreditJournalRow(entry = entry)
                }
            }
        }
    }

    if (showAddJournalDialog) {
        RecordJournalVoucherDialog(
            accountsList = chartOfAccounts,
            onDismiss = { showAddJournalDialog = false },
            onSave = { desc, fromAcc, toAcc, amount, cat ->
                viewModel.recordDoubleEntryJournal(desc, fromAcc, toAcc, amount, cat)
                showAddJournalDialog = false
            }
        )
    }
}

@Composable
fun DebitCreditJournalRow(entry: JournalEntity) {
    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(0.6f)) {
                    Text(text = entry.description, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Voucher Time ID: ${System.currentTimeMillis() % 100000}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    text = "$${String.format("%.2f", entry.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (entry.category == "REVENUE") Color(0xFF2E7D32) else Color(0xFFC62828),
                    modifier = Modifier.weight(0.4f),
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Source Account: \n  ${entry.accountFrom}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Debit Destination: \n  ${entry.accountTo}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun RecordJournalVoucherDialog(
    accountsList: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double, String) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var accountFrom by remember { mutableStateOf(accountsList.first().first) }
    var accountTo by remember { mutableStateOf(accountsList.last().first) }
    var amountString by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("EXPENSE") }

    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Record Ledger Voucher Entry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("General description (reconciliation) *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = amountString,
                    onValueChange = { amountString = it },
                    label = { Text("Transfer Voucher Amount ($) *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                // Select Category EXPENSE or REVENUE or LIABILITY
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("EXPENSE", "REVENUE", "LIABILITY").forEach { cat ->
                        Button(
                            onClick = { category = cat },
                            colors = if (category == cat) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(cat, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Account From dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = accountFrom,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Credit Source Account From") },
                        trailingIcon = {
                            IconButton(onClick = { fromExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    DropdownMenu(
                        expanded = fromExpanded,
                        onDismissRequest = { fromExpanded = false }
                    ) {
                        accountsList.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.first) },
                                onClick = {
                                    accountFrom = item.first
                                    fromExpanded = false
                                }
                            )
                        }
                    }
                }

                // Account To dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = accountTo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Debit Destination Account To") },
                        trailingIcon = {
                            IconButton(onClick = { toExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    DropdownMenu(
                        expanded = toExpanded,
                        onDismissRequest = { toExpanded = false }
                    ) {
                        accountsList.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.first) },
                                onClick = {
                                    accountTo = item.first
                                    toExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (description.isNotEmpty() && amountString.isNotEmpty()) {
                                onSave(description, accountFrom, accountTo, amountString.toDoubleOrNull() ?: 0.0, category)
                            }
                        }
                    ) {
                        Text("Post Entry")
                    }
                }
            }
        }
    }
}
