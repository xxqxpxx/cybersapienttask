package com.example.cybersapienttask

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cybersapienttask.data.model.Task
import com.example.cybersapienttask.data.model.TaskPriority
import com.example.cybersapienttask.domain.repo.TaskRepository
import com.example.cybersapienttask.ui.accessibility.LocalTextScaleFactor
import com.example.cybersapienttask.ui.screens.TaskCreationScreen
import com.example.cybersapienttask.ui.screens.settings.SettingsViewModel
import com.example.cybersapienttask.ui.screens.taskdetails.TaskDetailScreen
import com.example.cybersapienttask.ui.screens.taskdetails.TaskDetailViewModel
import com.example.cybersapienttask.ui.screens.tasklist.TaskListScreen
import com.example.cybersapienttask.ui.theme.TaskManagerTheme
import com.example.cybersapienttask.viewmodel.TaskListViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.S])
class ScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var screenshotDir: File

    // Mock repositories and view models
    private val mockTaskRepository = mockk<TaskRepository>(relaxed = true)
    private val mockTaskListViewModel = mockk<TaskListViewModel>(relaxed = true)
    private val mockTaskDetailViewModel = mockk<TaskDetailViewModel>(relaxed = true)
    private val mockSettingsViewModel = mockk<SettingsViewModel>(relaxed = true)

    // Sample test data
    private val sampleTasks = listOf(
        Task(
            id = 1,
            title = "Complete project",
            description = "Finish all requirements",
            priority = TaskPriority.HIGH,
            dueDate = LocalDate.now().plusDays(2),
            isCompleted = false
        ),
        Task(
            id = 2,
            title = "Buy groceries",
            description = "Milk, eggs, bread",
            priority = TaskPriority.MEDIUM,
            dueDate = LocalDate.now().plusDays(1),
            isCompleted = true
        ),
        Task(
            id = 3,
            title = "Call mom",
            description = "Ask about weekend plans",
            priority = TaskPriority.LOW,
            dueDate = LocalDate.now(),
            isCompleted = false
        )
    )

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        screenshotDir = File(context.cacheDir, "screenshots").apply {
            if (!exists()) mkdirs()
        }

        // Setup mock data for TaskListViewModel
        val taskListFlow = MutableStateFlow(sampleTasks)
        val filterFlow = MutableStateFlow(com.example.cybersapienttask.data.model.TaskFilter.ALL)
        val sortOrderFlow = MutableStateFlow(com.example.cybersapienttask.data.model.TaskSortOrder.DUE_DATE)
        val isManualOrderFlow = MutableStateFlow(false)
        val isLoadingFlow = MutableStateFlow(false)
        val taskStatsFlow = MutableStateFlow(
            TaskListViewModel.TaskStatistics(
                totalTasks = 3,
                completedTasks = 1
            )
        )

        every { mockTaskListViewModel.tasks } returns taskListFlow
        every { mockTaskListViewModel.filter } returns filterFlow
        every { mockTaskListViewModel.sortOrder } returns sortOrderFlow
        every { mockTaskListViewModel.isManualOrder } returns isManualOrderFlow
        every { mockTaskListViewModel.isLoading } returns isLoadingFlow
        every { mockTaskListViewModel.taskStats } returns taskStatsFlow

        // Setup mock data for TaskDetailViewModel
        val taskFlow = MutableStateFlow<Task?>(sampleTasks[0])
        val titleFlow = MutableStateFlow(sampleTasks[0].title)
        val descriptionFlow = MutableStateFlow(sampleTasks[0].description)
        val priorityFlow = MutableStateFlow(sampleTasks[0].priority)
        val dueDateFlow = MutableStateFlow(sampleTasks[0].dueDate)
        val isCompletedFlow = MutableStateFlow(sampleTasks[0].isCompleted)

        every { mockTaskDetailViewModel.task } returns taskFlow
        every { mockTaskDetailViewModel.title } returns titleFlow
        every { mockTaskDetailViewModel.description } returns descriptionFlow
        every { mockTaskDetailViewModel.priority } returns priorityFlow
        every { mockTaskDetailViewModel.dueDate } returns dueDateFlow
        every { mockTaskDetailViewModel.isCompleted } returns isCompletedFlow

        // Setup mock data for SettingsViewModel
        val primaryColorFlow = MutableStateFlow(androidx.compose.ui.graphics.Color(0xFF6650a4))
        val highContrastModeFlow = MutableStateFlow(false)
        val textScaleFactorFlow = MutableStateFlow(1.0f)

        every { mockSettingsViewModel.primaryColor } returns primaryColorFlow
        every { mockSettingsViewModel.highContrastMode } returns highContrastModeFlow
        every { mockSettingsViewModel.textScaleFactor } returns textScaleFactorFlow
    }

    @Test
    fun captureScreenshots_lightAndDarkMode() {
        // Capture screenshots of different screens in light and dark mode
        captureScreenshot("task_list_light", false) {
            TaskListScreen(
                viewModel = mockTaskListViewModel,
                onTaskClick = {},
                onAddTask = {},
                onSettingsClick = {}
            )
        }

        captureScreenshot("task_list_dark", true) {
            TaskListScreen(
                viewModel = mockTaskListViewModel,
                onTaskClick = {},
                onAddTask = {},
                onSettingsClick = {}
            )
        }

        captureScreenshot("task_detail_light", false) {
            TaskDetailScreen(
                viewModel = mockTaskDetailViewModel,
                onEditTask = {},
                onNavigateUp = {},
                onTaskDeleted = {}
            )
        }

        captureScreenshot("task_detail_dark", true) {
            TaskDetailScreen(
                viewModel = mockTaskDetailViewModel,
                onEditTask = {},
                onNavigateUp = {},
                onTaskDeleted = {}
            )
        }

        captureScreenshot("task_creation_light", false) {
            TaskCreationScreen(
                viewModel = mockTaskDetailViewModel,
                isNewTask = true,
                onNavigateUp = {},
                onDeleteTask = {}
            )
        }

        captureScreenshot("task_creation_dark", true) {
            TaskCreationScreen(
                viewModel = mockTaskDetailViewModel,
                isNewTask = true,
                onNavigateUp = {},
                onDeleteTask = {}
            )
        }
    }

    @Test
    fun captureScreenshots_accessibilityModes() {
        // Capture screenshots with different accessibility settings

        // High contrast mode
        every { mockSettingsViewModel.highContrastMode } returns MutableStateFlow(true)

        captureScreenshot("task_list_high_contrast", false) {
            TaskListScreen(
                viewModel = mockTaskListViewModel,
                onTaskClick = {},
                onAddTask = {},
                onSettingsClick = {}
            )
        }

        // Large text mode
        every { mockSettingsViewModel.highContrastMode } returns MutableStateFlow(false)
        every { mockSettingsViewModel.textScaleFactor } returns MutableStateFlow(1.5f)

        captureScreenshot("task_list_large_text", false) {
            TaskListScreen(
                viewModel = mockTaskListViewModel,
                onTaskClick = {},
                onAddTask = {},
                onSettingsClick = {}
            )
        }
    }

    private fun captureScreenshot(name: String, darkMode: Boolean, content: @Composable () -> Unit) {
        composeTestRule.setContent {
            val primaryColor by mockSettingsViewModel.primaryColor.collectAsState()
            val highContrastMode by mockSettingsViewModel.highContrastMode.collectAsState()
            val textScaleFactor by mockSettingsViewModel.textScaleFactor.collectAsState()

            CompositionLocalProvider(LocalTextScaleFactor provides textScaleFactor) {
                TaskManagerTheme(
                    darkTheme = darkMode,
                    customPrimaryColor = primaryColor
                ) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        content()
                    }
                }
            }
        }

        // Wait for rendering to complete
        composeTestRule.waitForIdle()

        // Capture the screenshot
        val bitmap = composeTestRule.onRoot().captureToImage().asAndroidBitmap()
        saveScreenshot(name, bitmap)
    }

    private fun saveScreenshot(name: String, bitmap: Bitmap) {
        val file = File(screenshotDir, "$name.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        println("Screenshot saved to: ${file.absolutePath}")
    }
}

// Extension function to convert Compose ImageBitmap to Android Bitmap
fun androidx.compose.ui.graphics.ImageBitmap.asAndroidBitmap(): Bitmap {
    val width = this.width
    val height = this.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val pixels = IntArray(width * height)
    this.readPixels(pixels, 0, 0, 0, width, height)
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    return bitmap
}