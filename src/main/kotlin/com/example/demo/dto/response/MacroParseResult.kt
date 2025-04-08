package com.example.demo.dto.response

import com.example.demo.enums.TaskPriority
import java.time.LocalDate

data class MacroParseResult(
    val cleanedTitle: String,
    val priority: TaskPriority?,
    val deadline: LocalDate?
)