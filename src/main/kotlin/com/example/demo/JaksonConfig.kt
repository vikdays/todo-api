package com.example.demo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JaksonConfig {

    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().registerModule(KotlinModule())
    }
}
