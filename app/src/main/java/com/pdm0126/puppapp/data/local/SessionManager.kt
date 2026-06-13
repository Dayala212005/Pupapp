package com.pdm0126.puppapp.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("pupapp_prefs", Context.MODE_PRIVATE)

    var isLoggedIn by mutableStateOf(getAccessToken() != null)
        private set

    fun saveSession(accessToken: String, refreshToken: String, businessDisplayName: String) {
        prefs.edit().apply {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            putString("business_display_name", businessDisplayName)
            apply()
        }
        isLoggedIn = true
    }

    fun updateTokens(accessToken: String, refreshToken: String) {
        prefs.edit().apply {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            apply()
        }
    }

    fun getAccessToken(): String? = prefs.getString("access_token", null)

    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)

    fun getBusinessDisplayName(): String? = prefs.getString("business_display_name", null)

    fun clearSession() {
        prefs.edit().clear().apply()
        isLoggedIn = false
    }
}
