package com.example.demo.controller

import com.example.demo.dto.request.CreateTask
import com.example.demo.dto.request.EditTaskRequest
import com.example.demo.dto.response.TaskResponse
import com.example.demo.service.ItemService
import org.springframework.http.ResponseEntity
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/tasks")
class TaskController(private val service: ItemService, private val objectMapper: ObjectMapper) {

    @PostMapping(("create"))
    fun createTask(@RequestBody request: CreateTask): ResponseEntity<TaskResponse> {
        val created = service.createTask(request)
        return ResponseEntity.ok(created)
    }

    @GetMapping
    fun getAllTasks(
        @RequestParam(required = false, defaultValue = "createdAt") sortBy: String,
        @RequestParam(required = false, defaultValue = "asc") order: String,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) isDone: Boolean?,
        @RequestParam(required = false) priority: String?
    ): ResponseEntity<List<TaskResponse>> {
        val tasks = service.getAllTasksFilteredSorted(sortBy, order, status, isDone, priority)
        return ResponseEntity.ok(tasks)
    }

    @GetMapping("/{id}")
    fun getTaskById(@PathVariable id: Long): ResponseEntity<TaskResponse> {
        val task = service.getTaskById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(task)
    }

    @PutMapping("/{id}")
    fun updateTask(@PathVariable id: Long, @RequestBody request: EditTaskRequest): ResponseEntity<TaskResponse> {
        val updated = service.editTask(id, request) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(updated)
    }

    @PutMapping("/{id}/toggle")
    fun toggleDone(@PathVariable id: Long): ResponseEntity<TaskResponse> {
        val result = service.toggleDoneStatus(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(result)
    }

    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: Long): ResponseEntity<Void> {
        service.deleteTask(id)
        return ResponseEntity.noContent().build()
    }
}
