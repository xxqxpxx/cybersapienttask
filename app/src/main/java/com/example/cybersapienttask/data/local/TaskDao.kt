package com.example.cybersapienttask.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cybersapienttask.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = :isCompleted")
    fun getTasksByStatus(isCompleted: Boolean): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?

    @Query("SELECT * FROM tasks ORDER BY priority DESC")
    fun getTasksOrderedByPriority(): Flow<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getTasksOrderedByDueDate(): Flow<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY title ASC")
    fun getTasksOrderedAlphabetically(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
}