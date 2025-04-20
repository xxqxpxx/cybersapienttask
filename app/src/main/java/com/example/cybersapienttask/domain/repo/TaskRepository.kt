package com.example.cybersapienttask.domain.repo

import com.example.cybersapienttask.data.local.TaskDao
import com.example.cybersapienttask.data.local.TaskOrderDao
import com.example.cybersapienttask.data.model.Task
import com.example.cybersapienttask.data.model.TaskOrder
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository for task data operations
 */
class TaskRepository(
    private val taskDao: TaskDao,
    private val taskOrderDao: TaskOrderDao
) {
    /**
     * Get ordered tasks by their positions
     */
    fun getOrderedTasks(): Flow<List<TaskOrder>> {
        return taskOrderDao.getTaskOrder()
    }

    /**
     * Get all tasks
     */
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    /**
     * Get tasks filtered by completion status
     */
    fun getTasksByStatus(isCompleted: Boolean): Flow<List<Task>> =
        taskDao.getTasksByStatus(isCompleted)

    /**
     * Get tasks ordered by priority
     */
    fun getTasksOrderedByPriority(): Flow<List<Task>> =
        taskDao.getTasksOrderedByPriority()

    /**
     * Get tasks ordered by due date
     */
    fun getTasksOrderedByDueDate(): Flow<List<Task>> =
        taskDao.getTasksOrderedByDueDate()

    /**
     * Get tasks ordered alphabetically
     */
    fun getTasksOrderedAlphabetically(): Flow<List<Task>> =
        taskDao.getTasksOrderedAlphabetically()

    /**
     * Get a specific task by ID
     * Returns null if the task doesn't exist
     */
    suspend fun getTaskById(taskId: Long): Task? =
        taskDao.getTaskById(taskId)

    /**
     * Insert a new task and add it to the task order list
     */
    suspend fun insertTask(task: Task): Long {
        val taskId = taskDao.insertTask(task)

        // Add the task to the order list at the end
        val maxPosition = taskOrderDao.getMaxPosition() ?: -1
        taskOrderDao.insertTaskOrder(TaskOrder(taskId, maxPosition + 1))

        return taskId
    }

    /**
     * Update an existing task
     */
    suspend fun updateTask(task: Task) =
        taskDao.updateTask(task)

    /**
     * Delete a task and its order entry
     */
    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
        taskOrderDao.deleteTaskOrder(task.id)
    }

    /**
     * Toggle the completion status of a task
     */
    suspend fun toggleTaskCompletion(task: Task) {
        val updatedTask = task.copy(isCompleted = !task.isCompleted)
        taskDao.updateTask(updatedTask)
    }

    /**
     * Move a task from one position to another in manual ordering mode
     */
    suspend fun moveTask(fromPosition: Int, toPosition: Int) {
        taskOrderDao.moveTask(fromPosition, toPosition)
    }

    /**
     * Generate sample tasks for testing
     */
    suspend fun insertSampleTasks() {
        val today = LocalDate.now()

        val sampleTasks = listOf(
            Task(
                title = "Complete project documentation",
                description = "Write comprehensive documentation for the task manager app",
                priority = com.example.cybersapienttask.data.model.TaskPriority.HIGH,
                dueDate = today.plusDays(2)
            ),
            Task(
                title = "Buy groceries",
                description = "Milk, eggs, bread, fruits",
                priority = com.example.cybersapienttask.data.model.TaskPriority.MEDIUM,
                dueDate = today.plusDays(1)
            ),
            Task(
                title = "Call mom",
                description = "Catch up and ask about the weekend plans",
                priority = com.example.cybersapienttask.data.model.TaskPriority.LOW,
                dueDate = today
            ),
            Task(
                title = "Exercise",
                description = "Go for a 30-minute jog",
                priority = com.example.cybersapienttask.data.model.TaskPriority.MEDIUM,
                isCompleted = true
            )
        )

        sampleTasks.forEach { insertTask(it) }
    }
}