package com.example.demo.exceptions

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mapOf("error" to e.message.orEmpty()))
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleIllegalArgument(ex: AccessDeniedException): ResponseEntity<String> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ex.message ?: "Нет прав")
    }

}