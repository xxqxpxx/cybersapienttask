package com.example.cybersapienttask.ui.screens.taskdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cybersapienttask.data.model.Task
import com.example.cybersapienttask.data.model.TaskPriority
import com.example.cybersapienttask.data.repo.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class TaskDetailViewModel(
    private val repository: TaskRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: Long? = savedStateHandle.get<Long>("taskId")

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
        if (taskId != null && taskId != 0L) {
            loadTask(taskId)
        }
    }

    private fun loadTask(id: Long) {
        viewModelScope.launch {
            val loadedTask = repository.getTaskById(id)
            _task.value = loadedTask
            loadedTask?.let { task ->
                _title.value = task.title
                _description.value = task.description
                _priority.value = task.priority
                _dueDate.value = task.dueDate
                _isCompleted.value = task.isCompleted
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

    fun deleteTask() {
        _task.value?.let { task ->
            viewModelScope.launch {
                repository.deleteTask(task)
            }
        }
    }
}

class TaskDetailViewModelFactory(
    private val repository: TaskRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskDetailViewModel(repository, savedStateHandle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}