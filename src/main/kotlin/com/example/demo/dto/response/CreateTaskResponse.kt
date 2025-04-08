package com.example.demo.dto.response

import com.example.demo.enums.TaskPriority
import java.time.LocalDate

data class CreateTaskResponse(
    val title: String,
    val description: String? = null,
    val deadline: LocalDate? = null,
    val priority: TaskPriority? = null
)