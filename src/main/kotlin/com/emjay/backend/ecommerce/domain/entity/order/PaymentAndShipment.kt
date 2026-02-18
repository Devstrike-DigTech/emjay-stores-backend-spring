package com.emjay.backend.ecommerce.domain.entity.order

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Payment domain entity
 * Represents payment transaction for an order
 */
data class Payment(
    val id: UUID? = null,
    val orderId: UUID,
    val paymentMethod: PaymentMethod,
    val paymentStatus: PaymentStatus,
    val amount: BigDecimal,
    val currency: String = "NGN",
    val gatewayProvider: String? = null,
    val gatewayTransactionId: String? = null,
    val gatewayReference: String? = null,
    val metadata: String? = null,
    val initiatedAt: LocalDateTime,
    val authorizedAt: LocalDateTime? = null,
    val capturedAt: LocalDateTime? = null,
    val failedAt: LocalDateTime? = null,
    val failureReason: String? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun isPending(): Boolean = paymentStatus == PaymentStatus.PENDING

    fun isAuthorized(): Boolean = paymentStatus == PaymentStatus.AUTHORIZED

    fun isCaptured(): Boolean = paymentStatus == PaymentStatus.CAPTURED

    fun isFailed(): Boolean = paymentStatus == PaymentStatus.FAILED

    fun isRefunded(): Boolean = paymentStatus == PaymentStatus.REFUNDED

    fun isSuccessful(): Boolean = paymentStatus in listOf(PaymentStatus.AUTHORIZED, PaymentStatus.CAPTURED)

    fun requiresGateway(): Boolean = paymentMethod !in listOf(PaymentMethod.CASH_ON_DELIVERY, PaymentMethod.BANK_TRANSFER)
}

/**
 * Payment status enum
 */
enum class PaymentStatus {
    PENDING,
    AUTHORIZED,
    CAPTURED,
    FAILED,
    REFUNDED,
    PARTIALLY_REFUNDED
}

/**
 * Payment method enum
 */
enum class PaymentMethod {
    PAYSTACK_CARD,
    PAYSTACK_BANK_TRANSFER,
    PAYSTACK_USSD,
    FLUTTERWAVE_CARD,
    FLUTTERWAVE_BANK_TRANSFER,
    FLUTTERWAVE_MOBILE_MONEY,
    STRIPE_CARD,
    STRIPE_APPLE_PAY,
    STRIPE_GOOGLE_PAY,
    CASH_ON_DELIVERY,
    BANK_TRANSFER
}

/**
 * Shipment domain entity
 * Represents shipment and delivery tracking
 */
data class Shipment(
    val id: UUID? = null,
    val orderId: UUID,
    val trackingNumber: String? = null,
    val carrier: String? = null,
    val status: ShipmentStatus,
    val shippingMethod: String? = null,
    val estimatedDeliveryDate: LocalDate? = null,
    val actualDeliveryDate: LocalDate? = null,
    val currentLocation: String? = null,
    val notes: String? = null,
    val shippedAt: LocalDateTime? = null,
    val deliveredAt: LocalDateTime? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun isPending(): Boolean = status == ShipmentStatus.PENDING

    fun isProcessing(): Boolean = status == ShipmentStatus.PROCESSING

    fun isShipped(): Boolean = status == ShipmentStatus.SHIPPED

    fun isInTransit(): Boolean = status == ShipmentStatus.IN_TRANSIT

    fun isOutForDelivery(): Boolean = status == ShipmentStatus.OUT_FOR_DELIVERY

    fun isDelivered(): Boolean = status == ShipmentStatus.DELIVERED

    fun hasTrackingNumber(): Boolean = trackingNumber != null

    fun isDelayed(): Boolean {
        return estimatedDeliveryDate?.let {
            LocalDate.now().isAfter(it) && !isDelivered()
        } ?: false
    }
}

/**
 * Shipment status enum
 */
enum class ShipmentStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    FAILED_DELIVERY,
    RETURNED
}

/**
 * Shipment Tracking Event domain entity
 */
data class ShipmentTrackingEvent(
    val id: UUID? = null,
    val shipmentId: UUID,
    val status: ShipmentStatus,
    val location: String? = null,
    val description: String? = null,
    val eventTimestamp: LocalDateTime,
    val createdAt: LocalDateTime? = null
)

/**
 * Order Status History domain entity
 */
data class OrderStatusHistory(
    val id: UUID? = null,
    val orderId: UUID,
    val fromStatus: OrderStatus? = null,
    val toStatus: OrderStatus,
    val changedBy: UUID? = null,
    val reason: String? = null,
    val changedAt: LocalDateTime
)