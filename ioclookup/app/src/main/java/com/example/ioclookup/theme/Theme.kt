package com.example.ioclookup.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val background: Color = DeepNavy,
    val surface: Color = CardSurface,
    val surfaceVariant: Color = CardSurfaceVariant,
    val textPrimary: Color = TextPrimary,
    val textSecondary: Color = TextSecondary,
    val textMuted: Color = TextMuted,
    val accent: Color = ElectricCyan,
    val divider: Color = DividerColor
)

val LocalAppColors = staticCompositionLocalOf { AppColors() }

fun parseHexColor(hex: String, defaultColor: Color = ElectricCyan): Color {
    return try {
        val clean = hex.removePrefix("#").trim()
        val colorInt = android.graphics.Color.parseColor("#$clean")
        Color(colorInt)
    } catch (e: Exception) {
        defaultColor
    }
}

@Composable
fun IOCLookupTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColorHex: String = "#00D4FF",
    content: @Composable () -> Unit
) {
    val accentColor = parseHexColor(accentColorHex, ElectricCyan)

    val appColors = if (darkTheme) {
        AppColors(
            background = Color(0xFF0A0E1A),
            surface = Color(0xFF161B2E),
            surfaceVariant = Color(0xFF1E2640),
            textPrimary = Color(0xFFE8EAED),
            textSecondary = Color(0xFF9AA0AC),
            textMuted = Color(0xFF555F7A),
            accent = accentColor,
            divider = Color(0xFF2A3050)
        )
    } else {
        AppColors(
            background = Color(0xFFF1F5F9),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFE2E8F0),
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF475569),
            textMuted = Color(0xFF94A3B8),
            accent = accentColor,
            divider = Color(0xFFCBD5E1)
        )
    }

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = accentColor,
            onPrimary = appColors.background,
            background = appColors.background,
            onBackground = appColors.textPrimary,
            surface = appColors.surface,
            onSurface = appColors.textPrimary,
            surfaceVariant = appColors.surfaceVariant,
            onSurfaceVariant = appColors.textSecondary,
            outline = appColors.divider,
            error = VerdictMalicious
        )
    } else {
        lightColorScheme(
            primary = accentColor,
            onPrimary = Color.White,
            background = appColors.background,
            onBackground = appColors.textPrimary,
            surface = appColors.surface,
            onSurface = appColors.textPrimary,
            surfaceVariant = appColors.surfaceVariant,
            onSurfaceVariant = appColors.textSecondary,
            outline = appColors.divider,
            error = VerdictMalicious
        )
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = IocTypography,
            content = content
        )
    }
}
