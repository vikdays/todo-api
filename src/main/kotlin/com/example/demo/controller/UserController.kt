package com.example.demo.controller

import com.example.demo.dto.CreateTask
import com.example.demo.entity.Task
import com.example.demo.service.ItemService
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.http.ResponseEntity
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.InputStreamReader

@RestController
@RequestMapping("api/tasks")
class TaskController(private val service: ItemService, private val objectMapper: ObjectMapper) {

    @PostMapping("create")
    fun createTask(@RequestBody createTaskDto: CreateTask): ResponseEntity<String> {
        service.saveTask(createTaskDto)
        return ResponseEntity.ok("Task created")
    }
    @PostMapping("load")
    fun loadTasks(@RequestBody tasks: List<CreateTask>): ResponseEntity<String> {
        service.deleteAllTasks()
        if (tasks.isEmpty()) {
            return ResponseEntity.badRequest().body("Task list is empty")
        }

        tasks.forEach { task ->
            if (task.description.isBlank()) {
                return ResponseEntity.badRequest().body("Task description cannot be empty")
            }
            service.saveTask(task)
        }

        return ResponseEntity.ok("Tasks loaded successfully")
    }


    @PostMapping("upload")
    fun uploadTasks(@RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        if (file.isEmpty) {
            return ResponseEntity.badRequest().body("File is empty")
        }

        val tasks = ObjectMapper().readValue(file.inputStream, object : TypeReference<List<CreateTask>>() {})
        service.deleteAllTasks() // Предварительное удаление всех задач
        tasks.forEach { service.saveTask(it) }

        return ResponseEntity.ok("Tasks loaded successfully")
    }

    @GetMapping
    fun getAllTasks(): ResponseEntity<List<Task>> {
        val tasks = service.getAllTasks()
        return ResponseEntity.ok(tasks)
    }

    @GetMapping("{id}")
    fun getTaskById(@PathVariable id: Long): ResponseEntity<Task?> {
        val task = service.getTaskById(id)
        return if (task != null) ResponseEntity.ok(task) else ResponseEntity.notFound().build()
    }

    @PutMapping("{id}")
    fun updateTask(@PathVariable id: Long, @RequestBody updatedTask: Task): ResponseEntity<Task?> {
        val updated = service.updateTask(id, updatedTask)
        return if (updated != null) ResponseEntity.ok(updated) else ResponseEntity.notFound().build()
    }

    @DeleteMapping("{id}")
    fun deleteTask(@PathVariable id: Long): ResponseEntity<String> {
        service.deleteTask(id)
        return ResponseEntity.ok("Task deleted")
    }
    @PutMapping("{id}/change-flag")
    fun changeFlag(@PathVariable id: Long): ResponseEntity<Task?> {
        val updated = service.changeTaskFlag(id)
        return if (updated != null) ResponseEntity.ok(updated) else ResponseEntity.notFound().build()
    }



}
