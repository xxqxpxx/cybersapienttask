package com.example.cybersapienttask.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cybersapienttask.ui.accessibility.withScaleFactor

/**
 * Settings screen that allows users to customize app appearance and accessibility options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateUp: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleHighContrastMode: (Boolean) -> Unit,
    onUpdateTextScale: (Float) -> Unit,
    isDarkMode: Boolean,
    isHighContrastMode: Boolean,
    textScaleFactor: Float
) {
    val selectedColor by viewModel.primaryColor.collectAsState()
    var localTextScale by remember { mutableFloatStateOf(textScaleFactor) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateUp,
                        modifier = Modifier.semantics {
                            contentDescription = "Navigate back"
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Theme section
            SectionHeader(title = "Theme")

            // Dark mode switch
            SettingItem(
                title = "Dark Mode",
                subtitle = "Switch between light and dark theme",
                trailing = {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = onToggleDarkMode
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Color customization
            Text(
                text = "Primary Color",
                style = MaterialTheme.typography.titleMedium.withScaleFactor(),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Color picker
            ColorPickerRow(
                colors = SettingsViewModel.COLOR_OPTIONS,
                selectedColor = selectedColor,
                onColorSelected = { viewModel.updatePrimaryColor(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Accessibility section
            SectionHeader(title = "Accessibility")

            // High contrast mode switch
            SettingItem(
                title = "High Contrast Mode",
                subtitle = "Increase color contrast for better visibility",
                trailing = {
                    Switch(
                        checked = isHighContrastMode,
                        onCheckedChange = onToggleHighContrastMode
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Text size slider
            Text(
                text = "Text Size",
                style = MaterialTheme.typography.titleMedium.withScaleFactor(),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Text size label
                Text(
                    text = when {
                        localTextScale <= 0.85f -> "Small"
                        localTextScale <= 1.0f -> "Normal"
                        localTextScale <= 1.15f -> "Large"
                        localTextScale <= 1.3f -> "Larger"
                        else -> "Largest"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.End)
                )

                // Sample text that updates with the slider
                Text(
                    text = "This is a preview of the text size",
                    style = MaterialTheme.typography.bodyLarge.withScaleFactor(localTextScale),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Slider for text size
                Slider(
                    value = localTextScale,
                    onValueChange = { localTextScale = it },
                    valueRange = 0.85f..1.5f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.semantics {
                        contentDescription = "Adjust text size"
                    }
                )

                // Apply button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    androidx.compose.material3.TextButton(
                        onClick = { onUpdateTextScale(localTextScale) }
                    ) {
                        Text("Apply")
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // About section
            SectionHeader(title = "About")

            SettingItem(
                title = "Version",
                subtitle = "1.0.0",
                trailing = {}
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Section header with larger, emphasized text
 */
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.withScaleFactor().copy(
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

/**
 * Standard setting item with title, subtitle and trailing component
 */
@Composable
fun SettingItem(
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.withScaleFactor()
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.withScaleFactor(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            trailing()
        }
    }
}

/**
 * Row of color options for primary color selection
 */
@Composable
fun ColorPickerRow(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        colors.forEach { color ->
            ColorOption(
                color = color,
                isSelected = color == selectedColor,
                onClick = { onColorSelected(color) }
            )
        }
    }
}

/**
 * Single color option in the color picker
 */
@Composable
fun ColorOption(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .padding(4.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = 2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onClick() }
            .semantics {
                contentDescription = "Color option"
            },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White
            )
        }
    }
}