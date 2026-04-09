package com.emjay.backend.ims.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "product_reviews")
data class ProductReviewEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "product_id", nullable = false)
    val productId: UUID,

    @Column(name = "customer_id")
    val customerId: UUID? = null,

    @Column(name = "reviewer_name", nullable = false, length = 150)
    val reviewerName: String,

    @Column(nullable = false)
    val rating: Int,

    @Column(length = 200)
    val title: String? = null,

    @Column(columnDefinition = "TEXT")
    val comment: String? = null,

    @Column(name = "is_verified_purchase", nullable = false)
    val isVerifiedPurchase: Boolean = false,

    @Column(name = "is_approved", nullable = false)
    val isApproved: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}
