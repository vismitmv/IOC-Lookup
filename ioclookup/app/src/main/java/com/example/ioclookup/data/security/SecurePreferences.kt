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

        const val DEFAULT_CACHE_TTL_HOURS = 24
        const val DEFAULT_THEME = "system"
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
}
