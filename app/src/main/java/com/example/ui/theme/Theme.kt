package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Blue800,
    onPrimary = Color.White,
    primaryContainer = Blue50,
    onPrimaryContainer = Navy900,
    secondary = Teal700,
    onSecondary = Color.White,
    secondaryContainer = Teal50,
    onSecondaryContainer = Teal700,
    tertiary = StatusApproved,
    onTertiary = Color.White,
    tertiaryContainer = StatusApprovedBg,
    onTertiaryContainer = StatusApproved,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,
    outline = OutlineLight,
    error = StatusRejected,
    onError = Color.White,
    errorContainer = StatusRejectedBg,
    onErrorContainer = StatusRejected
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue600,
    onPrimary = Color.White,
    primaryContainer = Navy900,
    onPrimaryContainer = Blue50,
    secondary = Teal700,
    onSecondary = Color.White,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent clinical branding
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
