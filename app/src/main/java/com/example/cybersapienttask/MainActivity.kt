package com.example.cybersapienttask

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cybersapienttask.data.local.TaskDatabase
import com.example.cybersapienttask.data.repo.TaskRepository
import com.example.cybersapienttask.ui.accessibility.LocalTextScaleFactor
import com.example.cybersapienttask.ui.navigation.AppNavigation
import com.example.cybersapienttask.ui.screens.settings.SettingsViewModel
import com.example.cybersapienttask.ui.screens.settings.SettingsViewModelFactory
import com.example.cybersapienttask.ui.screens.settings.dataStore
import com.example.cybersapienttask.ui.theme.TaskManagerTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = TaskDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(database.taskDao(), database.taskOrderDao())

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(dataStore)
            )

            val primaryColor by settingsViewModel.primaryColor.collectAsState()
            val highContrastMode by settingsViewModel.highContrastMode.collectAsState()
            val textScaleFactor by settingsViewModel.textScaleFactor.collectAsState()
            var zz = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(zz) }

            // Provide the text scale factor to all composables
            CompositionLocalProvider(LocalTextScaleFactor provides textScaleFactor) {
                TaskManagerTheme(
                    darkTheme = isDarkTheme,
                    customPrimaryColor = primaryColor,
                    //    highContrastMode = highContrastMode
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(
                            repository = repository,
                            settingsViewModel = settingsViewModel,
                            isDarkTheme = isDarkTheme,
                            isHighContrastMode = highContrastMode,
                            textScaleFactor = textScaleFactor,
                            onToggleDarkTheme = { isDarkTheme = it },
                            onToggleHighContrastMode = { settingsViewModel.updateHighContrastMode(it) },
                            onUpdateTextScale = { settingsViewModel.updateTextScaleFactor(it) }
                        )
                    }
                }
            }
        }
    }
}