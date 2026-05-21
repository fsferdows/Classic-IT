package com.omnishop.erp.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ShopEntity::class,
        ProductEntity::class,
        CustomerEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        JournalEntity::class,
        StaffEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shopDao(): ShopDao
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun saleDao(): SaleDao
    abstract fun journalDao(): JournalDao
    abstract fun staffDao(): StaffDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "omnishop_erp_db"
                )
                // Note: SQLCipher can be incorporated here seamlessly via standard SafeHelperFactory.
                // For direct stability and ease of integration under current sandboxed Kotlin,
                // we build a standard robust Room design that avoids native platform incompatibilities.
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
