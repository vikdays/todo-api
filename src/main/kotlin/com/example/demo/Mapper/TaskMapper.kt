package com.example.demo.Mapper

import com.example.demo.dto.request.CreateTask
import com.example.demo.dto.request.EditTaskRequest
import com.example.demo.dto.response.MacroParseResult
import com.example.demo.dto.response.TaskResponse
import com.example.demo.entity.Task
import com.example.demo.enums.TaskPriority
import com.example.demo.enums.TaskStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object TaskMapper {

    private val priorityRegex = Regex("!([1-4])")
    private val deadlineRegex = Regex("!before\\s+([\\d]{2,4}[.-][\\d]{1,2}[.-][\\d]{2,4})")
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    fun fromCreateRequest(request: CreateTask): Task {
        val macros = extractMacros(request.title)

        val effectiveDeadline = when {
            request.deadline != null -> parseDate(request.deadline)
            macros.deadline != null -> macros.deadline
            else -> null
        }

        return Task(
            title = macros.cleanedTitle,
            description = request.description,
            deadline = effectiveDeadline,
            priority = request.priority ?: macros.priority ?: TaskPriority.Medium,
            createdAt = LocalDate.now(),
            updatedAt = LocalDate.now(),
            status = TaskStatus.Active,
            isDone = false
        )
    }

    fun fromEditRequest(request: EditTaskRequest, existing: Task): Task {
        val macros = request.title?.let { extractMacros(it) }

        val newTitle = when {
            request.title != null && macros != null -> macros.cleanedTitle
            request.title != null -> request.title
            else -> existing.title
        }

        val newDescription = request.description ?: existing.description

        val effectivePriority = when {
            request.priority != null -> request.priority
            macros?.priority != null -> macros.priority
            else -> existing.priority
        }

        val effectiveDeadline = when {
            request.deadline != null -> {
                try {
                    val parsedDate = parseDate(request.deadline.toString())
                    if (parsedDate.isBefore(LocalDate.now())) {
                        throw IllegalArgumentException("Дедлайн не может быть в прошлом: ${request.deadline}")
                    }
                    parsedDate
                } catch (e: Exception) {
                    throw IllegalArgumentException("Неверный формат даты: ${request.deadline}. Используйте dd.MM.yyyy, dd-MM-yyyy или yyyy-MM-dd")
                }
            }

            macros?.deadline != null -> {
                if (macros.deadline.isBefore(LocalDate.now())) {
                    throw IllegalArgumentException("Дедлайн не может быть в прошлом: ${macros.deadline.format(dateFormatter)}")
                }
                macros.deadline
            }

            else -> existing.deadline
        }

        return existing.copy(
            title = newTitle,
            description = newDescription,
            deadline = effectiveDeadline,
            priority = effectivePriority,
            updatedAt = LocalDate.now()
        )
    }

    fun toResponse(task: Task): TaskResponse {
        return TaskResponse(
            id = task.id ?: 0,
            title = task.title,
            description = task.description,
            deadline = task.deadline,
            status = task.status,
            priority = task.priority,
            createdAt = task.createdAt,
            updatedAt = task.updatedAt,
            isDone = task.isDone
        )
    }

    private fun extractMacros(rawTitle: String): MacroParseResult {
        var title = rawTitle
        var priority: TaskPriority? = null
        var deadline: LocalDate? = null

        priorityRegex.find(title)?.let {
            priority = when (it.groupValues[1]) {
                "1" -> TaskPriority.Critical
                "2" -> TaskPriority.High
                "3" -> TaskPriority.Medium
                "4" -> TaskPriority.Low
                else -> throw IllegalArgumentException("Недопустимый приоритет: ${it.groupValues[1]}")
            }
            title = title.replace(it.value, "").trim()
        }

        deadlineRegex.find(title)?.let {
            val originalStr = it.groupValues[1]

            val normalized = originalStr.replace("-", ".")
            val strictDateRegex = Regex("""\d{2}\.\d{2}\.\d{4}""")

            if (!strictDateRegex.matches(normalized)) {
                throw IllegalArgumentException("Некорректный формат даты: $originalStr. Используйте dd.MM.yyyy или dd-MM-yyyy.")
            }

            try {
                val parsedDate = LocalDate.parse(normalized, dateFormatter)
                if (parsedDate.isBefore(LocalDate.now())) {
                    throw IllegalArgumentException("Дедлайн не может быть в прошлом: $normalized")
                }
                deadline = parsedDate
            } catch (e: Exception) {
                throw IllegalArgumentException("Дата указана некорректно: $originalStr")
            }

            title = title.replace(it.value, "").trim()
        }

        return MacroParseResult(
            cleanedTitle = title,
            priority = priority,
            deadline = deadline
        )
    }
}
private val supportedDateFormats = listOf(
    DateTimeFormatter.ISO_LOCAL_DATE,
    DateTimeFormatter.ofPattern("dd.MM.yyyy"),
    DateTimeFormatter.ofPattern("dd-MM-yyyy")
)

fun parseDate(dateStr: String): LocalDate {
    for (formatter in supportedDateFormats) {
        try {
            return LocalDate.parse(dateStr, formatter)
        } catch (e: DateTimeParseException) {
           throw e;
        }
    }
    throw IllegalArgumentException("Неподдерживаемый формат даты: $dateStr")
}