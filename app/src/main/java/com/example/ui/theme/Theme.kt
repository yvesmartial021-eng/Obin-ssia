package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ObinssRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2C0A0F),
    onPrimaryContainer = Color(0xFFFFB3B8),
    secondary = ObinssGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF382607),
    onSecondaryContainer = Color(0xFFFFE08A),
    background = ObinssDeepBlack,
    onBackground = ObinssTextPrimary,
    surface = ObinssSurface,
    onSurface = ObinssTextPrimary,
    surfaceVariant = ObinssSurfaceVariant,
    onSurfaceVariant = ObinssTextSecondary,
    outline = ObinssRedSubtleBorder,
    error = Color(0xFFEF4444)
)

private val LightColorScheme = lightColorScheme(
    primary = ObinssRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEBEB),
    onPrimaryContainer = ObinssRed,
    secondary = ObinssGold,
    onSecondary = Color.Black,
    background = ObinssLightBg,
    onBackground = ObinssLightTextPrimary,
    surface = ObinssLightSurface,
    onSurface = ObinssLightTextPrimary,
    surfaceVariant = ObinssLightCard,
    onSurfaceVariant = ObinssLightTextSecondary,
    outline = Color(0xFFE2E8F0),
    error = Color(0xFFDC2626)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
