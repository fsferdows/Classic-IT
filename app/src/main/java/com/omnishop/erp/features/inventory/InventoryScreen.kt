package com.omnishop.erp.features.inventory

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.omnishop.erp.core.data.local.ProductEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel
) {
    val products by viewModel.products.collectAsState()
    val lowStockCount by viewModel.lowStockCount.collectAsState()
    val csvMessage by viewModel.csvStatusMessage.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedProductForEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = remember(products, searchQuery) {
        products.filter {
            searchQuery.isEmpty() ||
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.sku.contains(searchQuery, ignoreCase = true) ||
            it.barcode.contains(searchQuery)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedProductForEdit = null
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Product")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Row count statistics overview cards
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1.5f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Active Catalog Items", style = MaterialTheme.typography.labelSmall)
                        Text("${products.size} Products", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
                Card(
                    modifier = Modifier.weight(1.5f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (lowStockCount > 0) Color.Red.copy(alpha = 0.15f) 
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Critical Low Stock Lines", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "$lowStockCount Low Items",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (lowStockCount > 0) Color.Red else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            // CSV Tools Strip Section
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("CSV Bulk Manager Tools", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("Perform inventory synchronization actions", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = { viewModel.simulateCsvImportDemo() },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import")
                        }
                        Button(
                            onClick = { viewModel.simulateCsvExport() }
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export")
                        }
                    }
                }
            }

            // Real-time toast logger for csv imports/exports
            if (csvMessage.isNotEmpty()) {
                Snackbar(
                    action = {
                        TextButton(onClick = { viewModel.clearCsvMessage() }) { Text("Dismiss", color = Color.White) }
                    },
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(csvMessage)
                }
            }

            // Search text field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter items by name, category, or barcode") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Dynamic items list
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Inventory catalog is empty for this Tenant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Configure items or run csv import simulator", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts) { product ->
                        ProductListRow(
                            product = product,
                            onEdit = {
                                selectedProductForEdit = product
                                showAddDialog = true
                            },
                            onBatchAdjust = { diff -> viewModel.adjustStockBatchValue(product.id, diff) },
                            onDelete = { viewModel.deleteProduct(product.id) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddOrEditProductDialog(
                product = selectedProductForEdit,
                onDismiss = { showAddDialog = false },
                onSave = { name, sku, barcode, price, cost, stock, threshold, category, expiry ->
                    viewModel.addOrUpdateProduct(
                        id = selectedProductForEdit?.id,
                        name = name,
                        sku = sku,
                        barcode = barcode,
                        price = price,
                        cost = cost,
                        stock = stock,
                        threshold = threshold,
                        category = category,
                        expiryDays = expiry
                    )
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ProductListRow(
    product: ProductEntity,
    onEdit: () -> Unit,
    onBatchAdjust: (Double) -> Unit,
    onDelete: () -> Unit
) {
    val isLowStock = product.stockQty <= product.lowStockThreshold

    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "Sku: ${product.sku} | Barcode: ${product.barcode}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(product.category) },
                        modifier = Modifier.height(26.dp)
                    )
                    
                    if (isLowStock) {
                        Text(
                            text = "Low Stock: ${product.stockQty.toInt()} left",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$${String.format("%.2f", product.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Cost: $${String.format("%.2f", product.purchaseCost)}",
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Quick Stock replenishment trigger buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onBatchAdjust(-5.0) },
                        modifier = Modifier.size(28.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                    ) {
                        Text("-5", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "${product.stockQty.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(
                        onClick = { onBatchAdjust(10.0) },
                        modifier = Modifier.size(28.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                    ) {
                        Text("+10", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp).padding(start = 2.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete product logically", tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AddOrEditProductDialog(
    product: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double, Double, Double, Double, String, Int?) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var sku by remember { mutableStateOf(product?.sku ?: "") }
    var barcode by remember { mutableStateOf(product?.barcode ?: "") }
    var priceString by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var costString by remember { mutableStateOf(product?.purchaseCost?.toString() ?: "") }
    var stockString by remember { mutableStateOf(product?.stockQty?.toString() ?: "10") }
    var thresholdString by remember { mutableStateOf(product?.lowStockThreshold?.toString() ?: "3") }
    var category by remember { mutableStateOf(product?.category ?: "IT Services") }
    var expiryDaysString by remember { mutableStateOf("") }

    var categoryExpanded by remember { mutableStateOf(false) }
    val categoriesList = listOf("IT Services", "IT Consulting", "Networking Hardware", "Computer Components", "Software & Licensing")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().heightIn(max = 620.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = if (product == null) "Create Catalog Product" else "Update Database Product",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = sku,
                            onValueChange = { sku = it },
                            label = { Text("SKU Number") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text("Barcode UPC") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = priceString,
                            onValueChange = { priceString = it },
                            label = { Text("Selling Price ($) *") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = costString,
                            onValueChange = { costString = it },
                            label = { Text("Supply Cost ($) *") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = stockString,
                            onValueChange = { stockString = it },
                            label = { Text("Qty In Stock *") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = thresholdString,
                            onValueChange = { thresholdString = it },
                            label = { Text("Low alert Level *") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Product Category") },
                            trailingIcon = {
                                IconButton(onClick = { categoryExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categoriesList.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = expiryDaysString,
                        onValueChange = { expiryDaysString = it },
                        label = { Text("Expires In (Days from today - Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotEmpty() && priceString.isNotEmpty()) {
                                    val price = priceString.toDoubleOrNull() ?: 1.0
                                    val cost = costString.toDoubleOrNull() ?: 0.5
                                    val stock = stockString.toDoubleOrNull() ?: 1.0
                                    val threshold = thresholdString.toDoubleOrNull() ?: 3.0
                                    val expiryDays = expiryDaysString.toIntOrNull()
                                    onSave(name, sku, barcode, price, cost, stock, threshold, category, expiryDays)
                                }
                            }
                        ) {
                            Text("Save Product")
                        }
                    }
                }
            }
        }
    }
}
