package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the state of the app's cyberpunk color schemes (Hot Pink, Red, Orange, Matrix Green)
 * and persists the user's selected choice across application sessions.
 */
class ThemeManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _currentTheme = MutableStateFlow(loadSavedTheme())
    val currentTheme: StateFlow<ColorTheme> = _currentTheme.asStateFlow()

    private val _currentPalette = MutableStateFlow(_currentTheme.value.palette)
    val currentPalette: StateFlow<ThemePalette> = _currentPalette.asStateFlow()

    /**
     * Switch to a new color scheme and persist the choice.
     */
    fun setTheme(theme: ColorTheme) {
        _currentTheme.value = theme
        _currentPalette.value = theme.palette
        prefs.edit().putString(KEY_THEME_ID, theme.id).apply()
    }

    /**
     * Returns all available cyberpunk color schemes.
     */
    fun getAvailableThemes(): List<ColorTheme> = ColorTheme.entries.toList()

    /**
     * Reset to the default theme (Matrix Green).
     */
    fun resetToDefault() {
        setTheme(ColorTheme.MATRIX_GREEN)
    }

    private fun loadSavedTheme(): ColorTheme {
        val savedId = prefs.getString(KEY_THEME_ID, ColorTheme.MATRIX_GREEN.id)
        return ColorTheme.entries.find { it.id == savedId } ?: ColorTheme.MATRIX_GREEN
    }

    companion object {
        private const val PREFS_NAME = "cyberpunk_theme_preferences"
        private const val KEY_THEME_ID = "cyberpunk_selected_theme"

        @Volatile
        private var instance: ThemeManager? = null

        fun getInstance(context: Context): ThemeManager {
            return instance ?: synchronized(this) {
                instance ?: ThemeManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
