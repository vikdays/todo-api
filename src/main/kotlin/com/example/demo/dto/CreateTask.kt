package com.example.demo.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.example.demo.entity.Task

data class CreateTask @JsonCreator constructor(
    @JsonProperty("description") val description: String,
    @JsonProperty("isDone") val isDone: Boolean = false
)

fun CreateTask.toDoMain(): Task {
    return Task(description = description, isDone = isDone)
}
