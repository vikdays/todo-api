package com.example.demo.dto.response

import com.example.demo.enums.TaskPriority
import com.example.demo.enums.TaskStatus
import java.time.LocalDate

data class TaskResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val deadline: LocalDate?,
    val status: TaskStatus,
    val priority: TaskPriority,
    val createdAt: LocalDate,
    val updatedAt: LocalDate,
    val isDone: Boolean,
)