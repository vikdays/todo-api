package com.example.demo.controller

import com.example.demo.dto.request.CreateTask
import com.example.demo.dto.request.EditTaskRequest
import com.example.demo.dto.response.TaskResponse
import com.example.demo.enums.TaskPriority
import com.example.demo.enums.TaskStatus
import com.example.demo.service.ItemService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate

@WebMvcTest(TaskController::class)
@Import(TaskControllerTest.MockServiceConfig::class)
class TaskControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var service: ItemService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @TestConfiguration
    class MockServiceConfig {
        @Bean
        fun itemService(): ItemService = mock(ItemService::class.java)
    }

    private fun sampleTaskResponse(): TaskResponse {
        return TaskResponse(
            id = 1L,
            title = "Test Task",
            description = "Test Description",
            deadline = LocalDate.of(2025, 4, 30),
            status = TaskStatus.Active,
            priority = TaskPriority.Medium,
            createdAt = LocalDate.now(),
            updatedAt = LocalDate.now(),
            isDone = false
        )
    }

    @Test
    fun fromCreateRequest_withPriorityMedium_parsesCorrectly() {
        val request = CreateTask(
            title = "Test Task",
            description = "Test Description",
            deadline = "30.04.2025",
            priority = TaskPriority.Medium
        )
        val response = sampleTaskResponse()

        `when`(service.createTask(request)).thenReturn(response)

        mockMvc.perform(
            post("/api/tasks/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("Test Task"))
    }

    @Test
    fun fromGetAllTasks_withOneTask_returnsTaskList() {
        val response = listOf(sampleTaskResponse())

        `when`(service.getAllTasksFilteredSorted("createdAt", "asc", null, null, null)).thenReturn(response)

        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].title").value("Test Task"))
    }

    @Test
    fun fromGetTaskById_withExistingId_returnsTask() {
        val response = sampleTaskResponse()

        `when`(service.getTaskById(1L)).thenReturn(response)

        mockMvc.perform(get("/api/tasks/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
    }

    @Test
    fun fromGetTaskById_withNonExistingId_returns404() {
        `when`(service.getTaskById(999L)).thenReturn(null)

        mockMvc.perform(get("/api/tasks/999"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun fromEditTaskRequest_withUpdatedData_returnsUpdatedTask() {
        val request = EditTaskRequest(
            title = "Updated Task",
            description = "Updated Description",
            deadline = "30.04.2025",
            priority = TaskPriority.High
        )
        val response = sampleTaskResponse().copy(
            title = request.title!!,
            description = request.description!!,
            priority = request.priority!!,
        )

        `when`(service.editTask(1L, request)).thenReturn(response)

        mockMvc.perform(
            put("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("Updated Task"))
    }

    @Test
    fun fromEditTaskRequest_withNonExistingTask_returns404() {
        val request = EditTaskRequest(
            title = "Doesn't matter",
            description = "irrelevant",
            deadline = "01.01.2026",
            priority = TaskPriority.Low
        )

        `when`(service.editTask(999L, request)).thenReturn(null)

        mockMvc.perform(
            put("/api/tasks/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isNotFound)
    }

    @Test
    fun fromToggleTaskDoneStatus_withValidTask_returnsUpdatedTask() {
        val response = sampleTaskResponse().copy(isDone = true)

        `when`(service.toggleDoneStatus(1L)).thenReturn(response)

        mockMvc.perform(put("/api/tasks/1/toggle"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isDone").value(true))
    }

    @Test
    fun fromToggleTaskDoneStatus_withNonExistingTask_returns404() {
        `when`(service.toggleDoneStatus(999L)).thenReturn(null)

        mockMvc.perform(put("/api/tasks/999/toggle"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun fromDeleteTask_withValidTask_returnsNoContent() {
        mockMvc.perform(delete("/api/tasks/1"))
            .andExpect(status().isNoContent)
    }
}
