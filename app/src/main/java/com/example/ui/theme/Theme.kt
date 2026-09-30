package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun UndergroundTheme(
    selectedTheme: ColorTheme = ColorTheme.MATRIX_GREEN,
    content: @Composable () -> Unit
) {
    val palette = selectedTheme.palette

    val colorScheme = darkColorScheme(
        primary = palette.primary,
        onPrimary = Color.Black,
        primaryContainer = palette.primaryDark,
        onPrimaryContainer = palette.primaryBright,
        secondary = MatrixCyan,
        onSecondary = Color.Black,
        secondaryContainer = Color(0xFF00383D),
        onSecondaryContainer = MatrixCyan,
        tertiary = MatrixAmber,
        onTertiary = Color.Black,
        tertiaryContainer = Color(0xFF382700),
        onTertiaryContainer = MatrixAmber,
        background = palette.background,
        onBackground = palette.textPrimary,
        surface = palette.surface,
        onSurface = palette.textPrimary,
        surfaceVariant = palette.surfaceElevated,
        onSurfaceVariant = palette.textSecondary,
        outline = palette.primaryDark,
        outlineVariant = palette.primary.copy(alpha = 0.35f),
        error = MatrixRed,
        onError = Color.White
    )

    CompositionLocalProvider(LocalThemePalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
