package com.example.cybersapienttask.ui.screens.taskdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cybersapienttask.data.model.Task
import com.example.cybersapienttask.data.model.TaskPriority
import com.example.cybersapienttask.domain.repo.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel for task details and creation screen
 */
class TaskDetailViewModel(
    private val repository: TaskRepository,
    private val savedStateHandle: SavedStateHandle,
    private val taskId: Long
) : ViewModel() {

    // Extract the taskId from SavedStateHandle
    //  private val taskId: Long = savedStateHandle.get<Long>("taskId") ?: 0L

    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _priority = MutableStateFlow(TaskPriority.MEDIUM)
    val priority: StateFlow<TaskPriority> = _priority.asStateFlow()

    private val _dueDate = MutableStateFlow<LocalDate?>(null)
    val dueDate: StateFlow<LocalDate?> = _dueDate.asStateFlow()

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    init {
        // Load task data if taskId is valid
        //  if (taskId > 0) {
        loadTask(taskId)
        //  }
    }

    private fun loadTask(id: Long) {
        viewModelScope.launch {
            try {
                val loadedTask = repository.getTaskById(id)
                _task.value = loadedTask
                loadedTask?.let { task ->
                    _title.value = task.title
                    _description.value = task.description
                    _priority.value = task.priority
                    _dueDate.value = task.dueDate
                    _isCompleted.value = task.isCompleted
                }
            } catch (e: Exception) {
                // Handle error loading task
                // You might want to add error handling here
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }

    fun updateDescription(newDescription: String) {
        _description.value = newDescription
    }

    fun updatePriority(newPriority: TaskPriority) {
        _priority.value = newPriority
    }

    fun updateDueDate(newDueDate: LocalDate?) {
        _dueDate.value = newDueDate
    }

    fun toggleCompletion() {
        _isCompleted.value = !_isCompleted.value
    }

    /**
     * Save the current task
     * @return true if the task was saved successfully, false otherwise
     */
    fun saveTask(): Boolean {
        if (_title.value.isBlank()) {
            return false
        }

        viewModelScope.launch {
            val taskToSave = _task.value?.copy(
                title = _title.value,
                description = _description.value,
                priority = _priority.value,
                dueDate = _dueDate.value,
                isCompleted = _isCompleted.value
            ) ?: Task(
                title = _title.value,
                description = _description.value,
                priority = _priority.value,
                dueDate = _dueDate.value,
                isCompleted = _isCompleted.value
            )

            if (taskToSave.id != 0L) {
                repository.updateTask(taskToSave)
            } else {
                repository.insertTask(taskToSave)
            }
        }
        return true
    }

    /**
     * Delete the current task
     */
    fun deleteTask() {
        _task.value?.let { task ->
            viewModelScope.launch {
                repository.deleteTask(task)
            }
        }
    }
}

/**
 * Factory for creating TaskDetailViewModel with dependencies
 */
class TaskDetailViewModelFactory(
    private val repository: TaskRepository,
    private val savedStateHandle: SavedStateHandle,
    private val task: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskDetailViewModel(repository, savedStateHandle, task) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}