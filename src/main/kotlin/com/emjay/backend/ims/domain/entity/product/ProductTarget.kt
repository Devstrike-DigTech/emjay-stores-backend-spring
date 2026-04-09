package com.emjay.backend.ims.domain.entity.product

import java.time.LocalDateTime
import java.util.UUID

data class ProductTarget(
    val id: UUID? = null,
    val productId: UUID,
    val targetYear: Int,
    val targetMonth: Int,
    val targetUnits: Int,
    val createdBy: UUID,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)
