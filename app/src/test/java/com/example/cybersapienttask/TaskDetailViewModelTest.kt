package com.example.cybersapienttask

import androidx.lifecycle.SavedStateHandle
import com.example.cybersapienttask.data.model.Task
import com.example.cybersapienttask.data.model.TaskPriority
import com.example.cybersapienttask.domain.repo.TaskRepository
import com.example.cybersapienttask.ui.screens.taskdetails.TaskDetailViewModel
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.bouncycastle.util.test.SimpleTest.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@ExperimentalCoroutinesApi
class TaskDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: TaskRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: TaskDetailViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        savedStateHandle = SavedStateHandle()
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `init with taskId loads task data`() {

            // Arrange
            val taskId = 1L
            savedStateHandle["taskId"] = taskId

            val task = Task(
                id = taskId,
                title = "Test Task",
                description = "Test Description",
                priority = TaskPriority.HIGH,
                dueDate = LocalDate.now(),
                isCompleted = true
            )

            coEvery { repository.getTaskById(taskId) } returns task

            // Act
            viewModel = TaskDetailViewModel(repository, savedStateHandle , taskId)
            testDispatcher.scheduler.advanceUntilIdle()

            // Assert
            assertEquals(task, viewModel.task.value)
            assertEquals(task.title, viewModel.title.value)
            assertEquals(task.description, viewModel.description.value)
            assertEquals(task.priority, viewModel.priority.value)
            assertEquals(task.dueDate, viewModel.dueDate.value)
            assertEquals(task.isCompleted, viewModel.isCompleted.value)


    }

    @Test
    fun `init without taskId initializes empty state`()  {
        // Arrange & Act
        viewModel = TaskDetailViewModel(repository, savedStateHandle , 0)
        
        // Assert
        assertNull(viewModel.task.value)
        assertEquals("", viewModel.title.value)
        assertEquals("", viewModel.description.value)
        assertEquals(TaskPriority.MEDIUM, viewModel.priority.value)
        assertNull(viewModel.dueDate.value)
        assertFalse(viewModel.isCompleted.value)
    }
    
    @Test
    fun `updateTitle updates the title state`() {
        // Arrange
        viewModel = TaskDetailViewModel(repository, savedStateHandle , 0)
        val newTitle = "New Title"
        
        // Act
        viewModel.updateTitle(newTitle)
        
        // Assert
        assertEquals(newTitle, viewModel.title.value)
    }
    
    @Test
    fun `updateDescription updates the description state`() {
        // Arrange
        viewModel = TaskDetailViewModel(repository, savedStateHandle , 0)
        val newDescription = "New Description"
        
        // Act
        viewModel.updateDescription(newDescription)
        
        // Assert
        assertEquals(newDescription, viewModel.description.value)
    }
    
    @Test
    fun `updatePriority updates the priority state`() {
        // Arrange
        viewModel = TaskDetailViewModel(repository, savedStateHandle , 0)
        val newPriority = TaskPriority.HIGH
        
        // Act
        viewModel.updatePriority(newPriority)
        
        // Assert
        assertEquals(newPriority, viewModel.priority.value)
    }
    
    @Test
    fun `updateDueDate updates the dueDate state`() {
        // Arrange
        viewModel = TaskDetailViewModel(repository, savedStateHandle , 0)
        val newDueDate = LocalDate.now().plusDays(7)
        
        // Act
        viewModel.updateDueDate(newDueDate)
        
        // Assert
        assertEquals(newDueDate, viewModel.dueDate.value)
    }

    @Test
    fun `toggleCompletion inverts the isCompleted state`() {
        // Arrange
        viewModel = TaskDetailViewModel(repository, savedStateHandle , 0)
        assertEquals(false, viewModel.isCompleted.value) // Default is false
        
        // Act - first toggle
        viewModel.toggleCompletion()
        
        // Assert
        assertEquals(true, viewModel.isCompleted.value)
        
        // Act - second toggle
        viewModel.toggleCompletion()
        
        // Assert
        assertEquals(false, viewModel.isCompleted.value)
    }
    
    @Test
    fun `saveTask with empty title returns false`() {
        // Arrange
        viewModel = TaskDetailViewModel(repository, savedStateHandle , 0)
        viewModel.updateTitle("")
        
        // Act
        val result = viewModel.saveTask()
        
        // Assert
        assertFalse(result)
        coVerify(exactly = 0) { repository.insertTask(any()) }
        coVerify(exactly = 0) { repository.updateTask(any()) }
    }
    
    @Test
    fun `saveTask with valid title and no ID creates new task`() {
        // Arrange
        viewModel = TaskDetailViewModel(repository, savedStateHandle , 0)
        val title = "New Task"
        viewModel.updateTitle(title)
        
        val taskSlot = slot<Task>()
        coEvery { repository.insertTask(capture(taskSlot)) } returns 1L
        
        // Act
        val result = viewModel.saveTask()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert
        assertTrue(result)
      //  assertEquals(title, taskSlot.captured.title)
        assertEquals(0L, taskSlot.captured.id) // New task
        coVerify { repository.insertTask(any()) }
    }
    
    @Test
    fun `saveTask with valid title and existing ID updates task`() {
        // Arrange
        val taskId = 5L
        savedStateHandle["taskId"] = taskId
        
        val existingTask = Task(
            id = taskId,
            title = "Existing Task",
            description = "Old Description"
        )
        
        coEvery { repository.getTaskById(taskId) } returns existingTask
        
        viewModel = TaskDetailViewModel(repository, savedStateHandle , taskId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Update task properties
        val newTitle = "Updated Task"
        viewModel.updateTitle(newTitle)
        
        val taskSlot = slot<Task>()
        coJustRun { repository.updateTask(capture(taskSlot)) }
        
        // Act
        val result = viewModel.saveTask()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert
        assertTrue(result)
        assertEquals(newTitle, taskSlot.captured.title)
        assertEquals(taskId, taskSlot.captured.id) // Same ID
        coVerify { repository.updateTask(any()) }
    }
    
    @Test
    fun `deleteTask calls repository delete`() {
        // Arrange
        val taskId = 5L
        savedStateHandle["taskId"] = taskId
        
        val existingTask = Task(
            id = taskId,
            title = "Task to Delete"
        )
        
        coEvery { repository.getTaskById(taskId) } returns existingTask
        coJustRun { repository.deleteTask(any()) }
        
        viewModel = TaskDetailViewModel(repository, savedStateHandle , taskId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Act
        viewModel.deleteTask()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert
        coVerify { repository.deleteTask(existingTask) }
    }
}