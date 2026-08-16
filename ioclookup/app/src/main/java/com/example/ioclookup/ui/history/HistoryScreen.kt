package com.example.ioclookup.ui.history

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ioclookup.domain.model.IocType
import com.example.ioclookup.domain.model.LookupResult
import com.example.ioclookup.domain.model.Verdict
import com.example.ioclookup.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onItemClick: (LookupResult) -> Unit
) {
    val lookups by viewModel.lookups.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }

    val appColors = LocalAppColors.current

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = { Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = VerdictMalicious) },
            title = { Text("Clear All History") },
            text = { Text("This will permanently delete all lookup history. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearAll(); showClearDialog = false }) {
                    Text("Clear All", color = VerdictMalicious)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            },
            containerColor = appColors.surface
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = appColors.background,
        topBar = {
            TopAppBar(
                title = { Text("History", fontWeight = FontWeight.Bold) },
                actions = {
                    if (lookups.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear All", tint = appColors.textSecondary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = appColors.background,
                    titleContentColor = appColors.textPrimary,
                    actionIconContentColor = appColors.textSecondary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = filter.query,
                onValueChange = { viewModel.onQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search history…", color = appColors.textMuted) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = appColors.textSecondary) },
                trailingIcon = {
                    if (filter.query.isNotBlank()) {
                        IconButton(onClick = { viewModel.onQueryChanged("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = appColors.textSecondary)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appColors.accent,
                    unfocusedBorderColor = appColors.divider,
                    focusedTextColor = appColors.textPrimary,
                    unfocusedTextColor = appColors.textPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(Modifier.height(10.dp))

            // Filter chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                // Verdict filters
                listOf("", "MALICIOUS", "SUSPICIOUS", "CLEAN").forEach { v ->
                    val selected = filter.verdictFilter == v
                    val chipColor = when (v) {
                        "MALICIOUS" -> VerdictMalicious
                        "SUSPICIOUS" -> VerdictSuspicious
                        "CLEAN" -> VerdictClean
                        else -> appColors.accent
                    }
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.onVerdictFilterChanged(if (selected) "" else v) },
                        label = { Text(if (v.isEmpty()) "All" else v.lowercase().replaceFirstChar { it.uppercase() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor.copy(alpha = 0.2f),
                            selectedLabelColor = chipColor
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (lookups.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.History, contentDescription = null, tint = appColors.textMuted, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No lookups found", style = MaterialTheme.typography.titleMedium, color = appColors.textSecondary)
                        Text("Your lookup history will appear here", style = MaterialTheme.typography.bodySmall, color = appColors.textMuted)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(lookups, key = { it.id }) { item ->
                        HistoryItem(item = item, onClick = { onItemClick(item) })
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(
    item: LookupResult,
    onClick: () -> Unit
) {
    val appColors = LocalAppColors.current
    val verdictColor = when (item.verdict) {
        Verdict.MALICIOUS -> VerdictMalicious
        Verdict.SUSPICIOUS -> VerdictSuspicious
        Verdict.CLEAN -> VerdictClean
        else -> VerdictUnknown
    }
    val typeColor = when {
        item.iocType.isHash -> NeonPurple
        item.iocType.isIp -> VTBlueDark
        item.iocType == IocType.URL -> ShodanGreen
        else -> OTXOrange
    }
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Verdict dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(verdictColor)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.ioc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(typeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(item.iocType.displayName, style = MaterialTheme.typography.labelSmall, color = typeColor)
                    }
                    Text("•", color = appColors.textMuted, style = MaterialTheme.typography.labelSmall)
                    Text(dateFormat.format(Date(item.timestamp)), style = MaterialTheme.typography.labelSmall, color = appColors.textMuted)
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    item.verdict.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = verdictColor,
                    fontWeight = FontWeight.SemiBold
                )
                if (item.isBookmarked) {
                    Icon(Icons.Filled.Bookmark, contentDescription = null, tint = appColors.accent, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}
