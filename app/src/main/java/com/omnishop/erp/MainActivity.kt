package com.omnishop.erp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.omnishop.erp.core.di.ServiceLocator
import com.omnishop.erp.core.di.ViewModelFactory
import com.omnishop.erp.core.ui.theme.OmniShopTheme
import com.omnishop.erp.features.accounting.AccountingViewModel
import com.omnishop.erp.features.auth.AuthViewModel
import com.omnishop.erp.features.auth.LoginScreen
import com.omnishop.erp.features.auth.ShopOnboardingScreen
import com.omnishop.erp.features.dashboard.DashboardMainLayout
import com.omnishop.erp.features.dashboard.DashboardViewModel
import com.omnishop.erp.features.inventory.InventoryViewModel
import com.omnishop.erp.features.pos.PosViewModel
import com.omnishop.erp.features.staff.StaffViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Fetch core repos and helpers from the clean Dependency Injection ServiceLocator
        val repository = ServiceLocator.getErpRepository(this)
        val prefs = ServiceLocator.getPreferencesManager(this)

        // ViewModel proper lifecycle-aware instantiation via Factory
        val factory = ViewModelFactory(repository, prefs)
        val authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
        val dashboardViewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]
        val posViewModel = ViewModelProvider(this, factory)[PosViewModel::class.java]
        val inventoryViewModel = ViewModelProvider(this, factory)[InventoryViewModel::class.java]
        val accountingViewModel = ViewModelProvider(this, factory)[AccountingViewModel::class.java]
        val staffViewModel = ViewModelProvider(this, factory)[StaffViewModel::class.java]

        setContent {
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
            val activeShop by dashboardViewModel.activeShop.collectAsState()
            
            // Dynamic White labeling brand accent hex extraction
            val brandHex = activeShop?.primaryColor ?: ""

            var navigationState by remember { mutableStateOf("MAIN") }

            OmniShopTheme(shopPrimaryHex = brandHex) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val ignoredPadding = innerPadding // Scaffold is full bleed for top bar edge-to-edge
                    
                    if (!isLoggedIn) {
                        if (navigationState == "ONBOARD_WIZARD") {
                            ShopOnboardingScreen(
                                authViewModel = authViewModel,
                                onComplete = { shopId ->
                                    navigationState = "MAIN"
                                    // Log user into newly generated tenant session automatically
                                    authViewModel.login("admin@omnishop.com", "1234")
                                }
                            )
                        } else {
                            LoginScreen(
                                authViewModel = authViewModel,
                                onOnboardNewShopClick = { navigationState = "ONBOARD_WIZARD" }
                            )
                        }
                    } else if (navigationState == "ONBOARD_WIZARD") {
                        ShopOnboardingScreen(
                            authViewModel = authViewModel,
                            onComplete = { shopId ->
                                navigationState = "MAIN"
                            }
                        )
                    } else {
                        val role by authViewModel.currentUserRole.collectAsState()
                        val name by authViewModel.currentUserName.collectAsState()

                        DashboardMainLayout(
                            viewModel = dashboardViewModel,
                            posViewModel = posViewModel,
                            inventoryViewModel = inventoryViewModel,
                            accountingViewModel = accountingViewModel,
                            staffViewModel = staffViewModel,
                            currentUserRole = role,
                            currentUserName = name,
                            onOnboardNewShopClick = { navigationState = "ONBOARD_WIZARD" },
                            onLogoutClick = { authViewModel.logout() }
                        )
                    }
                }
            }
        }
    }
}
