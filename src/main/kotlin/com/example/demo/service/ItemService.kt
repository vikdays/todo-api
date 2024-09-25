package com.example.demo.service
import com.example.demo.dto.CreateTask
import com.example.demo.dto.toDoMain
import com.example.demo.entity.Task
import com.example.demo.repository.ItemRepository
import org.springframework.stereotype.Service


@Service
class ItemService(private val repository : ItemRepository) {

    fun saveTask(createTaskDto: CreateTask) {
        repository.save(createTaskDto.toDoMain())
    }
    fun getAllTasks(): List<Task> {
        return repository.findAll()
    }

    fun getTaskById(id: Long): Task? {
        return repository.findById(id).orElse(null)
    }

    fun updateTask(id: Long, updatedTask: Task): Task? {
        val existingTask = repository.findById(id).orElse(null) ?: return null
        val newTask = existingTask.copy(description = updatedTask.description, isDone = updatedTask.isDone)
        return repository.save(newTask)
    }

    fun deleteTask(id: Long) {
        repository.deleteById(id)
    }
    fun deleteAllTasks() {
        repository.deleteAll()
    }
    fun changeTaskFlag(id: Long): Task? {
        val existingTask = repository.findById(id).orElse(null) ?: return null
        val updatedTask = existingTask.copy(isDone = !existingTask.isDone)
        return repository.save(updatedTask)
    }
}
