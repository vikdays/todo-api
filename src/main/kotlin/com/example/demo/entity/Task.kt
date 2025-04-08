package com.example.demo.entity

import com.example.demo.enums.TaskPriority
import com.example.demo.enums.TaskStatus
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "task")
data class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var title: String,

    val description: String?,

    var deadline: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TaskStatus = TaskStatus.Active,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var priority: TaskPriority = TaskPriority.Medium,

    @Column(nullable = false)
    val createdAt: LocalDate = LocalDate.now(),

    var updatedAt: LocalDate = LocalDate.now(),

    var isDone: Boolean = false
)
