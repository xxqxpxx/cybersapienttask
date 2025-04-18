package com.example.cybersapienttask.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cybersapienttask.data.model.Task
import com.example.cybersapienttask.data.model.TaskPriority
import com.example.cybersapienttask.ui.theme.PriorityHigh
import com.example.cybersapienttask.ui.theme.PriorityLow
import com.example.cybersapienttask.ui.theme.PriorityMedium
import java.time.format.DateTimeFormatter

@Composable
fun TaskItem(
    task: Task,
    onTaskClick: (Task) -> Unit,
    onCheckboxClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onTaskClick(task) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onCheckboxClick(task) }
            )
            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.alpha(if (task.isCompleted) 0.6f else 1f)
                )

                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.alpha(if (task.isCompleted) 0.6f else 0.8f)
                    )
                }

                task.dueDate?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Due: ${it.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.alpha(0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            PriorityIndicator(priority = task.priority)
        }
    }
}

@Composable
fun PriorityIndicator(priority: TaskPriority, modifier: Modifier = Modifier) {
    val color = when (priority) {
        TaskPriority.LOW -> PriorityLow
        TaskPriority.MEDIUM -> PriorityMedium
        TaskPriority.HIGH -> PriorityHigh
    }

    Box(
        modifier = modifier
            .size(16.dp)
            .background(color, shape = MaterialTheme.shapes.small)
    )
}