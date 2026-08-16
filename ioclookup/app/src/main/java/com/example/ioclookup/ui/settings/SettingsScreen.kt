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
