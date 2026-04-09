package com.emjay.backend.ecommerce.domain.entity.cart

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Shopping Cart domain entity
 * Supports both guest and registered customers
 */
data class ShoppingCart(
    val id: UUID? = null,
    val customerId: UUID? = null,
    val guestSessionId: String? = null,
    val status: CartStatus = CartStatus.ACTIVE,
    val subtotal: BigDecimal = BigDecimal.ZERO,
    val discountAmount: BigDecimal = BigDecimal.ZERO,
    val taxAmount: BigDecimal = BigDecimal.ZERO,
    val totalAmount: BigDecimal = BigDecimal.ZERO,
    val couponCode: String? = null,
    val expiresAt: LocalDateTime? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun isGuestCart(): Boolean = guestSessionId != null

    fun isRegisteredCart(): Boolean = customerId != null

    fun isActive(): Boolean = status == CartStatus.ACTIVE

    fun isAbandoned(): Boolean = status == CartStatus.ABANDONED

    fun isExpired(): Boolean {
        return expiresAt?.let { it.isBefore(LocalDateTime.now()) } ?: false
    }

    fun isValid(): Boolean {
        return isActive() && !isExpired()
    }

    fun hasCoupon(): Boolean = couponCode != null

    fun hasDiscount(): Boolean = discountAmount > BigDecimal.ZERO

    fun isEmpty(): Boolean = subtotal == BigDecimal.ZERO

    fun calculateFinalAmount(): BigDecimal {
        return subtotal - discountAmount + taxAmount
    }

    fun discountPercentage(): Double? {
        return if (subtotal > BigDecimal.ZERO && discountAmount > BigDecimal.ZERO) {
            (discountAmount.toDouble() / subtotal.toDouble()) * 100
        } else {
            null
        }
    }
}

/**
 * Cart status enum
 */
enum class CartStatus {
    ACTIVE,
    ABANDONED,
    CONVERTED,
    MERGED
}