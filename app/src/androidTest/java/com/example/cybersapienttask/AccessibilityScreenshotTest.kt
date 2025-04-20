package com.example.cybersapienttask

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.example.cybersapienttask.data.local.TaskDao
import com.example.cybersapienttask.data.local.TaskOrderDao
import com.example.cybersapienttask.domain.repo.TaskRepository
import com.example.cybersapienttask.ui.accessibility.LocalTextScaleFactor
import com.example.cybersapienttask.ui.screens.tasklist.TaskListScreen
import com.example.cybersapienttask.ui.theme.TaskManagerTheme
import com.example.cybersapienttask.viewmodel.TaskListViewModel
import org.junit.Rule
import org.junit.Test
import java.io.File

import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith



private lateinit var taskDao: TaskDao
private lateinit var taskOrderDao: TaskOrderDao


// In a utility file like PreviewUtils.kt
fun getMockViewModel(): TaskListViewModel {
    // Create a mock repository
    val mockRepository = TaskRepository(taskDao , taskOrderDao)

    // Return a ViewModel that uses the mock repository
    return TaskListViewModel(mockRepository)
}


@RunWith(RobolectricTestRunner::class)
class AccessibilityScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    
    private val context: Context = ApplicationProvider.getApplicationContext()
    
    // Test configurations
    private val configurations = listOf(
        AccessibilityConfig(darkTheme = false, textScale = 1.0f, highContrast = false),
        AccessibilityConfig(darkTheme = false, textScale = 1.0f, highContrast = true),
        AccessibilityConfig(darkTheme = false, textScale = 1.3f, highContrast = false),
        AccessibilityConfig(darkTheme = false, textScale = 1.3f, highContrast = true),
        AccessibilityConfig(darkTheme = true, textScale = 1.0f, highContrast = false),
        AccessibilityConfig(darkTheme = true, textScale = 1.0f, highContrast = true),
        AccessibilityConfig(darkTheme = true, textScale = 1.3f, highContrast = false),
        AccessibilityConfig(darkTheme = true, textScale = 1.3f, highContrast = true)
    )
    
    @Test
    fun testTaskListAccessibility() {
        configurations.forEach { config ->
            testScreenWithConfig("task_list", config) {
                TaskListScreen(
                    viewModel = getMockViewModel(),
                    onTaskClick = {},
                    onAddTask = {},
                    onSettingsClick = {}
                )
            }
        }
    }
    
    // Helper method to render and capture a screenshot with the specified configuration
    private fun testScreenWithConfig(
        screenName: String,
        config: AccessibilityConfig,
        content: @Composable () -> Unit
    ) {
        // Configure and render the screen
        composeTestRule.setContent {
            CompositionLocalProvider(LocalTextScaleFactor provides config.textScale) {
                TaskManagerTheme(
                    darkTheme = config.darkTheme,
                     config.highContrast
                ) {
                    content()
                }
            }
        }
        
        // Capture screenshot
        val configSuffix = "${if (config.darkTheme) "dark" else "light"}_" +
                           "${if (config.highContrast) "contrast" else "normal"}_" +
                           "scale${config.textScale}"
        takeScreenshot("${screenName}_${configSuffix}")
    }
    
    // Helper to capture screenshots
    private fun takeScreenshot(name: String) {
        val directory = File(context.filesDir, "accessibility_screenshots")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        
        val screenshotFile = File(directory, "$name.png")
        composeTestRule.onRoot().captureToImage().asAndroidBitmap().compress(
            android.graphics.Bitmap.CompressFormat.PNG,
            100,
            screenshotFile.outputStream()
        )
    }
    
    // Data class for test configurations
    data class AccessibilityConfig(
        val darkTheme: Boolean,
        val textScale: Float,
        val highContrast: Boolean
    )
}


