package com.example.cybersapienttask.ui.screens.settings

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsViewModel(private val dataStore: DataStore<Preferences>) : ViewModel() {

    companion object {
        private val PRIMARY_COLOR_KEY = intPreferencesKey("primary_color")
        val DEFAULT_PRIMARY_COLOR = Color(0xFF6650a4).toArgb() // Default purple

        // Predefined color options
        val COLOR_OPTIONS = listOf(
            Color(0xFF6650a4), // Purple
            Color(0xFF03A9F4), // Blue
            Color(0xFFE91E63), // Pink
            Color(0xFF4CAF50), // Green
            Color(0xFFFF9800), // Orange
            Color(0xFF9C27B0)  // Deep Purple
        )
    }

    // Convert between Color and Int representation
    private fun Color.toArgb() = android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )

    private fun Int.toColor() = Color(this)

    // Read primary color from DataStore
    val primaryColor: StateFlow<Color> = dataStore.data
        .map { preferences ->
            preferences[PRIMARY_COLOR_KEY]?.toColor() ?: DEFAULT_PRIMARY_COLOR.toColor()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEFAULT_PRIMARY_COLOR.toColor()
        )

    // Save primary color to DataStore
    fun updatePrimaryColor(color: Color) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PRIMARY_COLOR_KEY] = color.toArgb()
            }
        }
    }
}

class SettingsViewModelFactory(private val dataStore: DataStore<Preferences>) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(dataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}