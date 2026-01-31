package com.emjay.backend.infrastructure.config

import com.emjay.backend.infrastructure.storage.FileStorageService
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfig(
    private val fileStorageService: FileStorageService
) {

    @PostConstruct
    fun init() {
        fileStorageService.init()
    }
}