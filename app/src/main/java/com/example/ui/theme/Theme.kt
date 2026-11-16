package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class Win11Colors(
    val isDark: Boolean,
    val accent: Color,
    val accentSoft: Color,
    val micaSurface: Color,
    val acrylicTaskbar: Color,
    val cardSurface: Color,
    val cardHover: Color,
    val border: Color,
    val subtleBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val desktopIconText: Color,
    val selectionFill: Color,
    val selectionBorder: Color,
    val pillActive: Color,
    val pillInactive: Color,
    val searchFieldBg: Color
)

private val Win11DarkPalette = Win11Colors(
    isDark = true,
    accent = WinAccentBlueLight,
    accentSoft = WinAccentBlue,
    micaSurface = WinDarkMicaSurface,
    acrylicTaskbar = WinDarkAcrylicTaskbar,
    cardSurface = WinDarkCardSurface,
    cardHover = WinDarkCardHover,
    border = WinDarkBorder,
    subtleBorder = WinDarkSubtleBorder,
    textPrimary = WinDarkTextPrimary,
    textSecondary = WinDarkTextSecondary,
    desktopIconText = Color.White,
    selectionFill = WinSelectionFill,
    selectionBorder = WinSelectionBorder,
    pillActive = WinAccentCyan,
    pillInactive = Color(0x80FFFFFF),
    searchFieldBg = Color(0xFF141820)
)

private val Win11LightPalette = Win11Colors(
    isDark = false,
    accent = WinAccentBlue,
    accentSoft = WinAccentBlueHover,
    micaSurface = WinLightMicaSurface,
    acrylicTaskbar = WinLightAcrylicTaskbar,
    cardSurface = WinLightCardSurface,
    cardHover = WinLightCardHover,
    border = WinLightBorder,
    subtleBorder = WinLightSubtleBorder,
    textPrimary = WinLightTextPrimary,
    textSecondary = WinLightTextSecondary,
    desktopIconText = Color.White,
    selectionFill = WinSelectionFill,
    selectionBorder = WinSelectionBorder,
    pillActive = WinAccentBlue,
    pillInactive = Color(0x731A1D23),
    searchFieldBg = Color(0xFFFFFFFF)
)

val LocalWin11Colors = staticCompositionLocalOf { Win11DarkPalette }

private val DarkColorScheme = darkColorScheme(
    primary = WinAccentBlueLight,
    onPrimary = Color(0xFF00203A),
    primaryContainer = WinAccentBlue,
    onPrimaryContainer = Color.White,
    secondary = WinAccentCyan,
    background = WinDarkBackground,
    onBackground = WinDarkTextPrimary,
    surface = WinDarkMicaSurface,
    onSurface = WinDarkTextPrimary,
    surfaceVariant = WinDarkCardSurface,
    onSurfaceVariant = WinDarkTextSecondary,
    outline = WinDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = WinAccentBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E9FC),
    onPrimaryContainer = Color(0xFF001E36),
    secondary = WinAccentBlueHover,
    background = WinLightBackground,
    onBackground = WinLightTextPrimary,
    surface = WinLightMicaSurface,
    onSurface = WinLightTextPrimary,
    surfaceVariant = WinLightCardSurface,
    onSurfaceVariant = WinLightTextSecondary,
    outline = WinLightBorder
)

@Composable
fun Win11LauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val win11Colors = if (darkTheme) Win11DarkPalette else Win11LightPalette

    CompositionLocalProvider(LocalWin11Colors provides win11Colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
