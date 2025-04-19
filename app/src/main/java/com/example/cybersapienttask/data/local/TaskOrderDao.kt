package com.example.cybersapienttask.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.cybersapienttask.data.model.TaskOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskOrderDao {
    @Query("SELECT * FROM task_order ORDER BY position ASC")
    fun getTaskOrder(): Flow<List<TaskOrder>>

    @Query("SELECT * FROM task_order WHERE taskId = :taskId")
    suspend fun getTaskOrderItem(taskId: Long): TaskOrder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskOrder(taskOrder: TaskOrder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTaskOrders(taskOrders: List<TaskOrder>)

    @Query("DELETE FROM task_order WHERE taskId = :taskId")
    suspend fun deleteTaskOrder(taskId: Long)

    @Query("SELECT MAX(position) FROM task_order")
    suspend fun getMaxPosition(): Int?

    @Transaction
    suspend fun moveTask(fromPosition: Int, toPosition: Int) {
        // Get all task orders sorted by position
        val taskOrders = getTaskOrderSync()

        if (fromPosition < 0 || fromPosition >= taskOrders.size ||
            toPosition < 0 || toPosition >= taskOrders.size
        ) {
            return
        }

        // Remove the item from the old position
        val movedItem = taskOrders.removeAt(fromPosition)

        // Insert the item at the new position
        taskOrders.add(toPosition, movedItem)

        // Update positions for all items
        taskOrders.forEachIndexed { index, taskOrder ->
            taskOrder.position = index
        }

        // Save all changes
        insertAllTaskOrders(taskOrders)
    }

    @Query("SELECT * FROM task_order ORDER BY position ASC")
    suspend fun getTaskOrderSync(): MutableList<TaskOrder>
}