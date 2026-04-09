package com.emjay.backend.ims.infrastructure.persistence.repository

import com.emjay.backend.ims.infrastructure.persistence.entity.ProductReviewEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaProductReviewRepository : JpaRepository<ProductReviewEntity, UUID> {

    fun findByProductIdAndIsApproved(productId: UUID, isApproved: Boolean): List<ProductReviewEntity>

    fun findByProductId(productId: UUID): List<ProductReviewEntity>

    fun findByIsApprovedFalse(): List<ProductReviewEntity>

    fun countByProductIdAndIsApproved(productId: UUID, isApproved: Boolean): Long

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM ProductReviewEntity r WHERE r.productId = :productId AND r.isApproved = true")
    fun averageRatingByProductId(@Param("productId") productId: UUID): Double
}
