package com.example.cybersapienttask.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.cybersapienttask.data.model.Task
import kotlin.math.roundToInt

@Composable
fun DraggableTaskItem(
    task: Task,
    onTaskClick: (Task) -> Unit,
    onCheckboxClick: (Task) -> Unit,
    onDragComplete: (Int, Int) -> Unit,
    isDraggable: Boolean,
    currentPosition: Int,
    updateDragPosition: (Int, Float) -> Int,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val itemHeight = 116.dp  // Approximate height of TaskItem

    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }

    val elevation by animateDpAsState(
        targetValue = if (isDragging) 8.dp else 0.dp,
        label = "elevation"
    )

    // Calculate how many items we've dragged past
    val dragDistance = (dragOffset / itemHeight.value).roundToInt()

    // The dragging modifier
    val draggableModifier = if (isDraggable) {
        Modifier.pointerInput(task.id) {
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    isDragging = true
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onDragEnd = {
                    isDragging = false

                    // If we've dragged, update the position
                    if (dragDistance != 0) {
                        // Calculate the target position
                        val newPosition = currentPosition + dragDistance
                        onDragComplete(currentPosition, newPosition)
                    }

                    // Reset drag state
                    dragOffset = 0f
                },
                onDragCancel = {
                    isDragging = false
                    dragOffset = 0f
                },
                onDrag = { change, dragAmount ->
                    change.consume()

                    // Update drag offset
                    dragOffset += dragAmount.y

                    // Update the UI to reflect where this would be dropped
                    updateDragPosition(currentPosition, dragOffset)
                }
            )
        }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .padding(
                start = 8.dp,
                end = 8.dp,
                top = 4.dp,
                bottom = 4.dp
            )
            .shadow(
                elevation = elevation,
                shape = MaterialTheme.shapes.medium
            )
            .then(draggableModifier)
            .offset { IntOffset(0, dragOffset.roundToInt()) }
    ) {
        TaskItem(
            task = task,
            onTaskClick = onTaskClick,
            onCheckboxClick = onCheckboxClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}