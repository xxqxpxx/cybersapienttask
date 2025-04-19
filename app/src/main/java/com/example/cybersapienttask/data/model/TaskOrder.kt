package com.example.cybersapienttask.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_order")
data class TaskOrder(
    @PrimaryKey
    val taskId: Long,
    var position: Int
)