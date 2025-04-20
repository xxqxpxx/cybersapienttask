package com.example.cybersapienttask

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.cybersapienttask.data.model.Task
import com.example.cybersapienttask.data.model.TaskPriority
import com.example.cybersapienttask.ui.screens.TaskCreationScreen
import com.example.cybersapienttask.ui.screens.taskdetails.TaskDetailViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class TaskCreationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Mock ViewModel
    private val mockViewModel = mockk<TaskDetailViewModel>(relaxed = true)

    @Test
    fun taskCreation_savesTaskWithValidInput() {
        // Setup mock data
        val titleFlow = MutableStateFlow("Test Task")
        val descriptionFlow = MutableStateFlow("This is a test task")
        val priorityFlow = MutableStateFlow(TaskPriority.MEDIUM)
        val dueDateFlow = MutableStateFlow<LocalDate?>(LocalDate.now().plusDays(2))
        val isCompletedFlow = MutableStateFlow(false)
        val taskFlow = MutableStateFlow<Task?>(null)

        every { mockViewModel.title } returns titleFlow
        every { mockViewModel.description } returns descriptionFlow
        every { mockViewModel.priority } returns priorityFlow
        every { mockViewModel.dueDate } returns dueDateFlow
        every { mockViewModel.isCompleted } returns isCompletedFlow
        every { mockViewModel.task } returns taskFlow
        every { mockViewModel.saveTask() } returns true

        // Launch the UI for new task creation
        composeTestRule.setContent {
            TaskCreationScreen(
                viewModel = mockViewModel,
                isNewTask = true,
                onNavigateUp = {},
                onDeleteTask = {}
            )
        }

        // Verify the title is displayed correctly
        composeTestRule.onNode(hasText("Test Task") and hasSetTextAction()).assertIsDisplayed()

        // Update some fields
        composeTestRule.onNode(hasText("Test Task") and hasSetTextAction()).performTextClearance()
        composeTestRule.onNode(hasSetTextAction() and hasContentDescription("Title", substring = true, ignoreCase = true)).performTextInput("Updated Task Title")

        composeTestRule.onNode(hasText("This is a test task") and hasSetTextAction()).performTextClearance()
        composeTestRule.onNode(hasSetTextAction() and hasContentDescription("Description", substring = true, ignoreCase = true)).performTextInput("Updated task description")

        // Select high priority
        composeTestRule.onNode(hasContentDescription("High", substring = true, ignoreCase = true)).performClick()

        // Save the task by clicking FAB
        composeTestRule.onNode(hasContentDescription("Save task")).performClick()

        // Verify that model values were updated
        verify { mockViewModel.updateTitle("Updated Task Title") }
        verify { mockViewModel.updateDescription("Updated task description") }
        verify { mockViewModel.updatePriority(TaskPriority.HIGH) }
        verify { mockViewModel.saveTask() }
    }

    @Test
    fun taskCreation_validatesTitleIsRequired() {
        // Setup mock data with empty title
        val titleFlow = MutableStateFlow("")
        val descriptionFlow = MutableStateFlow("This is a test task")
        val priorityFlow = MutableStateFlow(TaskPriority.MEDIUM)
        val dueDateFlow = MutableStateFlow<LocalDate?>(LocalDate.now().plusDays(2))
        val isCompletedFlow = MutableStateFlow(false)
        val taskFlow = MutableStateFlow<Task?>(null)

        every { mockViewModel.title } returns titleFlow
        every { mockViewModel.description } returns descriptionFlow
        every { mockViewModel.priority } returns priorityFlow
        every { mockViewModel.dueDate } returns dueDateFlow
        every { mockViewModel.isCompleted } returns isCompletedFlow
        every { mockViewModel.task } returns taskFlow
        every { mockViewModel.saveTask() } returns false

        // Launch the UI for new task creation
        composeTestRule.setContent {
            TaskCreationScreen(
                viewModel = mockViewModel,
                isNewTask = true,
                onNavigateUp = {},
                onDeleteTask = {}
            )
        }

        // Try to save the task
        composeTestRule.onNode(hasContentDescription("Save task")).performClick()

        // Verify that task was not saved (snackbar should appear with error)
        composeTestRule.onNodeWithText("Title cannot be empty").assertExists()
    }

    @Test
    fun taskEdit_loadsExistingTaskData() {
        // Create existing task data
        val existingTask = Task(
            id = 1,
            title = "Existing Task",
            description = "This is an existing task",
            priority = TaskPriority.HIGH,
            dueDate = LocalDate.now().plusDays(3),
            isCompleted = false
        )

        // Setup mock data
        val titleFlow = MutableStateFlow(existingTask.title)
        val descriptionFlow = MutableStateFlow(existingTask.description)
        val priorityFlow = MutableStateFlow(existingTask.priority)
        val dueDateFlow = MutableStateFlow(existingTask.dueDate)
        val isCompletedFlow = MutableStateFlow(existingTask.isCompleted)
        val taskFlow = MutableStateFlow<Task?>(existingTask)

        every { mockViewModel.title } returns titleFlow
        every { mockViewModel.description } returns descriptionFlow
        every { mockViewModel.priority } returns priorityFlow
        every { mockViewModel.dueDate } returns dueDateFlow
        every { mockViewModel.isCompleted } returns isCompletedFlow
        every { mockViewModel.task } returns taskFlow
        every { mockViewModel.saveTask() } returns true

        // Launch the UI for editing
        composeTestRule.setContent {
            TaskCreationScreen(
                viewModel = mockViewModel,
                isNewTask = false,
                onNavigateUp = {},
                onDeleteTask = {}
            )
        }

        // Verify existing data is displayed
        composeTestRule.onNode(hasText("Existing Task") and hasSetTextAction()).assertIsDisplayed()
        composeTestRule.onNode(hasText("This is an existing task") and hasSetTextAction()).assertIsDisplayed()

        // Verify delete button is shown for editing (not for new tasks)
        composeTestRule.onNodeWithContentDescription("Delete task").assertIsDisplayed()
    }

    @Test
    fun taskCreation_priorityButtonsWork() {
        // Setup mock data
        val titleFlow = MutableStateFlow("Test Task")
        val descriptionFlow = MutableStateFlow("This is a test task")
        val priorityFlow = MutableStateFlow(TaskPriority.MEDIUM)
        val dueDateFlow = MutableStateFlow<LocalDate?>(null)
        val isCompletedFlow = MutableStateFlow(false)
        val taskFlow = MutableStateFlow<Task?>(null)

        every { mockViewModel.title } returns titleFlow
        every { mockViewModel.description } returns descriptionFlow
        every { mockViewModel.priority } returns priorityFlow
        every { mockViewModel.dueDate } returns dueDateFlow
        every { mockViewModel.isCompleted } returns isCompletedFlow
        every { mockViewModel.task } returns taskFlow

        // Launch the UI
        composeTestRule.setContent {
            TaskCreationScreen(
                viewModel = mockViewModel,
                isNewTask = true,
                onNavigateUp = {},
                onDeleteTask = {}
            )
        }

        // Change priority to low
        composeTestRule.onNodeWithText("L").performClick()
        verify { mockViewModel.updatePriority(TaskPriority.LOW) }

        // Update local flow to simulate change
        priorityFlow.value = TaskPriority.LOW

        // Change priority to high
        composeTestRule.onNodeWithText("H").performClick()
        verify { mockViewModel.updatePriority(TaskPriority.HIGH) }
        
        // Update local flow to simulate change
        priorityFlow.value = TaskPriority.HIGH
    }
}