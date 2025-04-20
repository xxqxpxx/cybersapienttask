package com.example.cybersapienttask.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.cybersapienttask.data.model.Task
import kotlinx.coroutines.delay

/**
 * A swipeable task item that supports:
 * - Swipe right to mark as complete
 * - Swipe left to delete
 * With visual feedback and haptic response
 *
 * Uses the newer SwipeToDismissBox API with SwipeToDismissBoxValue
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTaskItem(
    task: Task,
    onTaskClick: (Task) -> Unit,
    onCheckboxClick: (Task) -> Unit,
    onComplete: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    var isRemoved by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current

    val dismissState = rememberSwipeToDismissBoxState(
        initialValue = SwipeToDismissBoxValue.Settled,
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    // Swiped to delete
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    isRemoved = true
                    true
                }

                SwipeToDismissBoxValue.StartToEnd -> {
                    // Swiped to complete
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onComplete(task)
                    false  // We don't want to keep it dismissed
                }

                SwipeToDismissBoxValue.Settled -> false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.5f }
    )

    // If we've marked this as removed, actually delete it after the animation
    LaunchedEffect(isRemoved) {
        if (isRemoved) {
            delay(300)  // Wait for the animation to finish
            onDelete(task)
        }
    }

    // Reset if needed
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
            delay(500) // Wait for animation
            dismissState.reset()
        }
    }

    AnimatedVisibility(
        visible = !isRemoved,
        exit = fadeOut(animationSpec = tween(300)) +
                shrinkHorizontally(animationSpec = tween(300), shrinkTowards = Alignment.Start)
    ) {
        SwipeToDismissBox(
            state = dismissState,
            modifier = modifier,
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = true,
            backgroundContent = {
                SwipeBackground(dismissState)
            },
            content = {
                TaskItem(
                    task = task,
                    onTaskClick = onTaskClick,
                    onCheckboxClick = onCheckboxClick
                )
            }
        )
    }
}

/**
 * Background displayed during swipe gesture with color and icon based on direction
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeBackground(dismissState: SwipeToDismissBoxState) {
    val direction = dismissState.dismissDirection

    val color = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)  // Green for complete
        SwipeToDismissBoxValue.EndToStart -> Color(0xFFE91E63)  // Pink for delete
        SwipeToDismissBoxValue.Settled -> Color.Transparent
    }

    val alignment = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        SwipeToDismissBoxValue.Settled -> Alignment.Center
    }

    val icon = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Check
        SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
        SwipeToDismissBoxValue.Settled -> null
    }

    val iconTint = Color.White
    val targetValue = dismissState.targetValue

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        if (icon != null && targetValue != SwipeToDismissBoxValue.Settled) {
            Icon(
                imageVector = icon,
                contentDescription =
                if (direction == SwipeToDismissBoxValue.StartToEnd)
                    "Complete task"
                else
                    "Delete task",
                tint = iconTint
            )
        }
    }
}