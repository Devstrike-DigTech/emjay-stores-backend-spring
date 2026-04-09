package com.emjay.backend.ads.domain.entity

import java.time.LocalDateTime
import java.util.UUID

data class Ad(
    val id: UUID? = null,
    val headline: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val appliesTo: AdTarget = AdTarget.ALL,
    val targetIds: List<UUID> = emptyList(),
    val status: AdStatus = AdStatus.ACTIVE,
    val createdBy: UUID,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun isCurrentlyActive(): Boolean =
        status == AdStatus.ACTIVE &&
        LocalDateTime.now().isAfter(startDate) &&
        LocalDateTime.now().isBefore(endDate)
}

enum class AdTarget { ALL, PRODUCTS, SERVICES, CATEGORIES }
enum class AdStatus { ACTIVE, INACTIVE, SCHEDULED, EXPIRED }
