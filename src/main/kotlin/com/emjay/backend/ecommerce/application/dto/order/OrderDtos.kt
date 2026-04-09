package com.emjay.backend.ecommerce.application.dto.order

import com.emjay.backend.ecommerce.domain.entity.order.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class CheckoutRequest(
    // For registered users with saved addresses
    val shippingAddressId: UUID? = null,

    // For guest users - inline address
    val shippingAddress: InlineAddressRequest? = null,

    val billingAddressId: UUID? = null,

    @field:NotNull(message = "Payment method is required")
    val paymentMethod: PaymentMethod,

    val customerNotes: String? = null
) {
    init {
        // Must provide either saved address ID or inline address
        require(shippingAddressId != null || shippingAddress != null) {
            "Either shippingAddressId or shippingAddress is required"
        }
    }
}

data class InlineAddressRequest(
    @field:NotBlank(message = "Recipient name is required")
    val recipientName: String,

    @field:NotBlank(message = "Phone is required")
    val phone: String,

    @field:NotBlank(message = "Address line 1 is required")
    val addressLine1: String,

    val addressLine2: String? = null,

    @field:NotBlank(message = "City is required")
    val city: String,

    val stateProvince: String? = null,
    val postalCode: String? = null,

    @field:NotBlank(message = "Country is required")
    val country: String
)

data class CheckoutResponse(
    val order: OrderResponse,
    val payment: PaymentResponse?,
    val message: String
)

data class OrderResponse(
    val id: String,
    val orderNumber: String,
    val customerId: String,
    val status: OrderStatus,
    val subtotal: BigDecimal,
    val discountAmount: BigDecimal,
    val taxAmount: BigDecimal,
    val shippingCost: BigDecimal,
    val totalAmount: BigDecimal,
    val itemCount: Int,
    val orderedAt: LocalDateTime,
    val items: List<OrderItemResponse>
)

data class OrderItemResponse(
    val id: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal
)

data class PaymentResponse(
    val id: String,
    val orderId: String,
    val paymentMethod: PaymentMethod,
    val paymentStatus: PaymentStatus,
    val amount: BigDecimal
)

data class UpdateOrderStatusRequest(
    @field:NotNull val status: OrderStatus,
    val reason: String? = null
)

data class ShipmentResponse(
    val id: String,
    val orderId: String,
    val trackingNumber: String?,
    val status: ShipmentStatus,
    val estimatedDeliveryDate: LocalDate?
)