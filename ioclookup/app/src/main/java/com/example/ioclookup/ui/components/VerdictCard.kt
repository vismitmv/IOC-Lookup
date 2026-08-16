package com.example.ioclookup.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.example.ioclookup.domain.model.Verdict
import com.example.ioclookup.theme.*

@Composable
fun VerdictCard(
    verdict: Verdict,
    ioc: String,
    iocTypeLabel: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor, icon) = when (verdict) {
        Verdict.MALICIOUS -> listOf(VerdictMaliciousContainer, VerdictMalicious, VerdictMalicious, Icons.Filled.Warning)
        Verdict.SUSPICIOUS -> listOf(VerdictSuspiciousContainer, VerdictSuspicious, VerdictSuspicious, Icons.Filled.Info)
        Verdict.CLEAN -> listOf(VerdictCleanContainer, VerdictClean, VerdictClean, Icons.Filled.CheckCircle)
        Verdict.UNKNOWN -> listOf(VerdictUnknownContainer, VerdictUnknown, VerdictUnknown, Icons.Filled.HelpOutline)
    }

    // Pulse animation for malicious
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (verdict == Verdict.MALICIOUS) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(if (verdict == Verdict.MALICIOUS) scale else 1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor as Color),
        border = BorderStroke(1.5.dp, borderColor as Color)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf((textColor as Color).copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
                    .border(1.dp, textColor.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = icon as androidx.compose.ui.graphics.vector.ImageVector,
                    contentDescription = verdict.displayName,
                    tint = textColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = verdict.displayName.uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = textColor,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 3.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = ioc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.8f),
                    maxLines = 1
                )
                Text(
                    text = iocTypeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}
