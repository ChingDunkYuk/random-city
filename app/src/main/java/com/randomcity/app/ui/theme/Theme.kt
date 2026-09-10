package com.randomcity.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 中性冷色视觉系统(用户偏好:拒绝暖色/粉色)
private val LightColors = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDBE7FF),
    onPrimaryContainer = Color(0xFF0B1E4B),
    secondary = Color(0xFF0F766E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCFFAF3),
    onSecondaryContainer = Color(0xFF08332F),
    tertiary = Color(0xFF4F46E5),
    background = Color(0xFFFAFAFB),
    onBackground = Color(0xFF16181D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF16181D),
    surfaceVariant = Color(0xFFF1F3F6),
    onSurfaceVariant = Color(0xFF4A4F57),
    outline = Color(0xFFB9BEC7)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FB4FF),
    onPrimary = Color(0xFF0A1B3E),
    primaryContainer = Color(0xFF1D3F8C),
    onPrimaryContainer = Color(0xFFDBE7FF),
    secondary = Color(0xFF5EEAD4),
    onSecondary = Color(0xFF07332D),
    secondaryContainer = Color(0xFF0E4F48),
    onSecondaryContainer = Color(0xFFCFFAF3),
    tertiary = Color(0xFFA5B4FC),
    background = Color(0xFF14171C),
    onBackground = Color(0xFFE6E8EC),
    surface = Color(0xFF1B1F26),
    onSurface = Color(0xFFE6E8EC),
    surfaceVariant = Color(0xFF262B33),
    onSurfaceVariant = Color(0xFFB4BAC4),
    outline = Color(0xFF4A505A)
)

@Composable
fun RandomCityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
