package com.omnishop.erp.core.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "omnishop_erp_secure_prefs",
        Context.MODE_PRIVATE
    )

    private val _selectedShopId = MutableStateFlow(getSelectedShopId())
    val selectedShopIdState: StateFlow<String> = _selectedShopId

    fun getSelectedShopId(): String {
        return prefs.getString(KEY_SELECTED_SHOP_ID, "") ?: ""
    }

    fun setSelectedShopId(shopId: String) {
        prefs.edit().putString(KEY_SELECTED_SHOP_ID, shopId).apply()
        _selectedShopId.value = shopId
    }

    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun getUserEmail(): String {
        return prefs.getString(KEY_USER_EMAIL, "") ?: ""
    }

    fun setUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "Root Owner") ?: "Root Owner"
    }

    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun getUserRole(): String {
        return prefs.getString(KEY_USER_ROLE, "Owner") ?: "Owner"
    }

    fun setUserRole(role: String) {
        prefs.edit().putString(KEY_USER_ROLE, role).apply()
    }

    companion object {
        private const val KEY_SELECTED_SHOP_ID = "selected_shop_id"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
    }
}
