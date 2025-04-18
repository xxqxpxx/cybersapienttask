package com.example.cybersapienttask.data.repo

import com.example.cybersapienttask.data.local.TaskDao
import com.example.cybersapienttask.data.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    fun getTasksByStatus(isCompleted: Boolean): Flow<List<Task>> =
        taskDao.getTasksByStatus(isCompleted)

    fun getTasksOrderedByPriority(): Flow<List<Task>> =
        taskDao.getTasksOrderedByPriority()

    fun getTasksOrderedByDueDate(): Flow<List<Task>> =
        taskDao.getTasksOrderedByDueDate()

    fun getTasksOrderedAlphabetically(): Flow<List<Task>> =
        taskDao.getTasksOrderedAlphabetically()

    suspend fun getTaskById(taskId: Long): Task? =
        taskDao.getTaskById(taskId)

    suspend fun insertTask(task: Task): Long =
        taskDao.insertTask(task)

    suspend fun updateTask(task: Task) =
        taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) =
        taskDao.deleteTask(task)

    suspend fun toggleTaskCompletion(task: Task) {
        val updatedTask = task.copy(isCompleted = !task.isCompleted)
        taskDao.updateTask(updatedTask)
    }
}