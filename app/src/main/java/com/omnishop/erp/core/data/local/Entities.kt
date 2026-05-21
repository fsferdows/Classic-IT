package com.omnishop.erp.core.data.local

import androidx.room.*

@Entity(tableName = "shops")
data class ShopEntity(
    @PrimaryKey val id: String,
    val name: String,
    val businessType: String,
    val primaryColor: String, // Hex string
    val secondColor: String, // Hex string
    val logoUrl: String,
    val receiptFooter: String,
    val invoiceTemplate: String, // "STANDARD" | "MINIMAL" | "THERMAL"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = ShopEntity::class,
            parentColumns = ["id"],
            childColumns = ["shopId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["shopId"]), Index(value = ["barcode"])]
)
data class ProductEntity(
    @PrimaryKey val id: String,
    val shopId: String,
    val name: String,
    val sku: String,
    val barcode: String,
    val price: Double,
    val purchaseCost: Double,
    val stockQty: Double,
    val lowStockThreshold: Double,
    val category: String,
    val expiryTimestamp: Long? = null,
    val isDeleted: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "customers",
    foreignKeys = [
        ForeignKey(
            entity = ShopEntity::class,
            parentColumns = ["id"],
            childColumns = ["shopId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["shopId"])]
)
data class CustomerEntity(
    @PrimaryKey val id: String,
    val shopId: String,
    val name: String,
    val phone: String,
    val email: String,
    val creditLimit: Double,
    val outstandingBalance: Double,
    val loyaltyPoints: Int = 0,
    val address: String = ""
)

@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = ShopEntity::class,
            parentColumns = ["id"],
            childColumns = ["shopId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["shopId"])]
)
data class SaleEntity(
    @PrimaryKey val id: String,
    val shopId: String,
    val invoiceNo: String,
    val customerId: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val subTotal: Double,
    val taxAmount: Double,
    val discountAmount: Double,
    val totalAmount: Double,
    val paymentMethod: String, // "CASH" | "CARD" | "UPI" | "SPLIT"
    val cashPaid: Double,
    val cardPaid: Double,
    val upiPaid: Double,
    val isSynced: Boolean = false,
    val cashierName: String
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["saleId"])]
)
data class SaleItemEntity(
    @PrimaryKey val id: String,
    val saleId: String,
    val productId: String,
    val productName: String,
    val price: Double,
    val quantity: Double,
    val totalAmount: Double
)

@Entity(
    tableName = "journal_entries",
    foreignKeys = [
        ForeignKey(
            entity = ShopEntity::class,
            parentColumns = ["id"],
            childColumns = ["shopId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["shopId"])]
)
data class JournalEntity(
    @PrimaryKey val id: String,
    val shopId: String,
    val date: Long = System.currentTimeMillis(),
    val description: String,
    val accountFrom: String, // Account title credited
    val accountTo: String,   // Account title debited
    val amount: Double,
    val category: String     // "EXPENSE" | "REVENUE" | "ASSET" | "EQUITY" | "LIABILITY"
)

@Entity(
    tableName = "staff",
    foreignKeys = [
        ForeignKey(
            entity = ShopEntity::class,
            parentColumns = ["id"],
            childColumns = ["shopId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["shopId"])]
)
data class StaffEntity(
    @PrimaryKey val id: String,
    val shopId: String,
    val name: String,
    val email: String,
    val role: String, // "Owner" | "Manager" | "Cashier" | "Accountant" | "InventoryManager"
    val phoneNumber: String,
    val permissions: String // Comma separated permissions
)
