package com.emjay.backend.ecommerce.domain.entity.order

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Order Item domain entity
 * Represents a product in an order with price snapshot
 */
data class OrderItem(
    val id: UUID? = null,
    val orderId: UUID,
    val productId: UUID,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal,
    val productName: String,
    val productSku: String,
    val productImageUrl: String? = null,
    val createdAt: LocalDateTime? = null
) {
    fun calculateSubtotal(): BigDecimal {
        return unitPrice * BigDecimal(quantity)
    }

    fun isValid(): Boolean {
        return quantity > 0 && unitPrice >= BigDecimal.ZERO && subtotal == calculateSubtotal()
    }
}