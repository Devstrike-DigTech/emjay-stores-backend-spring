package com.emjay.backend.ecommerce.domain.entity.customer

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Wishlist Item domain entity
 * Customer's saved products with price tracking
 */
data class WishlistItem(
    val id: UUID? = null,
    val customerId: UUID,
    val productId: UUID,
    val priority: Int = 0,
    val notes: String? = null,
    val priceWhenAdded: BigDecimal? = null,
    val notifyOnPriceDrop: Boolean = false,
    val targetPrice: BigDecimal? = null,
    val addedAt: LocalDateTime? = null
) {
    fun hasPriceTarget(): Boolean = targetPrice != null

    fun isPriceDropNotificationEnabled(): Boolean = notifyOnPriceDrop

    fun isPriceAtOrBelowTarget(currentPrice: BigDecimal): Boolean {
        return targetPrice?.let { currentPrice <= it } ?: false
    }

    fun priceDropPercentage(currentPrice: BigDecimal): Double? {
        return priceWhenAdded?.let {
            if (it > BigDecimal.ZERO) {
                ((it - currentPrice).toDouble() / it.toDouble()) * 100
            } else {
                null
            }
        }
    }
}