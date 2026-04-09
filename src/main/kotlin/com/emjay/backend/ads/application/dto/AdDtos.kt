package com.emjay.backend.ads.application.dto

import com.emjay.backend.ads.domain.entity.AdStatus
import com.emjay.backend.ads.domain.entity.AdTarget
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.UUID

data class CreateAdRequest(
    @field:NotBlank(message = "Headline is required")
    val headline: String,
    val description: String? = null,
    val imageUrl: String? = null,
    @field:NotNull(message = "Start date is required")
    val startDate: LocalDateTime,
    @field:NotNull(message = "End date is required")
    val endDate: LocalDateTime,
    val appliesTo: AdTarget = AdTarget.ALL,
    val targetIds: List<UUID> = emptyList()
)

data class UpdateAdRequest(
    val headline: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val appliesTo: AdTarget? = null,
    val targetIds: List<UUID>? = null,
    val status: AdStatus? = null
)

data class AdResponse(
    val id: String,
    val headline: String,
    val description: String?,
    val imageUrl: String?,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val appliesTo: AdTarget,
    val targetIds: List<String>,
    val status: AdStatus,
    val isActive: Boolean,
    val createdBy: String,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)
