package com.example.cybersapienttask

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskManagerUITest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun test_emptyStateIsShown_whenNoTasks() {
        // Check that the empty state is displayed
        composeTestRule.onNodeWithText("No Tasks Yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create My First Task").assertIsDisplayed()
    }
    
    @Test
    fun test_taskCreationFlow() {
        // Click on FAB to create new task
        composeTestRule.onNodeWithText("New Task").performClick()
        
        // Check that we're on the creation screen
        composeTestRule.onNodeWithText("Create Task").assertIsDisplayed()
        
        // Enter task details
        composeTestRule.onNodeWithText("Title").performTextInput("Test Task")
        composeTestRule.onNodeWithText("Description").performTextInput("This is a test task")
        
        // Select High priority
        composeTestRule.onNodeWithText("H").performClick()
        
        // Save the task
        composeTestRule.onNodeWithContentDescription("Save task").performClick()
        
        // Verify that we're back on the list screen and task is shown
        composeTestRule.onNodeWithText("Task Manager").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Task").assertIsDisplayed()
        composeTestRule.onNodeWithText("This is a test task").assertIsDisplayed()
    }
    
    @Test
    fun test_taskFiltering() {
        // Create a completed task
        createTask("Completed Task", "This is completed", true)
        
        // Create a pending task
        createTask("Pending Task", "This is pending", false)
        
        // Filter by completed
        composeTestRule.onNodeWithContentDescription("Filter tasks").performClick()
        composeTestRule.onNodeWithText("Completed Tasks").performClick()
        
        // Check that only completed task is shown
        composeTestRule.onNodeWithText("Completed Task").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Pending Task").assertCountEquals(0)
        
        // Filter by pending
        composeTestRule.onNodeWithContentDescription("Filter tasks").performClick()
        composeTestRule.onNodeWithText("Pending Tasks").performClick()
        
        // Check that only pending task is shown
        composeTestRule.onNodeWithText("Pending Task").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Completed Task").assertCountEquals(0)
        
        // Show all tasks
        composeTestRule.onNodeWithContentDescription("Filter tasks").performClick()
        composeTestRule.onNodeWithText("All Tasks").performClick()
        
        // Check that all tasks are shown
        composeTestRule.onNodeWithText("Completed Task").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pending Task").assertIsDisplayed()
    }
    
    // Helper method to create a task
    private fun createTask(title: String, description: String, isCompleted: Boolean) {
        // Click on FAB to create new task
        composeTestRule.onNodeWithText("New Task").performClick()
        
        // Enter task details
        composeTestRule.onNodeWithText("Title").performTextInput(title)
        composeTestRule.onNodeWithText("Description").performTextInput(description)
        
        // Save the task
        composeTestRule.onNodeWithContentDescription("Save task").performClick()
        
        // If should be completed, click the checkbox
        if (isCompleted) {
            composeTestRule.onNodeWithText(title).performClick()
            composeTestRule.onNodeWithContentDescription("Mark as complete").performClick()
            composeTestRule.onNodeWithText("Task Manager").assertIsDisplayed()
        }
    }
}