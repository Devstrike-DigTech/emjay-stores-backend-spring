package com.emjay.backend.ims.domain.repository.product

import com.emjay.backend.ims.domain.entity.product.ProductReview
import java.util.UUID

interface ProductReviewRepository {
    fun save(review: ProductReview): ProductReview
    fun findById(id: UUID): ProductReview?
    fun findByProductId(productId: UUID, approvedOnly: Boolean = true): List<ProductReview>
    fun findPendingApproval(): List<ProductReview>
    fun countByProductId(productId: UUID, approvedOnly: Boolean = true): Long
    fun averageRatingByProductId(productId: UUID): Double
    fun ratingBreakdownByProductId(productId: UUID): Map<Int, Int>
    fun delete(id: UUID)
}
