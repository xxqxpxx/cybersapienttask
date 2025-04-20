package com.example.cybersapienttask

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.cybersapienttask.data.model.Task
import com.example.cybersapienttask.data.model.TaskPriority
import com.example.cybersapienttask.ui.screens.taskdetails.TaskDetailScreen
import com.example.cybersapienttask.ui.screens.taskdetails.TaskDetailViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TaskDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Mock ViewModel
    private val mockViewModel = mockk<TaskDetailViewModel>(relaxed = true)

    // Sample task for testing
    private val sampleTask = Task(
        id = 1,
        title = "Complete project",
        description = "Finish all requirements and submit documentation",
        priority = TaskPriority.HIGH,
        dueDate = LocalDate.now().plusDays(2),
        isCompleted = false,
        createdDate = LocalDate.now().minusDays(3)
    )

    @Test
    fun taskDetail_displaysTaskInfo() {
        // Setup mock data
        val taskFlow = MutableStateFlow<Task?>(sampleTask)
        val isCompletedFlow = MutableStateFlow(sampleTask.isCompleted)

        every { mockViewModel.task } returns taskFlow
        every { mockViewModel.isCompleted } returns isCompletedFlow

        // Launch the UI
        composeTestRule.setContent {
            TaskDetailScreen(
                viewModel = mockViewModel,
                onEditTask = {},
                onNavigateUp = {},
                onTaskDeleted = {}
            )
        }

        // Verify task information is displayed
        composeTestRule.onNodeWithText("Complete project").assertIsDisplayed()
        composeTestRule.onNodeWithText("Finish all requirements and submit documentation").assertIsDisplayed()

        // Verify dates are displayed
        val dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
        composeTestRule.onNodeWithText("Created: ${sampleTask.createdDate.format(dateFormatter)}", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Due: ${sampleTask.dueDate?.format(dateFormatter)}", substring = true).assertIsDisplayed()
        
        // Verify priority indicator (H for HIGH)
        composeTestRule.onNodeWithText("H").assertIsDisplayed()
        
        // Verify completion button text
        composeTestRule.onNodeWithText("Mark as Completed").assertIsDisplayed()
    }

    @Test
    fun taskDetail_togglesTaskCompletion() {
        // Setup mock data
        val taskFlow = MutableStateFlow<Task?>(sampleTask)
        val isCompletedFlow = MutableStateFlow(sampleTask.isCompleted)

        every { mockViewModel.task } returns taskFlow
        every { mockViewModel.isCompleted } returns isCompletedFlow

        // Launch the UI
        composeTestRule.setContent {
            TaskDetailScreen(
                viewModel = mockViewModel,
                onEditTask = {},
                onNavigateUp = {},
                onTaskDeleted = {}
            )
        }

        // Click the completion button
        composeTestRule.onNodeWithText("Mark as Completed").performClick()
        
        // Verify ViewModel methods were called
        verify { mockViewModel.toggleCompletion() }
        verify { mockViewModel.saveTask() }
        
        // Update the isCompleted flow to simulate the change
        isCompletedFlow.value = true
        
        // Verify button text changes
        composeTestRule.onNodeWithText("Mark as Pending").assertIsDisplayed()
    }

    @Test
    fun taskDetail_deletesTask() {
        // Setup mock data
        val taskFlow = MutableStateFlow<Task?>(sampleTask)
        val isCompletedFlow = MutableStateFlow(sampleTask.isCompleted)

        every { mockViewModel.task } returns taskFlow
        every { mockViewModel.isCompleted } returns isCompletedFlow

        // Track whether onTaskDeleted was called
        var taskDeletedCalled = false

        // Launch the UI
        composeTestRule.setContent {
            TaskDetailScreen(
                viewModel = mockViewModel,
                onEditTask = {},
                onNavigateUp = {},
                onTaskDeleted = { taskDeletedCalled = true }
            )
        }

        // Click the delete button in the top bar
        composeTestRule.onNodeWithContentDescription("Delete task").performClick()
        
        // Verify ViewModel method was called
        verify { mockViewModel.deleteTask() }
        
        // Verify onTaskDeleted callback was triggered
        assert(taskDeletedCalled)
    }

    @Test
    fun taskDetail_navigatesToEditScreen() {
        // Setup mock data
        val taskFlow = MutableStateFlow<Task?>(sampleTask)
        val isCompletedFlow = MutableStateFlow(sampleTask.isCompleted)

        every { mockViewModel.task } returns taskFlow
        every { mockViewModel.isCompleted } returns isCompletedFlow

        // Track whether onEditTask was called
        var editTaskCalled = false

        // Launch the UI
        composeTestRule.setContent {
            TaskDetailScreen(
                viewModel = mockViewModel,
                onEditTask = { editTaskCalled = true },
                onNavigateUp = {},
                onTaskDeleted = {}
            )
        }

        // Click the edit FAB
        composeTestRule.onNodeWithContentDescription("Edit task").performClick()
        
        // Verify onEditTask callback was triggered
        assert(editTaskCalled)
    }

    @Test
    fun taskDetail_showsLoadingStateWhenTaskIsNull() {
        // Setup mock data with null task (loading state)
        val taskFlow = MutableStateFlow<Task?>(null)
        val isCompletedFlow = MutableStateFlow(false)

        every { mockViewModel.task } returns taskFlow
        every { mockViewModel.isCompleted } returns isCompletedFlow

        // Launch the UI
        composeTestRule.setContent {
            TaskDetailScreen(
                viewModel = mockViewModel,
                onEditTask = {},
                onNavigateUp = {},
                onTaskDeleted = {}
            )
        }

        // Verify CircularProgressIndicator is shown (loading state)
        // We can't directly check for the CircularProgressIndicator, but the task title should not be visible
        composeTestRule.onNodeWithText("Complete project").assertDoesNotExist()
        
        // CircularProgressIndicator should exist
        composeTestRule.onNode(hasTestTag("CircularProgressIndicator")).assertExists()
    }
}