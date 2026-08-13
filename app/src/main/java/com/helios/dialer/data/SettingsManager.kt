package com.helios.dialer.data


import android.content.Context

object SettingsManager {
    private const val PREFS_NAME = "helios_settings"

    fun getBoolean(context: Context, key: String, default: Boolean = false): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(key, default)
    }

    fun setBoolean(context: Context, key: String, value: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(key, value).apply()
    }
}
