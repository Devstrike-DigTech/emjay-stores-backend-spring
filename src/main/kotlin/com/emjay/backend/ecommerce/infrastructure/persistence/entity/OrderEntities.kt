package com.emjay.backend.ecommerce.infrastructure.persistence.entity

import com.emjay.backend.ecommerce.domain.entity.order.*
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "orders")
data class OrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    val orderNumber: String,

    @Column(name = "customer_id", nullable = false)
    val customerId: UUID,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "order_status")
    val status: OrderStatus,

    @Column(nullable = false, precision = 12, scale = 2)
    val subtotal: BigDecimal,

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    val discountAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    val taxAmount: BigDecimal,

    @Column(name = "shipping_cost", nullable = false, precision = 12, scale = 2)
    val shippingCost: BigDecimal = BigDecimal.ZERO,

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    val totalAmount: BigDecimal,

    @Column(name = "coupon_code", length = 50)
    val couponCode: String? = null,

    // Shipping address
    @Column(name = "shipping_address_id")
    val shippingAddressId: UUID? = null,

    @Column(name = "shipping_address_line1")
    val shippingAddressLine1: String? = null,

    @Column(name = "shipping_address_line2")
    val shippingAddressLine2: String? = null,

    @Column(name = "shipping_city", length = 100)
    val shippingCity: String? = null,

    @Column(name = "shipping_state", length = 100)
    val shippingState: String? = null,

    @Column(name = "shipping_postal_code", length = 20)
    val shippingPostalCode: String? = null,

    @Column(name = "shipping_country", length = 100)
    val shippingCountry: String? = null,

    @Column(name = "recipient_name", length = 200)
    val recipientName: String? = null,

    @Column(name = "recipient_phone", length = 20)
    val recipientPhone: String? = null,

    // Billing address
    @Column(name = "billing_address_id")
    val billingAddressId: UUID? = null,

    @Column(name = "billing_address_line1")
    val billingAddressLine1: String? = null,

    @Column(name = "billing_address_line2")
    val billingAddressLine2: String? = null,

    @Column(name = "billing_city", length = 100)
    val billingCity: String? = null,

    @Column(name = "billing_state", length = 100)
    val billingState: String? = null,

    @Column(name = "billing_postal_code", length = 20)
    val billingPostalCode: String? = null,

    @Column(name = "billing_country", length = 100)
    val billingCountry: String? = null,

    @Column(name = "customer_notes", columnDefinition = "TEXT")
    val customerNotes: String? = null,

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    val adminNotes: String? = null,

    @Column(name = "ordered_at", nullable = false)
    val orderedAt: LocalDateTime,

    @Column(name = "paid_at")
    val paidAt: LocalDateTime? = null,

    @Column(name = "shipped_at")
    val shippedAt: LocalDateTime? = null,

    @Column(name = "delivered_at")
    val deliveredAt: LocalDateTime? = null,

    @Column(name = "cancelled_at")
    val cancelledAt: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
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

@Entity
@Table(name = "order_items")
data class OrderItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "order_id", nullable = false)
    val orderId: UUID,

    @Column(name = "product_id", nullable = false)
    val productId: UUID,

    @Column(nullable = false)
    val quantity: Int,

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    val unitPrice: BigDecimal,

    @Column(nullable = false, precision = 12, scale = 2)
    val subtotal: BigDecimal,

    @Column(name = "product_name", nullable = false)
    val productName: String,

    @Column(name = "product_sku", nullable = false, length = 100)
    val productSku: String,

    @Column(name = "product_image_url", length = 500)
    val productImageUrl: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}

@Entity
@Table(name = "payments")
data class PaymentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "order_id", nullable = false)
    val orderId: UUID,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "payment_method", nullable = false, columnDefinition = "payment_method")
    val paymentMethod: PaymentMethod,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "payment_status", nullable = false, columnDefinition = "payment_status")
    val paymentStatus: PaymentStatus,

    @Column(nullable = false, precision = 12, scale = 2)
    val amount: BigDecimal,

    @Column(nullable = false, length = 3)
    val currency: String = "NGN",

    @Column(name = "gateway_provider", length = 50)
    val gatewayProvider: String? = null,

    @Column(name = "gateway_transaction_id")
    val gatewayTransactionId: String? = null,

    @Column(name = "gateway_reference")
    val gatewayReference: String? = null,

    @Column(columnDefinition = "TEXT")
    val metadata: String? = null,

    @Column(name = "initiated_at", nullable = false)
    val initiatedAt: LocalDateTime,

    @Column(name = "authorized_at")
    val authorizedAt: LocalDateTime? = null,

    @Column(name = "captured_at")
    val capturedAt: LocalDateTime? = null,

    @Column(name = "failed_at")
    val failedAt: LocalDateTime? = null,

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    val failureReason: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
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

@Entity
@Table(name = "shipments")
data class ShipmentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "order_id", nullable = false)
    val orderId: UUID,

    @Column(name = "tracking_number", length = 100)
    val trackingNumber: String? = null,

    @Column(length = 100)
    val carrier: String? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "shipment_status")
    val status: ShipmentStatus,

    @Column(name = "shipping_method", length = 100)
    val shippingMethod: String? = null,

    @Column(name = "estimated_delivery_date")
    val estimatedDeliveryDate: LocalDate? = null,

    @Column(name = "actual_delivery_date")
    val actualDeliveryDate: LocalDate? = null,

    @Column(name = "current_location")
    val currentLocation: String? = null,

    @Column(columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(name = "shipped_at")
    val shippedAt: LocalDateTime? = null,

    @Column(name = "delivered_at")
    val deliveredAt: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
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

@Entity
@Table(name = "shipment_tracking_events")
data class ShipmentTrackingEventEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "shipment_id", nullable = false)
    val shipmentId: UUID,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "shipment_status")
    val status: ShipmentStatus,

    @Column
    val location: String? = null,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "event_timestamp", nullable = false)
    val eventTimestamp: LocalDateTime,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}

@Entity
@Table(name = "order_status_history")
data class OrderStatusHistoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "order_id", nullable = false)
    val orderId: UUID,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "from_status", columnDefinition = "order_status")
    val fromStatus: OrderStatus? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "to_status", nullable = false, columnDefinition = "order_status")
    val toStatus: OrderStatus,

    @Column(name = "changed_by")
    val changedBy: UUID? = null,

    @Column(columnDefinition = "TEXT")
    val reason: String? = null,

    @Column(name = "changed_at", nullable = false)
    val changedAt: LocalDateTime
)