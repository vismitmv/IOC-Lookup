package com.example.ioclookup.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ioclookup.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onClearHistory: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val appColors = LocalAppColors.current

    Scaffold(
        containerColor = appColors.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = appColors.background,
                    titleContentColor = appColors.textPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // ── API Keys ───────────────────────────────────────────────────
            SettingsSection(title = "API Keys", icon = Icons.Filled.Key) {
                ApiKeyField(
                    label = "VirusTotal",
                    value = state.vtApiKey,
                    onValueChange = { viewModel.setVtKey(it) },
                    accentColor = VTBlueDark,
                    hint = "Get at virustotal.com"
                )
                Spacer(Modifier.height(10.dp))
                ApiKeyField(
                    label = "AbuseIPDB",
                    value = state.abuseApiKey,
                    onValueChange = { viewModel.setAbuseKey(it) },
                    accentColor = AbuseRed,
                    hint = "Get at abuseipdb.com"
                )
                Spacer(Modifier.height(10.dp))
                ApiKeyField(
                    label = "Shodan",
                    value = state.shodanApiKey,
                    onValueChange = { viewModel.setShodanKey(it) },
                    accentColor = ShodanGreen,
                    hint = "Get at shodan.io"
                )
                Spacer(Modifier.height(10.dp))
                ApiKeyField(
                    label = "AlienVault OTX",
                    value = state.otxApiKey,
                    onValueChange = { viewModel.setOtxKey(it) },
                    accentColor = OTXOrange,
                    hint = "Get at otx.alienvault.com"
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Source Toggles ─────────────────────────────────────────────
            SettingsSection(title = "Active Sources", icon = Icons.Filled.ToggleOn) {
                SourceToggle("VirusTotal", state.vtEnabled, VTBlueDark) { viewModel.setVtEnabled(it) }
                SourceToggle("AbuseIPDB", state.abuseEnabled, AbuseRed) { viewModel.setAbuseEnabled(it) }
                SourceToggle("Shodan", state.shodanEnabled, ShodanGreen) { viewModel.setShodanEnabled(it) }
                SourceToggle("AlienVault OTX", state.otxEnabled, OTXOrange) { viewModel.setOtxEnabled(it) }
            }

            Spacer(Modifier.height(16.dp))

            // ── Cache ─────────────────────────────────────────────────────
            SettingsSection(title = "Cache", icon = Icons.Filled.Timer) {
                Text(
                    "Cache TTL: ${state.cacheTtlHours} hour${if (state.cacheTtlHours != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = appColors.textPrimary
                )
                Slider(
                    value = state.cacheTtlHours.toFloat(),
                    onValueChange = { viewModel.setCacheTtl(it.toInt()) },
                    valueRange = 1f..72f,
                    steps = 70,
                    colors = SliderDefaults.colors(
                        thumbColor = appColors.accent,
                        activeTrackColor = appColors.accent,
                        inactiveTrackColor = appColors.divider
                    )
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("1h", style = MaterialTheme.typography.labelSmall, color = appColors.textMuted)
                    Text("72h", style = MaterialTheme.typography.labelSmall, color = appColors.textMuted)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Theme & Appearance ─────────────────────────────────────────
            SettingsSection(title = "Theme & Appearance", icon = Icons.Filled.Palette, accentColor = appColors.accent) {
                Text("Theme Mode", style = MaterialTheme.typography.labelSmall, color = appColors.textMuted)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("system" to "System", "light" to "Light", "dark" to "Dark").forEach { (key, label) ->
                        val selected = state.theme == key
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setTheme(key) },
                            label = { Text(label) },
                            leadingIcon = if (selected) {{ Icon(Icons.Filled.Check, null, modifier = Modifier.size(14.dp)) }} else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = appColors.accent.copy(alpha = 0.2f),
                                selectedLabelColor = appColors.accent
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("App Accent Color", style = MaterialTheme.typography.labelSmall, color = appColors.textMuted)
                Spacer(Modifier.height(8.dp))

                // Color Swatches
                val colorPresets = listOf(
                    "#00D4FF" to "Cyan",
                    "#00E676" to "Green",
                    "#9D4EDD" to "Purple",
                    "#FF1744" to "Red",
                    "#FF9800" to "Amber",
                    "#1E88E5" to "Blue"
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colorPresets.forEach { (hex, name) ->
                        val color = parseHexColor(hex)
                        val isSelected = state.accentColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 2.5.dp else 0.dp,
                                    color = if (isSelected) appColors.textPrimary else Color.Transparent,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                                .clickable { viewModel.setAccentColor(hex) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Filled.Check, contentDescription = name, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Custom Color Picker", style = MaterialTheme.typography.labelSmall, color = appColors.textMuted)
                Spacer(Modifier.height(8.dp))

                var hue by remember(state.accentColorHex) { 
                    mutableStateOf(
                        try {
                            val hsv = FloatArray(3)
                            android.graphics.Color.colorToHSV(android.graphics.Color.parseColor(state.accentColorHex), hsv)
                            hsv[0]
                        } catch(e: Exception) { 190f }
                    ) 
                }

                val rainbowBrush = remember {
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF0000), // Red
                            Color(0xFFFFFF00), // Yellow
                            Color(0xFF00FF00), // Green
                            Color(0xFF00FFFF), // Cyan
                            Color(0xFF0000FF), // Blue
                            Color(0xFFFF00FF), // Magenta
                            Color(0xFFFF0000)  // Red
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
                    // Rainbow Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .align(Alignment.Center)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                            .background(rainbowBrush)
                    )
                    
                    // Invisible slider over the track to handle thumb and gestures
                    Slider(
                        value = hue,
                        onValueChange = { 
                            hue = it 
                            val hex = String.format("#%06X", (0xFFFFFF and android.graphics.Color.HSVToColor(floatArrayOf(it, 1f, 1f))))
                            viewModel.setAccentColor(hex)
                        },
                        valueRange = 0f..360f,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.Transparent,
                            inactiveTrackColor = Color.Transparent,
                            activeTickColor = Color.Transparent,
                            inactiveTickColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Custom Threat Feeds ───────────────────────────────────────
            val blocklistFeeds by viewModel.blocklistFeeds.collectAsStateWithLifecycle()
            val isSyncingBlocklists by viewModel.isSyncingBlocklists.collectAsStateWithLifecycle()
            val feedSyncStates by viewModel.feedSyncStates.collectAsStateWithLifecycle()
            var showAddBlocklistDialog by remember { mutableStateOf(false) }

            SettingsSection(title = "Custom Threat Feeds", icon = Icons.Filled.RssFeed, accentColor = appColors.accent) {
                Text(
                    "Import plain-text threat lists (1 indicator per line) for instant offline matching.",
                    style = MaterialTheme.typography.labelMedium,
                    color = appColors.textMuted
                )
                Spacer(Modifier.height(10.dp))

                // Presets Buttons
                Text("Quick Add Presets:", style = MaterialTheme.typography.labelSmall, color = appColors.textMuted)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {
                            viewModel.addBlocklistFeed(
                                "URLhaus Text Online",
                                "https://urlhaus.abuse.ch/downloads/text_online/"
                            )
                        },
                        label = { Text("URLhaus Online URLs", color = appColors.textPrimary) },
                        leadingIcon = { Icon(Icons.Filled.FlashOn, null, Modifier.size(14.dp), tint = appColors.accent) }
                    )
                    AssistChip(
                        onClick = {
                            viewModel.addBlocklistFeed(
                                "BinaryDefense IP Feed",
                                "https://binarydefense.com/banlist.txt"
                            )
                        },
                        label = { Text("BinaryDefense IPs", color = appColors.textPrimary) },
                        leadingIcon = { Icon(Icons.Filled.FlashOn, null, Modifier.size(14.dp), tint = appColors.accent) }
                    )
                }

                Spacer(Modifier.height(12.dp))

                if (blocklistFeeds.isEmpty()) {
                    Text("No custom threat feeds configured.", style = MaterialTheme.typography.bodySmall, color = appColors.textMuted)
                } else {
                    blocklistFeeds.forEach { feed ->
                        val syncState = feedSyncStates[feed.id.toString()] ?: com.example.ioclookup.ui.settings.FeedSyncState(
                            autoSyncEnabled = false,
                            syncIntervalHours = 24L,
                            wifiOnly = false,
                            lastSyncedTimestamp = 0L
                        )

                        BlocklistFeedCard(
                            feed = feed,
                            syncState = syncState,
                            appColors = appColors,
                            isSyncing = isSyncingBlocklists,
                            onToggleFeed = { viewModel.toggleBlocklistFeed(feed) },
                            onDeleteFeed = { viewModel.deleteBlocklistFeed(feed) },
                            onClearFeed = { viewModel.clearFeedEntries(feed.id) },
                            onSyncNow = { viewModel.syncImmediate(feed.id.toString(), feed.feedUrl) },
                            onAutoSyncChanged = { enabled -> viewModel.setAutoSyncEnabled(feed.id.toString(), feed.feedUrl, enabled) },
                            onSyncIntervalChanged = { hours -> viewModel.setSyncInterval(feed.id.toString(), feed.feedUrl, hours) },
                            onWifiOnlyChanged = { wifiOnly -> viewModel.setSyncWifiOnly(feed.id.toString(), feed.feedUrl, wifiOnly) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showAddBlocklistDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, appColors.accent.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = appColors.accent)
                        Spacer(Modifier.width(6.dp))
                        Text("Add Threat Feed URL", color = appColors.accent)
                    }

                    if (blocklistFeeds.isNotEmpty()) {
                        Button(
                            onClick = { viewModel.syncAllBlocklists() },
                            enabled = !isSyncingBlocklists,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = appColors.accent, contentColor = appColors.background)
                        ) {
                            if (isSyncingBlocklists) {
                                CircularProgressIndicator(Modifier.size(16.dp), color = appColors.textPrimary, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.Sync, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Sync All")
                            }
                        }
                    }
                }
            }

            if (showAddBlocklistDialog) {
                AddBlocklistDialog(
                    onDismiss = { showAddBlocklistDialog = false },
                    onConfirm = { name, url ->
                        viewModel.addBlocklistFeed(name, url)
                        showAddBlocklistDialog = false
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Danger Zone ───────────────────────────────────────────────
            SettingsSection(title = "Data Management", icon = Icons.Filled.Warning, accentColor = VerdictMalicious) {
                OutlinedButton(
                    onClick = onClearHistory,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdictMalicious),
                    border = BorderStroke(1.dp, VerdictMalicious.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Clear All Lookup History")
                }
            }

            Spacer(Modifier.height(32.dp))
            Text(
                "IOC Lookup v1.0.0 • Secure Threat Intel",
                style = MaterialTheme.typography.labelSmall,
                color = appColors.textMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    accentColor: Color = LocalAppColors.current.accent,
    content: @Composable ColumnScope.() -> Unit
) {
    val appColors = LocalAppColors.current
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 14.dp)) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
            }
            HorizontalDivider(color = appColors.divider, thickness = 0.5.dp, modifier = Modifier.padding(bottom = 14.dp))
            content()
        }
    }
}

@Composable
private fun ApiKeyField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    accentColor: Color,
    hint: String
) {
    val appColors = LocalAppColors.current
    var showKey by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, color = accentColor.copy(alpha = 0.8f)) },
        placeholder = { Text(hint, color = appColors.textMuted) },
        visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { showKey = !showKey }) {
                Icon(
                    if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = "Toggle visibility",
                    tint = appColors.textMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(if (value.isNotBlank()) accentColor else appColors.textMuted)
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            unfocusedBorderColor = appColors.divider,
            focusedLabelColor = accentColor,
            unfocusedLabelColor = appColors.textMuted,
            focusedTextColor = appColors.textPrimary,
            unfocusedTextColor = appColors.textPrimary
        ),
        shape = RoundedCornerShape(10.dp),
        singleLine = true
    )
}

@Composable
private fun SourceToggle(
    label: String,
    enabled: Boolean,
    accentColor: Color,
    onToggle: (Boolean) -> Unit
) {
    val appColors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(if (enabled) accentColor else appColors.textMuted)
        )
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = if (enabled) appColors.textPrimary else appColors.textMuted, modifier = Modifier.weight(1f))
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = accentColor,
                checkedTrackColor = accentColor.copy(alpha = 0.3f),
                uncheckedTrackColor = appColors.divider
            )
        )
    }
}

@Composable
private fun AddBlocklistDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, url: String) -> Unit
) {
    val appColors = LocalAppColors.current
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appColors.surface,
        title = { Text("Add Custom Threat Feed", color = appColors.textPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Feed Name (e.g. BinaryDefense)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.accent,
                        unfocusedBorderColor = appColors.divider,
                        focusedTextColor = appColors.textPrimary,
                        unfocusedTextColor = appColors.textPrimary
                    )
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Plain-Text Feed URL") },
                    placeholder = { Text("https://binarydefense.com/banlist.txt") },
                    supportingText = { Text("Plain-text list (1 indicator per line)", color = appColors.textMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.accent,
                        unfocusedBorderColor = appColors.divider,
                        focusedTextColor = appColors.textPrimary,
                        unfocusedTextColor = appColors.textPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && url.startsWith("http")) onConfirm(name, url) },
                enabled = name.isNotBlank() && url.startsWith("http"),
                colors = ButtonDefaults.buttonColors(containerColor = appColors.accent, contentColor = appColors.background)
            ) {
                Text("Add & Sync Feed")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = appColors.textSecondary) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlocklistFeedCard(
    feed: com.example.ioclookup.data.local.entity.BlocklistFeedEntity,
    syncState: com.example.ioclookup.ui.settings.FeedSyncState,
    appColors: AppColors,
    isSyncing: Boolean,
    onToggleFeed: () -> Unit,
    onDeleteFeed: () -> Unit,
    onClearFeed: () -> Unit,
    onSyncNow: () -> Unit,
    onAutoSyncChanged: (Boolean) -> Unit,
    onSyncIntervalChanged: (Long) -> Unit,
    onWifiOnlyChanged: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var expandedInterval by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.background),
        border = BorderStroke(1.dp, appColors.divider)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(feed.name, style = MaterialTheme.typography.titleSmall, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${feed.entryCount} entries",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (feed.entryCount > 0) VerdictClean else appColors.textMuted
                    )
                }
                Switch(
                    checked = feed.isEnabled,
                    onCheckedChange = { onToggleFeed() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = appColors.accent,
                        uncheckedThumbColor = appColors.textMuted,
                        uncheckedTrackColor = appColors.surface
                    )
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = appColors.textMuted
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = appColors.divider, thickness = 0.5.dp)
                    Spacer(Modifier.height(8.dp))

                    Text("Auto Sync", style = MaterialTheme.typography.labelMedium, color = appColors.accent, fontWeight = FontWeight.Bold)
                    
                    // Auto Sync Toggle
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("Enable Auto Sync", style = MaterialTheme.typography.bodyMedium, color = appColors.textPrimary, modifier = Modifier.weight(1f))
                        Switch(
                            checked = syncState.autoSyncEnabled,
                            onCheckedChange = onAutoSyncChanged,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = appColors.accent,
                                uncheckedThumbColor = appColors.textMuted,
                                uncheckedTrackColor = appColors.surface
                            )
                        )
                    }

                    AnimatedVisibility(visible = syncState.autoSyncEnabled) {
                        Column {
                            // Sync Interval Dropdown
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text("Sync Interval", style = MaterialTheme.typography.bodyMedium, color = appColors.textPrimary, modifier = Modifier.weight(1f))
                                
                                Box {
                                    OutlinedButton(
                                        onClick = { expandedInterval = true },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = appColors.textPrimary),
                                        border = BorderStroke(1.dp, appColors.divider)
                                    ) {
                                        val displayStr = when(syncState.syncIntervalHours) {
                                            12L -> "12 hours"
                                            24L -> "24 hours"
                                            48L -> "48 hours"
                                            168L -> "7 days"
                                            else -> "${syncState.syncIntervalHours} hours"
                                        }
                                        Text(displayStr, style = MaterialTheme.typography.bodySmall)
                                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                    
                                    DropdownMenu(
                                        expanded = expandedInterval,
                                        onDismissRequest = { expandedInterval = false }
                                    ) {
                                        listOf(12L, 24L, 48L, 168L).forEach { hours ->
                                            DropdownMenuItem(
                                                text = { Text(if (hours == 168L) "7 days" else "$hours hours", color = appColors.textPrimary) },
                                                onClick = {
                                                    onSyncIntervalChanged(hours)
                                                    expandedInterval = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // WiFi Only Toggle
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text("Sync on WiFi Only", style = MaterialTheme.typography.bodyMedium, color = appColors.textPrimary, modifier = Modifier.weight(1f))
                                Switch(
                                    checked = syncState.wifiOnly,
                                    onCheckedChange = onWifiOnlyChanged,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = appColors.accent,
                                        uncheckedThumbColor = appColors.textMuted,
                                        uncheckedTrackColor = appColors.surface
                                    )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    
                    val timeStr = if (syncState.lastSyncedTimestamp > 0) {
                        val diff = System.currentTimeMillis() - syncState.lastSyncedTimestamp
                        val hours = diff / (1000 * 60 * 60)
                        if (hours == 0L) "Less than an hour ago" else "$hours hours ago"
                    } else "Never synced"
                    
                    Text("Last synced: $timeStr", style = MaterialTheme.typography.labelSmall, color = appColors.textMuted)

                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onSyncNow,
                            enabled = !isSyncing,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = appColors.accent, contentColor = appColors.background)
                        ) {
                            Text("Sync Now")
                        }
                        
                        OutlinedButton(
                            onClick = { showClearConfirm = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdictMalicious),
                            border = BorderStroke(1.dp, VerdictMalicious.copy(alpha = 0.5f))
                        ) {
                            Text("Clear Feed")
                        }
                        
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete Feed", tint = VerdictMalicious)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Feed") },
            text = { Text("Are you sure you want to delete ${feed.name}? This will also delete all its downloaded indicators.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteFeed()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdictMalicious)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel", color = appColors.textMuted) }
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Feed Entries") },
            text = { Text("Are you sure you want to clear all ${feed.entryCount} entries for ${feed.name}? You can re-sync later.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearFeed()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdictMalicious)
                ) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Cancel", color = appColors.textMuted) }
            }
        )
    }
}
