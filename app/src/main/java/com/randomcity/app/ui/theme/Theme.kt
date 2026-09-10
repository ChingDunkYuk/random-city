package com.randomcity.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Soft Cards 柔和卡片风(浅色为主):柔浅灰底、白卡、雾感蓝
private val LightColors = lightColorScheme(
    primary = Color(0xFF6B8FE8),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEAF0FD),
    onPrimaryContainer = Color(0xFF44609F),
    secondary = Color(0xFF7BB8C4),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE4F2F4),
    onSecondaryContainer = Color(0xFF3A5F68),
    tertiary = Color(0xFF6B8FE8),
    background = Color(0xFFF6F7F9),
    onBackground = Color(0xFF2A2E35),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF2A2E35),
    surfaceVariant = Color(0xFFF3F5F8),
    onSurfaceVariant = Color(0xFF7B818B),
    outline = Color(0xFFE9EBEF),
    outlineVariant = Color(0xFFF0F2F5)
)

// 深色(同步柔和):深底 + 柔蓝
private val DarkColors = darkColorScheme(
    primary = Color(0xFF9DB4F0),
    onPrimary = Color(0xFF1B2B4E),
    primaryContainer = Color(0xFF2E3C60),
    onPrimaryContainer = Color(0xFFDDE6FC),
    secondary = Color(0xFF86BEC9),
    onSecondary = Color(0xFF10333B),
    secondaryContainer = Color(0xFF274750),
    onSecondaryContainer = Color(0xFFD2EBF0),
    tertiary = Color(0xFF9DB4F0),
    background = Color(0xFF141922),
    onBackground = Color(0xFFE9ECF2),
    surface = Color(0xFF1E2530),
    onSurface = Color(0xFFE9ECF2),
    surfaceVariant = Color(0xFF262E3A),
    onSurfaceVariant = Color(0xFF9AA1AE),
    outline = Color(0xFF2E3744),
    outlineVariant = Color(0xFF293039)
)

@Composable
fun RandomCityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
