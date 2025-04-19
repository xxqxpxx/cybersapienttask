package com.example.cybersapienttask.ui.screens.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cybersapienttask.data.model.Task
import com.example.cybersapienttask.data.repo.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilter {
    ALL, COMPLETED, PENDING
}

enum class TaskSortOrder {
    PRIORITY, DUE_DATE, ALPHABETICAL
}

class TaskListViewModel(private val repository: TaskRepository) : ViewModel() {

    // Task statistics
    val taskStats: StateFlow<TaskStatistics> = combine(
        repository.getAllTasks(),
        repository.getTasksByStatus(true)
    ) { allTasks, completedTasks ->
        TaskStatistics(
            totalTasks = allTasks.size,
            completedTasks = completedTasks.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskStatistics(0, 0)
    )

    data class TaskStatistics(
        val totalTasks: Int,
        val completedTasks: Int
    )

    private val _filter = MutableStateFlow(TaskFilter.ALL)
    val filter: StateFlow<TaskFilter> = _filter

    private val _sortOrder = MutableStateFlow(TaskSortOrder.DUE_DATE)
    val sortOrder: StateFlow<TaskSortOrder> = _sortOrder

    private val _allTasks = MutableStateFlow<List<Task>>(emptyList())
    private val _orderedTasks = MutableStateFlow<List<Task>>(emptyList())
    private val _filteredTasks = MutableStateFlow<List<Task>>(emptyList())
    private val _isManualOrder = MutableStateFlow(false)

    val isManualOrder: StateFlow<Boolean> = _isManualOrder

    val tasks: StateFlow<List<Task>> = combine(
        _allTasks,
        _orderedTasks,
        _filter,
        _sortOrder,
        _isManualOrder
    ) { allTasks, orderedTasks, filter, sortOrder, isManualOrder ->
        // First, choose between manual ordering or automatic sorting
        val baseList = if (isManualOrder) orderedTasks else allTasks

        // Then apply filtering
        val filtered = when (filter) {
            TaskFilter.ALL -> baseList
            TaskFilter.COMPLETED -> baseList.filter { it.isCompleted }
            TaskFilter.PENDING -> baseList.filter { !it.isCompleted }
        }

        // Apply sorting if not using manual ordering
        if (!isManualOrder) {
            when (sortOrder) {
                TaskSortOrder.PRIORITY -> filtered.sortedByDescending { it.priority }
                TaskSortOrder.DUE_DATE -> filtered.sortedWith(compareBy(nullsLast()) { it.dueDate })
                TaskSortOrder.ALPHABETICAL -> filtered.sortedBy { it.title }
            }
        } else {
            filtered  // Keep the manual order
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.getAllTasks().collect { tasks ->
                _allTasks.value = tasks
            }
        }

        viewModelScope.launch {
            repository.getOrderedTasks().collect { tasks ->
                _orderedTasks.value = tasks
            }
        }
    }

    // Toggle between manual ordering and automatic sorting
    fun toggleManualOrdering() {
        _isManualOrder.value = !_isManualOrder.value
    }

    // Update task order via drag and drop
    fun moveTask(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            repository.moveTask(fromIndex, toIndex)
        }
    }

    fun setFilter(filter: TaskFilter) {
        _filter.value = filter
    }

    fun setSortOrder(sortOrder: TaskSortOrder) {
        _sortOrder.value = sortOrder
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun restoreTask(task: Task) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    companion object {
        fun <T : Comparable<T>> nullsLast(): Comparator<T?> {
            return Comparator { a, b ->
                when {
                    a == null && b == null -> 0
                    a == null -> 1
                    b == null -> -1
                    else -> a.compareTo(b)
                }
            }
        }
    }
}

class TaskListViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}