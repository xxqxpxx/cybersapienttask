package com.example.cybersapienttask.ui.screens.tasklist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cybersapienttask.data.model.Task
import com.example.cybersapienttask.data.model.TaskFilter
import com.example.cybersapienttask.data.model.TaskSortOrder
import com.example.cybersapienttask.ui.accessibility.keyboardNavigable
import com.example.cybersapienttask.ui.accessibility.withScaleFactor
import com.example.cybersapienttask.ui.animations.taskEnterTransition
import com.example.cybersapienttask.ui.components.BounceFAB
import com.example.cybersapienttask.ui.components.DraggableTaskItem
import com.example.cybersapienttask.ui.components.EmptyState
import com.example.cybersapienttask.ui.components.ShimmerTaskList
import com.example.cybersapienttask.ui.components.SwipeableTaskItem
import com.example.cybersapienttask.viewmodel.TaskListViewModel
import com.example.taskmanager.ui.components.TaskProgressIndicator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel,
    onTaskClick: (Long) -> Unit,
    onAddTask: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Enable performance monitoring in debug builds
    //  PerformanceMonitor(enabled = false)

    // State from ViewModel
    val tasks by viewModel.tasks.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val isManualOrder by viewModel.isManualOrder.collectAsState()
    val taskStats by viewModel.taskStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // UI state
    var showFilterMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    // State for tracking drag operations
    var dragTargetIndex by remember { mutableStateOf(-1) }

    // Snackbar host state for undo functionality
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Keep track of the last deleted task for potential undo
    var lastDeletedTask by remember { mutableStateOf<Task?>(null) }

    // Lazy list state for scrolling
    val listState = rememberLazyListState()

    // Focus requester for keyboard navigation
    val headerFocusRequester = remember { FocusRequester() }

    // Initialize haptic feedback
    val hapticFeedback = LocalHapticFeedback.current

    // Derived state to detect when we're at the top of the list
    val showElevation by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Task Manager",
                        modifier = Modifier.semantics {
                            heading()
                            contentDescription = "Task Manager, main screen"
                        }
                    )
                },
                actions = {
                    // Filter button
                    IconButton(
                        onClick = { showFilterDialog = true },
                        modifier = Modifier
                            .keyboardNavigable(
                                onEnter = { showFilterDialog = true }
                            )
                            .semantics {
                                contentDescription = "Filter tasks by ${
                                    when (filter) {
                                        TaskFilter.ALL -> "all"
                                        TaskFilter.COMPLETED -> "completed"
                                        TaskFilter.PENDING -> "pending"
                                    }
                                }"
                            }
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter tasks",
                            tint = if (filter != TaskFilter.ALL)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Sort button
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier
                            .keyboardNavigable(
                                onEnter = { showSortMenu = true }
                            )
                            .semantics {
                                contentDescription = "Sort tasks by ${
                                    when (sortOrder) {
                                        TaskSortOrder.PRIORITY -> "priority"
                                        TaskSortOrder.DUE_DATE -> "due date"
                                        TaskSortOrder.ALPHABETICAL -> "alphabetical order"
                                    }
                                }"
                            }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort tasks"
                        )
                    }

                    // Sort dropdown menu
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
                    IconButton(
                        onClick = {
                            viewModel.toggleManualOrdering()
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        modifier = Modifier
                            .keyboardNavigable(
                                onEnter = { viewModel.toggleManualOrdering() }
                            )
                            .semantics {
                                contentDescription = if (isManualOrder)
                                    "Disable manual ordering"
                                else
                                    "Enable manual ordering"
                            }
                    ) {
                        Icon(
                            Icons.Default.DragIndicator,
                            contentDescription = null,
                            tint = if (isManualOrder)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Settings button
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .keyboardNavigable(
                                onEnter = onSettingsClick
                            )
                            .semantics {
                                contentDescription = "Open settings"
                            }
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            BounceFAB(
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = "New Task",
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onAddTask()
                },
                modifier = Modifier.semantics {
                    contentDescription = "Add new task"
                }
            )
        }
    ) { paddingValues ->
        // Filter dialog
        if (showFilterDialog) {
            FilterDialog(
                currentFilter = filter,
                onFilterSelected = {
                    viewModel.setFilter(it)
                    showFilterDialog = false
                },
                onDismiss = { showFilterDialog = false }
            )
        }

        // Main content
        if (isLoading) {
            ShimmerTaskList(
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (tasks.isEmpty()) {
                    // Show empty state
                    EmptyState(
                        onAddTask = onAddTask,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Show task stats and progress
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TaskProgressIndicator(
                                completedTasks = taskStats.completedTasks,
                                totalTasks = taskStats.totalTasks
                            )
                        }

                        // Filter chips row
                        FilterChipsRow(
                            currentFilter = filter,
                            onFilterSelected = { viewModel.setFilter(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )

                        // Task list
                        TaskList(
                            tasks = tasks,
                            onTaskClick = { onTaskClick(it.id) },
                            onTaskToggle = { viewModel.toggleTaskCompletion(it) },
                            onTaskDelete = { taskToDelete ->
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
                            isManualOrder = isManualOrder,
                            onTaskMove = { fromIndex, toIndex ->
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.moveTask(fromIndex, toIndex)
                            },
                            listState = listState,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    // Request initial focus for accessibility
    LaunchedEffect(Unit) {
        try {
            headerFocusRequester.requestFocus()
        } catch (e: Exception) {
            // Focus request might fail if the UI isn't ready
        }
    }
}

@Composable
fun FilterDialog(
    currentFilter: TaskFilter,
    onFilterSelected: (TaskFilter) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Tasks") },
        text = {
            Column {
                Text(
                    "Select which tasks to display:",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                FilterOption(
                    title = "All Tasks",
                    subtitle = "Show both completed and pending tasks",
                    selected = currentFilter == TaskFilter.ALL,
                    onClick = { onFilterSelected(TaskFilter.ALL) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                FilterOption(
                    title = "Pending Tasks",
                    subtitle = "Show only tasks that need to be completed",
                    selected = currentFilter == TaskFilter.PENDING,
                    onClick = { onFilterSelected(TaskFilter.PENDING) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                FilterOption(
                    title = "Completed Tasks",
                    subtitle = "Show only tasks that have been completed",
                    selected = currentFilter == TaskFilter.COMPLETED,
                    onClick = { onFilterSelected(TaskFilter.COMPLETED) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.keyboardNavigable(onEnter = onDismiss)
            ) {
                Text("Close")
            }
        },
        dismissButton = {}
    )
}

@Composable
fun FilterOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (selected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .keyboardNavigable(onEnter = onClick)
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.withScaleFactor(),
                    color = if (selected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.withScaleFactor(),
                    color = if (selected)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (selected) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipsRow(
    currentFilter: TaskFilter,
    onFilterSelected: (TaskFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
    ) {
        SegmentedButton(
            selected = currentFilter == TaskFilter.ALL,
            onClick = { onFilterSelected(TaskFilter.ALL) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                "All",
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        SegmentedButton(
            selected = currentFilter == TaskFilter.PENDING,
            onClick = { onFilterSelected(TaskFilter.PENDING) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                "Pending",
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        SegmentedButton(
            selected = currentFilter == TaskFilter.COMPLETED,
            onClick = { onFilterSelected(TaskFilter.COMPLETED) },
            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                "Completed",
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskList(
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    onTaskToggle: (Task) -> Unit,
    onTaskDelete: (Task) -> Unit,
    isManualOrder: Boolean,
    onTaskMove: (Int, Int) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 80.dp
            ), // Add padding at bottom for FAB
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(
                items = tasks,
                key = { _, task -> task.id }
            ) { index, task ->
                AnimatedVisibility(
                    visible = true,
                    enter = taskEnterTransition()
                ) {
                    if (isManualOrder) {
                        DraggableTaskItem(
                            task = task,
                            onTaskClick = { onTaskClick(task) },
                            onCheckboxClick = { onTaskToggle(task) },
                            onDragComplete = onTaskMove,
                            isDraggable = true,
                            currentPosition = index,
                            updateDragPosition = { startIndex, dragOffset ->
                                // Calculate target position
                                val itemHeight = 116f  // Approximate height of item in dp
                                val dragDistance = (dragOffset / itemHeight).toInt()
                                (startIndex + dragDistance).coerceIn(0, tasks.size - 1)
                            },
                            modifier = Modifier
                                .animateItemPlacement(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    } else {
                        SwipeableTaskItem(
                            task = task,
                            onTaskClick = { onTaskClick(task) },
                            onCheckboxClick = { onTaskToggle(task) },
                            onComplete = { onTaskToggle(task) },
                            onDelete = onTaskDelete,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Show "No matches" message if filtering resulted in empty list
        AnimatedVisibility(
            visible = tasks.isEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No matching tasks",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Try changing your filter settings or add a new task",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}