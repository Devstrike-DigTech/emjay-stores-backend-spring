package com.emjay.backend.ims.application.dto.product

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class SetProductTargetRequest(
    @field:NotNull(message = "Year is required")
    val year: Int,
    @field:NotNull(message = "Month is required")
    @field:Min(value = 1, message = "Month must be between 1 and 12")
    val month: Int,
    @field:NotNull(message = "Target units is required")
    @field:Min(value = 1, message = "Target must be at least 1")
    val targetUnits: Int
)

data class ProductTargetResponse(
    val id: String,
    val productId: String,
    val year: Int,
    val month: Int,
    val targetUnits: Int,
    val actualUnitsSold: Int,
    val progressPercentage: BigDecimal,
    val createdBy: String
)
