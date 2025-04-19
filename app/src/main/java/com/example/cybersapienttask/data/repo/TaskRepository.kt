package com.example.cybersapienttask.data.repo

import com.example.cybersapienttask.data.local.TaskDao
import com.example.cybersapienttask.data.local.TaskOrderDao
import com.example.cybersapienttask.data.model.Task
import com.example.cybersapienttask.data.model.TaskOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class TaskRepository(
    private val taskDao: TaskDao,
    private val taskOrderDao: TaskOrderDao
) {

    // Get ordered tasks by combining the task data with order data
    fun getOrderedTasks(): Flow<List<Task>> {
        return combine(
            taskDao.getAllTasks(),
            taskOrderDao.getTaskOrder()
        ) { tasks, orderList ->
            // Create a map of task IDs to their positions
            val orderMap = orderList.associateBy { it.taskId }

            // Sort tasks by their position in the order list
            // If a task doesn't have an order entry, put it at the end
            tasks.sortedBy { task ->
                orderMap[task.id]?.position ?: Int.MAX_VALUE
            }
        }
    }

    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    fun getTasksByStatus(isCompleted: Boolean): Flow<List<Task>> =
        taskDao.getTasksByStatus(isCompleted)

    fun getTasksOrderedByPriority(): Flow<List<Task>> =
        taskDao.getTasksOrderedByPriority()

    fun getTasksOrderedByDueDate(): Flow<List<Task>> =
        taskDao.getTasksOrderedByDueDate()

    fun getTasksOrderedAlphabetically(): Flow<List<Task>> =
        taskDao.getTasksOrderedAlphabetically()

    suspend fun getTaskById(taskId: Long): Task? = taskDao.getTaskById(taskId)

    suspend fun insertTask(task: Task): Long {
        val taskId = taskDao.insertTask(task)

        // Add the task to the order list at the end
        val maxPosition = taskOrderDao.getMaxPosition() ?: -1
        taskOrderDao.insertTaskOrder(TaskOrder(taskId, maxPosition + 1))

        return taskId
    }

    suspend fun updateTask(task: Task) =
        taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
        taskOrderDao.deleteTaskOrder(task.id)
    }

    suspend fun toggleTaskCompletion(task: Task) {
        val updatedTask = task.copy(isCompleted = !task.isCompleted)
        taskDao.updateTask(updatedTask)
    }

    suspend fun moveTask(fromPosition: Int, toPosition: Int) {
        taskOrderDao.moveTask(fromPosition, toPosition)
    }
}