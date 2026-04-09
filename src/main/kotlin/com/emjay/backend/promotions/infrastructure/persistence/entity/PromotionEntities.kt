package com.emjay.backend.promotions.infrastructure.persistence.entity

import com.emjay.backend.promotions.domain.entity.BundleStatus
import com.emjay.backend.promotions.domain.entity.PromotionStatus
import com.emjay.backend.promotions.domain.entity.PromotionType
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

// ========== PRODUCT BUNDLE ENTITY ==========

@Entity
@Table(name = "product_bundles")
data class ProductBundleEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(nullable = false, length = 300)
    val name: String,

    @Column(nullable = false, unique = true, length = 300)
    val slug: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "short_description", length = 500)
    val shortDescription: String? = null,

    @Column(name = "original_total_price", nullable = false, precision = 12, scale = 2)
    val originalTotalPrice: BigDecimal,

    @Column(name = "bundle_price", nullable = false, precision = 12, scale = 2)
    val bundlePrice: BigDecimal,

    @Column(name = "savings_amount", nullable = false, precision = 12, scale = 2)
    val savingsAmount: BigDecimal,

    @Column(name = "savings_percentage", precision = 5, scale = 2)
    val savingsPercentage: BigDecimal? = null,

    @Column(name = "min_quantity", nullable = false)
    val minQuantity: Int = 1,

    @Column(name = "max_quantity", nullable = false)
    val maxQuantity: Int = 100,

    @Column(name = "available_stock")
    val availableStock: Int? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "bundle_status")
    val status: BundleStatus = BundleStatus.ACTIVE,

    @Column(name = "is_featured", nullable = false)
    val isFeatured: Boolean = false,

    @Column(name = "start_date")
    val startDate: LocalDateTime? = null,

    @Column(name = "end_date")
    val endDate: LocalDateTime? = null,

    @Column(name = "primary_image_url", length = 500)
    val primaryImageUrl: String? = null,

    @Column(name = "meta_title", length = 200)
    val metaTitle: String? = null,

    @Column(name = "meta_description", length = 500)
    val metaDescription: String? = null,

    @Column(name = "meta_keywords", columnDefinition = "TEXT")
    val metaKeywords: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_by")
    val createdBy: UUID? = null
) {
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

// ========== BUNDLE PRODUCT ENTITY ==========

@Entity
@Table(
    name = "bundle_products",
    uniqueConstraints = [UniqueConstraint(columnNames = ["bundle_id", "product_id"])]
)
data class BundleProductEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "bundle_id", nullable = false)
    val bundleId: UUID,

    @Column(name = "product_id", nullable = false)
    val productId: UUID,

    @Column(nullable = false)
    val quantity: Int = 1,

    @Column(name = "display_order")
    val displayOrder: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}

// ========== BUNDLE IMAGE ENTITY ==========

@Entity
@Table(name = "bundle_images")
data class BundleImageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "bundle_id", nullable = false)
    val bundleId: UUID,

    @Column(name = "image_url", nullable = false, length = 500)
    val imageUrl: String,

    @Column(name = "alt_text", length = 200)
    val altText: String? = null,

    @Column(name = "display_order")
    val displayOrder: Int = 0,

    @Column(name = "is_primary", nullable = false)
    val isPrimary: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}

// ========== PROMOTION ENTITY ==========

@Entity
@Table(name = "promotions")
data class PromotionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(nullable = false, length = 300)
    val name: String,

    @Column(unique = true, length = 50)
    val code: String? = null,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "promotion_type", nullable = false, columnDefinition = "promotion_type")
    val promotionType: PromotionType,

    @Column(name = "discount_value", precision = 12, scale = 2)
    val discountValue: BigDecimal? = null,

    @Column(name = "min_purchase_amount", precision = 12, scale = 2)
    val minPurchaseAmount: BigDecimal? = null,

    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    val maxDiscountAmount: BigDecimal? = null,

    @Column(name = "usage_limit")
    val usageLimit: Int? = null,

    @Column(name = "usage_per_customer", nullable = false)
    val usagePerCustomer: Int = 1,

    @Column(name = "applies_to", nullable = false, length = 50)
    val appliesTo: String,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "promotion_status")
    val status: PromotionStatus = PromotionStatus.ACTIVE,

    @Column(name = "start_date", nullable = false)
    val startDate: LocalDateTime,

    @Column(name = "end_date", nullable = false)
    val endDate: LocalDateTime,

    @Column(name = "total_usage_count", nullable = false)
    val totalUsageCount: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_by")
    val createdBy: UUID? = null
) {
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

// ========== PROMOTION PRODUCT ENTITY ==========

@Entity
@Table(
    name = "promotion_products",
    uniqueConstraints = [UniqueConstraint(columnNames = ["promotion_id", "product_id"])]
)
data class PromotionProductEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "promotion_id", nullable = false)
    val promotionId: UUID,

    @Column(name = "product_id", nullable = false)
    val productId: UUID,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}

// ========== PROMOTION CATEGORY ENTITY ==========

@Entity
@Table(
    name = "promotion_categories",
    uniqueConstraints = [UniqueConstraint(columnNames = ["promotion_id", "category_id"])]
)
data class PromotionCategoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "promotion_id", nullable = false)
    val promotionId: UUID,

    @Column(name = "category_id", nullable = false)
    val categoryId: UUID,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}

// ========== PROMOTION SERVICE ENTITY ==========

@Entity
@Table(
    name = "promotion_services",
    uniqueConstraints = [UniqueConstraint(columnNames = ["promotion_id", "service_id"])]
)
data class PromotionServiceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "promotion_id", nullable = false)
    val promotionId: UUID,

    @Column(name = "service_id", nullable = false)
    val serviceId: UUID,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}

// ========== PROMOTION USAGE ENTITY ==========

@Entity
@Table(name = "promotion_usage")
data class PromotionUsageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "promotion_id", nullable = false)
    val promotionId: UUID,

    @Column(name = "customer_id", nullable = false)
    val customerId: UUID,

    @Column(name = "order_id")
    val orderId: UUID? = null,

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    val discountAmount: BigDecimal,

    @Column(name = "used_at", nullable = false)
    val usedAt: LocalDateTime = LocalDateTime.now()
)