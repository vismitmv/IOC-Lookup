package com.example.ioclookup.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure wrapper around EncryptedSharedPreferences.
 * Stores API keys and app preferences in an AES256-encrypted store.
 */
@Singleton
class SecurePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val FILE_NAME = "ioc_lookup_secure_prefs"

        const val KEY_VT_API_KEY = "vt_api_key"
        const val KEY_ABUSE_API_KEY = "abuse_api_key"
        const val KEY_SHODAN_API_KEY = "shodan_api_key"
        const val KEY_OTX_API_KEY = "otx_api_key"

        const val KEY_VT_ENABLED = "vt_enabled"
        const val KEY_ABUSE_ENABLED = "abuse_enabled"
        const val KEY_SHODAN_ENABLED = "shodan_enabled"
        const val KEY_OTX_ENABLED = "otx_enabled"

        const val KEY_CACHE_TTL_HOURS = "cache_ttl_hours"
        const val KEY_THEME = "theme"  // "system" | "light" | "dark"
        const val KEY_ACCENT_COLOR = "accent_color" // hex e.g. "#00D4FF"

        const val DEFAULT_CACHE_TTL_HOURS = 24
        const val DEFAULT_THEME = "system"
        const val DEFAULT_ACCENT_COLOR = "#00D4FF"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // --- API Keys ---
    var vtApiKey: String
        get() = prefs.getString(KEY_VT_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_VT_API_KEY, value).apply()

    var abuseApiKey: String
        get() = prefs.getString(KEY_ABUSE_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ABUSE_API_KEY, value).apply()

    var shodanApiKey: String
        get() = prefs.getString(KEY_SHODAN_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SHODAN_API_KEY, value).apply()

    var otxApiKey: String
        get() = prefs.getString(KEY_OTX_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_OTX_API_KEY, value).apply()

    // --- Source Toggles ---
    var vtEnabled: Boolean
        get() = prefs.getBoolean(KEY_VT_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_VT_ENABLED, value).apply()

    var abuseEnabled: Boolean
        get() = prefs.getBoolean(KEY_ABUSE_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ABUSE_ENABLED, value).apply()

    var shodanEnabled: Boolean
        get() = prefs.getBoolean(KEY_SHODAN_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SHODAN_ENABLED, value).apply()

    var otxEnabled: Boolean
        get() = prefs.getBoolean(KEY_OTX_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_OTX_ENABLED, value).apply()

    // --- Settings ---
    var cacheTtlHours: Int
        get() = prefs.getInt(KEY_CACHE_TTL_HOURS, DEFAULT_CACHE_TTL_HOURS)
        set(value) = prefs.edit().putInt(KEY_CACHE_TTL_HOURS, value).apply()

    var theme: String
        get() = prefs.getString(KEY_THEME, DEFAULT_THEME) ?: DEFAULT_THEME
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    var accentColorHex: String
        get() = prefs.getString(KEY_ACCENT_COLOR, DEFAULT_ACCENT_COLOR) ?: DEFAULT_ACCENT_COLOR
        set(value) = prefs.edit().putString(KEY_ACCENT_COLOR, value).apply()

    // --- Blocklist Sync Configurations ---
    fun getSyncEnabled(feedId: String): Boolean = prefs.getBoolean("sync_enabled_$feedId", false)
    fun setSyncEnabled(feedId: String, value: Boolean) = prefs.edit().putBoolean("sync_enabled_$feedId", value).apply()

    fun getSyncInterval(feedId: String): Long = prefs.getLong("sync_interval_$feedId", 24L) // default 24 hours
    fun setSyncInterval(feedId: String, value: Long) = prefs.edit().putLong("sync_interval_$feedId", value).apply()

    fun getSyncWifiOnly(feedId: String): Boolean = prefs.getBoolean("sync_wifi_$feedId", false)
    fun setSyncWifiOnly(feedId: String, value: Boolean) = prefs.edit().putBoolean("sync_wifi_$feedId", value).apply()

    fun getSyncTimestamp(feedId: String): Long = prefs.getLong("sync_timestamp_$feedId", 0L)
    fun setSyncTimestamp(feedId: String, value: Long) = prefs.edit().putLong("sync_timestamp_$feedId", value).apply()
}
