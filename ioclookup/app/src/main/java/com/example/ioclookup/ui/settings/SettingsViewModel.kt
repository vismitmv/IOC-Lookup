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
import com.example.ioclookup.data.local.dao.BlocklistDao
import com.example.ioclookup.data.local.entity.BlocklistFeedEntity
import com.example.ioclookup.data.repository.BlocklistRepository
import com.example.ioclookup.domain.SyncScheduler
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
    val theme: String = "system",
    val accentColorHex: String = "#00D4FF"
)

data class FeedSyncState(
    val autoSyncEnabled: Boolean,
    val syncIntervalHours: Long,
    val wifiOnly: Boolean,
    val lastSyncedTimestamp: Long
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: SecurePreferences,
    private val customFeedDao: CustomFeedDao,
    private val blocklistDao: BlocklistDao,
    private val blocklistRepository: BlocklistRepository,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(loadFromPrefs())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    private val _feedSyncStates = MutableStateFlow<Map<String, FeedSyncState>>(emptyMap())
    val feedSyncStates: StateFlow<Map<String, FeedSyncState>> = _feedSyncStates.asStateFlow()

    val customFeeds: StateFlow<List<CustomFeedEntity>> = customFeedDao.getAllFeedsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blocklistFeeds: StateFlow<List<BlocklistFeedEntity>> = blocklistDao.getAllFeedsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            blocklistFeeds.collect { feeds ->
                val states = feeds.associate { feed ->
                    val idStr = feed.id.toString()
                    idStr to FeedSyncState(
                        autoSyncEnabled = prefs.getSyncEnabled(idStr),
                        syncIntervalHours = prefs.getSyncInterval(idStr),
                        wifiOnly = prefs.getSyncWifiOnly(idStr),
                        lastSyncedTimestamp = prefs.getSyncTimestamp(idStr)
                    )
                }
                _feedSyncStates.value = states
            }
        }
    }

    private val _isSyncingBlocklists = MutableStateFlow(false)
    val isSyncingBlocklists: StateFlow<Boolean> = _isSyncingBlocklists.asStateFlow()

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
        theme = prefs.theme,
        accentColorHex = prefs.accentColorHex
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
    fun setAccentColor(hex: String) { prefs.accentColorHex = hex; _state.update { it.copy(accentColorHex = hex) } }

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

    // ── Blocklists (Plain-Text / Sophos Style) ─────────────────────────────────

    fun addBlocklistFeed(name: String, url: String) {
        viewModelScope.launch {
            val feedId = blocklistDao.insertFeed(
                BlocklistFeedEntity(
                    name = name,
                    feedUrl = url,
                    isEnabled = true
                )
            )
            val inserted = BlocklistFeedEntity(id = feedId, name = name, feedUrl = url, isEnabled = true)
            syncBlocklistFeed(inserted)
        }
    }

    fun syncBlocklistFeed(feed: BlocklistFeedEntity) {
        viewModelScope.launch {
            _isSyncingBlocklists.value = true
            blocklistRepository.syncFeed(feed)
            _isSyncingBlocklists.value = false
        }
    }

    fun syncAllBlocklists() {
        viewModelScope.launch {
            _isSyncingBlocklists.value = true
            blocklistRepository.syncAllFeeds()
            _isSyncingBlocklists.value = false
        }
    }

    fun toggleBlocklistFeed(feed: BlocklistFeedEntity) {
        viewModelScope.launch {
            blocklistDao.updateFeed(feed.copy(isEnabled = !feed.isEnabled))
        }
    }

    fun deleteBlocklistFeed(feed: BlocklistFeedEntity) {
        viewModelScope.launch {
            blocklistDao.deleteFeed(feed)
            syncScheduler.cancel(feed.id.toString())
            blocklistDao.deleteEntriesForFeed(feed.id)
        }
    }

    fun setAutoSyncEnabled(feedId: String, feedUrl: String, enabled: Boolean) {
        prefs.setSyncEnabled(feedId, enabled)
        updateFeedSyncState(feedId) { it.copy(autoSyncEnabled = enabled) }
        
        if (enabled) {
            val interval = prefs.getSyncInterval(feedId)
            val wifiOnly = prefs.getSyncWifiOnly(feedId)
            syncScheduler.schedule(feedId, feedUrl, interval, wifiOnly)
        } else {
            syncScheduler.cancel(feedId)
        }
    }

    fun setSyncInterval(feedId: String, feedUrl: String, hours: Long) {
        prefs.setSyncInterval(feedId, hours)
        updateFeedSyncState(feedId) { it.copy(syncIntervalHours = hours) }
        
        if (prefs.getSyncEnabled(feedId)) {
            val wifiOnly = prefs.getSyncWifiOnly(feedId)
            syncScheduler.schedule(feedId, feedUrl, hours, wifiOnly)
        }
    }

    fun setSyncWifiOnly(feedId: String, feedUrl: String, wifiOnly: Boolean) {
        prefs.setSyncWifiOnly(feedId, wifiOnly)
        updateFeedSyncState(feedId) { it.copy(wifiOnly = wifiOnly) }
        
        if (prefs.getSyncEnabled(feedId)) {
            val interval = prefs.getSyncInterval(feedId)
            syncScheduler.schedule(feedId, feedUrl, interval, wifiOnly)
        }
    }

    fun syncImmediate(feedId: String, feedUrl: String) {
        syncScheduler.scheduleImmediate(feedId, feedUrl)
    }

    fun clearFeedEntries(feedId: Long) {
        viewModelScope.launch {
            blocklistDao.deleteEntriesForFeed(feedId)
            blocklistDao.getActiveFeeds().find { it.id == feedId }?.let {
                blocklistDao.updateFeed(it.copy(entryCount = 0))
            }
        }
    }

    private fun updateFeedSyncState(feedId: String, update: (FeedSyncState) -> FeedSyncState) {
        val current = _feedSyncStates.value
        val state = current[feedId] ?: return
        _feedSyncStates.value = current.toMutableMap().apply {
            put(feedId, update(state))
        }
    }
}
