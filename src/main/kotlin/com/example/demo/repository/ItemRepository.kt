package com.example.demo.repository

import com.example.demo.entity.Task
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface ItemRepository : JpaRepository<Task, Long> {
    override fun findById(id: Long): Optional<Task>
}
