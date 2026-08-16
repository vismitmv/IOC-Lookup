package com.example.ioclookup.ui.bookmarks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ioclookup.domain.model.IocType
import com.example.ioclookup.domain.model.LookupResult
import com.example.ioclookup.domain.model.Verdict
import com.example.ioclookup.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    viewModel: BookmarksViewModel = hiltViewModel(),
    onItemClick: (LookupResult) -> Unit
) {
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    var editingItem by remember { mutableStateOf<LookupResult?>(null) }
    var noteText by remember { mutableStateOf("") }

    val appColors = LocalAppColors.current

    // Edit note bottom sheet
    editingItem?.let { item ->
        ModalBottomSheet(
            onDismissRequest = { editingItem = null },
            containerColor = appColors.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Edit Bookmark Note", style = MaterialTheme.typography.titleLarge, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(item.ioc, style = MaterialTheme.typography.bodySmall, color = appColors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Note (e.g. Seen in phishing email 12 Aug)") },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.accent,
                        unfocusedBorderColor = appColors.divider,
                        focusedTextColor = appColors.textPrimary,
                        unfocusedTextColor = appColors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            viewModel.removeBookmark(item.id)
                            editingItem = null
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdictMalicious)
                    ) {
                        Icon(Icons.Filled.BookmarkRemove, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Remove")
                    }
                    Button(
                        onClick = {
                            viewModel.updateNote(item.id, noteText)
                            editingItem = null
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = appColors.accent, contentColor = appColors.background)
                    ) {
                        Text("Save Note", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    Scaffold(
        containerColor = appColors.background,
        topBar = {
            TopAppBar(
                title = { Text("Bookmarks", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = appColors.background,
                    titleContentColor = appColors.textPrimary
                )
            )
        }
    ) { padding ->
        if (bookmarks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Bookmarks, contentDescription = null, tint = TextMuted, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No bookmarks", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                    Text("Star a lookup result to save it here", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }
                items(bookmarks, key = { it.id }) { item ->
                    BookmarkCard(
                        item = item,
                        onClick = { onItemClick(item) },
                        onEditNote = {
                            noteText = item.bookmarkNote
                            editingItem = item
                        }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun BookmarkCard(
    item: LookupResult,
    onClick: () -> Unit,
    onEditNote: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val verdictColor = when (item.verdict) {
        Verdict.MALICIOUS -> VerdictMalicious
        Verdict.SUSPICIOUS -> VerdictSuspicious
        Verdict.CLEAN -> VerdictClean
        Verdict.UNKNOWN -> VerdictUnknown
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Bookmark, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    item.ioc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(verdictColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(item.verdict.displayName, style = MaterialTheme.typography.labelSmall, color = verdictColor, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "${item.iocType.displayName} • ${dateFormat.format(Date(item.timestamp))}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            if (item.bookmarkNote.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.StickyNote2, contentDescription = null, tint = ElectricCyan.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        item.bookmarkNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = onEditNote,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Filled.EditNote, contentDescription = null, modifier = Modifier.size(14.dp), tint = ElectricCyan)
                Spacer(Modifier.width(4.dp))
                Text(
                    if (item.bookmarkNote.isBlank()) "Add note" else "Edit note",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricCyan
                )
            }
        }
    }
}
