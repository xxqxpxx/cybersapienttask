package com.example.cybersapienttask

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.cybersapienttask.data.model.Task
import com.example.cybersapienttask.data.model.TaskPriority
import com.example.cybersapienttask.ui.animations.circularConcealExitTransition
import com.example.cybersapienttask.ui.animations.circularRevealEnterTransition
import com.example.cybersapienttask.ui.animations.taskEnterTransition
import com.example.cybersapienttask.ui.components.BounceFAB
import com.example.cybersapienttask.ui.components.EmptyState
import com.example.cybersapienttask.ui.components.TaskItem
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalAnimationApi::class)
class AnimationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun fabBounceAnimation_triggersCorrectly() {
        composeTestRule.setContent {
            BounceFAB(
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = "Add Task",
                onClick = {},
                modifier = Modifier.fillMaxSize()
            )
        }

        // Animation requires some time to appear
        composeTestRule.waitForIdle()
        
        // Verify the FAB is displayed after the animation
        composeTestRule.onNodeWithTag("bounce_fab", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun taskEnterTransition_animatesTasks() {
        // Create a sample task
        val task = Task(
            id = 1,
            title = "Test Task",
            description = "Description",
            priority = TaskPriority.MEDIUM,
            dueDate = LocalDate.now(),
            isCompleted = false
        )

        composeTestRule.setContent {
            AnimatedTaskList(task)
        }

        // Allow time for animation to complete
        composeTestRule.waitForIdle()
        
        // Verify task is visible after animation
        composeTestRule.onNodeWithTag("animated_task").assertIsDisplayed()
    }

    @Test
    fun emptyStateAnimation_displaysCompletely() {
        composeTestRule.setContent {
            EmptyState(
                onAddTask = {},
                modifier = Modifier.fillMaxSize()
            )
        }

        // Wait for animations to complete
        composeTestRule.waitForIdle()
        
        // Verify elements are properly displayed after animation
        composeTestRule.onNodeWithTag("empty_state_illustration").assertIsDisplayed()
        composeTestRule.onNodeWithTag("empty_state_text").assertIsDisplayed()
        composeTestRule.onNodeWithTag("empty_state_button").assertIsDisplayed()
    }

    @Test
    fun circularRevealTransition_works() {
        var showDetails by mutableStateOf(false)
        
        composeTestRule.setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = showDetails,
                    enter = circularRevealEnterTransition(),
                    exit = circularConcealExitTransition()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("revealed_content")
                    )
                }
                
                if (!showDetails) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("trigger_button")
                            .clickable { showDetails = true }
                    )
                }
            }
        }
        
        // Trigger the reveal animation
        composeTestRule.onNodeWithTag("trigger_button").performClick()
        
        // Allow time for animation
        composeTestRule.waitForIdle()
        
        // Verify content is revealed
        composeTestRule.onNodeWithTag("revealed_content").assertIsDisplayed()
    }
}

@Composable
fun AnimatedTaskList(task: Task) {
    var visible by remember { mutableStateOf(false) }
    
    // Trigger animation after composition
    androidx.compose.runtime.LaunchedEffect(Unit) {
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = taskEnterTransition()
    ) {
        TaskItem(
            task = task,
            onTaskClick = {},
            onCheckboxClick = {},
            modifier = Modifier.testTag("animated_task")
        )
    }
}