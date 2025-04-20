package com.example.cybersapienttask.ui.accessibility


import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.cybersapienttask.data.model.Task
import com.example.cybersapienttask.data.model.TaskPriority
import java.time.format.DateTimeFormatter


// Extension function to provide full task content description for screen readers
fun Modifier.taskAccessibility(task: Task): Modifier {
    // Format the due date if available
    val dueDateText = task.dueDate?.let {
        "Due date: ${it.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))}"
    } ?: "No due date"

    // Format priority
    val priorityText = when (task.priority) {
        TaskPriority.LOW -> "Low priority"
        TaskPriority.MEDIUM -> "Medium priority"
        TaskPriority.HIGH -> "High priority"
    }

    // Build the complete description
    val stateText = if (task.isCompleted) "Task completed" else "Task not completed"
    val fullDescription =
        "${task.title}. ${task.description}. $priorityText. $dueDateText. $stateText"

    return this.semantics {
        contentDescription = fullDescription
        stateDescription = stateText
        role = Role.Button
    }
}

// Extension modifier for headings (for screen readers)
fun Modifier.heading(): Modifier {
    return this.semantics { heading() }
}

// Extension modifier for custom accessibility actions
fun Modifier.customAccessibilityActions(
    actionLabels: List<Pair<String, () -> Boolean>>
): Modifier {
    val actions = actionLabels.map { (label, action) ->
        CustomAccessibilityAction(label, action)
    }
    return this.semantics {
        customActions = actions
    }
}

// Extension modifier for keyboard focus
fun Modifier.keyboardFocusable(
    onClick: () -> Unit
): Modifier {
    return this.pointerInput(Unit) {
        detectTapGestures {
            onClick()
        }
    }
}


/**
 * Extension function to create large-text-mode variations of a TextStyle
 *
 * @param scaleFactor The scaling factor for large text mode (default from LocalTextScaleFactor)
 * @param minSize Minimum font size in sp
 * @param maxSize Maximum font size in sp
 * @return A TextStyle with appropriately scaled font size
 */
@Composable
fun TextStyle.forLargeTextMode(
    scaleFactor: Float = LocalTextScaleFactor.current,
    minSize: TextUnit = 12.sp,
    maxSize: TextUnit = 32.sp
): TextStyle {
    val originalSizeSp = this.fontSize.value
    val scaledSizeSp = originalSizeSp * scaleFactor

    // Constrain the size between min and max
    val constrainedSizeSp = scaledSizeSp.coerceIn(minSize.value, maxSize.value)

    // Calculate line height based on font size to maintain readability
    val lineHeightMultiplier = if (scaleFactor > 1.2f) 1.4f else 1.2f

    return this.copy(
        fontSize = constrainedSizeSp.sp,
        lineHeight = (constrainedSizeSp * lineHeightMultiplier).sp,
        // Adjust letter spacing for better readability in large text
        letterSpacing = if (scaleFactor > 1.2f) 0.0.sp else this.letterSpacing
    )
}

/**
 * Dialog to let users adjust text size
 */
@Composable
fun TextSizeAdjustmentDialog(
    currentScale: Float,
    onScaleSelected: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var tempScale by remember { mutableStateOf(currentScale) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Adjust Text Size",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Sample Text",
                    style = MaterialTheme.typography.bodyLarge.forLargeTextMode(tempScale)
                )

                Text(
                    text = when {
                        tempScale <= 0.85f -> "Small"
                        tempScale <= 1.0f -> "Normal"
                        tempScale <= 1.15f -> "Large"
                        tempScale <= 1.3f -> "Larger"
                        else -> "Largest"
                    },
                    style = MaterialTheme.typography.labelMedium
                )

                Slider(
                    value = tempScale,
                    onValueChange = { tempScale = it },
                    valueRange = 0.85f..1.5f,
                    steps = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onScaleSelected(tempScale)
                    onDismiss()
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


/**
 * Add keyboard navigation support to a composable
 *
 * @param onEnter Function to be called when Enter key is pressed while focused
 * @param focusRequester Optional FocusRequester to manage focus
 * @return Modified Modifier with keyboard navigation support
 */
@Composable
fun Modifier.keyboardNavigable(
    onEnter: () -> Unit,
    focusRequester: FocusRequester? = null
): Modifier {
    val baseModifier = if (focusRequester != null) {
        this.focusRequester(focusRequester)
    } else {
        this
    }

    return baseModifier
        .focusable(interactionSource = remember { MutableInteractionSource() })
        .onKeyEvent { keyEvent ->
            if (keyEvent.type == KeyEventType.KeyDown &&
                (keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter)
            ) {
                onEnter()
                true
            } else {
                false
            }
        }
}

/**
 * Add accessibility semantics to an interactive element
 *
 * @param description Content description for screen readers
 * @param role Semantic role of the element
 * @return Modified Modifier with semantic properties
 */
fun Modifier.accessibleClickable(
    description: String,
    role: Role = Role.Button,
    onClick: () -> Unit
): Modifier = this
    .semantics {
        contentDescription = description
        this.role = role
    }
    .clickable(onClick = onClick)


/**
 * CompositionLocal to provide text scaling factor for accessibility
 */
val LocalTextScaleFactor = compositionLocalOf { 1.0f }

@Composable
fun TextStyle.withScaleFactor(scaleFactor: Float? = null): TextStyle {
    val actualScaleFactor = scaleFactor ?: LocalTextScaleFactor.current

    return remember(this, actualScaleFactor) {
        copy(
            fontSize = fontSize * actualScaleFactor,
            lineHeight = lineHeight.times(actualScaleFactor)
        )
    }
}

/**
 * Extension function to scale TextStyle based on the user's preference (non-composable version)
 */
fun TextStyle.scaleTextSize(scaleFactor: Float = 1.0f): TextStyle {
    return copy(
        fontSize = fontSize * scaleFactor,
        lineHeight = lineHeight.times(scaleFactor)
    )
}

/**
 * Extension function to scale sp value based on accessibility settings
 */
@Composable
fun TextUnit.withScaleFactor(scaleFactor: Float? = null): TextUnit {
    val actualScaleFactor = scaleFactor ?: LocalTextScaleFactor.current

    return remember(this, actualScaleFactor) {
        value * actualScaleFactor
    }.sp
}