package com.emjay.backend.ims.application.service

import com.emjay.backend.ims.application.dto.product.CreateReviewRequest
import com.emjay.backend.ims.application.dto.product.ReviewResponse
import com.emjay.backend.ims.application.dto.product.ReviewSummaryResponse
import com.emjay.backend.ims.domain.entity.product.ProductReview
import com.emjay.backend.ims.domain.repository.product.ProductReviewRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ProductReviewService(
    private val productReviewRepository: ProductReviewRepository
) {

    @Transactional
    fun createReview(productId: UUID, request: CreateReviewRequest): ReviewResponse {
        val review = ProductReview(
            productId = productId,
            customerId = request.customerId,
            reviewerName = request.reviewerName,
            rating = request.rating,
            title = request.title,
            comment = request.comment,
            isVerifiedPurchase = false,
            isApproved = false   // requires admin approval
        )
        return toResponse(productReviewRepository.save(review))
    }

    fun getApprovedReviews(productId: UUID): List<ReviewResponse> =
        productReviewRepository.findByProductId(productId, approvedOnly = true).map { toResponse(it) }

    fun getAllReviews(productId: UUID): List<ReviewResponse> =
        productReviewRepository.findByProductId(productId, approvedOnly = false).map { toResponse(it) }

    fun getPendingReviews(): List<ReviewResponse> =
        productReviewRepository.findPendingApproval().map { toResponse(it) }

    fun getReviewSummary(productId: UUID): ReviewSummaryResponse {
        val allReviews = productReviewRepository.findByProductId(productId, approvedOnly = false)
        val approvedReviews = allReviews.filter { it.isApproved }
        val avgRating = if (approvedReviews.isNotEmpty())
            approvedReviews.sumOf { it.rating }.toDouble() / approvedReviews.size
        else 0.0

        val breakdown = (1..5).associateWith { star ->
            approvedReviews.count { it.rating == star }
        }

        return ReviewSummaryResponse(
            productId = productId.toString(),
            averageRating = Math.round(avgRating * 10.0) / 10.0,
            totalReviews = allReviews.size,
            approvedReviews = approvedReviews.size,
            pendingReviews = allReviews.count { !it.isApproved },
            breakdown = breakdown
        )
    }

    @Transactional
    fun approveReview(productId: UUID, reviewId: UUID): ReviewResponse {
        val review = productReviewRepository.findById(reviewId)
            ?: throw NoSuchElementException("Review not found: $reviewId")
        require(review.productId == productId) { "Review does not belong to this product" }
        val approved = review.copy(isApproved = true)
        return toResponse(productReviewRepository.save(approved))
    }

    @Transactional
    fun deleteReview(productId: UUID, reviewId: UUID) {
        val review = productReviewRepository.findById(reviewId)
            ?: throw NoSuchElementException("Review not found: $reviewId")
        require(review.productId == productId) { "Review does not belong to this product" }
        productReviewRepository.delete(reviewId)
    }

    private fun toResponse(r: ProductReview) = ReviewResponse(
        id = r.id.toString(),
        productId = r.productId.toString(),
        customerId = r.customerId?.toString(),
        reviewerName = r.reviewerName,
        rating = r.rating,
        title = r.title,
        comment = r.comment,
        isVerifiedPurchase = r.isVerifiedPurchase,
        isApproved = r.isApproved,
        createdAt = r.createdAt
    )
}
