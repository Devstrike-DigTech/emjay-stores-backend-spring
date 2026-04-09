package com.emjay.backend.ims.domain.entity.product

import java.time.LocalDateTime
import java.util.UUID

data class ProductReview(
    val id: UUID? = null,
    val productId: UUID,
    val customerId: UUID? = null,
    val reviewerName: String,
    val rating: Int,
    val title: String? = null,
    val comment: String? = null,
    val isVerifiedPurchase: Boolean = false,
    val isApproved: Boolean = false,
    val createdAt: LocalDateTime? = null
) {
    fun isValid(): Boolean = rating in 1..5
}
