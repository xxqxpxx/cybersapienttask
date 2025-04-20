package com.example.cybersapienttask

import com.example.cybersapienttask.data.local.TaskDao
import com.example.cybersapienttask.data.local.TaskOrderDao
import com.example.cybersapienttask.data.model.Task
import com.example.cybersapienttask.data.model.TaskOrder
import com.example.cybersapienttask.data.model.TaskPriority
import com.example.cybersapienttask.data.repo.TaskRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class TaskRepositoryTest {

    private lateinit var taskDao: TaskDao
    private lateinit var taskOrderDao: TaskOrderDao
    private lateinit var taskRepository: TaskRepository
    
    @Before
    fun setup() {
        taskDao = mockk(relaxed = true)
        taskOrderDao = mockk(relaxed = true)
        taskRepository = TaskRepository(taskDao, taskOrderDao)
    }
    
    @Test
    fun `getAllTasks returns flow from DAO`() = runBlocking {
        // Arrange
        val tasks = listOf(
            Task(id = 1, title = "Task 1"),
            Task(id = 2, title = "Task 2")
        )
        every { taskDao.getAllTasks() } returns flowOf(tasks)
        
        // Act
        val result = taskRepository.getAllTasks().first()
        
        // Assert
        assertEquals(tasks, result)
    }
    
    @Test
    fun `getTasksByStatus returns filtered tasks`() = runBlocking {
        // Arrange
        val completedTasks = listOf(
            Task(id = 1, title = "Task 1", isCompleted = true)
        )
        every { taskDao.getTasksByStatus(true) } returns flowOf(completedTasks)
        
        // Act
        val result = taskRepository.getTasksByStatus(true).first()
        
        // Assert
        assertEquals(completedTasks, result)
        assertEquals(true, result.all { it.isCompleted })
    }
    
    @Test
    fun `getTaskById returns task from DAO`() = runBlocking {
        // Arrange
        val task = Task(id = 1, title = "Task 1")
        coEvery { taskDao.getTaskById(1) } returns task
        
        // Act
        val result = taskRepository.getTaskById(1)
        
        // Assert
        assertNotNull(result)
        assertEquals(task, result)
    }
    
    @Test
    fun `insertTask adds task and creates task order`() = runBlocking {
        // Arrange
        val task = Task(title = "New Task")
        val taskId = 1L
        coEvery { taskDao.insertTask(task) } returns taskId
        coEvery { taskOrderDao.getMaxPosition() } returns 2
        
        val taskOrderSlot = slot<TaskOrder>()
        coJustRun { taskOrderDao.insertTaskOrder(capture(taskOrderSlot)) }
        
        // Act
        val result = taskRepository.insertTask(task)
        
        // Assert
        assertEquals(taskId, result)
        assertEquals(taskId, taskOrderSlot.captured.taskId)
        assertEquals(3, taskOrderSlot.captured.position) // Max + 1
    }
    
    @Test
    fun `toggleTaskCompletion inverts completion status`() = runBlocking {
        // Arrange
        val task = Task(id = 1, title = "Task", isCompleted = false)
        val expectedUpdatedTask = task.copy(isCompleted = true)
        
        val updatedTaskSlot = slot<Task>()
        coJustRun { taskDao.updateTask(capture(updatedTaskSlot)) }
        
        // Act
        taskRepository.toggleTaskCompletion(task)
        
        // Assert
        assertEquals(expectedUpdatedTask, updatedTaskSlot.captured)
        assertEquals(true, updatedTaskSlot.captured.isCompleted)
    }
    
    @Test
    fun `deleteTask removes task and its order entry`() = runBlocking {
        // Arrange
        val task = Task(id = 1, title = "Task")
        coJustRun { taskDao.deleteTask(task) }
        coJustRun { taskOrderDao.deleteTaskOrder(task.id) }
        
        // Act
        taskRepository.deleteTask(task)
        
        // Assert
        coVerify { taskDao.deleteTask(task) }
        coVerify { taskOrderDao.deleteTaskOrder(task.id) }
    }
    
    @Test
    fun `moveTask delegates to DAO`() = runBlocking {
        // Arrange
        coJustRun { taskOrderDao.moveTask(1, 2) }
        
        // Act
        taskRepository.moveTask(1, 2)
        
        // Assert
        coVerify { taskOrderDao.moveTask(1, 2) }
    }
    
    @Test
    fun `insertSampleTasks creates predefined tasks`() = runBlocking {
        // Arrange
        val taskSlot = mutableListOf<Task>()
        coEvery { taskDao.insertTask(capture(taskSlot)) } returns 1L
        coEvery { taskOrderDao.getMaxPosition() } returns 0
        coJustRun { taskOrderDao.insertTaskOrder(any()) }
        
        // Act
        taskRepository.insertSampleTasks()
        
        // Assert
        // Verify we inserted multiple tasks
        assert(taskSlot.size >= 3) { "Should have inserted at least 3 sample tasks" }
        
        // Verify task properties were properly set
        taskSlot.forEach { task ->
            assertNotNull(task.title)
            assert(task.title.isNotEmpty()) { "Task title should not be empty" }
        }
    }
}