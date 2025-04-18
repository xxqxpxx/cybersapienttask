package com.example.cybersapienttask.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cybersapienttask.data.model.TaskPriority
import com.example.cybersapienttask.ui.theme.PriorityHigh
import com.example.cybersapienttask.ui.theme.PriorityLow
import com.example.cybersapienttask.ui.theme.PriorityMedium


@Composable
fun PriorityPicker(
    selectedPriority: TaskPriority,
    onPrioritySelected: (TaskPriority) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Priority:",
            style = MaterialTheme.typography.bodyLarge
        )

        PriorityButton(
            priority = TaskPriority.LOW,
            selected = selectedPriority == TaskPriority.LOW,
            onClick = { onPrioritySelected(TaskPriority.LOW) }
        )

        PriorityButton(
            priority = TaskPriority.MEDIUM,
            selected = selectedPriority == TaskPriority.MEDIUM,
            onClick = { onPrioritySelected(TaskPriority.MEDIUM) }
        )

        PriorityButton(
            priority = TaskPriority.HIGH,
            selected = selectedPriority == TaskPriority.HIGH,
            onClick = { onPrioritySelected(TaskPriority.HIGH) }
        )
    }
}

@Composable
fun PriorityButton(
    priority: TaskPriority,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = when (priority) {
        TaskPriority.LOW -> PriorityLow
        TaskPriority.MEDIUM -> PriorityMedium
        TaskPriority.HIGH -> PriorityHigh
    }

    val label = when (priority) {
        TaskPriority.LOW -> "Low"
        TaskPriority.MEDIUM -> "Medium"
        TaskPriority.HIGH -> "High"
    }

    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (selected) color else Color.Transparent)
            .border(2.dp, color, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.first().toString(),
            color = if (selected) Color.White else color,
            style = MaterialTheme.typography.labelSmall
        )
    }
}