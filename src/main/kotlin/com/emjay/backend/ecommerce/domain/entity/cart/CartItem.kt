package com.emjay.backend.ecommerce.domain.entity.cart

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Cart Item domain entity
 * Represents a product in the shopping cart
 */
data class CartItem(
    val id: UUID? = null,
    val cartId: UUID,
    val productId: UUID,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal,
    val productName: String? = null,
    val productSku: String? = null,
    val productImageUrl: String? = null,
    val addedAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun calculateSubtotal(): BigDecimal {
        return unitPrice * BigDecimal(quantity)
    }

    fun isValid(): Boolean {
        return quantity > 0 && unitPrice >= BigDecimal.ZERO
    }

    fun hasProductDetails(): Boolean {
        return productName != null && productSku != null
    }

    fun updateQuantity(newQuantity: Int): CartItem {
        require(newQuantity > 0) { "Quantity must be greater than 0" }
        val newSubtotal = unitPrice * BigDecimal(newQuantity)
        return copy(
            quantity = newQuantity,
            subtotal = newSubtotal
        )
    }
}