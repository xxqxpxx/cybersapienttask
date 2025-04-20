package com.example.cybersapienttask

import org.robolectric.RobolectricTestRunner
import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.example.cybersapienttask.data.model.Task
import com.example.cybersapienttask.data.model.TaskPriority
import com.example.cybersapienttask.ui.components.TaskItem
import com.example.cybersapienttask.ui.theme.TaskManagerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate


@RunWith(RobolectricTestRunner::class)
class ScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    
    private val context: Context = ApplicationProvider.getApplicationContext()
    
    @Test
    fun test_taskItem_lightMode() {
        // Set up sample task
        val task = Task(
            id = 1,
            title = "Sample Task",
            description = "This is a sample task for screenshot testing",
            priority = TaskPriority.HIGH,
            dueDate = LocalDate.now().plusDays(1),
            isCompleted = false
        )
        
        // Render TaskItem in light mode
        composeTestRule.setContent {
            TaskManagerTheme(darkTheme = false) {
                TaskItem(
                    task = task,
                    onTaskClick = {},
                    onCheckboxClick = {}
                )
            }
        }
        
        // Take screenshot
        takeScreenshot("task_item_light")
    }
    
    @Test
    fun test_taskItem_darkMode() {
        // Set up sample task
        val task = Task(
            id = 1,
            title = "Sample Task",
            description = "This is a sample task for screenshot testing",
            priority = TaskPriority.HIGH,
            dueDate = LocalDate.now().plusDays(1),
            isCompleted = false
        )
        
        // Render TaskItem in dark mode
        composeTestRule.setContent {
            TaskManagerTheme(darkTheme = true) {
                TaskItem(
                    task = task,
                    onTaskClick = {},
                    onCheckboxClick = {}
                )
            }
        }
        
        // Take screenshot
        takeScreenshot("task_item_dark")
    }
    
    private fun takeScreenshot(name: String) {
        // Create directory if it doesn't exist
        val directory = File(context.filesDir, "screenshots")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        
        // Create bitmap and save to file
        val screenshotFile = File(directory, "$name.png")
        composeTestRule.onRoot().captureToImage().asAndroidBitmap().compress(
            android.graphics.Bitmap.CompressFormat.PNG,
            100,
            screenshotFile.outputStream()
        )
    }
}