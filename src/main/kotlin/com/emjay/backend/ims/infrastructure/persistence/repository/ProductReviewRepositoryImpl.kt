package com.emjay.backend.ims.infrastructure.persistence.repository

import com.emjay.backend.ims.domain.entity.product.ProductReview
import com.emjay.backend.ims.domain.repository.product.ProductReviewRepository
import com.emjay.backend.ims.infrastructure.persistence.entity.ProductReviewEntity
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ProductReviewRepositoryImpl(
    private val jpaRepository: JpaProductReviewRepository
) : ProductReviewRepository {

    override fun save(review: ProductReview): ProductReview =
        toDomain(jpaRepository.save(toEntity(review)))

    override fun findById(id: UUID): ProductReview? =
        jpaRepository.findById(id).map { toDomain(it) }.orElse(null)

    override fun findByProductId(productId: UUID, approvedOnly: Boolean): List<ProductReview> =
        if (approvedOnly)
            jpaRepository.findByProductIdAndIsApproved(productId, true).map { toDomain(it) }
        else
            jpaRepository.findByProductId(productId).map { toDomain(it) }

    override fun findPendingApproval(): List<ProductReview> =
        jpaRepository.findByIsApprovedFalse().map { toDomain(it) }

    override fun countByProductId(productId: UUID, approvedOnly: Boolean): Long =
        if (approvedOnly)
            jpaRepository.countByProductIdAndIsApproved(productId, true)
        else
            jpaRepository.countByProductIdAndIsApproved(productId, false) +
            jpaRepository.countByProductIdAndIsApproved(productId, true)

    override fun averageRatingByProductId(productId: UUID): Double =
        jpaRepository.averageRatingByProductId(productId)

    override fun ratingBreakdownByProductId(productId: UUID): Map<Int, Int> {
        val reviews = jpaRepository.findByProductIdAndIsApproved(productId, true)
        return (1..5).associateWith { star -> reviews.count { it.rating == star } }
    }

    override fun delete(id: UUID) = jpaRepository.deleteById(id)

    private fun toDomain(e: ProductReviewEntity) = ProductReview(
        id = e.id,
        productId = e.productId,
        customerId = e.customerId,
        reviewerName = e.reviewerName,
        rating = e.rating,
        title = e.title,
        comment = e.comment,
        isVerifiedPurchase = e.isVerifiedPurchase,
        isApproved = e.isApproved,
        createdAt = e.createdAt
    )

    private fun toEntity(d: ProductReview) = ProductReviewEntity(
        id = d.id,
        productId = d.productId,
        customerId = d.customerId,
        reviewerName = d.reviewerName,
        rating = d.rating,
        title = d.title,
        comment = d.comment,
        isVerifiedPurchase = d.isVerifiedPurchase,
        isApproved = d.isApproved,
        createdAt = d.createdAt ?: java.time.LocalDateTime.now()
    )
}
