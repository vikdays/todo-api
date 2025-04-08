package com.example.demo.dto.request

import com.example.demo.enums.TaskPriority
import java.time.LocalDate

data class EditTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val deadline: LocalDate? = null,
    val priority: TaskPriority? = null
)
