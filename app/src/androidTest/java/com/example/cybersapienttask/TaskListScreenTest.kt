package com.example.cybersapienttask

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.example.cybersapienttask.data.model.Task
import com.example.cybersapienttask.data.model.TaskFilter
import com.example.cybersapienttask.data.model.TaskPriority
import com.example.cybersapienttask.data.model.TaskSortOrder
import com.example.cybersapienttask.ui.screens.tasklist.TaskListScreen
import com.example.cybersapienttask.viewmodel.TaskListViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class TaskListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Mock ViewModel
    private val mockViewModel = mockk<TaskListViewModel>(relaxed = true)

    // Sample tasks for testing
    private val task1 = Task(
        id = 1,
        title = "Finish project",
        description = "Complete all requirements",
        priority = TaskPriority.HIGH,
        dueDate = LocalDate.now().plusDays(1),
        isCompleted = false
    )

    private val task2 = Task(
        id = 2,
        title = "Buy groceries",
        description = "Milk, eggs, bread",
        priority = TaskPriority.MEDIUM,
        dueDate = LocalDate.now().plusDays(2),
        isCompleted = true
    )

    private val task3 = Task(
        id = 3,
        title = "Call mom",
        description = "Ask about weekend plans",
        priority = TaskPriority.LOW,
        dueDate = LocalDate.now(),
        isCompleted = false
    )

    @Test
    fun taskList_displaysAllTasks() {
        // Setup mock data
        val taskListFlow = MutableStateFlow(listOf(task1, task2, task3))
        val filterFlow = MutableStateFlow(TaskFilter.ALL)
        val sortOrderFlow = MutableStateFlow(TaskSortOrder.DUE_DATE)
        val isManualOrderFlow = MutableStateFlow(false)
        val isLoadingFlow = MutableStateFlow(false)
        val taskStatsFlow = MutableStateFlow(
            TaskListViewModel.TaskStatistics(
                totalTasks = 3,
                completedTasks = 1
            )
        )

        every { mockViewModel.tasks } returns taskListFlow
        every { mockViewModel.filter } returns filterFlow
        every { mockViewModel.sortOrder } returns sortOrderFlow
        every { mockViewModel.isManualOrder } returns isManualOrderFlow
        every { mockViewModel.isLoading } returns isLoadingFlow
        every { mockViewModel.taskStats } returns taskStatsFlow

        // Launch the UI
        composeTestRule.setContent {
            TaskListScreen(
                viewModel = mockViewModel,
                onTaskClick = {},
                onAddTask = {},
                onSettingsClick = {}
            )
        }

        // Verify that all tasks are displayed
        composeTestRule.onNodeWithText("Finish project").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buy groceries").assertIsDisplayed()
        composeTestRule.onNodeWithText("Call mom").assertIsDisplayed()
        
        // Verify task stats are displayed
        composeTestRule.onNodeWithText("1/3").assertIsDisplayed()
        composeTestRule.onNodeWithText("33% Complete").assertIsDisplayed()
    }

    @Test
    fun taskList_filtersPendingTasks() {
        // Setup mock data
        val allTasksFlow = MutableStateFlow(listOf(task1, task2, task3))
        val pendingTasksFlow = MutableStateFlow(listOf(task1, task3))
        val filterFlow = MutableStateFlow(TaskFilter.ALL)
        val sortOrderFlow = MutableStateFlow(TaskSortOrder.DUE_DATE)
        val isManualOrderFlow = MutableStateFlow(false)
        val isLoadingFlow = MutableStateFlow(false)
        val taskStatsFlow = MutableStateFlow(
            TaskListViewModel.TaskStatistics(
                totalTasks = 3,
                completedTasks = 1
            )
        )

        every { mockViewModel.tasks } returns allTasksFlow
        every { mockViewModel.filter } returns filterFlow
        every { mockViewModel.sortOrder } returns sortOrderFlow
        every { mockViewModel.isManualOrder } returns isManualOrderFlow
        every { mockViewModel.isLoading } returns isLoadingFlow
        every { mockViewModel.taskStats } returns taskStatsFlow

        // Launch the UI
        composeTestRule.setContent {
            TaskListScreen(
                viewModel = mockViewModel,
                onTaskClick = {},
                onAddTask = {},
                onSettingsClick = {}
            )
        }

        // Click on the Pending filter
        composeTestRule.onNodeWithText("Pending").performClick()
        
        // Verify filter was set
        verify { mockViewModel.setFilter(TaskFilter.PENDING) }
        
        // Update the tasks flow to simulate filtering
        allTasksFlow.value = pendingTasksFlow.value
        
        // Verify only pending tasks are displayed
        composeTestRule.onNodeWithText("Finish project").assertIsDisplayed()
        composeTestRule.onNodeWithText("Call mom").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buy groceries").assertDoesNotExist()
    }

    @Test
    fun taskList_togglesTaskCompletion() {
        // Setup mock data
        val taskListFlow = MutableStateFlow(listOf(task1, task2, task3))
        val filterFlow = MutableStateFlow(TaskFilter.ALL)
        val sortOrderFlow = MutableStateFlow(TaskSortOrder.DUE_DATE)
        val isManualOrderFlow = MutableStateFlow(false)
        val isLoadingFlow = MutableStateFlow(false)
        val taskStatsFlow = MutableStateFlow(
            TaskListViewModel.TaskStatistics(
                totalTasks = 3,
                completedTasks = 1
            )
        )

        every { mockViewModel.tasks } returns taskListFlow
        every { mockViewModel.filter } returns filterFlow
        every { mockViewModel.sortOrder } returns sortOrderFlow
        every { mockViewModel.isManualOrder } returns isManualOrderFlow
        every { mockViewModel.isLoading } returns isLoadingFlow
        every { mockViewModel.taskStats } returns taskStatsFlow

        // Launch the UI
        composeTestRule.setContent {
            TaskListScreen(
                viewModel = mockViewModel,
                onTaskClick = {},
                onAddTask = {},
                onSettingsClick = {}
            )
        }

        // Find and click the checkbox for the first task
        composeTestRule.onNode(hasText("Finish project") and isDisplayed())
            .performScrollTo()
            .onChildren()
            .filterToOne(isToggleable())
            .performClick()
        
        // Verify toggle action was called
        verify { mockViewModel.toggleTaskCompletion(task1) }
    }

    @Test
    fun taskList_sortsTasksByPriority() {
        // Setup mock data
        val taskListFlow = MutableStateFlow(listOf(task1, task2, task3))
        val filterFlow = MutableStateFlow(TaskFilter.ALL)
        val sortOrderFlow = MutableStateFlow(TaskSortOrder.DUE_DATE)
        val isManualOrderFlow = MutableStateFlow(false)
        val isLoadingFlow = MutableStateFlow(false)
        val taskStatsFlow = MutableStateFlow(
            TaskListViewModel.TaskStatistics(
                totalTasks = 3,
                completedTasks = 1
            )
        )

        every { mockViewModel.tasks } returns taskListFlow
        every { mockViewModel.filter } returns filterFlow
        every { mockViewModel.sortOrder } returns sortOrderFlow
        every { mockViewModel.isManualOrder } returns isManualOrderFlow
        every { mockViewModel.isLoading } returns isLoadingFlow
        every { mockViewModel.taskStats } returns taskStatsFlow

        // Launch the UI
        composeTestRule.setContent {
            TaskListScreen(
                viewModel = mockViewModel,
                onTaskClick = {},
                onAddTask = {},
                onSettingsClick = {}
            )
        }

        // Click on sort button
        composeTestRule.onNode(hasContentDescription("Sort tasks")).performClick()
        
        // Click on "By Priority" option
        composeTestRule.onNodeWithText("By Priority").performClick()
        
        // Verify sort order was set
        verify { mockViewModel.setSortOrder(TaskSortOrder.PRIORITY) }
        
        // Update task list to simulate sorting by priority
        taskListFlow.value = listOf(task1, task2, task3).sortedByDescending { it.priority }
    }

    @Test
    fun emptyTaskList_showsEmptyState() {
        // Setup mock data with empty task list
        val taskListFlow = MutableStateFlow(emptyList<Task>())
        val filterFlow = MutableStateFlow(TaskFilter.ALL)
        val sortOrderFlow = MutableStateFlow(TaskSortOrder.DUE_DATE)
        val isManualOrderFlow = MutableStateFlow(false)
        val isLoadingFlow = MutableStateFlow(false)
        val taskStatsFlow = MutableStateFlow(
            TaskListViewModel.TaskStatistics(
                totalTasks = 0,
                completedTasks = 0
            )
        )

        every { mockViewModel.tasks } returns taskListFlow
        every { mockViewModel.filter } returns filterFlow
        every { mockViewModel.sortOrder } returns sortOrderFlow
        every { mockViewModel.isManualOrder } returns isManualOrderFlow
        every { mockViewModel.isLoading } returns isLoadingFlow
        every { mockViewModel.taskStats } returns taskStatsFlow

        // Launch the UI
        composeTestRule.setContent {
            TaskListScreen(
                viewModel = mockViewModel,
                onTaskClick = {},
                onAddTask = {},
                onSettingsClick = {}
            )
        }

        // Verify empty state elements are displayed
        composeTestRule.onNodeWithText("No Tasks Yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start creating tasks to organize your day and boost your productivity!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create My First Task").assertIsDisplayed()
    }
}