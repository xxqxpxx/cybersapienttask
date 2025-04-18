package com.example.cybersapienttask.data.model

import  androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate


@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueDate: LocalDate? = null,
    val isCompleted: Boolean = false,
    val createdDate: LocalDate = LocalDate.now()
)