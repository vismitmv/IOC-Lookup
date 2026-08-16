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

    Scaffold(
        containerColor = DeepNavy,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepNavy,
                    titleContentColor = TextPrimary
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
                    color = TextPrimary
                )
                Slider(
                    value = state.cacheTtlHours.toFloat(),
                    onValueChange = { viewModel.setCacheTtl(it.toInt()) },
                    valueRange = 1f..72f,
                    steps = 70,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan,
                        inactiveTrackColor = DividerColor
                    )
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("1h", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("72h", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Theme ─────────────────────────────────────────────────────
            SettingsSection(title = "Theme", icon = Icons.Filled.Palette) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("system" to "System", "light" to "Light", "dark" to "Dark").forEach { (key, label) ->
                        val selected = state.theme == key
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setTheme(key) },
                            label = { Text(label) },
                            leadingIcon = if (selected) {{ Icon(Icons.Filled.Check, null, modifier = Modifier.size(14.dp)) }} else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                                selectedLabelColor = ElectricCyan
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Custom Threat Feeds ───────────────────────────────────────
            val customFeeds by viewModel.customFeeds.collectAsStateWithLifecycle()
            var showAddFeedDialog by remember { mutableStateOf(false) }

            SettingsSection(title = "Custom Threat Feeds", icon = Icons.Filled.RssFeed, accentColor = ElectricCyan) {
                Text(
                    "Integrate your own MISP or REST threat feeds. Place {ioc} in the URL template.",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )
                Spacer(Modifier.height(10.dp))

                if (customFeeds.isEmpty()) {
                    Text("No custom threat feeds configured.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                } else {
                    customFeeds.forEach { feed ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(feed.name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text(feed.urlTemplate, style = MaterialTheme.typography.labelSmall, color = TextMuted, maxLines = 1)
                            }
                            Switch(
                                checked = feed.isEnabled,
                                onCheckedChange = { viewModel.toggleCustomFeed(feed) },
                                colors = SwitchDefaults.colors(checkedThumbColor = ElectricCyan)
                            )
                            IconButton(onClick = { viewModel.deleteCustomFeed(feed) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = VerdictMalicious, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showAddFeedDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = ElectricCyan)
                    Spacer(Modifier.width(6.dp))
                    Text("Add Custom Feed", color = ElectricCyan)
                }
            }

            if (showAddFeedDialog) {
                AddCustomFeedDialog(
                    onDismiss = { showAddFeedDialog = false },
                    onConfirm = { name, url, headerName, headerVal, path ->
                        viewModel.addCustomFeed(name, url, headerName, headerVal, path)
                        showAddFeedDialog = false
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Plain-Text Banlists (Sophos/Firewall Style) ─────────────
            val blocklistFeeds by viewModel.blocklistFeeds.collectAsStateWithLifecycle()
            val isSyncingBlocklists by viewModel.isSyncingBlocklists.collectAsStateWithLifecycle()
            var showAddBlocklistDialog by remember { mutableStateOf(false) }

            SettingsSection(title = "Firewall Banlists (Sophos Style)", icon = Icons.Filled.ListAlt, accentColor = OTXOrange) {
                Text(
                    "Import 10,000+ IP/URL/Domain plain-text banlists (one indicator per line) for instant offline matching.",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )
                Spacer(Modifier.height(10.dp))

                // Presets Buttons
                Text("Quick Add Presets:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {
                            viewModel.addBlocklistFeed(
                                "URLhaus Text Online",
                                "https://urlhaus.abuse.ch/downloads/text_online/"
                            )
                        },
                        label = { Text("URLhaus Online URLs") },
                        leadingIcon = { Icon(Icons.Filled.FlashOn, null, Modifier.size(14.dp), tint = OTXOrange) }
                    )
                    AssistChip(
                        onClick = {
                            viewModel.addBlocklistFeed(
                                "BinaryDefense Banlist",
                                "https://binarydefense.com/banlist.txt"
                            )
                        },
                        label = { Text("BinaryDefense IPs") },
                        leadingIcon = { Icon(Icons.Filled.FlashOn, null, Modifier.size(14.dp), tint = OTXOrange) }
                    )
                }

                Spacer(Modifier.height(12.dp))

                if (blocklistFeeds.isEmpty()) {
                    Text("No plain-text banlists added yet.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                } else {
                    blocklistFeeds.forEach { feed ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(feed.name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${feed.entryCount} entries • ${if (feed.lastSyncedAt > 0) "Synced" else "Not synced"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (feed.entryCount > 0) VerdictClean else TextMuted
                                )
                            }
                            IconButton(onClick = { viewModel.syncBlocklistFeed(feed) }) {
                                Icon(Icons.Filled.Refresh, contentDescription = "Sync", tint = ElectricCyan, modifier = Modifier.size(18.dp))
                            }
                            Switch(
                                checked = feed.isEnabled,
                                onCheckedChange = { viewModel.toggleBlocklistFeed(feed) },
                                colors = SwitchDefaults.colors(checkedThumbColor = OTXOrange)
                            )
                            IconButton(onClick = { viewModel.deleteBlocklistFeed(feed) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = VerdictMalicious, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showAddBlocklistDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, OTXOrange.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = OTXOrange)
                        Spacer(Modifier.width(6.dp))
                        Text("Add Banlist URL", color = OTXOrange)
                    }

                    if (blocklistFeeds.isNotEmpty()) {
                        Button(
                            onClick = { viewModel.syncAllBlocklists() },
                            enabled = !isSyncingBlocklists,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OTXOrange)
                        ) {
                            if (isSyncingBlocklists) {
                                CircularProgressIndicator(Modifier.size(16.dp), color = TextPrimary, strokeWidth = 2.dp)
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
            SettingsSection(title = "Danger Zone", icon = Icons.Filled.Warning, accentColor = VerdictMalicious) {
                Button(
                    onClick = onClearHistory,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerdictMaliciousContainer,
                        contentColor = VerdictMalicious
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, VerdictMalicious.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Filled.DeleteForever, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Clear All History", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))

            // App version info
            Text(
                "IOC Lookup v1.0 • Built for security professionals",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
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
    accentColor: Color = ElectricCyan,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 14.dp)) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            }
            HorizontalDivider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(bottom = 14.dp))
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
    var showKey by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, color = accentColor.copy(alpha = 0.8f)) },
        placeholder = { Text(hint, color = TextMuted) },
        visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { showKey = !showKey }) {
                Icon(
                    if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = "Toggle visibility",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(if (value.isNotBlank()) accentColor else TextMuted)
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            unfocusedBorderColor = DividerColor,
            focusedLabelColor = accentColor,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
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
                .background(if (enabled) accentColor else TextMuted)
        )
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = if (enabled) TextPrimary else TextMuted, modifier = Modifier.weight(1f))
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = accentColor,
                checkedTrackColor = accentColor.copy(alpha = 0.3f),
                uncheckedTrackColor = DividerColor
            )
        )
    }
}

@Composable
private fun AddCustomFeedDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, url: String, headerName: String?, headerVal: String?, jsonPath: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var headerName by remember { mutableStateOf("") }
    var headerVal by remember { mutableStateOf("") }
    var jsonPath by remember { mutableStateOf("malicious") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        title = { Text("Add Custom Threat Feed", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Feed Name (e.g. MISP Internal)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL Template with {ioc}") },
                    placeholder = { Text("https://feed.org/api/{ioc}") },
                    supportingText = {
                        if (url.isNotBlank() && !url.contains("{ioc}")) {
                            Text("URL must contain {ioc} (e.g. https://feed.org/api/{ioc})", color = VerdictMalicious)
                        } else {
                            Text("Use {ioc} where the IP/Domain/URL will be inserted", color = TextMuted)
                        }
                    },
                    isError = url.isNotBlank() && !url.contains("{ioc}"),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = headerName,
                    onValueChange = { headerName = it },
                    label = { Text("Auth Header Name (Optional)") },
                    placeholder = { Text("Authorization or X-API-Key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = headerVal,
                    onValueChange = { headerVal = it },
                    label = { Text("Auth Header Token (Optional)") },
                    placeholder = { Text("Bearer secret_token") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = jsonPath,
                    onValueChange = { jsonPath = it },
                    label = { Text("JSON Malicious Field Name") },
                    placeholder = { Text("malicious or is_flagged") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && url.isNotBlank()) onConfirm(name, url, headerName, headerVal, jsonPath) },
                enabled = name.isNotBlank() && url.contains("{ioc}")
            ) {
                Text("Add Feed")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddBlocklistDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, url: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        title = { Text("Add Plain-Text Banlist / Blocklist", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Banlist Name (e.g. BinaryDefense)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Plain-Text List URL") },
                    placeholder = { Text("https://binarydefense.com/banlist.txt") },
                    supportingText = { Text("Plain-text file (1 indicator per line)", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && url.startsWith("http")) onConfirm(name, url) },
                enabled = name.isNotBlank() && url.startsWith("http")
            ) {
                Text("Add & Sync Banlist")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
