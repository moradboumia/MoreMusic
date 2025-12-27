package com.example.moremusic

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class Theme {
    LIGHT,
    DARK,
    SYSTEM
}

class ThemeViewModel(application: Application) : ViewModel() {

    private val sharedPreferences = application.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(getCurrentTheme())
    val theme = _theme.asStateFlow()

    fun setTheme(theme: Theme) {
        _theme.value = theme
        sharedPreferences.edit().putString("theme", theme.name).apply()
    }

    private fun getCurrentTheme(): Theme {
        val savedTheme = sharedPreferences.getString("theme", Theme.SYSTEM.name)
        return Theme.valueOf(savedTheme ?: Theme.SYSTEM.name)
    }

    fun toggleTheme() {
        val currentTheme = _theme.value
        val nextTheme = when (currentTheme) {
            Theme.LIGHT -> Theme.DARK
            Theme.DARK -> Theme.SYSTEM
            Theme.SYSTEM -> Theme.LIGHT
        }
        setTheme(nextTheme)
    }
}