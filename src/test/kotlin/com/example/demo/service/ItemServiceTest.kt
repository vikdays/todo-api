package com.example.demo.service

import com.example.demo.dto.request.CreateTask
import com.example.demo.dto.response.TaskResponse
import com.example.demo.entity.Task
import com.example.demo.enums.TaskPriority
import com.example.demo.enums.TaskStatus
import com.example.demo.repository.ItemRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
/*
@ExtendWith(MockitoExtension::class)
class ItemServiceTest {
    private lateinit var repository: ItemRepository
    private lateinit var service: ItemService

    @BeforeEach
    fun setup() {
        repository = mock()
        service = ItemService(repository)
    }

    private fun sampleTask(
        id: Long = 1L,
        priority: TaskPriority = TaskPriority.Medium,
        deadline: LocalDate? = LocalDate.now().plusDays(1),
        isDone: Boolean = false,
        status: TaskStatus = TaskStatus.Active
    ): Task {
        return Task(
            id = id,
            title = "Sample",
            description = "Description",
            deadline = deadline,
            priority = priority,
            status = status,
            isDone = isDone,
            createdAt = LocalDate.now().minusDays(1),
            updatedAt = LocalDate.now()
        )
    }
    // 🆕 Эквивалентное разбиение: корректные макросы приоритетов
    @ParameterizedTest
    @CsvSource(
        "!1 Critical task, Critical",
        "!2 High task, High",
        "!3 Medium task, Medium",
        "!4 Low task, Low"
    )
    fun `createTask should parse priority macros correctly`(macroTitle: String, expectedPriority: String) {
        val request = CreateTask(macroTitle, "desc")
        val fakeTask = sampleTask(priority = TaskPriority.valueOf(expectedPriority))
        whenever(repository.save(any())).thenReturn(fakeTask)

        val result = service.createTask(request)

        assertEquals(TaskPriority.valueOf(expectedPriority), result.priority)
    }

    @ParameterizedTest
    @CsvSource(
        "Task !before 01.01.2026, 2026-01-01",  // стандартный формат
        "Task !before 01-01-2026, 2026-01-01",  // с дефисами

    )
    fun `createTask should parse different date formats in deadline macro`(title: String, expectedDate: String) {
        val request = CreateTask(title, "desc")
        val expected = LocalDate.parse(expectedDate)
        val savedTask = sampleTask(deadline = expected)

        `when`(repository.save(any())).thenReturn(savedTask)

        val result = service.createTask(request)

        assertEquals(expected, result.deadline)
        assertFalse(result.title.contains("!before"))
    }

    @ParameterizedTest
    @CsvSource(
        "!1 Critical !before 01.01.2026, Critical, 2026-01-01",
        "!2 High !before 15.06.2025, High, 2025-06-15"
    )
    fun `createTask should handle combined priority and deadline macros`(
        title: String,
        expectedPriority: String,
        expectedDate: String
    ) {
        val request = CreateTask(title, "desc")
        val priority = TaskPriority.valueOf(expectedPriority)
        val deadline = LocalDate.parse(expectedDate)
        val savedTask = sampleTask(priority = priority, deadline = deadline)

        `when`(repository.save(any())).thenReturn(savedTask)

        val result = service.createTask(request)

        assertEquals(priority, result.priority)
        assertEquals(deadline, result.deadline)
        assertFalse(result.title.contains("!"))
    }

    @Test
    fun createTask_withMinimalRequest_returnsCorrectTaskResponse() {
        val request = CreateTask(
            title = "Test task",
            description = "Test description",
            deadline = "01.01.2026"
        )

        val savedTask = createTestTask()

        `when`(repository.save(any())).thenReturn(savedTask)

        val result = service.createTask(request)

        assertTaskEquals(savedTask, result)
        verify(repository).save(any())
    }

    @Test
    fun createTask_withDeadlineToday_returnsCorrectTaskResponse() {
        val today = LocalDate.now()
        val formatted = today.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))

        val request = CreateTask(
            title = "Task today",
            description = "Due today",
            deadline = formatted
        )

        val expected = Task(
            id = 3L,
            title = "Task today",
            description = "Due today",
            deadline = today,
            priority = TaskPriority.Medium,
            status = TaskStatus.Active,
            isDone = false,
            createdAt = today,
            updatedAt = today
        )

        Mockito.`when`(repository.save(any())).thenReturn(expected)

        val result = service.createTask(request)
        assertEquals(today, result.deadline)
    }

    @Test
    fun createTask_withMacrosForDeadline_returnsCorrectTaskResponse() {
        val request = CreateTask(
            title = "Important task !before 01.01.2026",
            description = "Test with macro",
            deadline = null
        )

        val expectedDeadline = LocalDate.of(2026, 1, 1)
        val expected = Task(
            id = 4L,
            title = "Important task",
            description = "Test with macro",
            deadline = expectedDeadline,
            priority = TaskPriority.Medium,
            status = TaskStatus.Active,
            isDone = false,
            createdAt = LocalDate.now(),
            updatedAt = LocalDate.now()
        )

        Mockito.`when`(repository.save(any())).thenReturn(expected)

        val result = service.createTask(request)
        assertEquals(expectedDeadline, result.deadline)
        assertEquals("Important task", result.title)
    }


    private fun createTestTask(): Task {
        return Task(
            id = 1L,
            title = "Test task",
            description = "Test description",
            deadline = LocalDate.of(2026, 1, 1),
            priority = TaskPriority.Medium,
            status = TaskStatus.Active,
            isDone = false,
            createdAt = LocalDate.now(),
            updatedAt = LocalDate.now()
        )
    }

    private fun assertTaskEquals(expected: Task, actual: TaskResponse) {
        assertEquals(expected.id, actual.id)
        assertEquals(expected.title, actual.title)
        assertEquals(expected.description, actual.description)
        assertEquals(expected.priority, actual.priority)
        assertEquals(expected.deadline, actual.deadline)
        assertEquals(expected.status, actual.status)
    }

    @Test
    fun `createTask should throw exception for invalid date format in macro`() {
        val request = CreateTask(
            title = "Do something !before 32.13.2024", // Неправильная дата
            description = "Bad macro test",
            deadline = null
        )

        // Не нужно мокировать repository.save(), так как исключение должно выброситься раньше
        val ex = assertThrows<IllegalArgumentException> {
            service.createTask(request)
        }

        assertTrue(ex.message!!.contains("Некорректный формат даты") ||
                ex.message!!.contains("Дата указана некорректно"))
    }

    @Test
    fun `createTask should throw exception for past date in macro`() {
        val pastDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        val request = CreateTask(
            title = "Finish project !before $pastDate",
            description = "Past deadline test",
            deadline = null
        )

        val ex = assertThrows<IllegalArgumentException> {
            service.createTask(request)
        }

        assertTrue(ex.message!!.contains("Дедлайн не может быть в прошлом"))
    }

    @Test
    fun `createTask should throw exception for invalid priority macro`() {
        val request = CreateTask(
            title = "Critical issue !5",
            description = "Invalid priority macro",
            deadline = null
        )
        val ex = assertThrows<IllegalArgumentException> {
            service.createTask(request)
        }

        assertEquals("Недопустимый приоритет: 5", ex.message)
    }


    // 🎯 Эквивалентное разбиение
    @Test
    fun `createTask with valid request creates task`() {
        val request = CreateTask("!1 Valid Task", "desc")
        val task = sampleTask(priority = TaskPriority.Low)
        `when`(repository.save(any())).thenReturn(task)

        val result = service.createTask(request)

        assertEquals(TaskPriority.Low, result.priority)
        assertEquals("Sample", result.title)
    }

    // 🎯 Граничные значения: дата дедлайна = сегодня
    @Test
    fun `createTask with today deadline assigns Active status`() {
        val today = LocalDate.now()
        val formatted = today.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        val request = CreateTask("!1 !before $formatted Title", "desc")

        whenever(repository.save(any())).thenAnswer { it.arguments[0] }

        val result = service.createTask(request)

        assertEquals(TaskStatus.Active, result.status)
        assertEquals(TaskPriority.Critical, result.priority)
        assertEquals(today, result.deadline)
    }

    // 🎯 Попарное тестирование: статус + дедлайн
    @Test
    fun `toggleDoneStatus for overdue task sets Late`() {
        val overdueTask = sampleTask(deadline = LocalDate.now().minusDays(3), isDone = false)
        `when`(repository.findById(1L)).thenReturn(Optional.of(overdueTask))
        `when`(repository.save(any())).thenAnswer { it.arguments[0] }

        val result = service.toggleDoneStatus(1L)

        assertEquals(TaskStatus.Late, result!!.status)
    }

    // 🎯 Граничные значения: фильтрация с null параметрами
    @Test
    fun `getAllTasksFilteredSorted with null filters returns all`() {
        val task1 = sampleTask(1L)
        val task2 = sampleTask(2L)
        `when`(repository.findAll()).thenReturn(listOf(task1, task2))

        val result = service.getAllTasksFilteredSorted("createdAt", "asc", null, null, null)

        assertEquals(2, result.size)
    }

    // 🎯 Неверное значение в фильтре — эквивалентное разбиение (негативный кейс)
    @Test
    fun `getAllTasksFilteredSorted with invalid status value returns empty list`() {
        val task = sampleTask()
        `when`(repository.findAll()).thenReturn(listOf(task))

        val result = service.getAllTasksFilteredSorted("priority", "asc", "INVALID_STATUS", null, null)

        assertEquals(0, result.size)
    }

    // 🎯 Комбинации фильтров: isDone + priority
    @Test
    fun `getAllTasksFilteredSorted filters by isDone and priority`() {
        val task1 = sampleTask(1, isDone = true, priority = TaskPriority.High)
        val task2 = sampleTask(2, isDone = false, priority = TaskPriority.High)
        val task3 = sampleTask(3, isDone = true, priority = TaskPriority.Medium)
        `when`(repository.findAll()).thenReturn(listOf(task1, task2, task3))

        val result = service.getAllTasksFilteredSorted("priority", "asc", null, true, "High")

        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
    }

    // 🎯 Пустой список задач
    @Test
    fun `getAllTasksFilteredSorted returns empty when repository empty`() {
        `when`(repository.findAll()).thenReturn(emptyList())

        val result = service.getAllTasksFilteredSorted("createdAt", "asc", null, null, null)

        assertTrue(result.isEmpty())
    }

    // 🎯 Проверка корректности сортировки по дедлайну
    //необязательно!
    @Test
    fun `getAllTasksFilteredSorted sorts by deadline ascending`() {
        val t1 = sampleTask(1L, deadline = LocalDate.of(2025, 5, 1))
        val t2 = sampleTask(2L, deadline = LocalDate.of(2025, 4, 1))
        `when`(repository.findAll()).thenReturn(listOf(t1, t2))

        val result = service.getAllTasksFilteredSorted("deadline", "asc", null, null, null)

        assertEquals(2L, result[0].id)
        assertEquals(1L, result[1].id)
    }

    // 🎯 Проверка граничного условия: дедлайн null
    //необязательно!
    @Test
    fun `getAllTasksFilteredSorted handles null deadlines by pushing to end`() {
        val t1 = sampleTask(1L, deadline = null)
        val t2 = sampleTask(2L, deadline = LocalDate.now())
        `when`(repository.findAll()).thenReturn(listOf(t1, t2))

        val result = service.getAllTasksFilteredSorted("deadline", "asc", null, null, null)

        assertEquals(2L, result[0].id)  // у кого есть дедлайн — должен быть первым
        assertEquals(1L, result[1].id)
    }
}*/

@ExtendWith(MockitoExtension::class)
class ItemServiceTest {

    @Mock
    private lateinit var repository: ItemRepository

    @InjectMocks
    private lateinit var service: ItemService

    // 🎯 Эквивалентное разбиение: проверка корректной интерпретации макросов приоритета
    @ParameterizedTest
    @CsvSource(
        "!1 Critical task, Critical",
        "!2 High task, High",
        "!3 Medium task, Medium",
        "!4 Low task, Low"
    )
    @DisplayName("createTask: корректный разбор макросов приоритета (!1-!4)")
    fun `createTask should parse priority macros correctly`(macroTitle: String, expectedPriority: String) {
        val request = CreateTask(macroTitle, "desc")
        val fakeTask = sampleTask(priority = TaskPriority.valueOf(expectedPriority))

        whenever(repository.save(any())).thenReturn(fakeTask)

        val result = service.createTask(request)

        assertEquals(TaskPriority.valueOf(expectedPriority), result.priority)
    }

    // 🎯 Граничные значения: два разных допустимых формата даты
    @ParameterizedTest
    @CsvSource(
        "Task !before 01.01.2026, 2026-01-01",
        "Task !before 01-01-2026, 2026-01-01"
    )
    @DisplayName("createTask: поддержка разных форматов даты в макросе дедлайна")
    fun `createTask should parse different date formats in deadline macro`(title: String, expectedDate: String) {
        val request = CreateTask(title, "desc")
        val expected = LocalDate.parse(expectedDate)

        val savedTask = sampleTask(deadline = expected)
        whenever(repository.save(any())).thenReturn(savedTask)

        val result = service.createTask(request)

        assertEquals(expected, result.deadline)
        assertFalse(result.title.contains("!before"))
    }

    // 🎯 Комбинированное эквивалентное разбиение: макрос приоритета + дедлайна
    @ParameterizedTest
    @CsvSource(
        "!1 Critical !before 01.01.2026, Critical, 2026-01-01",
        "!2 High !before 15.06.2025, High, 2025-06-15"
    )
    @DisplayName("createTask: корректная обработка комбинированных макросов (!1 !before)")
    fun `createTask should handle combined priority and deadline macros`(
        title: String,
        expectedPriority: String,
        expectedDate: String
    ) {
        val request = CreateTask(title, "desc")
        val priority = TaskPriority.valueOf(expectedPriority)
        val deadline = LocalDate.parse(expectedDate)

        val savedTask = sampleTask(priority = priority, deadline = deadline)
        whenever(repository.save(any())).thenReturn(savedTask)

        val result = service.createTask(request)

        assertEquals(priority, result.priority)
        assertEquals(deadline, result.deadline)
        assertFalse(result.title.contains("!"))
    }

    @Test
    @DisplayName("createTask: с минимальными данными возвращает корректный результат")
    fun createTask_withMinimalRequest_returnsCorrectTaskResponse() {
        val request = CreateTask("Test task", "Test description", "01.01.2026")
        val savedTask = createTestTask()

        whenever(repository.save(any())).thenReturn(savedTask)

        val result = service.createTask(request)

        assertTaskEquals(savedTask, result)
        verify(repository).save(any())
    }

    @Test
    @DisplayName("createTask: дедлайн сегодня — статус Active")
    fun createTask_withDeadlineToday_returnsCorrectTaskResponse() {
        val today = LocalDate.now()
        val formatted = today.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

        val request = CreateTask("Task today", "Due today", formatted)
        val expected = sampleTask(deadline = today)

        whenever(repository.save(any())).thenReturn(expected)

        val result = service.createTask(request)
        assertEquals(today, result.deadline)
    }

    @Test
    @DisplayName("createTask: дедлайн из макроса !before обрабатывается корректно")
    fun createTask_withMacrosForDeadline_returnsCorrectTaskResponse() {
        val request = CreateTask("Important task !before 01.01.2026", "Test with macro")
        val expectedDeadline = LocalDate.of(2026, 1, 1)
        val expected = sampleTask(title = "Important task", deadline = expectedDeadline)

        whenever(repository.save(any())).thenReturn(expected)

        val result = service.createTask(request)

        assertEquals(expectedDeadline, result.deadline)
        assertEquals("Important task", result.title)
    }

    // ❌ Негативные кейсы: некорректная дата
    @Test
    @DisplayName("createTask: ошибка при некорректной дате в макросе")
    fun `createTask should throw exception for invalid date format in macro`() {
        val request = CreateTask("Do something !before 32.13.2024", "Bad macro test")

        val ex = assertThrows<IllegalArgumentException> {
            service.createTask(request)
        }

        assertTrue(ex.message!!.contains("Некорректный формат даты"))
    }

    // ❌ Граничное значение: дедлайн в прошлом
    @Test
    @DisplayName("createTask: ошибка при дедлайне в прошлом")
    fun `createTask should throw exception for past date in macro`() {
        val pastDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        val request = CreateTask("Finish project !before $pastDate", "Past deadline test")

        val ex = assertThrows<IllegalArgumentException> {
            service.createTask(request)
        }

        assertTrue(ex.message!!.contains("Дедлайн не может быть в прошлом"))
    }

    @Test
    @DisplayName("createTask: '!5' не считается макросом приоритета и остаётся в заголовке")
    fun `createTask should ignore invalid priority macro and keep title unchanged`() {
        // Arrange
        val request = CreateTask(
            title = "Проблема уровня !5",
            description = "Неподдерживаемый макрос"
        )

        // Act
        val result = service.createTask(request)

        // Assert
        assertEquals("Проблема уровня !5", result.title)
        assertEquals(TaskPriority.Medium, result.priority) // дефолт
    }

    @Test
    @DisplayName("createTask: happy path — создаёт задачу")
    fun `createTask with valid request creates task`() {
        val request = CreateTask("!1 Valid Task", "desc")
        val task = sampleTask(priority = TaskPriority.Critical)

        whenever(repository.save(any())).thenReturn(task)

        val result = service.createTask(request)

        assertEquals(TaskPriority.Critical, result.priority)
        assertEquals("Sample", result.title)
    }

    @Test
    @DisplayName("toggleDoneStatus: просроченная задача получает статус Late")
    fun `toggleDoneStatus for overdue task sets Late`() {
        val overdueTask = sampleTask(deadline = LocalDate.now().minusDays(3), isDone = false)
        whenever(repository.findById(1L)).thenReturn(Optional.of(overdueTask))
        whenever(repository.save(any())).thenAnswer { it.arguments[0] }

        val result = service.toggleDoneStatus(1L)

        assertEquals(TaskStatus.Late, result!!.status)
    }

    // 🎯 Эквивалентное разбиение: null фильтры
    @Test
    @DisplayName("getAllTasksFilteredSorted: null фильтры возвращают все задачи")
    fun `getAllTasksFilteredSorted with null filters returns all`() {
        val task1 = sampleTask(1L)
        val task2 = sampleTask(2L)
        whenever(repository.findAll()).thenReturn(listOf(task1, task2))

        val result = service.getAllTasksFilteredSorted("createdAt", "asc", null, null, null)

        assertEquals(2, result.size)
    }

    @Test
    @DisplayName("getAllTasksFilteredSorted: фильтрация по isDone и priority")
    fun `getAllTasksFilteredSorted filters by isDone and priority`() {
        val task1 = sampleTask(1, isDone = true, priority = TaskPriority.High)
        val task2 = sampleTask(2, isDone = false, priority = TaskPriority.High)
        val task3 = sampleTask(3, isDone = true, priority = TaskPriority.Medium)

        whenever(repository.findAll()).thenReturn(listOf(task1, task2, task3))

        val result = service.getAllTasksFilteredSorted("priority", "asc", null, true, "High")

        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
    }

    @Test
    @DisplayName("getAllTasksFilteredSorted: пустой список из репозитория")
    fun `getAllTasksFilteredSorted returns empty when repository empty`() {
        whenever(repository.findAll()).thenReturn(emptyList())

        val result = service.getAllTasksFilteredSorted("createdAt", "asc", null, null, null)

        assertTrue(result.isEmpty())
    }

    @Test
    @DisplayName("getAllTasksFilteredSorted: дедлайн null — задачи в конце")
    fun `getAllTasksFilteredSorted handles null deadlines by pushing to end`() {
        val t1 = sampleTask(1L, deadline = null)
        val t2 = sampleTask(2L, deadline = LocalDate.now())

        whenever(repository.findAll()).thenReturn(listOf(t1, t2))

        val result = service.getAllTasksFilteredSorted("deadline", "asc", null, null, null)

        assertEquals(2L, result[0].id)
        assertEquals(1L, result[1].id)
    }

    // 🔧 Утилиты
    private fun sampleTask(
        id: Long = 1L,
        title: String = "Sample",
        description: String = "Description",
        deadline: LocalDate? = LocalDate.now().plusDays(1),
        priority: TaskPriority = TaskPriority.Medium,
        status: TaskStatus = TaskStatus.Active,
        isDone: Boolean = false
    ) = Task(id, title, description, deadline, status, priority, LocalDate.now().minusDays(1), LocalDate.now(), isDone)

    private fun createTestTask(): Task = sampleTask(deadline = LocalDate.of(2026, 1, 1))

    private fun assertTaskEquals(expected: Task, actual: TaskResponse) {
        assertEquals(expected.id, actual.id)
        assertEquals(expected.title, actual.title)
        assertEquals(expected.description, actual.description)
        assertEquals(expected.priority, actual.priority)
        assertEquals(expected.deadline, actual.deadline)
        assertEquals(expected.status, actual.status)
    }
}


