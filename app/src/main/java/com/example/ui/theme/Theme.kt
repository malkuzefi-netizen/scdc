package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TacticalMonochromeColorScheme = darkColorScheme(
    primary = TacticalWhite,
    onPrimary = TacticalPitchBlack,
    primaryContainer = TacticalSurfaceElevated,
    onPrimaryContainer = TacticalWhite,
    secondary = TacticalHighlightWhite,
    onSecondary = TacticalPitchBlack,
    secondaryContainer = TacticalSurfaceMedium,
    onSecondaryContainer = TacticalWhite,
    tertiary = TacticalTextSecondary,
    onTertiary = TacticalPitchBlack,
    background = TacticalPitchBlack,
    onBackground = TacticalTextPrimary,
    surface = TacticalSurfaceDark,
    onSurface = TacticalTextPrimary,
    surfaceVariant = TacticalSurfaceMedium,
    onSurfaceVariant = TacticalTextSecondary,
    outline = TacticalBorderStrong,
    outlineVariant = TacticalBorderSubtle
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Strictly adhere to military Black and White HUD design
    MaterialTheme(
        colorScheme = TacticalMonochromeColorScheme,
        typography = Typography,
        content = content
    )
}
