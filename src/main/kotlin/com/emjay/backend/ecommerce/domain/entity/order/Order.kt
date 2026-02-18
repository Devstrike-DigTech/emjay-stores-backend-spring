package com.emjay.backend.ecommerce.domain.entity.order

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Order domain entity
 * Represents a customer order with complete transaction details
 */
data class Order(
    val id: UUID? = null,
    val orderNumber: String,
    val customerId: UUID,
    val status: OrderStatus,
    val subtotal: BigDecimal,
    val discountAmount: BigDecimal = BigDecimal.ZERO,
    val taxAmount: BigDecimal,
    val shippingCost: BigDecimal = BigDecimal.ZERO,
    val totalAmount: BigDecimal,
    val couponCode: String? = null,

    // Shipping address snapshot
    val shippingAddressId: UUID? = null,
    val shippingAddressLine1: String? = null,
    val shippingAddressLine2: String? = null,
    val shippingCity: String? = null,
    val shippingState: String? = null,
    val shippingPostalCode: String? = null,
    val shippingCountry: String? = null,
    val recipientName: String? = null,
    val recipientPhone: String? = null,

    // Billing address snapshot
    val billingAddressId: UUID? = null,
    val billingAddressLine1: String? = null,
    val billingAddressLine2: String? = null,
    val billingCity: String? = null,
    val billingState: String? = null,
    val billingPostalCode: String? = null,
    val billingCountry: String? = null,

    val customerNotes: String? = null,
    val adminNotes: String? = null,

    val orderedAt: LocalDateTime,
    val paidAt: LocalDateTime? = null,
    val shippedAt: LocalDateTime? = null,
    val deliveredAt: LocalDateTime? = null,
    val cancelledAt: LocalDateTime? = null,

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun isPendingPayment(): Boolean = status == OrderStatus.PENDING_PAYMENT

    fun isPaid(): Boolean = status == OrderStatus.PAID

    fun isProcessing(): Boolean = status == OrderStatus.PROCESSING

    fun isShipped(): Boolean = status == OrderStatus.SHIPPED

    fun isDelivered(): Boolean = status == OrderStatus.DELIVERED

    fun isCancelled(): Boolean = status == OrderStatus.CANCELLED

    fun isRefunded(): Boolean = status == OrderStatus.REFUNDED

    fun canBePaid(): Boolean = status == OrderStatus.PENDING_PAYMENT

    fun canBeProcessed(): Boolean = status == OrderStatus.PAID

    fun canBeShipped(): Boolean = status == OrderStatus.PROCESSING

    fun canBeCancelled(): Boolean = status in listOf(
        OrderStatus.PENDING_PAYMENT,
        OrderStatus.PAID,
        OrderStatus.PROCESSING
    )

    fun hasDiscount(): Boolean = discountAmount > BigDecimal.ZERO

    fun hasShippingCost(): Boolean = shippingCost > BigDecimal.ZERO

    fun fullShippingAddress(): String {
        val parts = mutableListOf<String>()
        shippingAddressLine1?.let { parts.add(it) }
        shippingAddressLine2?.let { parts.add(it) }
        shippingCity?.let { parts.add(it) }
        shippingState?.let { parts.add(it) }
        shippingPostalCode?.let { parts.add(it) }
        shippingCountry?.let { parts.add(it) }
        return parts.joinToString(", ")
    }

    fun savingsAmount(): BigDecimal = discountAmount

    fun savingsPercentage(): Double? {
        return if (subtotal > BigDecimal.ZERO && discountAmount > BigDecimal.ZERO) {
            (discountAmount.toDouble() / subtotal.toDouble()) * 100
        } else {
            null
        }
    }
}

/**
 * Order status enum
 */
enum class OrderStatus {
    PENDING_PAYMENT,
    PAYMENT_FAILED,
    PAID,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}