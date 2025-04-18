package com.example.cybersapienttask.data.util

import androidx.room.TypeConverter
import com.example.cybersapienttask.data.model.TaskPriority
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority): String {
        return priority.name
    }

    @TypeConverter
    fun toTaskPriority(priority: String): TaskPriority {
        return enumValueOf(priority)
    }
}