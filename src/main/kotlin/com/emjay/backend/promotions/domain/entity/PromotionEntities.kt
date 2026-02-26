package com.emjay.backend.promotions.domain.entity

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

// ========== ENUMS ==========

enum class BundleStatus {
    ACTIVE,
    INACTIVE,
    SCHEDULED,
    EXPIRED
}

enum class PromotionType {
    PERCENTAGE_DISCOUNT,      // 20% off
    FIXED_AMOUNT_DISCOUNT,    // $10 off
    BUY_X_GET_Y,              // Buy 2 get 1 free
    FREE_SHIPPING,            // Free delivery
    BUNDLE_DISCOUNT           // Special bundle pricing
}

enum class PromotionStatus {
    ACTIVE,
    INACTIVE,
    SCHEDULED,
    EXPIRED
}

enum class PromotionAppliesTo {
    ALL,          // All products/services
    CATEGORIES,   // Specific categories
    PRODUCTS,     // Specific products
    SERVICES      // Specific services
}

// ========== PRODUCT BUNDLE ==========

data class ProductBundle(
    val id: UUID? = null,
    val name: String,
    val slug: String,
    val description: String? = null,
    val shortDescription: String? = null,

    // Pricing
    val originalTotalPrice: BigDecimal,
    val bundlePrice: BigDecimal,
    val savingsAmount: BigDecimal,
    val savingsPercentage: BigDecimal? = null,

    // Bundle details
    val minQuantity: Int = 1,
    val maxQuantity: Int = 100,
    val availableStock: Int? = null,

    // Status
    val status: BundleStatus = BundleStatus.ACTIVE,
    val isFeatured: Boolean = false,

    // Scheduling
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,

    // Images
    val primaryImageUrl: String? = null,

    // SEO
    val metaTitle: String? = null,
    val metaDescription: String? = null,
    val metaKeywords: String? = null,

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val createdBy: UUID? = null
) {
    fun isActive(): Boolean {
        if (status != BundleStatus.ACTIVE) return false

        val now = LocalDateTime.now()
        if (startDate != null && now.isBefore(startDate)) return false
        if (endDate != null && now.isAfter(endDate)) return false

        return true
    }

    fun isInStock(): Boolean = availableStock == null || availableStock > 0

    fun calculateSavings(): BigDecimal = originalTotalPrice - bundlePrice

    fun calculateSavingsPercentage(): BigDecimal {
        if (originalTotalPrice == BigDecimal.ZERO) return BigDecimal.ZERO
        return (savingsAmount / originalTotalPrice * BigDecimal(100))
            .setScale(2, java.math.RoundingMode.HALF_UP)
    }
}

// ========== BUNDLE PRODUCT (Item in bundle) ==========

data class BundleProduct(
    val id: UUID? = null,
    val bundleId: UUID,
    val productId: UUID,
    val quantity: Int = 1,
    val displayOrder: Int = 0,
    val createdAt: LocalDateTime? = null
)

// ========== BUNDLE IMAGE ==========

data class BundleImage(
    val id: UUID? = null,
    val bundleId: UUID,
    val imageUrl: String,
    val altText: String? = null,
    val displayOrder: Int = 0,
    val isPrimary: Boolean = false,
    val createdAt: LocalDateTime? = null
)

// ========== PROMOTION ==========

data class Promotion(
    val id: UUID? = null,
    val name: String,
    val code: String? = null, // NULL = auto-applied
    val description: String? = null,

    // Type and value
    val promotionType: PromotionType,
    val discountValue: BigDecimal? = null,

    // Conditions
    val minPurchaseAmount: BigDecimal? = null,
    val maxDiscountAmount: BigDecimal? = null,
    val usageLimit: Int? = null,
    val usagePerCustomer: Int = 1,

    // Applicability
    val appliesTo: String, // ALL, CATEGORIES, PRODUCTS, SERVICES

    // Status
    val status: PromotionStatus = PromotionStatus.ACTIVE,

    // Scheduling
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,

    // Tracking
    val totalUsageCount: Int = 0,

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val createdBy: UUID? = null
) {
    fun isActive(): Boolean {
        if (status != PromotionStatus.ACTIVE) return false

        val now = LocalDateTime.now()
        if (now.isBefore(startDate) || now.isAfter(endDate)) return false

        return true
    }

    fun canBeUsed(): Boolean {
        if (!isActive()) return false
        if (usageLimit != null && totalUsageCount >= usageLimit) return false
        return true
    }

    fun hasCode(): Boolean = code != null

    fun isAutoApplied(): Boolean = code == null

    fun calculateDiscount(orderAmount: BigDecimal): BigDecimal {
        if (!canBeUsed()) return BigDecimal.ZERO
        if (minPurchaseAmount != null && orderAmount < minPurchaseAmount) return BigDecimal.ZERO

        val discount = when (promotionType) {
            PromotionType.PERCENTAGE_DISCOUNT -> {
                orderAmount * (discountValue ?: BigDecimal.ZERO) / BigDecimal(100)
            }
            PromotionType.FIXED_AMOUNT_DISCOUNT -> {
                discountValue ?: BigDecimal.ZERO
            }
            PromotionType.FREE_SHIPPING -> {
                BigDecimal.ZERO // Handled separately
            }
            PromotionType.BUY_X_GET_Y -> {
                BigDecimal.ZERO // Handled separately
            }
            PromotionType.BUNDLE_DISCOUNT -> {
                discountValue ?: BigDecimal.ZERO
            }
        }

        // Cap at max discount amount
        return if (maxDiscountAmount != null && discount > maxDiscountAmount) {
            maxDiscountAmount
        } else {
            discount
        }
    }
}

// ========== PROMOTION PRODUCT ==========

data class PromotionProduct(
    val id: UUID? = null,
    val promotionId: UUID,
    val productId: UUID,
    val createdAt: LocalDateTime? = null
)

// ========== PROMOTION CATEGORY ==========

data class PromotionCategory(
    val id: UUID? = null,
    val promotionId: UUID,
    val categoryId: UUID,
    val createdAt: LocalDateTime? = null
)

// ========== PROMOTION SERVICE ==========

data class PromotionService(
    val id: UUID? = null,
    val promotionId: UUID,
    val serviceId: UUID,
    val createdAt: LocalDateTime? = null
)

// ========== PROMOTION USAGE ==========

data class PromotionUsage(
    val id: UUID? = null,
    val promotionId: UUID,
    val customerId: UUID,
    val orderId: UUID? = null,
    val discountAmount: BigDecimal,
    val usedAt: LocalDateTime = LocalDateTime.now()
)