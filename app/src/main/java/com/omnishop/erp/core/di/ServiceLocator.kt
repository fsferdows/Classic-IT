package com.omnishop.erp.core.di

import android.content.Context
import com.omnishop.erp.core.data.local.AppDatabase
import com.omnishop.erp.core.data.local.PreferencesManager
import com.omnishop.erp.core.data.repository.ErpRepository

object ServiceLocator {
    private var database: AppDatabase? = null
    private var preferencesManager: PreferencesManager? = null
    private var erpRepository: ErpRepository? = null

    fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val db = AppDatabase.getDatabase(context)
            database = db
            db
        }
    }

    fun getPreferencesManager(context: Context): PreferencesManager {
        return preferencesManager ?: synchronized(this) {
            val pm = PreferencesManager(context)
            preferencesManager = pm
            pm
        }
    }

    fun getErpRepository(context: Context): ErpRepository {
        return erpRepository ?: synchronized(this) {
            val db = getDatabase(context)
            val pm = getPreferencesManager(context)
            val repo = ErpRepository(db, pm)
            erpRepository = repo
            repo
        }
    }
}
