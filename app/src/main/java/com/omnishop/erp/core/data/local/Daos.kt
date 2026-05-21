package com.omnishop.erp.core.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {
    @Query("SELECT * FROM shops")
    fun getAllShops(): Flow<List<ShopEntity>>

    @Query("SELECT * FROM shops WHERE id = :id LIMIT 1")
    suspend fun getShopById(id: String): ShopEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShop(shop: ShopEntity)

    @Delete
    suspend fun deleteShop(shop: ShopEntity)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE shopId = :shopId AND isDeleted = 0")
    fun getProductsByShop(shopId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE shopId = :shopId AND isDeleted = 0 AND stockQty <= lowStockThreshold")
    fun getLowStockProducts(shopId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE shopId = :shopId AND id = :id LIMIT 1")
    suspend fun getProductById(shopId: String, id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE shopId = :shopId AND barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(shopId: String, barcode: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("UPDATE products SET stockQty = :newStock, lastUpdated = :timestamp WHERE shopId = :shopId AND id = :id")
    suspend fun updateStock(shopId: String, id: String, newStock: Double, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE products SET isDeleted = 1, lastUpdated = :timestamp WHERE shopId = :shopId AND id = :id")
    suspend fun deleteProductLogically(shopId: String, id: String, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE shopId = :shopId")
    fun getCustomersByShop(shopId: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE shopId = :shopId AND id = :id LIMIT 1")
    suspend fun getCustomerById(shopId: String, id: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET outstandingBalance = :newBalance WHERE shopId = :shopId AND id = :id")
    suspend fun updateOutstandingBalance(shopId: String, id: String, newBalance: Double)
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales WHERE shopId = :shopId ORDER BY timestamp DESC")
    fun getSalesByShop(shopId: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE shopId = :shopId AND id = :id LIMIT 1")
    suspend fun getSaleById(shopId: String, id: String): SaleEntity?

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getItemsForSale(saleId: String): List<SaleItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Transaction
    suspend fun insertCompleteSale(sale: SaleEntity, items: List<SaleItemEntity>) {
        insertSale(sale)
        insertSaleItems(items)
    }

    @Query("SELECT SUM(totalAmount) FROM sales WHERE shopId = :shopId")
    fun getTotalSalesAmount(shopId: String): Flow<Double?>

    @Query("SELECT COUNT(*) FROM sales WHERE shopId = :shopId")
    fun getSalesCount(shopId: String): Flow<Int>
}

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries WHERE shopId = :shopId ORDER BY date DESC")
    fun getJournalEntries(shopId: String): Flow<List<JournalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntity)

    @Query("SELECT SUM(amount) FROM journal_entries WHERE shopId = :shopId AND category = :category")
    fun getTotalAmountByCategory(shopId: String, category: String): Flow<Double?>
}

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff WHERE shopId = :shopId")
    fun getStaffByShop(shopId: String): Flow<List<StaffEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffEntity)

    @Query("DELETE FROM staff WHERE shopId = :shopId AND id = :id")
    suspend fun deleteStaff(shopId: String, id: String)
}
