package com.example.demo.mapper

import com.example.demo.Mapper.TaskMapper
import com.example.demo.dto.request.CreateTask
import com.example.demo.dto.request.EditTaskRequest
import com.example.demo.entity.Task
import com.example.demo.enums.TaskPriority
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.log

class TaskMapperTest {

    // region: CreateTask

    /**
     * Корректный парсинг приоритета из заголовка задачи
     */
    @ParameterizedTest
    @CsvSource(
        "!1 Critical task, Critical",
        "!2 High task, High",
        "!3 Medium task, Medium",
        "!4 Low task, Low"
    )
    fun fromCreateRequest_withPriorityMacro_parsesCorrectly(title: String, expectedPriority: String) {
        val request = CreateTask(title, "desc")
        val result = TaskMapper.fromCreateRequest(request)

        assertEquals(TaskPriority.valueOf(expectedPriority), result.priority)
    }

    /**
     * Удаление макроса приоритета из заголовка
     */

    @ParameterizedTest
    @CsvSource(
        "!1 Critical task, Critical task",
        "!2 High task, High task",
        "!3 Medium task, Medium task",
        "!4 Low task, Low task"
    )
    fun fromCreateRequest_withPriorityMacro_removesMacroFromTitleCorrectly(title: String, expectedTitle: String) {
        val request = CreateTask(title, "desc")
        val result = TaskMapper.fromCreateRequest(request)

        assertEquals(expectedTitle, result.title)
    }

    /**
     * Парсинг макроса дедлайна (!before) с разными форматами даты
     */
    @ParameterizedTest
    @CsvSource(
        "Task !before 01.01.2026, 2026-01-01",
        "Task !before 01-01-2026, 2026-01-01"
    )
    fun fromCreateRequest_withValidDateFormats_parsesSuccessfully(title: String, expectedDate: String) {
        val request = CreateTask(title, "desc")
        val expected = LocalDate.parse(expectedDate)

        val result = TaskMapper.fromCreateRequest(request)

        assertEquals(expected, result.deadline)
        assertFalse(result.title.contains("!before"))

    }

    /**
     * Заголовок отчищается от макроса
     */
    @ParameterizedTest
    @CsvSource(
        "Task1 !before 01.01.2026, Task1",
        "Task2 !before 01-01-2026, Task2"
    )
    fun fromCreateRequest_withValidDateFormats_removesMacroFromTitleCorrectly(title: String, expectedTitle: String) {
        val request = CreateTask(title, "desc")

        val result = TaskMapper.fromCreateRequest(request)

        assertEquals(expectedTitle, result.title)

    }

    /**
     * Некорректный формат макроса дедлайна должен вызвать ошибку
     */
    @Test
    fun fromCreateRequest_withDeadlineMacroButNoDate_throwsException() {
        val request = CreateTask("Task !before", "desc")

        val ex = assertThrows<IllegalArgumentException> {
            TaskMapper.fromCreateRequest(request)
        }

        assertTrue(ex.message!!.contains("Макрос дедлайна написан с ошибкой"))
    }

    /**
     * Проверка на обработку макроса дедлайна и приоритета одновременно
     */
    @ParameterizedTest
    @CsvSource(
        "!1 Critical !before 01.01.2026, Critical, 2026-01-01",
        "!2 High !before 15.06.2025, High, 2025-06-15"
    )
    fun fromCreateRequest_withPriorityAndDeadlineMacros_parsesBothCorrectly(
        title: String,
        expectedPriority: String,
        expectedDate: String
    ) {
        val request = CreateTask(title, "desc")
        val result = TaskMapper.fromCreateRequest(request)

        assertEquals(TaskPriority.valueOf(expectedPriority), result.priority)
        assertEquals(LocalDate.parse(expectedDate), result.deadline)
        assertFalse(result.title.contains("!"))
    }

    /**
     * Некорректный формат макроса дедлайна должен вызвать ошибку
     */
    @Test
    fun fromCreateRequest_withInvalidDateMacro_throwsException() {
        val request = CreateTask("Task !before 32.12.2026", "desc")
        val ex = assertThrows<IllegalArgumentException> {
            TaskMapper.fromCreateRequest(request)
        }
        assertTrue(ex.message!!.contains("Некорректный формат даты"))
    }

    /**
     * Дедлайн в прошлом должен вызвать исключение
     */
    @Test
    fun fromCreateRequest_withPastDeadline_throwsException() {
        val past = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        val request = CreateTask("Task !before $past", "desc")

        val ex = assertThrows<IllegalArgumentException> {
            TaskMapper.fromCreateRequest(request)
        }

        assertTrue(ex.message!!.contains("Дедлайн не может быть в прошлом"))
    }

    /**
     * Некорректный формат макроса приоритета должен остаться в заголовке
     */
    @Test
    fun fromCreateRequest_withUnknownPriorityMacro_keepsMacroAndDefaultsPriority() {
        val request = CreateTask("Something !5 important", "desc")
        val task = TaskMapper.fromCreateRequest(request)

        assertEquals("Something !5 important", task.title)
        assertEquals("desc", task.description)
        assertEquals(TaskPriority.Medium, task.priority)
    }

    /**
     * Дедлайн сегодня
     */
    @Test
    fun fromCreateRequest_withTodayAsDeadline_parsesSuccessfully() {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        val request = CreateTask("Today task !before $today", "desc")

        val result = TaskMapper.fromCreateRequest(request)
        assertEquals(LocalDate.now(), result.deadline)
    }

    /**
     * Валидный запрос создаёт задачу
     */
    @Test
    fun fromCreateRequest_withValidPriorityAndTitle_createsTaskSuccessfully() {
        val request = CreateTask("!1 Important task", "desc")
        val result = TaskMapper.fromCreateRequest(request)

        assertEquals(TaskPriority.Critical, result.priority)
        assertEquals("Important task", result.title)
    }

    /**
     * Пустой заголовок должен вызвать исключение
     */
    @Test
    fun fromCreateRequest_withBlankTitle_throwsException() {
        val request = CreateTask("   ", "desc")

        val ex = assertThrows<IllegalArgumentException> {
            TaskMapper.fromCreateRequest(request)
        }

        assertEquals("Title can't be empty!", ex.message)
    }

    /**
     * Длина заголовка меньше 4 символов должна вызвать исключение
     */
    @Test
    fun fromCreateRequest_withTooShortTitleAfterMacroRemoval_throwsException() {
        val request = CreateTask("!1 A", "desc")

        val ex = assertThrows<IllegalArgumentException> {
            TaskMapper.fromCreateRequest(request)
        }

        assertTrue(ex.message!!.contains("Минимальная длина заголовка"))
        assertTrue(ex.message!!.contains("Было: 1"))
    }

    // endregion


    // region: EditTaskRequest

    /**
     * Приоритет должен обновляться при редактировании, если указан макрос
     */
    @ParameterizedTest
    @CsvSource(
        "!1 Critical task, Critical",
        "!2 High task, High",
        "!3 Medium task, Medium",
        "!4 Low task, Low"
    )
    fun fromEditRequest_withPriorityMacro_parsesCorrectly(title: String, expectedPriority: String) {
        val request = EditTaskRequest(title, "desc", null, null)
        val existingTask = Task(id = 1, title = "Old title", description = "desc", priority = TaskPriority.Medium, createdAt = LocalDate.now(), updatedAt = LocalDate.now())

        val result = TaskMapper.fromEditRequest(request, existingTask)

        assertEquals(TaskPriority.valueOf(expectedPriority), result.priority)
    }

    /**
     * Макрос приоритета должен удаляться из заголовка при редактировании
     */
    @ParameterizedTest
    @CsvSource(
        "!1 Critical task, Critical task",
        "!2 High task, High task",
        "!3 Medium task, Medium task",
        "!4 Low task, Low task"
    )
    fun fromEditRequest_withPriorityMacro_removesMacroFromTitleCorrectly(title: String, expectedTitle: String) {
        val request = EditTaskRequest(title, "desc", null, null)
        val existingTask = Task(id = 1, title = "Old title", description = "desc", priority = TaskPriority.Medium, createdAt = LocalDate.now(), updatedAt = LocalDate.now())

        val result = TaskMapper.fromEditRequest(request, existingTask)

        assertEquals(expectedTitle, result.title)
    }

    /**
     * Корректная обработка валидного дедлайна через макрос при редактировании
     */
    @ParameterizedTest
    @CsvSource(
        "Task !before 01.01.2026, 2026-01-01",
        "Task !before 01-01-2026, 2026-01-01"
    )
    fun fromEditRequest_withValidDateFormats_parsesSuccessfully(title: String, expectedDate: String) {
        val request = EditTaskRequest(title, "desc", null, null)
        val existingTask = Task(id = 1, title = "Old title", description = "desc", priority = TaskPriority.Medium, createdAt = LocalDate.now(), updatedAt = LocalDate.now())

        val expected = LocalDate.parse(expectedDate)

        val result = TaskMapper.fromEditRequest(request, existingTask)

        assertEquals(expected, result.deadline)
        assertFalse(result.title.contains("!before"))
    }

    /**
     * Обработка валидного дедлайна и приоритета через макрос при редактировании
     */
    @ParameterizedTest
    @CsvSource(
        "!1 Critical !before 01.01.2026, Critical, 2026-01-01",
        "!2 High !before 15.06.2025, High, 2025-06-15"
    )
    fun fromEditRequest_withPriorityAndDeadlineMacros_parsesBothCorrectly(
        title: String,
        expectedPriority: String,
        expectedDate: String
    ) {
        val request = EditTaskRequest(title, "desc", null, null)
        val existingTask = Task(id = 1, title = "Old title", description = "desc", priority = TaskPriority.Medium, createdAt = LocalDate.now(), updatedAt = LocalDate.now())

        val result = TaskMapper.fromEditRequest(request, existingTask)

        assertEquals(TaskPriority.valueOf(expectedPriority), result.priority)
        assertEquals(LocalDate.parse(expectedDate), result.deadline)
        assertFalse(result.title.contains("!"))
    }

    /**
     * Обработка не валидного дедлайна через макрос при редактировании
     */
    @Test
    fun fromEditRequest_withInvalidDateMacro_throwsException() {
        val request = EditTaskRequest("Task !before 32.12.2026", "desc", null, null)
        val existingTask = Task(id = 1, title = "Old title", description = "desc", priority = TaskPriority.Medium, createdAt = LocalDate.now(), updatedAt = LocalDate.now())

        val ex = assertThrows<IllegalArgumentException> {
            TaskMapper.fromEditRequest(request, existingTask)
        }
        assertTrue(ex.message!!.contains("Некорректный формат даты"))
    }

    /**
     * Обработка валидного дедлайна при редактировании
     */
    @Test
    fun fromEditRequest_withFutureRequestDeadline_parsesSuccessfully() {
        val futureDate = LocalDate.now().plusDays(5).toString()
        val request = EditTaskRequest("Edit title", "desc", deadline = futureDate, priority = null)
        val existing = Task(
            id = 1,
            title = "Old",
            description = "desc",
            priority = TaskPriority.Medium,
            createdAt = LocalDate.now(),
            updatedAt = LocalDate.now()
        )

        val result = TaskMapper.fromEditRequest(request, existing)

        assertEquals(futureDate, result.deadline.toString())
    }

    /**
     * Обработка дедлайна в прошлом через макрос при редактировании
     */
    @Test
    fun fromEditRequest_withPastDeadlineMacro_throwsException() {
        val dateStr = LocalDate.now().minusDays(3).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        val request = EditTaskRequest("Old task !before $dateStr", "desc", null, null)
        val existing = Task(
            id = 1,
            title = "Old task",
            description = "desc",
            priority = TaskPriority.Low,
            createdAt = LocalDate.now(),
            updatedAt = LocalDate.now()
        )

        val ex = assertThrows<IllegalArgumentException> {
            TaskMapper.fromEditRequest(request, existing)
        }

        assertTrue(ex.message!!.contains("Дедлайн не может быть в прошлом"))
        assertTrue(ex.message!!.contains(dateStr))
    }

    /**
     * Обработка дедлайна в прошлом при редактировании
     */
    @Test
    fun fromEditRequest_withPastRequestDeadline_throwsException() {
        val pastDate = LocalDate.now().minusDays(1).toString()
        val request = EditTaskRequest("Title", "desc", deadline = pastDate, priority = null)
        val existing = Task(
            id = 1,
            title = "Old",
            description = "desc",
            priority = TaskPriority.Medium,
            createdAt = LocalDate.now(),
            updatedAt = LocalDate.now()
        )

        val ex = assertThrows<IllegalArgumentException> {
            TaskMapper.fromEditRequest(request, existing)
        }

        assertEquals("Дедлайн не может быть в прошлом: $pastDate", ex.message)
    }

    /**
     * Обработка неподдерживаемого формата дедлайна при редактировании
     */
    @Test
    fun fromEditRequest_withInvalidRequestDeadlineFormat_throwsException() {
        val request = EditTaskRequest("Title", "desc", deadline = "tomorrow", priority = null)
        val existing = Task(
            id = 1,
            title = "Old",
            description = "desc",
            priority = TaskPriority.Medium,
            createdAt = LocalDate.now(),
            updatedAt = LocalDate.now()
        )

        val ex = assertThrows<IllegalArgumentException> {
            TaskMapper.fromEditRequest(request, existing)
        }
        assertTrue(ex.message!!.contains("Неподдерживаемый формат даты"))
    }

    /**
     * Обработка несуществуещего приоритета через макрос при редактировании
     */
    @Test
    fun fromEditRequest_withUnknownPriorityMacro_keepsMacroAndDefaultsPriority() {
        val request = EditTaskRequest("Something !5 important", "desc", null, null)
        val existingTask = Task(id = 1, title = "Old title", description = "desc", priority = TaskPriority.Medium, createdAt = LocalDate.now(), updatedAt = LocalDate.now())

        val task = TaskMapper.fromEditRequest(request, existingTask)

        assertEquals("Something !5 important", task.title)
        assertEquals("desc", task.description)
        assertEquals(TaskPriority.Medium, task.priority)
    }

    /**
     * Обработка дедлайна сегодня через макрос при редактировании
     */
    @Test
    fun fromEditRequest_withTodayAsDeadline_parsesSuccessfully() {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        val request = EditTaskRequest("Today task !before $today", "desc", null, null)
        val existingTask = Task(id = 1, title = "Old title", description = "desc", priority = TaskPriority.Medium, createdAt = LocalDate.now(), updatedAt = LocalDate.now())

        val result = TaskMapper.fromEditRequest(request, existingTask)

        assertEquals(LocalDate.now(), result.deadline)
    }

    /**
     * Обработка валидного запроса при редактировании
     */
    @Test
    fun fromEditRequest_withValidPriorityAndTitle_updatesTaskCorrectly() {
        val request = EditTaskRequest("!1 Important task", "desc", null, null)
        val existingTask = Task(id = 1, title = "Old title", description = "desc", priority = TaskPriority.Medium, createdAt = LocalDate.now(), updatedAt = LocalDate.now())

        val result = TaskMapper.fromEditRequest(request, existingTask)

        assertEquals(TaskPriority.Critical, result.priority)
        assertEquals("Important task", result.title)
    }

    /**
     * Обработка не валидного дедлайна через макрос при редактировании
     */
    @Test
    fun fromEditRequest_withDeadlineMacroButNoDate_throwsException() {
        val request = EditTaskRequest("Task !before", "desc", null, null)
        val existingTask = Task(
            id = 1,
            title = "Old title",
            description = "desc",
            priority = TaskPriority.Medium,
            createdAt = LocalDate.now(),
            updatedAt = LocalDate.now()
        )

        val ex = assertThrows<IllegalArgumentException> {
            TaskMapper.fromEditRequest(request, existingTask)
        }

        assertTrue(ex.message!!.contains("Макрос дедлайна написан с ошибкой"))
    }
    // endregion3
}
