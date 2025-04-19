package com.example.cybersapienttask.ui.screens.tasklist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cybersapienttask.data.model.Task
import com.example.cybersapienttask.ui.components.DraggableTaskItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel,
    onTaskClick: (Long) -> Unit,
    onAddTask: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val isManualOrder by viewModel.isManualOrder.collectAsState()

    var showFilterMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    // State for tracking drag operations
    var dragTargetIndex by remember { mutableStateOf(-1) }

    // Snackbar host state for undo functionality
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Keep track of the last deleted task for potential undo
    var lastDeletedTask by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Task Manager") },
                actions = {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.Person, contentDescription = "Filter tasks")
                    }

                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Tasks") },
                            onClick = {
                                viewModel.setFilter(TaskFilter.ALL)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Completed Tasks") },
                            onClick = {
                                viewModel.setFilter(TaskFilter.COMPLETED)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Pending Tasks") },
                            onClick = {
                                viewModel.setFilter(TaskFilter.PENDING)
                                showFilterMenu = false
                            }
                        )
                    }

                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Person, contentDescription = "Sort tasks")
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("By Priority") },
                            onClick = {
                                viewModel.setSortOrder(TaskSortOrder.PRIORITY)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("By Due Date") },
                            onClick = {
                                viewModel.setSortOrder(TaskSortOrder.DUE_DATE)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Alphabetically") },
                            onClick = {
                                viewModel.setSortOrder(TaskSortOrder.ALPHABETICAL)
                                showSortMenu = false
                            }
                        )
                    }

                    // Manual ordering toggle
                    IconButton(onClick = { viewModel.toggleManualOrdering() }) {
                        Icon(
                            Icons.Default.Person, // update
                            contentDescription = if (isManualOrder) "Disable manual ordering" else "Enable manual ordering",
                            tint = if (isManualOrder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTask,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Task") },
                text = { Text("New Task") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { paddingValues ->
        TaskListContent(
            tasks = tasks,
            onTaskClick = { onTaskClick(it.id) },
            onCheckboxClick = { viewModel.toggleTaskCompletion(it) },
            modifier = Modifier.padding(paddingValues),
            isManualOrder = isManualOrder,
            onTaskMove = { fromIndex, toIndex ->
                viewModel.moveTask(fromIndex, toIndex)
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskListContent(
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    onCheckboxClick: (Task) -> Unit,
    modifier: Modifier = Modifier,
    isManualOrder: Boolean = false,
    onTaskMove: (Int, Int) -> Unit = { _, _ -> }
) {
    if (tasks.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No tasks found",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Add a new task to get started",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    } else {
        // State for tracking drag target positions
        var dragTargetIndex by remember { mutableStateOf(-1) }

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            itemsIndexed(
                items = tasks,
                key = { _, task -> task.id }
            ) { index, task ->
                if (isManualOrder) {
                    DraggableTaskItem(
                        task = task,
                        onTaskClick = onTaskClick,
                        onCheckboxClick = onCheckboxClick,
                        onDragComplete = { fromIndex, toIndex ->
                            onTaskMove(fromIndex, toIndex)
                        },
                        isDraggable = isManualOrder,
                        currentPosition = index,
                        updateDragPosition = { startIndex, dragOffset ->
                            // Calculate how many items we've dragged past
                            val itemHeight = 116  // Approximate height of item in dp
                            val dragDistance = (dragOffset / itemHeight).toInt()
                            val targetIndex =
                                (startIndex + dragDistance).coerceIn(0, tasks.size - 1)

                            // Update the visual feedback
                            dragTargetIndex = targetIndex

                            targetIndex
                        },
                        modifier = Modifier.animateItemPlacement()
                    )
                } /*else {
                    SwipeableTaskItem(
                        task = task,
                        onTaskClick = onTaskClick,
                        onCheckboxClick = onCheckboxClick,
                        onComplete = onCheckboxClick,
                        onDelete = { taskToDelete ->
                            // Save the task before deletion for potential undo
                            lastDeletedTask = taskToDelete

                            // Delete the task
                            viewModel.deleteTask(taskToDelete)

                            // Show snackbar with undo option
                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Task deleted",
                                    actionLabel = "UNDO"
                                )

                                // If user clicked UNDO, restore the task
                                if (result == SnackbarResult.ActionPerformed) {
                                    lastDeletedTask?.let { task ->
                                        viewModel.restoreTask(task)
                                        lastDeletedTask = null
                                    }
                                }
                            }
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }*/
            }
        }
    }
}