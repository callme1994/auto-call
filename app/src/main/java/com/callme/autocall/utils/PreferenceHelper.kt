package com.callme.autocall.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper class for managing SharedPreferences
 */
class PreferenceHelper(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "auto_call_prefs"
        private const val KEY_API_URL = "api_url"
        private const val KEY_SERVICE_RUNNING = "service_running"
        private const val DEFAULT_API_URL = "http://example.com/api/phone"
    }

    /**
     * Get the configured API URL
     */
    fun getApiUrl(): String {
        return prefs.getString(KEY_API_URL, DEFAULT_API_URL) ?: DEFAULT_API_URL
    }

    /**
     * Set the API URL
     */
    fun setApiUrl(url: String) {
        prefs.edit().putString(KEY_API_URL, url).apply()
    }

    /**
     * Check if service is running
     */
    fun isServiceRunning(): Boolean {
        return prefs.getBoolean(KEY_SERVICE_RUNNING, false)
    }

    /**
     * Set service running state
     */
    fun setServiceRunning(running: Boolean) {
        prefs.edit().putBoolean(KEY_SERVICE_RUNNING, running).apply()
    }
}
