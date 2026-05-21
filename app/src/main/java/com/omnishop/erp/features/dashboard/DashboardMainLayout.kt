package com.omnishop.erp.features.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.omnishop.erp.core.designsystem.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omnishop.erp.core.data.local.ProductEntity
import com.omnishop.erp.core.data.local.ShopEntity
import com.omnishop.erp.features.accounting.AccountingScreen
import com.omnishop.erp.features.accounting.AccountingViewModel
import com.omnishop.erp.features.inventory.InventoryScreen
import com.omnishop.erp.features.inventory.InventoryViewModel
import com.omnishop.erp.features.pos.PosMainScreen
import com.omnishop.erp.features.pos.PosViewModel
import com.omnishop.erp.features.staff.StaffScreen
import com.omnishop.erp.features.staff.StaffViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardMainLayout(
    viewModel: DashboardViewModel,
    posViewModel: PosViewModel,
    inventoryViewModel: InventoryViewModel,
    accountingViewModel: AccountingViewModel,
    staffViewModel: StaffViewModel,
    currentUserRole: String,
    currentUserName: String,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onOnboardNewShopClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val shops by viewModel.allShops.collectAsState()
    val activeShopId by viewModel.activeShopId.collectAsState()
    val activeShop by viewModel.activeShop.collectAsState()
    
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val totalSalesHistoryCount by viewModel.totalSalesHistoryCount.collectAsState()
    val customersCount by viewModel.customersCount.collectAsState()

    var activeTab by remember { mutableStateOf("ANALYTICS") }
    var shopDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    // Reactive Multitenancy Tenant Switcher dropdown
                    Row(
                        modifier = Modifier
                            .clickable { shopDropdownExpanded = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = activeShop?.name ?: "OmniShop ERP Gateway",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Vertical: ${activeShop?.businessType ?: "Loading Business..."} ▾",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    DropdownMenu(
                        expanded = shopDropdownExpanded,
                        onDismissRequest = { shopDropdownExpanded = false }
                    ) {
                        shops.forEach { shop ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(shop.name, fontWeight = FontWeight.Bold)
                                        Text(shop.businessType, style = MaterialTheme.typography.labelSmall)
                                    }
                                },
                                onClick = {
                                    viewModel.switchActiveShop(shop.id)
                                    shopDropdownExpanded = false
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Onboard New Tenant Shop", color = MaterialTheme.colorScheme.primary)
                                }
                            },
                            onClick = {
                                shopDropdownExpanded = false
                                onOnboardNewShopClick()
                            }
                        )
                    }
                },
                actions = {
                    // Current Active User Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        // Dynamic Theme Mode Switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                text = if (isDarkTheme) "Dark" else "Light",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(end = 6.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = onThemeToggle
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(currentUserName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(currentUserRole, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onLogoutClick) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out Security Session")
                        }
                    }
                }
            )
        },
        bottomBar = {
            // High fidelity bottom standard navigation bar respecting edge to edge WindowInsets
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = activeTab == "ANALYTICS",
                    onClick = { activeTab = "ANALYTICS" },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Overview") }
                )
                NavigationBarItem(
                    selected = activeTab == "POS",
                    onClick = { activeTab = "POS" },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    label = { Text("POS Desk") },
                    modifier = Modifier.testTag("nav_pos_tab")
                )
                NavigationBarItem(
                    selected = activeTab == "INVENTORY",
                    onClick = { activeTab = "INVENTORY" },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = null) },
                    label = { Text("Stock") },
                    modifier = Modifier.testTag("nav_inventory_tab")
                )
                NavigationBarItem(
                    selected = activeTab == "ACCOUNTING",
                    onClick = { activeTab = "ACCOUNTING" },
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                    label = { Text("Ledger") }
                )
                NavigationBarItem(
                    selected = activeTab == "STAFF",
                    onClick = { activeTab = "STAFF" },
                    icon = { Icon(Icons.Default.Group, contentDescription = null) },
                    label = { Text("Staffing") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "ANALYTICS" -> {
                    AnalyticsDashboardTab(
                        totalRevenue = totalRevenue,
                        totalExpenses = totalExpenses,
                        totalSalesHistoryCount = totalSalesHistoryCount,
                        customersCount = customersCount,
                        lowStockProducts = lowStockProducts,
                        onGotoInventory = { activeTab = "INVENTORY" },
                        onGotoPos = { activeTab = "POS" }
                    )
                }
                "POS" -> {
                    PosMainScreen(
                        viewModel = posViewModel,
                        dashboardViewModel = viewModel,
                        currentUserRole = currentUserRole
                    )
                }
                "INVENTORY" -> {
                    InventoryScreen(viewModel = inventoryViewModel)
                }
                "ACCOUNTING" -> {
                    AccountingScreen(viewModel = accountingViewModel)
                }
                "STAFF" -> {
                    StaffScreen(viewModel = staffViewModel)
                }
            }
        }
    }
}

@Composable
fun AnalyticsDashboardTab(
    totalRevenue: Double,
    totalExpenses: Double,
    totalSalesHistoryCount: Int,
    customersCount: Int,
    lowStockProducts: List<ProductEntity>,
    onGotoInventory: () -> Unit,
    onGotoPos: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Classic IT Analytics Dashboard",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Summary Quick Indicators beautifully rendered as premium stacked MetricCards
        item {
            MetricCard(
                title = "Total Revenue Earned",
                value = "$${String.format("%.2f", totalRevenue)}",
                icon = Icons.Default.TrendingUp,
                trend = "+12.4%",
                trendIsUpward = true,
                accentColor = Color(0xFF4CAF50)
            )
        }
        item {
            MetricCard(
                title = "Ledgers & POS Invoices",
                value = "$totalSalesHistoryCount Invoices Billed",
                icon = Icons.Default.Receipt,
                trend = "+6.8%",
                trendIsUpward = true,
                accentColor = MaterialTheme.colorScheme.primary
            )
        }
        item {
            MetricCard(
                title = "Loyalty Directory",
                value = "$customersCount Active Clients",
                icon = Icons.Default.Star,
                trend = "+8.2%",
                trendIsUpward = true,
                accentColor = Color(0xFFFFB300)
            )
        }

        // Beautiful Glassmorphic Cash flow chart!
        item {
            LuxCard(
                modifier = Modifier.fillMaxWidth(),
                variant = LuxCardVariant.Glass,
                cornerRadius = 24.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Cash Flow Velocity Chart (Last 7 Days)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Compose Canvas 
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        CashFlowCanvasChart(
                            revenueValue = totalRevenue,
                            expenseValue = totalExpenses
                        )
                    }
                }
            }
        }

        // Low stock tracker custom glassmorphic status block
        item {
            LuxCard(
                modifier = Modifier.fillMaxWidth(),
                variant = if (lowStockProducts.isNotEmpty()) LuxCardVariant.GradientBorder else LuxCardVariant.Glass,
                cornerRadius = 20.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (lowStockProducts.isNotEmpty()) Icons.Default.CrisisAlert else Icons.Default.Verified,
                            contentDescription = null,
                            tint = if (lowStockProducts.isNotEmpty()) Color(0xFFF44336) else Color(0xFF4CAF50),
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = if (lowStockProducts.isNotEmpty()) "Critical Stock Shortage Alert!" else "All Stock Quantities Secure",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (lowStockProducts.isNotEmpty()) "${lowStockProducts.size} items have dropped below safety thresholds." else "All supply lines healthy. Ready for high POS output.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Button(
                        onClick = { if (lowStockProducts.isNotEmpty()) onGotoInventory() else onGotoPos() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (lowStockProducts.isNotEmpty()) Color(0xFFF44336) else MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (lowStockProducts.isNotEmpty()) "Replenish" else "POS Desk",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryStatNode(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

// Breathtaking custom Canvas drawing graph complying with Design Guidelines!
@Composable
fun CashFlowCanvasChart(
    revenueValue: Double,
    expenseValue: Double
) {
    val textPaintColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val revenueLineColor = Color(0xFF2E7D32)
    val expenseLineColor = Color(0xFFC62828)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        val width = size.width
        val height = size.height

        // Draw horizontal background grids
        val gridLinesCount = 4
        for (i in 0..gridLinesCount) {
            val yPos = (height / gridLinesCount) * i
            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(0f, yPos),
                end = Offset(width, yPos),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Plot Revenue Path
        val revenuePath = Path().apply {
            moveTo(0f, height * 0.8f)
            quadraticTo(width * 0.25f, height * 0.7f, width * 0.5f, height * 0.3f)
            quadraticTo(width * 0.75f, height * 0.4f, width, (height * 0.1f) - (revenueValue.toFloat() * 0.01f).coerceAtMost(height * 0.1f))
        }

        // Plot Expense Path
        val expensePath = Path().apply {
            moveTo(0f, height * 0.6f)
            quadraticTo(width * 0.3f, height * 0.5f, width * 0.6f, height * 0.7f)
            quadraticTo(width * 0.8f, height * 0.4f, width, (height * 0.5f) + (expenseValue.toFloat() * 0.005f).coerceAtMost(height * 0.2f))
        }

        // Draw Revenue Curve Shadow Gradient
        val shadowRevenuePath = Path().apply {
            addPath(revenuePath)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = shadowRevenuePath,
            brush = Brush.verticalGradient(
                colors = listOf(revenueLineColor.copy(alpha = 0.21f), Color.Transparent)
            )
        )

        // Render line bounds
        drawPath(
            path = revenuePath,
            color = revenueLineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        drawPath(
            path = expensePath,
            color = expenseLineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw indicator nodes
        drawCircle(
            color = revenueLineColor,
            radius = 5.dp.toPx(),
            center = Offset(width, (height * 0.1f) - (revenueValue.toFloat() * 0.01f).coerceAtMost(height * 0.1f))
        )
        drawCircle(
            color = expenseLineColor,
            radius = 5.dp.toPx(),
            center = Offset(width, (height * 0.5f) + (expenseValue.toFloat() * 0.005f).coerceAtMost(height * 0.2f))
        )
    }
}

// Extension to map Color to ARGB
fun Color.toArgb(): Int {
    return android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}
