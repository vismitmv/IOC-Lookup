package com.example.ioclookup.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = DeepNavy,
    primaryContainer = NeonPurple,
    onPrimaryContainer = TextPrimary,
    secondary = VioletAccent,
    onSecondary = TextPrimary,
    secondaryContainer = CardSurfaceVariant,
    onSecondaryContainer = TextSecondary,
    background = DeepNavy,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    error = VerdictMalicious,
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    secondary = VioletAccent,
    background = LightBackground,
    onBackground = Color(0xFF111827),
    surface = LightSurface,
    onSurface = Color(0xFF1F2937),
    surfaceVariant = Color(0xFFE8EAF0),
    onSurfaceVariant = Color(0xFF4B5563),
    outline = Color(0xFFCDD5E0),
    error = VerdictMalicious,
)

private fun Color(color: Long) = androidx.compose.ui.graphics.Color(color)

@Composable
fun IOCLookupTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = IocTypography,
        content = content
    )
}
