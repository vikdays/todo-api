package com.example.demo.controller

import com.example.demo.dto.request.CreateTask
import com.example.demo.dto.request.EditTaskRequest
import com.example.demo.dto.response.TaskResponse
import com.example.demo.service.ItemService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/tasks")
class TaskController(private val service: ItemService) {

    @PostMapping("/create")
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
        return service.getTaskById(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @PutMapping("/{id}")
    fun updateTask(@PathVariable id: Long, @RequestBody request: EditTaskRequest): ResponseEntity<TaskResponse> {
        return service.editTask(id, request)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @PutMapping("/{id}/toggle")
    fun toggleDone(@PathVariable id: Long): ResponseEntity<TaskResponse> {
        return service.toggleDoneStatus(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: Long): ResponseEntity<Void> {
        service.deleteTask(id)
        return ResponseEntity.noContent().build()
    }
}
