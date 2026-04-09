package com.emjay.backend.ims.application.dto.product

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.UUID

data class CreateReviewRequest(
    @field:NotBlank(message = "Reviewer name is required")
    val reviewerName: String,
    @field:NotNull(message = "Rating is required")
    @field:Min(value = 1, message = "Rating must be at least 1")
    @field:Max(value = 5, message = "Rating must be at most 5")
    val rating: Int,
    val title: String? = null,
    val comment: String? = null,
    val customerId: UUID? = null
)

data class ReviewResponse(
    val id: String,
    val productId: String,
    val customerId: String?,
    val reviewerName: String,
    val rating: Int,
    val title: String?,
    val comment: String?,
    val isVerifiedPurchase: Boolean,
    val isApproved: Boolean,
    val createdAt: LocalDateTime?
)

data class ReviewSummaryResponse(
    val productId: String,
    val averageRating: Double,
    val totalReviews: Int,
    val approvedReviews: Int,
    val pendingReviews: Int,
    val breakdown: Map<Int, Int>   // star (1-5) → count
)
