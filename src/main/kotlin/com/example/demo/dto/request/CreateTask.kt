package com.example.demo.dto.request

import com.example.demo.enums.TaskPriority
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class CreateTask @JvmOverloads constructor(
    val title: String,
    val description: String? = null,
    val deadline: String? = null,
    val priority: TaskPriority? = null
)