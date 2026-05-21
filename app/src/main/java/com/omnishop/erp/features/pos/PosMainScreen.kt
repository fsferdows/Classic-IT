package com.omnishop.erp.features.pos

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.omnishop.erp.core.data.local.ProductEntity
import com.omnishop.erp.core.data.local.SaleEntity
import com.omnishop.erp.core.data.local.ShopEntity
import com.omnishop.erp.features.dashboard.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosMainScreen(
    viewModel: PosViewModel,
    dashboardViewModel: DashboardViewModel,
    currentUserRole: String
) {
    val products by viewModel.filteredProducts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    val cart by viewModel.cart.collectAsState()
    val subtotal by viewModel.subtotal.collectAsState()
    val discountAmount by viewModel.discountAmount.collectAsState()
    val discountPercent by viewModel.discountPercent.collectAsState()
    val taxes by viewModel.taxes.collectAsState()
    val total by viewModel.total.collectAsState()
    val checkoutSuccess by viewModel.checkoutSuccess.collectAsState()
    val printerLogs by viewModel.printerConnectionLog.collectAsState()
    
    val activeShop by dashboardViewModel.activeShop.collectAsState()

    var showDiscountDialog by remember { mutableStateOf(false) }
    var showBarcodeSimulator by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            // LEFT COLUMN: Categories Filter & POS Product Item Grid Selector
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                // Header Search & Actions Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search by name, SKU, or Barcode") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pos_search_input")
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { showBarcodeSimulator = true },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                            .size(56.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Simulate Barcode Picker")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable category row filter capsules
                ScrollableCategoryRow(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onSelected = { viewModel.filterByCategory(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Grid of Products Catalog
                if (products.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No items match filters", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(products) { product ->
                            PosProductCard(
                                product = product,
                                onClick = { viewModel.addToCart(product) }
                            )
                        }
                    }
                }
            }

            // RIGHT COLUMN: Active Cart Lines & Quick Split Multi-Payment Desk
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Current Order Terminal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    if (cart.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Select catalog items to bill", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    } else {
                        // Scrollable invoice line items list
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(cart) { item ->
                                InvoiceCartItemRow(
                                    item = item,
                                    onQtyChanged = { actionAdd -> viewModel.updateQuantity(item.product, actionAdd) },
                                    onRemoved = { viewModel.removeFromCart(item.product) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Bill Summary Calculations
                    BillingSummarySection(
                        subtotal = subtotal,
                        discountPercent = discountPercent,
                        discountAmount = discountAmount,
                        taxes = taxes,
                        total = total,
                        onAddDiscountClick = { showDiscountDialog = true }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showCheckoutDialog = true },
                        enabled = cart.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("checkout_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "PROCEED TO MULTI-PAY ($${String.format("%.2f", total)})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // DIALOGS & OVERLAYS
        if (showDiscountDialog) {
            DiscountSelectorDialog(
                currentDiscount = discountPercent,
                onDismiss = { showDiscountDialog = false },
                onSelect = {
                    viewModel.applyDiscountPercent(it)
                    showDiscountDialog = false
                }
            )
        }

        if (showBarcodeSimulator) {
            BarcodeModelScannerSelector(
                productsList = products,
                onDismiss = { showBarcodeSimulator = false },
                onScan = { barcode ->
                    viewModel.simulateBarcodeScanner(barcode)
                    showBarcodeSimulator = false
                }
            )
        }

        if (showCheckoutDialog) {
            SplitCheckoutDrawer(
                totalAmount = total,
                onDismiss = { showCheckoutDialog = false },
                onSubmitPayment = { cash, card, upi, method ->
                    viewModel.checkoutSplit(cash, card, upi, method, "Yuki Tanaka")
                    showCheckoutDialog = false
                }
            )
        }

        if (checkoutSuccess != null) {
            PosReceiptSuccessModal(
                sale = checkoutSuccess!!,
                cartItems = cart,
                logs = printerLogs,
                activeShop = activeShop,
                onClose = { viewModel.resetCheckout() }
            )
        }
    }
}

@Composable
fun ScrollableCategoryRow(
    categories: List<String>,
    selectedCategory: String,
    onSelected: (String) -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            InputChip(
                selected = isSelected,
                onClick = { onSelected(category) },
                label = { Text(category) },
                colors = InputChipDefaults.inputChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

// POS Product Grid Card Layout
@Composable
fun PosProductCard(
    product: ProductEntity,
    onClick: () -> Unit
) {
    val isLowStock = product.stockQty <= product.lowStockThreshold
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.95f
            isHovered -> 1.04f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "productCardScaleAnimation"
    )

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHovered) 8.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHovered) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(color = MaterialTheme.colorScheme.primary),
                onClick = onClick
            )
            .border(
                1.dp,
                if (isLowStock) Color.Red.copy(alpha = 0.6f)
                else if (isHovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = product.category.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                modifier = Modifier.height(42.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$${String.format("%.2f", product.price)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isLowStock) Color.Red.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(2.dp)
                ) {
                    Text(
                        text = "Stk: ${product.stockQty.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = if (isLowStock) Color.Red else MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun InvoiceCartItemRow(
    item: CartItemState,
    onQtyChanged: (Boolean) -> Unit,
    onRemoved: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = "$${String.format("%.2f", item.product.price)} x ${item.quantity}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { onQtyChanged(false) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Sub qty")
            }
            Box(
                modifier = Modifier.widthIn(min = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${item.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            IconButton(
                onClick = { onQtyChanged(true) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = "Add qty")
            }
            IconButton(
                onClick = { onRemoved() },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete item", tint = Color.Red.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun BillingSummarySection(
    subtotal: Double,
    discountPercent: Int,
    discountAmount: Double,
    taxes: Double,
    total: Double,
    onAddDiscountClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
            Text("$${String.format("%.2f", subtotal)}")
        }
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onAddDiscountClick() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Discount ($discountPercent%)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Text("-$${String.format("%.2f", discountAmount)}", color = Color(0xFF2E7D32))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Taxes (18% GST)", style = MaterialTheme.typography.bodyMedium)
            Text("$${String.format("%.2f", taxes)}")
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Bill Due", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("$${String.format("%.2f", total)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun DiscountSelectorDialog(
    currentDiscount: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Apply Coupon Discount", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(0, 5, 10, 15, 20).forEach { pct ->
                        Button(
                            onClick = { onSelect(pct) },
                            colors = if (pct == currentDiscount) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors()
                        ) {
                            Text("$pct%")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    }
}

@Composable
fun BarcodeModelScannerSelector(
    productsList: List<ProductEntity>,
    onDismiss: () -> Unit,
    onScan: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "MLKit Omnidirectional Barcode Scanner",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "A customer has brought an item. Choose a barcode package to simulate scanning with device laser trigger:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(productsList) { prod ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onScan(prod.barcode) }
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(prod.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("SKU: ${prod.sku} | Barcode: ${prod.barcode}", style = MaterialTheme.typography.labelSmall)
                            }
                            Icon(Icons.Default.Input, contentDescription = "Pick barcode")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Hide Scanner") }
            }
        }
    }
}

@Composable
fun SplitCheckoutDrawer(
    totalAmount: Double,
    onDismiss: () -> Unit,
    onSubmitPayment: (Double, Double, Double, String) -> Unit
) {
    var paymentMethod by remember { mutableStateOf("CASH") }
    var cashAmount by remember { mutableStateOf(totalAmount.toString()) }
    var cardAmount by remember { mutableStateOf("0.0") }
    var upiAmount by remember { mutableStateOf("0.0") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.width(420.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Secure POS Checkout Desk", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Invoice total: $${String.format("%.2f", totalAmount)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Select Billing Mechanism:", style = MaterialTheme.typography.labelSmall)
                
                // Segments
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("CASH", "CARD", "UPI", "SPLIT").forEach { m ->
                        val isSel = paymentMethod == m
                        Button(
                            onClick = {
                                paymentMethod = m
                                if (m == "CASH") {
                                    cashAmount = totalAmount.toString()
                                    cardAmount = "0.0"
                                    upiAmount = "0.0"
                                } else if (m == "CARD") {
                                    cashAmount = "0.0"
                                    cardAmount = totalAmount.toString()
                                    upiAmount = "0.0"
                                } else if (m == "UPI") {
                                    cashAmount = "0.0"
                                    cardAmount = "0.0"
                                    upiAmount = totalAmount.toString()
                                } else {
                                    cashAmount = (totalAmount / 3).toString()
                                    cardAmount = (totalAmount / 3).toString()
                                    upiAmount = (totalAmount / 3).toString()
                                }
                            },
                            colors = if (isSel) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors(),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Text(m, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input fields based on methods
                if (paymentMethod == "SPLIT" || paymentMethod == "CASH") {
                    OutlinedTextField(
                        value = cashAmount,
                        onValueChange = { cashAmount = it },
                        label = { Text("Cash Drawer Payment ($)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                if (paymentMethod == "SPLIT" || paymentMethod == "CARD") {
                    OutlinedTextField(
                        value = cardAmount,
                        onValueChange = { cardAmount = it },
                        label = { Text("Card Swipe Payment ($)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                if (paymentMethod == "SPLIT" || paymentMethod == "UPI") {
                    OutlinedTextField(
                        value = upiAmount,
                        onValueChange = { upiAmount = it },
                        label = { Text("M-Check UPI Mobile Wallet ($)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cashVal = cashAmount.toDoubleOrNull() ?: 0.0
                            val cardVal = cardAmount.toDoubleOrNull() ?: 0.0
                            val upiVal = upiAmount.toDoubleOrNull() ?: 0.0
                            onSubmitPayment(cashVal, cardVal, upiVal, paymentMethod)
                        }
                    ) {
                        Text("Record Final Ledger")
                    }
                }
            }
        }
    }
}

// Receipt Success Modal + Bluetooth ESC/POS preloader
@Composable
fun PosReceiptSuccessModal(
    sale: SaleEntity,
    cartItems: List<CartItemState>,
    logs: List<String>,
    activeShop: ShopEntity?,
    onClose: () -> Unit
) {
    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.width(450.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(36.dp))
                    Column {
                        Text("Ledger Synchronized", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Invoice Reference: ${sale.invoiceNo}", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Thermal receipt paper mimic container
                Column(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = activeShop?.name ?: "OmniShopERP Unit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Tenant: ${activeShop?.businessType ?: "General Business"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "----------------------------------",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cashier Name", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                        Text(sale.cashierName, style = MaterialTheme.typography.labelSmall, color = Color.Black)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Time Issued", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                        Text("May 21, 2026 08:42 AM", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                    }

                    Text(
                        text = "----------------------------------",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Payment Concept", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(sale.paymentMethod, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    
                    if (sale.cashPaid > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("  - Cash Share", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
                            Text("$${String.format("%.2f", sale.cashPaid)}", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                        }
                    }
                    if (sale.cardPaid > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("  - Card Swipe Share", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
                            Text("$${String.format("%.2f", sale.cardPaid)}", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                        }
                    }
                    if (sale.upiPaid > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("  - Digital Wallet UPI", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
                            Text("$${String.format("%.2f", sale.upiPaid)}", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                        }
                    }

                    Text(
                        text = "----------------------------------",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                        Text("$${String.format("%.2f", sale.subTotal)}", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Discount (Coupon)", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                        Text("-$${String.format("%.2f", sale.discountAmount)}", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Taxes (GST 18%)", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                        Text("$${String.format("%.2f", sale.taxAmount)}", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Bill Issued", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                        Text("$${String.format("%.2f", sale.totalAmount)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = activeShop?.receiptFooter ?: "Thank you!",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                // Simulated Printer driver active logs
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("ESC/POS Hardware Console", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyColumn(modifier = Modifier.height(80.dp)) {
                            items(logs) { log ->
                                Text(
                                    text = log,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (log.contains("SUCCESS")) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    OutlinedButton(
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            val saleItems = cartItems.map { cartItem ->
                                com.omnishop.erp.core.data.local.SaleItemEntity(
                                    id = "",
                                    saleId = sale.id,
                                    productId = cartItem.product.id,
                                    productName = cartItem.product.name,
                                    price = cartItem.product.price,
                                    quantity = cartItem.quantity.toDouble(),
                                    totalAmount = cartItem.product.price * cartItem.quantity
                                )
                            }
                            com.omnishop.erp.core.common.PdfGenerator.downloadShareBillPdf(
                                context = context,
                                shopName = activeShop?.name ?: "OmniShop Unit",
                                shopBusinessType = activeShop?.businessType ?: "General Business",
                                receiptFooter = activeShop?.receiptFooter ?: "Thank you!",
                                sale = sale,
                                items = saleItems
                            )
                        }
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download PDF Invoice", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    
                    Button(
                        shape = RoundedCornerShape(12.dp),
                        onClick = onClose
                    ) {
                        Text("OK, Clear Desk", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}
