package com.example.cybersapienttask.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cybersapienttask.data.model.Task
import com.example.cybersapienttask.data.model.TaskFilter
import com.example.cybersapienttask.data.model.TaskSortOrder
import com.example.cybersapienttask.domain.repo.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate


class TaskListViewModel(private val repository: TaskRepository) : ViewModel() {

    // Task filtering, sorting and ordering state
    private val _filter = MutableStateFlow(TaskFilter.ALL)
    val filter: StateFlow<TaskFilter> = _filter

    private val _sortOrder = MutableStateFlow(TaskSortOrder.DUE_DATE)
    val sortOrder: StateFlow<TaskSortOrder> = _sortOrder

    private val _isManualOrder = MutableStateFlow(false)
    val isManualOrder: StateFlow<Boolean> = _isManualOrder

    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Task statistics
    data class TaskStatistics(
        val totalTasks: Int,
        val completedTasks: Int,
        val pendingTasks: Int = totalTasks - completedTasks,
        val completionPercentage: Float = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f,
        val hasDueTasks: Boolean = false,
        val hasOverdueTasks: Boolean = false
    )

    // Cache for task operations to avoid loading flicker
    private val _taskOperationCache = MutableStateFlow<Map<Long, Task>>(emptyMap())

    /**
     * Task statistics derived from current task list
     */
    val taskStats: StateFlow<TaskStatistics> = combine(
        repository.getAllTasks(),
        repository.getTasksByStatus(true)
    ) { allTasks, completedTasks ->
        val hasDueTasks = allTasks.any { it.dueDate != null }
        val hasOverdueTasks = allTasks.any {
            it.dueDate != null && it.dueDate.isBefore(LocalDate.now()) && !it.isCompleted
        }

        TaskStatistics(
            totalTasks = allTasks.size,
            completedTasks = completedTasks.size,
            hasDueTasks = hasDueTasks,
            hasOverdueTasks = hasOverdueTasks
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskStatistics(0, 0)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _filteredTasks = combine(
        _filter,
        _searchQuery
    ) { filter, query ->
        Pair(filter, query)
    }.flatMapLatest { (filter, query) ->
        when (filter) {
            TaskFilter.ALL -> repository.getAllTasks()
            TaskFilter.COMPLETED -> repository.getTasksByStatus(true)
            TaskFilter.PENDING -> repository.getTasksByStatus(false)
        }.map { tasks ->
            if (query.isBlank()) {
                tasks
            } else {
                tasks.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.description.contains(query, ignoreCase = true)
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Combined flow that provides the final task list based on:
     * - Filtered tasks (by status and search)
     * - Sort order
     * - Manual ordering preference
     * - Task operation cache (for immediate UI updates)
     */
    val tasks: StateFlow<List<Task>> = combine(
        _filteredTasks,
        _sortOrder,
        _isManualOrder,
        repository.getOrderedTasks(),
        _taskOperationCache
    ) { filteredTasks, sortOrder, isManualOrder, orderedTasks, taskCache ->
        // First, apply any cached task operations
        val tasksWithCache = filteredTasks.map { task ->
            taskCache[task.id] ?: task
        }

        // Then apply the appropriate ordering
        if (isManualOrder) {
            // For manual ordering, sort tasks by their position in the ordered list
            val orderMap = orderedTasks.associateBy { it.taskId }
            return@combine tasksWithCache.sortedBy { task ->
                orderMap[task.id]?.position ?: Int.MAX_VALUE
            }
        } else {
            // For automatic sorting, apply the selected sort order
            return@combine when (sortOrder) {
                TaskSortOrder.PRIORITY -> tasksWithCache.sortedByDescending { it.priority }
                TaskSortOrder.DUE_DATE -> {
                    val tasksWithoutDueDate = tasksWithCache.filter { it.dueDate == null }
                    val tasksWithDueDate = tasksWithCache.filter { it.dueDate != null }
                        .sortedBy { it.dueDate }

                    tasksWithDueDate + tasksWithoutDueDate
                }

                TaskSortOrder.ALPHABETICAL -> tasksWithCache.sortedBy { it.title.lowercase() }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Start loading data
        viewModelScope.launch {
            repository.getAllTasks().collect {
                _isLoading.value = false
            }
        }
    }

    /**
     * Set the task filter
     */
    fun setFilter(filter: TaskFilter) {
        _filter.value = filter
    }

    /**
     * Set the sort order
     */
    fun setSortOrder(sortOrder: TaskSortOrder) {
        _sortOrder.value = sortOrder
    }

    /**
     * Toggle manual ordering of tasks
     */
    fun toggleManualOrdering() {
        _isManualOrder.value = !_isManualOrder.value
    }

    /**
     * Set search query for filtering tasks
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Toggle completion status of a task
     */
    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            // Update cache for immediate UI feedback
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            _taskOperationCache.value = _taskOperationCache.value + (task.id to updatedTask)

            // Update in repository
            repository.toggleTaskCompletion(task)

            // Clear cache after short delay to ensure repository update is reflected
            delay(200)
            _taskOperationCache.value = _taskOperationCache.value - task.id
        }
    }

    /**
     * Delete a task
     */
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            // Remove from cache if present
            _taskOperationCache.value = _taskOperationCache.value - task.id

            // Delete from repository
            repository.deleteTask(task)
        }
    }

    /**
     * Restore a previously deleted task (for undo functionality)
     */
    fun restoreTask(task: Task) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    /**
     * Move a task from one position to another in manual ordering mode
     */
    fun moveTask(fromPosition: Int, toPosition: Int) {
        viewModelScope.launch {
            repository.moveTask(fromPosition, toPosition)
        }
    }

    /**
     * Helper function for sorting by nullable values
     */
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

    /**
     * Adds a small delay for UI interactions
     */
    private suspend fun delay(timeMillis: Long) {
        kotlinx.coroutines.delay(timeMillis)
    }
}

/**
 * Factory for creating TaskListViewModel instances
 */
class TaskListViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}