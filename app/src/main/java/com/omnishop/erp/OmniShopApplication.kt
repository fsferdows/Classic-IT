package com.omnishop.erp

import android.app.Application
import android.util.Log
import com.omnishop.erp.core.data.local.*
import com.omnishop.erp.core.di.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class OmniShopApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("OmniShopApplication", "OmniShopApplication is initializing...")
        
        // Warm up and pre-populate local sqlite room database with diverse tenants
        val repo = ServiceLocator.getErpRepository(this)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if any shops exist; if empty, pre-populate
                val db = ServiceLocator.getDatabase(this@OmniShopApplication)
                val existingShops = db.shopDao().getShopById("shop_1")
                if (existingShops == null) {
                    populateDefaultSeedData(db)
                }
            } catch (e: Exception) {
                Log.e("OmniShopApplication", "Error seeding initial application demo databases", e)
            }
        }
    }

    private suspend fun populateDefaultSeedData(db: AppDatabase) {
        // Shop 1: Classic IT Solutions (IT Services & Enterprise Hardware)
        val shop1Id = "shop_1"
        val shop1 = ShopEntity(
            id = shop1Id,
            name = "Classic IT Solutions",
            businessType = "Classic IT Services & Hardware",
            primaryColor = "#1565C0", // IT Royal Sapphire Blue
            secondColor = "#0D47A1", // Dark Cobalt
            logoUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3",
            receiptFooter = "Thank you for partnering with Classic IT Solutions. High-performance infrastructure and tech support. support@classic-it.com",
            invoiceTemplate = "STANDARD"
        )
        db.shopDao().insertShop(shop1)

        // Add Staff for Classic IT
        db.staffDao().insertStaff(
            StaffEntity(
                id = "staff_1",
                shopId = shop1Id,
                name = "Alex Mercer",
                email = "alex.mercer@classic-it.com",
                role = "Manager",
                phoneNumber = "+1-555-0192-IT",
                permissions = "POS,INVENTORY,ACCOUNTING,STAFF"
            )
        )
        db.staffDao().insertStaff(
            StaffEntity(
                id = "staff_2",
                shopId = shop1Id,
                name = "Sarah Connor",
                email = "sarah.c@classic-it.com",
                role = "Cashier",
                phoneNumber = "+1-555-0144-IT",
                permissions = "POS"
            )
        )

        // Add Real-World IT Hardware Products & Consultation Services
        db.productDao().insertProducts(
            listOf(
                ProductEntity(
                    id = "p1",
                    shopId = shop1Id,
                    name = "Enterprise Server Rack Deployment",
                    sku = "SRV-RK-XP9",
                    barcode = "9780201379624",
                    price = 1499.00,
                    purchaseCost = 450.00,
                    stockQty = 35.0,
                    lowStockThreshold = 5.0,
                    category = "IT Services"
                ),
                ProductEntity(
                    id = "p2",
                    shopId = shop1Id,
                    name = "Full Cyber Security Vulnerability Audit",
                    sku = "CYBER-AUDIT-01",
                    barcode = "4901301236523",
                    price = 2499.99,
                    purchaseCost = 150.00,
                    stockQty = 120.0,
                    lowStockThreshold = 10.0,
                    category = "IT Consulting"
                ),
                ProductEntity(
                    id = "p3",
                    shopId = shop1Id,
                    name = "Managed 24-Port PoEt Gigabit Switch",
                    sku = "NET-SW-24P",
                    barcode = "8806085994246",
                    price = 349.50,
                    purchaseCost = 175.00,
                    stockQty = 4.0, // Trigger low stock!
                    lowStockThreshold = 6.0,
                    category = "Networking Hardware"
                ),
                ProductEntity(
                    id = "p4",
                    shopId = shop1Id,
                    name = "1TB NVMe SSD Upgrade Kit (DDR5 Compatible)",
                    sku = "SSD-NVME-1T",
                    barcode = "6901028076623",
                    price = 129.00,
                    purchaseCost = 55.00,
                    stockQty = 25.0,
                    lowStockThreshold = 5.0,
                    category = "Computer Components"
                )
            )
        )

        // Seed Customers for Tokyo Tech
        db.customerDao().insertCustomer(
            CustomerEntity(
                id = "cust_1",
                shopId = shop1Id,
                name = "Haruto Takahashi",
                phone = "+81-80-6663-8821",
                email = "haruto.t@gmail.com",
                creditLimit = 1500.00,
                outstandingBalance = 350.00,
                loyaltyPoints = 280,
                address = "Shibuya 2-Chome, Tokyo"
            )
        )
        db.customerDao().insertCustomer(
            CustomerEntity(
                id = "cust_2",
                shopId = shop1Id,
                name = "Ami Watanabe",
                phone = "+81-80-4441-2910",
                email = "ami.w@outlook.com",
                creditLimit = 500.00,
                outstandingBalance = 0.00,
                loyaltyPoints = 45,
                address = "Minato-ku, Tokyo"
            )
        )

        // Shop 2: Paris Fashion Boutique (Clothing & Luxury)
        val shop2Id = "shop_2"
        val shop2 = ShopEntity(
            id = shop2Id,
            name = "Parisian Chic Boutique",
            businessType = "Apparel & Luxury Fashion",
            primaryColor = "#EC407A", // Pink Rose Accent
            secondColor = "#1D1D2C", // Royal Space Black
            logoUrl = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3",
            receiptFooter = "Merci pour votre visite! Thank you for shopping with Parisian Chic Boutique. Live beautifully.",
            invoiceTemplate = "MINIMAL"
        )
        db.shopDao().insertShop(shop2)

        // Add Products for Paris Chic
        db.productDao().insertProducts(
            listOf(
                ProductEntity(
                    id = "p20",
                    shopId = shop2Id,
                    name = "Cashmere Classic Overcoat",
                    sku = "CASH-OC-88",
                    barcode = "3011360057018",
                    price = 450.00,
                    purchaseCost = 210.00,
                    stockQty = 15.0,
                    lowStockThreshold = 3.0,
                    category = "Winterwear"
                ),
                ProductEntity(
                    id = "p21",
                    shopId = shop2Id,
                    name = "Silk Scarves of Provence",
                    sku = "SILK-S-PRV",
                    barcode = "3232323232323",
                    price = 85.00,
                    purchaseCost = 30.00,
                    stockQty = 35.0,
                    lowStockThreshold = 10.0,
                    category = "Accessories"
                ),
                ProductEntity(
                    id = "p22",
                    shopId = shop2Id,
                    name = "Leather Crossbody Handbag",
                    sku = "LTH-CRB-01",
                    barcode = "3453453453456",
                    price = 280.00,
                    purchaseCost = 120.00,
                    stockQty = 2.0, // Low stock trigger
                    lowStockThreshold = 4.0,
                    category = "Leather Goods"
                )
            )
        )

        // Seed Customers for Paris Chic
        db.customerDao().insertCustomer(
            CustomerEntity(
                id = "cust_10",
                shopId = shop2Id,
                name = "Sophie Laurent",
                phone = "+33-6-5555-1200",
                email = "sophie.laurent@paris.fr",
                creditLimit = 3000.00,
                outstandingBalance = 0.00,
                loyaltyPoints = 1200,
                address = "Rue de Rivoli, Paris"
            )
        )

        // Seed some accounting entries to kickstart the statistics!
        db.journalDao().insertJournalEntry(
            JournalEntity(
                id = UUID.randomUUID().toString(),
                shopId = shop1Id,
                date = System.currentTimeMillis() - 86400000 * 2, // 2 days ago
                description = "Shop onboarding inventory purchase",
                accountFrom = "Cash Capital Reserves",
                accountTo = "Inventory Asset Account",
                amount = 2500.0,
                category = "EXPENSE"
            )
        )
        db.journalDao().insertJournalEntry(
            JournalEntity(
                id = UUID.randomUUID().toString(),
                shopId = shop1Id,
                date = System.currentTimeMillis() - 86400000, // 1 day ago
                description = "Starter sale - client receipt",
                accountFrom = "Sales Revenue Account",
                accountTo = "Cash Drawer Desk",
                amount = 1200.0,
                category = "REVENUE"
            )
        )
    }
}
