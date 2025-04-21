package com.example.demo.service

import com.example.demo.Mapper.TaskMapper
import com.example.demo.dto.request.CreateTask
import com.example.demo.dto.request.EditTaskRequest
import com.example.demo.dto.response.TaskResponse
import com.example.demo.entity.Task
import com.example.demo.enums.TaskPriority
import com.example.demo.enums.TaskStatus
import com.example.demo.repository.ItemRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ItemService(private val repository: ItemRepository) {

    fun createTask(request: CreateTask): TaskResponse {
        val task = TaskMapper.fromCreateRequest(request)
        val saved = repository.save(updateStatus(task))
        return TaskMapper.toResponse(saved)
    }

    fun getAllTasksFilteredSorted(
        sortBy: String,
        order: String,
        status: String?,
        isDone: Boolean?,
        priority: String?
    ): List<TaskResponse> {
        val allTasks = repository.findAll().map { updateStatus(it) }

        val filtered = allTasks.filter { task ->
            val statusMatches = status?.let {
                runCatching {
                    task.status == TaskStatus.valueOf(it.capitalize())
                }.getOrDefault(false)
            } ?: true

            val doneMatches = isDone?.let { task.isDone == it } ?: true

            val priorityMatches = priority?.let {
                runCatching {
                    task.priority == TaskPriority.valueOf(it.capitalize())
                }.getOrDefault(false)
            } ?: true

            statusMatches && doneMatches && priorityMatches
        }

        val sorted = when (sortBy.lowercase()) {
            "priority" -> filtered.sortedBy { it.priority.ordinal }
            "createdat" -> filtered.sortedBy { it.createdAt }
            "updatedat" -> filtered.sortedBy { it.updatedAt }
            "deadline" -> filtered.sortedBy { it.deadline ?: LocalDate.MAX }
            "status" -> filtered.sortedBy { it.status.ordinal }
            "isdone" -> filtered.sortedBy { it.isDone }
            else -> filtered.sortedBy { it.createdAt }
        }

        return if (order.equals("desc", ignoreCase = true)) {
            sorted.reversed()
        } else {
            sorted
        }.map(TaskMapper::toResponse)
    }

    fun getTaskById(id: Long): TaskResponse? {
        val task = repository.findById(id).orElse(null) ?: return null
        return TaskMapper.toResponse(updateStatus(task))
    }

    fun editTask(id: Long, request: EditTaskRequest): TaskResponse? {
        val existing = repository.findById(id).orElse(null) ?: return null
        val updated = TaskMapper.fromEditRequest(request, existing)
        val saved = repository.save(updateStatus(updated))
        return TaskMapper.toResponse(saved)
    }

    fun deleteTask(id: Long) {
        if (!repository.existsById(id)) {
            throw IllegalArgumentException("Задача с id $id не найдена")
        }
        repository.deleteById(id)
    }

    fun toggleDoneStatus(id: Long): TaskResponse? {
        val existing = repository.findById(id).orElse(null) ?: return null
        val now = LocalDate.now()
        val toggled = existing.copy(
            isDone = !existing.isDone,
            status = resolveStatus(existing.deadline, !existing.isDone, now),
            updatedAt = now
        )
        return TaskMapper.toResponse(repository.save(toggled))
    }

    private fun updateStatus(task: Task): Task {
        val now = LocalDate.now()
        val status = resolveStatus(task.deadline, task.isDone, now)
        return task.copy(status = status)
    }

    private fun resolveStatus(deadline: LocalDate?, isDone: Boolean, now: LocalDate): TaskStatus {
        return when {
            isDone && deadline != null && now.isAfter(deadline) -> TaskStatus.Late
            isDone -> TaskStatus.Completed
            deadline != null && now.isAfter(deadline) -> TaskStatus.Overdue
            else -> TaskStatus.Active
        }
    }
}
