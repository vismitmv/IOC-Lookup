package com.example.ioclookup.ui.settings

import androidx.lifecycle.ViewModel
import com.example.ioclookup.data.security.SecurePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

import androidx.lifecycle.viewModelScope
import com.example.ioclookup.data.local.dao.CustomFeedDao
import com.example.ioclookup.data.local.entity.CustomFeedEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val vtApiKey: String = "",
    val abuseApiKey: String = "",
    val shodanApiKey: String = "",
    val otxApiKey: String = "",
    val vtEnabled: Boolean = true,
    val abuseEnabled: Boolean = true,
    val shodanEnabled: Boolean = true,
    val otxEnabled: Boolean = true,
    val cacheTtlHours: Int = 24,
    val theme: String = "system"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: SecurePreferences,
    private val customFeedDao: CustomFeedDao
) : ViewModel() {

    private val _state = MutableStateFlow(loadFromPrefs())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    val customFeeds: StateFlow<List<CustomFeedEntity>> = customFeedDao.getAllFeedsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun loadFromPrefs() = SettingsUiState(
        vtApiKey = prefs.vtApiKey,
        abuseApiKey = prefs.abuseApiKey,
        shodanApiKey = prefs.shodanApiKey,
        otxApiKey = prefs.otxApiKey,
        vtEnabled = prefs.vtEnabled,
        abuseEnabled = prefs.abuseEnabled,
        shodanEnabled = prefs.shodanEnabled,
        otxEnabled = prefs.otxEnabled,
        cacheTtlHours = prefs.cacheTtlHours,
        theme = prefs.theme
    )

    fun setVtKey(key: String) { prefs.vtApiKey = key; _state.update { it.copy(vtApiKey = key) } }
    fun setAbuseKey(key: String) { prefs.abuseApiKey = key; _state.update { it.copy(abuseApiKey = key) } }
    fun setShodanKey(key: String) { prefs.shodanApiKey = key; _state.update { it.copy(shodanApiKey = key) } }
    fun setOtxKey(key: String) { prefs.otxApiKey = key; _state.update { it.copy(otxApiKey = key) } }

    fun setVtEnabled(v: Boolean) { prefs.vtEnabled = v; _state.update { it.copy(vtEnabled = v) } }
    fun setAbuseEnabled(v: Boolean) { prefs.abuseEnabled = v; _state.update { it.copy(abuseEnabled = v) } }
    fun setShodanEnabled(v: Boolean) { prefs.shodanEnabled = v; _state.update { it.copy(shodanEnabled = v) } }
    fun setOtxEnabled(v: Boolean) { prefs.otxEnabled = v; _state.update { it.copy(otxEnabled = v) } }

    fun setCacheTtl(hours: Int) { prefs.cacheTtlHours = hours; _state.update { it.copy(cacheTtlHours = hours) } }
    fun setTheme(theme: String) { prefs.theme = theme; _state.update { it.copy(theme = theme) } }

    fun addCustomFeed(name: String, urlTemplate: String, headerName: String?, headerValue: String?, jsonPath: String) {
        viewModelScope.launch {
            customFeedDao.insertFeed(
                CustomFeedEntity(
                    name = name,
                    urlTemplate = urlTemplate,
                    headerName = headerName?.ifBlank { null },
                    headerValue = headerValue?.ifBlank { null },
                    jsonPathMalicious = jsonPath.ifBlank { "malicious" },
                    isEnabled = true
                )
            )
        }
    }

    fun toggleCustomFeed(feed: CustomFeedEntity) {
        viewModelScope.launch {
            customFeedDao.updateFeed(feed.copy(isEnabled = !feed.isEnabled))
        }
    }

    fun deleteCustomFeed(feed: CustomFeedEntity) {
        viewModelScope.launch {
            customFeedDao.deleteFeed(feed)
        }
    }
}
