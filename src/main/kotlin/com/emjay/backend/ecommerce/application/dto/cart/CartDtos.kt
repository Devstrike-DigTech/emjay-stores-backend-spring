package com.emjay.backend.ecommerce.application.dto.cart

import com.emjay.backend.ecommerce.domain.entity.cart.CartStatus
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

// ========== Add to Cart ==========

data class AddToCartRequest(
    @field:NotNull(message = "Product ID is required")
    val productId: UUID,

    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int = 1
)

data class AddToCartResponse(
    val cartId: String,
    val item: CartItemResponse,
    val cart: CartSummaryResponse
)

// ========== Update Cart Item ==========

data class UpdateCartItemRequest(
    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int
)

// ========== Cart Item Response ==========

data class CartItemResponse(
    val id: String,
    val cartId: String,
    val productId: String,
    val productName: String,
    val productSku: String,
    val productImageUrl: String?,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal,
    val isInStock: Boolean,
    val availableQuantity: Int?,
    val addedAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

// ========== Cart Summary ==========

data class CartSummaryResponse(
    val cartId: String,
    val itemCount: Int,
    val totalQuantity: Int,
    val subtotal: BigDecimal,
    val discountAmount: BigDecimal,
    val taxAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val couponCode: String?,
    val discountPercentage: Double?,
    val isGuest: Boolean,
    val expiresAt: LocalDateTime?,
    val items: List<CartItemResponse>
)

// ========== Full Cart Response ==========

data class ShoppingCartResponse(
    val id: String,
    val customerId: String?,
    val guestSessionId: String?,
    val status: CartStatus,
    val subtotal: BigDecimal,
    val discountAmount: BigDecimal,
    val taxAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val couponCode: String?,
    val itemCount: Int,
    val totalQuantity: Int,
    val isGuest: Boolean,
    val isActive: Boolean,
    val isExpired: Boolean,
    val expiresAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

// ========== Apply Coupon ==========

data class ApplyCouponRequest(
    @field:NotNull(message = "Coupon code is required")
    val couponCode: String
)

data class ApplyCouponResponse(
    val success: Boolean,
    val couponCode: String,
    val discountAmount: BigDecimal,
    val discountPercentage: Double?,
    val message: String,
    val cart: CartSummaryResponse
)

// ========== Merge Carts ==========

data class MergeCartsRequest(
    @field:NotNull(message = "Guest session ID is required")
    val guestSessionId: String
)

data class MergeCartsResponse(
    val success: Boolean,
    val message: String,
    val mergedItemCount: Int,
    val cart: CartSummaryResponse
)

// ========== Cart Validation ==========

data class CartValidationResponse(
    val isValid: Boolean,
    val issues: List<CartValidationIssue>,
    val cart: CartSummaryResponse?
)

data class CartValidationIssue(
    val itemId: String,
    val productId: String,
    val productName: String,
    val issueType: ValidationIssueType,
    val message: String,
    val requestedQuantity: Int?,
    val availableQuantity: Int?
)

enum class ValidationIssueType {
    OUT_OF_STOCK,
    INSUFFICIENT_STOCK,
    PRICE_CHANGED,
    PRODUCT_DISCONTINUED,
    PRODUCT_DELETED
}

// ========== Cart Operations Response ==========

data class CartOperationResponse(
    val success: Boolean,
    val message: String,
    val cart: CartSummaryResponse?
)

// ========== Guest Cart Creation ==========

data class CreateGuestCartRequest(
    val guestSessionId: String
)

data class CreateGuestCartResponse(
    val cartId: String,
    val guestSessionId: String,
    val expiresAt: LocalDateTime,
    val message: String
)

// ========== Cart Statistics (Admin) ==========

data class CartStatisticsResponse(
    val totalActiveCarts: Long,
    val totalGuestCarts: Long,
    val totalRegisteredCarts: Long,
    val totalAbandonedCarts: Long,
    val averageCartValue: BigDecimal,
    val totalCartValue: BigDecimal,
    val averageItemsPerCart: Double
)