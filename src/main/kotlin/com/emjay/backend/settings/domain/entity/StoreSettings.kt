package com.emjay.backend.settings.domain.entity

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class StoreSettings(
    val id: UUID? = null,
    val storeName: String,
    val storeDescription: String? = null,
    val logoUrl: String? = null,
    val contactEmail: String? = null,
    val contactPhone: String? = null,
    val address: String? = null,
    val dateStarted: LocalDate? = null,
    val lastMaintenance: LocalDate? = null,
    val developerCompany: String? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)
