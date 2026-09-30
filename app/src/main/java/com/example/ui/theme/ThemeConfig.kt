package com.example.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ThemePalette(
    val id: String,
    val displayName: String,
    val primary: Color,
    val primaryBright: Color,
    val primaryDark: Color,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val rainColor: Color
)

enum class ColorTheme(
    val id: String,
    val displayName: String,
    val palette: ThemePalette
) {
    MATRIX_GREEN(
        id = "matrix_green",
        displayName = "Matrix Green & Black",
        palette = ThemePalette(
            id = "matrix_green",
            displayName = "Matrix Green & Black",
            primary = Color(0xFF00FF66),
            primaryBright = Color(0xFF39FF14),
            primaryDark = Color(0xFF004D1A),
            background = Color(0xFF030704),
            surface = Color(0xFF08140B),
            surfaceElevated = Color(0xFF0F2414),
            textPrimary = Color(0xFFE6FFE8),
            textSecondary = Color(0xFF82B38D),
            textMuted = Color(0xFF476E4F),
            rainColor = Color(0xFF00FF66)
        )
    ),
    HOT_PINK(
        id = "hot_pink",
        displayName = "Hot Pink & Black",
        palette = ThemePalette(
            id = "hot_pink",
            displayName = "Hot Pink & Black",
            primary = Color(0xFFFF1493),
            primaryBright = Color(0xFFFF40A1),
            primaryDark = Color(0xFF5A0030),
            background = Color(0xFF080206),
            surface = Color(0xFF140510),
            surfaceElevated = Color(0xFF22091B),
            textPrimary = Color(0xFFFFEBF5),
            textSecondary = Color(0xFFD67DAE),
            textMuted = Color(0xFF8C4770),
            rainColor = Color(0xFFFF1493)
        )
    ),
    CRIMSON_RED(
        id = "crimson_red",
        displayName = "Red & Black",
        palette = ThemePalette(
            id = "crimson_red",
            displayName = "Red & Black",
            primary = Color(0xFFFF1E40),
            primaryBright = Color(0xFFFF4D6A),
            primaryDark = Color(0xFF5A0010),
            background = Color(0xFF080203),
            surface = Color(0xFF140407),
            surfaceElevated = Color(0xFF22080D),
            textPrimary = Color(0xFFFFEBEF),
            textSecondary = Color(0xFFD67D8D),
            textMuted = Color(0xFF8C4753),
            rainColor = Color(0xFFFF1E40)
        )
    ),
    CYBER_ORANGE(
        id = "cyber_orange",
        displayName = "Orange & Black",
        palette = ThemePalette(
            id = "cyber_orange",
            displayName = "Orange & Black",
            primary = Color(0xFFFF9100),
            primaryBright = Color(0xFFFFAB40),
            primaryDark = Color(0xFF5A2F00),
            background = Color(0xFF080401),
            surface = Color(0xFF140B03),
            surfaceElevated = Color(0xFF221305),
            textPrimary = Color(0xFFFFF2E6),
            textSecondary = Color(0xFFD69F7D),
            textMuted = Color(0xFF8C6047),
            rainColor = Color(0xFFFF9100)
        )
    )
}

val LocalThemePalette = staticCompositionLocalOf { ColorTheme.MATRIX_GREEN.palette }
