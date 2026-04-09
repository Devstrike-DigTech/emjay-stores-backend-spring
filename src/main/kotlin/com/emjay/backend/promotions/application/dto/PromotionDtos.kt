package com.emjay.backend.promotions.application.dto

import com.emjay.backend.promotions.domain.entity.BundleStatus
import com.emjay.backend.promotions.domain.entity.PromotionStatus
import com.emjay.backend.promotions.domain.entity.PromotionType
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

// ========== BUNDLE DTOs ==========

data class CreateBundleRequest(
    @field:NotBlank(message = "Bundle name is required")
    val name: String,

    val description: String? = null,
    val shortDescription: String? = null,

    @field:NotEmpty(message = "Bundle must contain at least one product")
    val products: List<BundleProductRequest>,

    @field:NotNull(message = "Bundle price is required")
    @field:DecimalMin(value = "0.0")
    val bundlePrice: BigDecimal,

    val minQuantity: Int = 1,
    val maxQuantity: Int = 100,
    val availableStock: Int? = null,

    val isFeatured: Boolean = false,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,

    val primaryImageUrl: String? = null,
    val metaTitle: String? = null,
    val metaDescription: String? = null
)

data class BundleProductRequest(
    @field:NotNull(message = "Product ID is required")
    val productId: UUID,

    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int = 1,

    val displayOrder: Int = 0
)

data class UpdateBundleRequest(
    val name: String? = null,
    val description: String? = null,
    val shortDescription: String? = null,
    val bundlePrice: BigDecimal? = null,
    val status: BundleStatus? = null,
    val isFeatured: Boolean? = null,
    val availableStock: Int? = null
)

data class BundleResponse(
    val id: String,
    val name: String,
    val slug: String,
    val description: String?,
    val shortDescription: String?,
    val originalTotalPrice: BigDecimal,
    val bundlePrice: BigDecimal,
    val savingsAmount: BigDecimal,
    val savingsPercentage: BigDecimal,
    val minQuantity: Int,
    val maxQuantity: Int,
    val availableStock: Int?,
    val status: BundleStatus,
    val isFeatured: Boolean,
    val isActive: Boolean,
    val isInStock: Boolean,
    val startDate: LocalDateTime?,
    val endDate: LocalDateTime?,
    val primaryImageUrl: String?,
    val images: List<BundleImageResponse>,
    val products: List<BundleProductResponse>,
    val createdAt: LocalDateTime
)

data class BundleProductResponse(
    val productId: String,
    val productName: String,
    val productImage: String?,
    val productPrice: BigDecimal,
    val quantity: Int,
    val subtotal: BigDecimal,
    val displayOrder: Int
)

data class BundleImageResponse(
    val id: String,
    val imageUrl: String,
    val altText: String?,
    val displayOrder: Int,
    val isPrimary: Boolean
)

data class BundleSummaryResponse(
    val id: String,
    val name: String,
    val slug: String,
    val shortDescription: String?,
    val originalTotalPrice: BigDecimal,
    val bundlePrice: BigDecimal,
    val savingsAmount: BigDecimal,
    val savingsPercentage: BigDecimal,
    val primaryImageUrl: String?,
    val productCount: Int,
    val isFeatured: Boolean,
    val isActive: Boolean
)

data class AddBundleImageRequest(
    @field:NotBlank(message = "Image URL is required")
    val imageUrl: String,

    val altText: String? = null,
    val displayOrder: Int = 0,
    val isPrimary: Boolean = false
)

// ========== PROMOTION DTOs ==========

data class CreatePromotionRequest(
    @field:NotBlank(message = "Promotion name is required")
    val name: String,

    val code: String? = null, // NULL = auto-applied
    val description: String? = null,

    @field:NotNull(message = "Promotion type is required")
    val promotionType: PromotionType,

    val discountValue: BigDecimal? = null,

    val minPurchaseAmount: BigDecimal? = null,
    val maxDiscountAmount: BigDecimal? = null,
    val usageLimit: Int? = null,
    val usagePerCustomer: Int = 1,

    @field:NotBlank(message = "Applies to is required")
    val appliesTo: String, // ALL, CATEGORIES, PRODUCTS, SERVICES

    val productIds: List<UUID> = emptyList(),
    val categoryIds: List<UUID> = emptyList(),
    val serviceIds: List<UUID> = emptyList(),

    @field:NotNull(message = "Start date is required")
    val startDate: LocalDateTime,

    @field:NotNull(message = "End date is required")
    val endDate: LocalDateTime
)

data class UpdatePromotionRequest(
    val name: String? = null,
    val description: String? = null,
    val discountValue: BigDecimal? = null,
    val status: PromotionStatus? = null,
    val usageLimit: Int? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null
)

data class PromotionResponse(
    val id: String,
    val name: String,
    val code: String?,
    val description: String?,
    val promotionType: PromotionType,
    val discountValue: BigDecimal?,
    val minPurchaseAmount: BigDecimal?,
    val maxDiscountAmount: BigDecimal?,
    val usageLimit: Int?,
    val usagePerCustomer: Int,
    val totalUsageCount: Int,
    val remainingUses: Int?,
    val appliesTo: String,
    val status: PromotionStatus,
    val isActive: Boolean,
    val hasCode: Boolean,
    val isAutoApplied: Boolean,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val applicableProducts: List<UUID>,
    val applicableCategories: List<UUID>,
    val applicableServices: List<UUID>,
    val createdAt: LocalDateTime
)

data class PromotionSummaryResponse(
    val id: String,
    val name: String,
    val code: String?,
    val promotionType: PromotionType,
    val discountValue: BigDecimal?,
    val status: PromotionStatus,
    val isActive: Boolean,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime
)

data class ValidatePromoCodeRequest(
    @field:NotBlank(message = "Promo code is required")
    val code: String,

    @field:NotNull(message = "Order amount is required")
    val orderAmount: BigDecimal,

    val productIds: List<UUID> = emptyList()
)

data class ValidatePromoCodeResponse(
    val isValid: Boolean,
    val promotion: PromotionResponse?,
    val discountAmount: BigDecimal,
    val message: String
)

data class ApplyPromotionRequest(
    @field:NotNull(message = "Promotion ID is required")
    val promotionId: UUID,

    @field:NotNull(message = "Order amount is required")
    val orderAmount: BigDecimal
)

data class ApplyPromotionResponse(
    val discountAmount: BigDecimal,
    val finalAmount: BigDecimal,
    val promotion: PromotionSummaryResponse
)

data class CalculateBundlePriceRequest(
    val products: List<BundleProductRequest>,
    val desiredDiscountPercentage: BigDecimal? = null
)

data class CalculateBundlePriceResponse(
    val originalTotalPrice: BigDecimal,
    val suggestedBundlePrice: BigDecimal,
    val minimumBundlePrice: BigDecimal,
    val savingsAmount: BigDecimal,
    val savingsPercentage: BigDecimal,
    val products: List<ProductPriceInfo>
)

data class ProductPriceInfo(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal
)