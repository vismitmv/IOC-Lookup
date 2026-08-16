package com.example.ioclookup.ui.lookup

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ioclookup.domain.model.IocType
import com.example.ioclookup.domain.model.SourceResult
import com.example.ioclookup.domain.model.Verdict
import com.example.ioclookup.theme.*
import com.example.ioclookup.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LookupScreen(viewModel: LookupViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val keyboard = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    val appColors = LocalAppColors.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = appColors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            // ── Header ──────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(listOf(appColors.accent, NeonPurple))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("IOC Lookup", style = MaterialTheme.typography.headlineMedium, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                    Text("Threat Intelligence", style = MaterialTheme.typography.labelMedium, color = appColors.textSecondary)
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Input Card ──────────────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = appColors.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Type badge
                    if (uiState.detectedType != IocType.UNKNOWN) {
                        val typeColor = when {
                            uiState.detectedType.isHash -> NeonPurple
                            uiState.detectedType.isIp -> VTBlueDark
                            uiState.detectedType == IocType.URL -> ShodanGreen
                            else -> OTXOrange
                        }
                        AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(typeColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        uiState.detectedType.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = typeColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = { viewModel.onInputChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Paste IP, domain, URL, or hash…", color = TextMuted) },
                        label = { Text("Indicator of Compromise", color = TextSecondary) },
                        trailingIcon = {
                            if (uiState.inputText.isNotBlank()) {
                                IconButton(onClick = { viewModel.onInputChanged("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = TextSecondary)
                                }
                            }
                        },
                        isError = uiState.error != null,
                        supportingText = uiState.error?.let { { Text(it, color = VerdictMalicious) } },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DividerColor,
                            focusedLabelColor = ElectricCyan,
                            cursorColor = ElectricCyan,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            keyboard?.hide()
                            viewModel.performLookup()
                        }),
                        singleLine = false,
                        maxLines = 3
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            keyboard?.hide()
                            viewModel.performLookup()
                        },
                        enabled = !uiState.isLooking && uiState.inputText.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = DeepNavy,
                            disabledContainerColor = DividerColor,
                            disabledContentColor = TextMuted
                        )
                    ) {
                        if (uiState.isLooking) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = DeepNavy)
                            Spacer(Modifier.width(8.dp))
                            Text("Querying sources…", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Filled.Search, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Lookup", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Results ─────────────────────────────────────────────────────
            val result = uiState.result
            AnimatedVisibility(
                visible = result != null,
                enter = fadeIn() + expandVertically()
            ) {
                result?.let { r ->
                    Column {
                        // Cache badge
                        if (r.isFromCache) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(Icons.Filled.History, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Cached result", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Spacer(Modifier.width(8.dp))
                                TextButton(
                                    onClick = { viewModel.performLookup(forceRefresh = true) },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Refresh", style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
                                }
                            }
                        }

                        // Verdict card
                        VerdictCard(
                            verdict = r.verdict,
                            ioc = r.ioc,
                            iocTypeLabel = r.iocType.displayName
                        )

                        Spacer(Modifier.height(8.dp))

                        // Action row
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            // Bookmark
                            IconButton(onClick = { viewModel.toggleBookmark() }) {
                                Icon(
                                    if (r.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (r.isBookmarked) ElectricCyan else TextSecondary
                                )
                            }
                            // Share text
                            IconButton(onClick = {
                                val text = viewModel.exportAsText() ?: return@IconButton
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Report"))
                            }) {
                                Icon(Icons.Filled.Share, contentDescription = "Share", tint = TextSecondary)
                            }
                            // Export PDF
                            IconButton(onClick = {
                                scope.launch {
                                    val file = viewModel.exportAsPdf() ?: return@launch
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        file
                                    )
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "application/pdf")
                                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    }
                                    context.startActivity(intent)
                                }
                            }) {
                                Icon(Icons.Filled.PictureAsPdf, contentDescription = "Export PDF", tint = TextSecondary)
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Source cards
                        r.vtResult?.let { vt ->
                            VirusTotalCard(vt)
                            Spacer(Modifier.height(10.dp))
                        }
                        r.abuseResult?.let { abuse ->
                            AbuseIPDBCard(abuse)
                            Spacer(Modifier.height(10.dp))
                        }
                        (r.sources["abusech"] as? SourceResult.AbuseCh)?.let { abuseCh ->
                            AbuseChCard(abuseCh)
                            Spacer(Modifier.height(10.dp))
                        }
                        r.shodanResult?.let { sh ->
                            ShodanCard(sh)
                            Spacer(Modifier.height(10.dp))
                        }
                        r.otxResult?.let { otx ->
                            OtxCard(otx)
                            Spacer(Modifier.height(10.dp))
                        }
                        // Custom Feeds
                        r.sources.values.filterIsInstance<SourceResult.CustomFeed>().forEach { customFeed ->
                            CustomFeedCard(customFeed)
                            Spacer(Modifier.height(10.dp))
                        }
                        // Errors from sources
                        r.sources.values.filterIsInstance<SourceResult.Error>().forEach { err ->
                            SourceCard(
                                title = err.sourceName.replaceFirstChar { it.uppercase() },
                                subtitle = "Query failed",
                                accentColor = VerdictMalicious,
                                icon = Icons.Filled.ErrorOutline,
                                result = err
                            ) {}
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun VirusTotalCard(vt: SourceResult.VirusTotal) {
    val detectionColor = when {
        vt.detectionCount == 0 -> VerdictClean
        vt.detectionRatio > 0.3 -> VerdictMalicious
        else -> VerdictSuspicious
    }
    SourceCard(
        title = "VirusTotal",
        subtitle = vt.detectionLabel,
        accentColor = VTBlueDark,
        icon = Icons.Filled.BugReport,
        result = vt
    ) {
        // Detection bar
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Detection", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                Text(
                    vt.detectionLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = detectionColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { vt.detectionRatio.toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = detectionColor,
                trackColor = DividerColor
            )
        }
        if (vt.reputation != 0) {
            Spacer(Modifier.height(10.dp))
            InfoRow("Reputation", "${vt.reputation}", valueColor = if (vt.reputation < 0) VerdictMalicious else VerdictClean)
        }
        if (vt.categories.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text("Categories", style = MaterialTheme.typography.labelMedium, color = TextMuted)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                vt.categories.forEach { TagChip(it, VTBlueDark) }
            }
        }
        if (vt.tags.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text("Tags", style = MaterialTheme.typography.labelMedium, color = TextMuted)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                vt.tags.take(10).forEach { TagChip(it, TextSecondary) }
            }
        }
    }
}

@Composable
private fun AbuseIPDBCard(abuse: SourceResult.AbuseIPDB) {
    val scoreColor = when {
        abuse.abuseConfidenceScore >= 70 -> VerdictMalicious
        abuse.abuseConfidenceScore >= 25 -> VerdictSuspicious
        else -> VerdictClean
    }
    SourceCard(
        title = "AbuseIPDB",
        subtitle = "Score: ${abuse.abuseConfidenceScore}%",
        accentColor = AbuseRed,
        icon = Icons.Filled.GppBad,
        result = abuse
    ) {
        // Score gauge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Abuse Score", style = MaterialTheme.typography.labelMedium, color = TextMuted)
            Text("${abuse.abuseConfidenceScore}%", style = MaterialTheme.typography.titleMedium, color = scoreColor, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { abuse.abuseConfidenceScore / 100f },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = scoreColor,
            trackColor = DividerColor
        )
        Spacer(Modifier.height(12.dp))
        InfoRow("Total Reports", "${abuse.totalReports} (${abuse.numDistinctUsers} users)")
        abuse.lastReportedAt?.let { InfoRow("Last Reported", it) }
        abuse.countryCode?.let { InfoRow("Country", it) }
        abuse.isp?.let { InfoRow("ISP", it) }
        abuse.usageType?.let { InfoRow("Usage Type", it) }
        if (abuse.isTor) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(VerdictSuspiciousContainer)
                    .padding(8.dp)
            ) {
                Icon(Icons.Filled.VpnLock, contentDescription = null, tint = VerdictSuspicious, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Tor Exit Node", style = MaterialTheme.typography.bodySmall, color = VerdictSuspicious)
            }
        }
    }
}

@Composable
private fun ShodanCard(sh: SourceResult.Shodan) {
    SourceCard(
        title = "Shodan",
        subtitle = "${sh.ports.size} open ports",
        accentColor = ShodanGreen,
        icon = Icons.Filled.Router,
        result = sh
    ) {
        if (sh.ports.isNotEmpty()) {
            Text("Open Ports", style = MaterialTheme.typography.labelMedium, color = TextMuted)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                sh.ports.take(20).forEach { TagChip("$it", ShodanGreen) }
            }
            Spacer(Modifier.height(12.dp))
        }
        sh.org?.let { InfoRow("Organization", it) }
        sh.isp?.let { InfoRow("ISP", it) }
        sh.country?.let { InfoRow("Country", it) }
        sh.os?.let { InfoRow("OS", it) }
        if (sh.hostnames.isNotEmpty()) InfoRow("Hostnames", sh.hostnames.take(3).joinToString(", "))
        if (sh.cves.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text("CVEs (${sh.cves.size})", style = MaterialTheme.typography.labelMedium, color = TextMuted)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                sh.cves.take(10).forEach { TagChip(it, VerdictMalicious) }
            }
        }
    }
}

@Composable
private fun OtxCard(otx: SourceResult.OTX) {
    SourceCard(
        title = "AlienVault OTX",
        subtitle = "${otx.pulseCount} pulses",
        accentColor = OTXOrange,
        icon = Icons.Filled.Radar,
        result = otx
    ) {
        InfoRow("Pulse Count", "${otx.pulseCount}", valueColor = if (otx.pulseCount > 0) OTXOrange else VerdictClean)
        if (otx.tags.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text("Threat Tags", style = MaterialTheme.typography.labelMedium, color = TextMuted)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                otx.tags.take(12).forEach { TagChip(it, OTXOrange) }
            }
        }
        if (otx.malwareFamilies.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text("Malware Families", style = MaterialTheme.typography.labelMedium, color = TextMuted)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                otx.malwareFamilies.forEach { TagChip(it, VerdictMalicious) }
            }
        }
        if (otx.adversaries.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            InfoRow("Adversaries", otx.adversaries.joinToString(", "), valueColor = VerdictSuspicious)
        }
    }
}

@Composable
private fun AbuseChCard(abuseCh: SourceResult.AbuseCh) {
    val accent = if (abuseCh.isFlagged) VerdictMalicious else VerdictClean
    SourceCard(
        title = "abuse.ch (URLhaus/MB)",
        subtitle = if (abuseCh.isFlagged) "MALICIOUS DETECTED" else "Clean / Not Flagged",
        accentColor = accent,
        icon = Icons.Filled.Shield,
        result = abuseCh
    ) {
        InfoRow("Status", abuseCh.status?.uppercase() ?: "CLEAN", valueColor = accent)
        abuseCh.threatType?.let { InfoRow("Threat Type", it, valueColor = VerdictMalicious) }
        abuseCh.signature?.let { InfoRow("Signature", it, valueColor = VerdictMalicious) }
        abuseCh.reporter?.let { InfoRow("Reporter", it) }
        if (abuseCh.tags.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text("Tags", style = MaterialTheme.typography.labelMedium, color = TextMuted)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                abuseCh.tags.forEach { TagChip(it, accent) }
            }
        }
    }
}

@Composable
private fun CustomFeedCard(feed: SourceResult.CustomFeed) {
    val accent = if (feed.isFlagged) VerdictMalicious else VerdictClean
    SourceCard(
        title = feed.feedName,
        subtitle = if (feed.isFlagged) "FLAGGED MALICIOUS" else "Clean (HTTP ${feed.responseCode})",
        accentColor = accent,
        icon = Icons.Filled.RssFeed,
        result = feed
    ) {
        InfoRow("Result", if (feed.isFlagged) "MALICIOUS" else "CLEAN", valueColor = accent)
        feed.summary?.let { InfoRow("Summary", it) }
        InfoRow("HTTP Status", "${feed.responseCode}")
    }
}
