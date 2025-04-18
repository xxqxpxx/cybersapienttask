package com.example.cybersapienttask.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cybersapienttask.data.model.Task
import com.example.cybersapienttask.data.model.TaskFilter
import com.example.cybersapienttask.data.model.TaskSortOrder
import com.example.cybersapienttask.ui.components.TaskItem
import com.example.cybersapienttask.viewmodel.TaskListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel,
    onTaskClick: (Long) -> Unit,
    onAddTask: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()

    var showFilterMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Manager") },
                actions = {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Filter tasks")
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
                        Icon(Icons.Default.Edit, contentDescription = "Sort tasks")
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
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun TaskListContent(
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    onCheckboxClick: (Task) -> Unit,
    modifier: Modifier = Modifier
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
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(
                items = tasks,
                key = { it.id }
            ) { task ->
                TaskItem(
                    task = task,
                    onTaskClick = onTaskClick,
                    onCheckboxClick = onCheckboxClick
                )
            }
        }
    }
}