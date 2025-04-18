package com.example.cybersapienttask.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cybersapienttask.data.model.Task
import com.example.cybersapienttask.data.model.TaskFilter
import com.example.cybersapienttask.data.model.TaskSortOrder
import com.example.cybersapienttask.data.repo.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskListViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _filter = MutableStateFlow(TaskFilter.ALL)
    val filter: StateFlow<TaskFilter> = _filter

    private val _sortOrder = MutableStateFlow(TaskSortOrder.DUE_DATE)
    val sortOrder: StateFlow<TaskSortOrder> = _sortOrder

    private val _allTasks = MutableStateFlow<List<Task>>(emptyList())
    private val _filteredTasks = MutableStateFlow<List<Task>>(emptyList())

    val tasks: StateFlow<List<Task>> =
        combine(_allTasks, _filter, _sortOrder) { tasks, filter, sortOrder ->
            val filtered = when (filter) {
                TaskFilter.ALL -> tasks
                TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
                TaskFilter.PENDING -> tasks.filter { !it.isCompleted }
            }

            // Apply sorting
            when (sortOrder) {
                TaskSortOrder.PRIORITY -> filtered.sortedByDescending { it.priority }
                TaskSortOrder.DUE_DATE -> filtered.sortedWith(compareBy(nullsLast()) { it.dueDate })
                TaskSortOrder.ALPHABETICAL -> filtered.sortedBy { it.title }
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