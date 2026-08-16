package com.example.ioclookup.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ioclookup.domain.model.SourceResult
import com.example.ioclookup.theme.*

@Composable
fun SourceCard(
    title: String,
    subtitle: String,
    accentColor: Color,
    icon: ImageVector,
    result: SourceResult,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    var showRawJson by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            when (result) {
                is SourceResult.Loading -> CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = accentColor
                )
                is SourceResult.Error -> Icon(Icons.Filled.ErrorOutline, contentDescription = "Error", tint = VerdictMalicious)
                else -> Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = "Expand",
                    tint = TextSecondary
                )
            }
        }

        // Error banner
        if (result is SourceResult.Error) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(VerdictMaliciousContainer)
                    .padding(10.dp)
            ) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = VerdictMalicious, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(result.error, style = MaterialTheme.typography.bodySmall, color = VerdictMalicious)
            }
            return@Card
        }

        // Content
        AnimatedVisibility(
            visible = expanded && result !is SourceResult.Loading,
            enter = expandVertically(animationSpec = tween(250)) + fadeIn(),
            exit = shrinkVertically(animationSpec = tween(200)) + fadeOut()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)) {
                HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                Spacer(Modifier.height(12.dp))
                content()
                Spacer(Modifier.height(8.dp))

                // Raw JSON toggle
                result.rawJson?.let { raw ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { showRawJson = !showRawJson },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                if (showRawJson) Icons.Filled.Code else Icons.Filled.DataObject,
                                contentDescription = "JSON",
                                modifier = Modifier.size(14.dp),
                                tint = accentColor
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                if (showRawJson) "Hide JSON" else "View Raw JSON",
                                style = MaterialTheme.typography.labelMedium,
                                color = accentColor
                            )
                        }
                        if (showRawJson) {
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = { clipboard.setText(AnnotatedString(raw)) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy JSON", modifier = Modifier.size(14.dp), tint = TextSecondary)
                            }
                        }
                    }
                    AnimatedVisibility(visible = showRawJson) {
                        SelectionContainer {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CardSurfaceVariant)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = raw,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TextPrimary,
    copyable: Boolean = true
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted,
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            modifier = Modifier.weight(1f)
        )
        if (copyable) {
            IconButton(
                onClick = {
                    clipboard.setText(AnnotatedString(value))
                    copied = true
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                    contentDescription = "Copy",
                    modifier = Modifier.size(14.dp),
                    tint = if (copied) VerdictClean else TextMuted
                )
            }
            LaunchedEffect(copied) {
                if (copied) {
                    kotlinx.coroutines.delay(1500)
                    copied = false
                }
            }
        }
    }
}

@Composable
fun TagChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
