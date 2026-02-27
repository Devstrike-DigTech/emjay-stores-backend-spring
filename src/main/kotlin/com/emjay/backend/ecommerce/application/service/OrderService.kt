package com.emjay.backend.ecommerce.application.service

import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.ecommerce.application.dto.order.*
import com.emjay.backend.ecommerce.domain.entity.cart.CartStatus
import com.emjay.backend.ecommerce.domain.entity.customer.Customer
import com.emjay.backend.ecommerce.domain.entity.customer.CustomerAddress
import com.emjay.backend.ecommerce.domain.entity.order.*
import com.emjay.backend.ecommerce.domain.repository.cart.CartItemRepository
import com.emjay.backend.ecommerce.domain.repository.cart.ShoppingCartRepository
import com.emjay.backend.ecommerce.domain.repository.customer.CustomerAddressRepository
import com.emjay.backend.ecommerce.domain.repository.customer.CustomerRepository
import com.emjay.backend.ecommerce.domain.repository.order.*
import com.emjay.backend.ims.domain.repository.product.ProductRepository
import com.emjay.backend.notifications.application.dto.QueueNotificationRequest
import com.emjay.backend.notifications.application.service.NotificationService
import com.emjay.backend.notifications.domain.entity.NotificationChannel
import com.emjay.backend.notifications.domain.entity.NotificationType
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val paymentRepository: PaymentRepository,
    private val shoppingCartRepository: ShoppingCartRepository,
    private val cartItemRepository: CartItemRepository,
    private val customerRepository: CustomerRepository,
    private val customerAddressRepository: CustomerAddressRepository,
    private val productRepository: ProductRepository,
    private val orderStatusHistoryRepository: OrderStatusHistoryRepository,
    private val notificationService: NotificationService
) {

    companion object {
        private const val TAX_RATE = 0.075 // 7.5% VAT
    }

    @Transactional
    fun checkout(customerId: UUID?, guestSessionId: String?, request: CheckoutRequest): CheckoutResponse {
        // Get customer (either registered or guest)
        val actualCustomerId = customerId
            ?: if (guestSessionId != null) {
                val guestCustomer = customerRepository.findByGuestSessionId(guestSessionId)
                    ?: throw ResourceNotFoundException("Guest session not found")
                guestCustomer.id!!
            } else {
                throw IllegalArgumentException("Either customerId or guestSessionId required")
            }

        // Get customer cart
        // Get customer cart - search by guestSessionId for guests, customerId for registered
        val cart = if (guestSessionId != null) {
            shoppingCartRepository.findActiveByGuestSessionId(guestSessionId)
        } else {
            shoppingCartRepository.findActiveByCustomerId(actualCustomerId)
        } ?: throw IllegalArgumentException("No active cart found")


        val cartItems = cartItemRepository.findByCartId(cart.id!!)
        if (cartItems.isEmpty()) {
            throw IllegalArgumentException("Cart is empty")
        }

        // Validate stock availability
        cartItems.forEach { item ->
            val product = productRepository.findById(item.productId)
                ?: throw IllegalArgumentException("Product ${item.productId} not found")

            if (product.stockQuantity < item.quantity) {
                throw IllegalArgumentException(
                    "Insufficient stock for ${product.name}. Available: ${product.stockQuantity}"
                )
            }
        }

        // Get or create shipping address
        val shippingAddress = if (request.shippingAddressId != null) {
            customerAddressRepository.findById(request.shippingAddressId)
                ?: throw ResourceNotFoundException("Shipping address not found")
        } else {
            val inline = request.shippingAddress!!
            val snapshot = CustomerAddress(
                customerId = actualCustomerId,
                addressLabel = "Order Address",
                recipientName = inline.recipientName,
                phone = inline.phone,
                addressLine1 = inline.addressLine1,
                addressLine2 = inline.addressLine2,
                city = inline.city,
                stateProvince = inline.stateProvince,
                postalCode = inline.postalCode,
                country = inline.country,
                isDefault = false,
                isShippingAddress = true,
                isBillingAddress = false
            )
            customerAddressRepository.save(snapshot)
        }

        // ✅ Create order FIRST (before order items)
        val orderNumber = orderRepository.generateOrderNumber()
        val order = Order(
            orderNumber = orderNumber,
            customerId = actualCustomerId,
            status = OrderStatus.PENDING_PAYMENT,
            subtotal = cart.subtotal,
            discountAmount = cart.discountAmount,
            taxAmount = cart.taxAmount,
            shippingCost = BigDecimal.ZERO,
            totalAmount = cart.totalAmount,
            couponCode = cart.couponCode,
            shippingAddressId = shippingAddress.id,
            shippingAddressLine1 = shippingAddress.addressLine1,
            shippingAddressLine2 = shippingAddress.addressLine2,
            shippingCity = shippingAddress.city,
            shippingState = shippingAddress.stateProvince,
            shippingPostalCode = shippingAddress.postalCode,
            shippingCountry = shippingAddress.country,
            recipientName = shippingAddress.recipientName,
            recipientPhone = shippingAddress.phone,
            customerNotes = request.customerNotes,
            orderedAt = LocalDateTime.now()
        )
        val savedOrder = orderRepository.save(order) // ✅ savedOrder defined here

        // ✅ NOW create order items (savedOrder.id is available)
        val orderItems = cartItems.map { cartItem ->
            OrderItem(
                orderId = savedOrder.id!!,
                productId = cartItem.productId,
                quantity = cartItem.quantity,
                unitPrice = cartItem.unitPrice,
                subtotal = cartItem.subtotal,
                productName = cartItem.productName ?: "",
                productSku = cartItem.productSku ?: "",
                productImageUrl = cartItem.productImageUrl
            )
        }
        orderItemRepository.saveAll(orderItems)

        // Mark cart as converted
        val convertedCart = cart.copy(status = CartStatus.CONVERTED)
        shoppingCartRepository.save(convertedCart)

        // Create payment record
        val payment = Payment(
            orderId = savedOrder.id!!,
            paymentMethod = request.paymentMethod,
            paymentStatus = PaymentStatus.PENDING,
            amount = savedOrder.totalAmount,
            initiatedAt = LocalDateTime.now()
        )
        val savedPayment = paymentRepository.save(payment)

        // Record status history
        recordStatusChange(savedOrder.id!!, null, OrderStatus.PENDING_PAYMENT, null)

        // Send order confirmation notification
        val customer = customerRepository.findById(actualCustomerId)
            ?: throw ResourceNotFoundException("Customer not found")
        sendOrderConfirmationNotification(savedOrder, customer)


        return CheckoutResponse(
            order = toOrderResponse(savedOrder, orderItems),
            payment = toPaymentResponse(savedPayment),
            message = "Order created successfully"
        )
    }
    fun getOrder(orderId: UUID): OrderResponse {
        val order = orderRepository.findById(orderId)
            ?: throw ResourceNotFoundException("Order not found")
        val items = orderItemRepository.findByOrderId(orderId)
        return toOrderResponse(order, items)
    }

    fun getCustomerOrders(customerId: UUID, page: Int = 0, size: Int = 20): List<OrderResponse> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderedAt"))
        val orders = orderRepository.findByCustomerId(customerId, pageable)
        return orders.content.map {
            val items = orderItemRepository.findByOrderId(it.id!!)
            toOrderResponse(it, items)
        }
    }

    @Transactional
    fun updateOrderStatus(orderId: UUID, request: UpdateOrderStatusRequest, userId: UUID): OrderResponse {
        val order = orderRepository.findById(orderId)
            ?: throw ResourceNotFoundException("Order not found")

        val updated = order.copy(
            status = request.status,
            paidAt = if (request.status == OrderStatus.PAID) LocalDateTime.now() else order.paidAt,
            shippedAt = if (request.status == OrderStatus.SHIPPED) LocalDateTime.now() else order.shippedAt,
            deliveredAt = if (request.status == OrderStatus.DELIVERED) LocalDateTime.now() else order.deliveredAt,
            cancelledAt = if (request.status == OrderStatus.CANCELLED) LocalDateTime.now() else order.cancelledAt
        )

        val saved = orderRepository.save(updated)
        recordStatusChange(orderId, order.status, request.status, userId, request.reason)

        val items = orderItemRepository.findByOrderId(orderId)
        return toOrderResponse(saved, items)
    }

    @Transactional
    fun cancelOrder(orderId: UUID, userId: UUID, reason: String?): OrderResponse {
        val order = orderRepository.findById(orderId)
            ?: throw ResourceNotFoundException("Order not found")

        if (!order.canBeCancelled()) {
            throw IllegalStateException("Order cannot be cancelled in status: ${order.status}")
        }

        val cancelled = order.copy(
            status = OrderStatus.CANCELLED,
            cancelledAt = LocalDateTime.now()
        )

        val saved = orderRepository.save(cancelled)
        recordStatusChange(orderId, order.status, OrderStatus.CANCELLED, userId, reason)

        // Restore stock
        val items = orderItemRepository.findByOrderId(orderId)
        items.forEach { item ->
            val product = productRepository.findById(item.productId)
            product?.let {
                val updated = it.updateStock(item.quantity)
                productRepository.save(updated)
            }
        }

        return toOrderResponse(saved, items)
    }

    private fun recordStatusChange(
        orderId: UUID,
        fromStatus: OrderStatus?,
        toStatus: OrderStatus,
        changedBy: UUID?, // ← already nullable, just pass null for guests/customers
        reason: String? = null
    ) {
        val history = OrderStatusHistory(
            orderId = orderId,
            fromStatus = fromStatus,
            toStatus = toStatus,
            changedBy = changedBy, // null is fine - means system/customer initiated
            reason = reason,
            changedAt = LocalDateTime.now()
        )
        orderStatusHistoryRepository.save(history)
    }

    private fun toOrderResponse(order: Order, items: List<OrderItem>): OrderResponse {
        return OrderResponse(
            id = order.id.toString(),
            orderNumber = order.orderNumber,
            customerId = order.customerId.toString(),
            status = order.status,
            subtotal = order.subtotal,
            discountAmount = order.discountAmount,
            taxAmount = order.taxAmount,
            shippingCost = order.shippingCost,
            totalAmount = order.totalAmount,
            itemCount = items.size,
            orderedAt = order.orderedAt,
            items = items.map { toOrderItemResponse(it) }
        )
    }

    private fun toOrderItemResponse(item: OrderItem): OrderItemResponse {
        return OrderItemResponse(
            id = item.id.toString(),
            productId = item.productId.toString(),
            productName = item.productName,
            quantity = item.quantity,
            unitPrice = item.unitPrice,
            subtotal = item.subtotal
        )
    }

    private fun toPaymentResponse(payment: Payment): PaymentResponse {
        return PaymentResponse(
            id = payment.id.toString(),
            orderId = payment.orderId.toString(),
            paymentMethod = payment.paymentMethod,
            paymentStatus = payment.paymentStatus,
            amount = payment.amount
        )
    }


    // ========== NOTIFICATION HELPER METHODS ==========

    private fun sendOrderConfirmationNotification(order: Order, customer: Customer) {
        try {
            val shippingAddress = "${order.shippingAddressLine1}, ${order.shippingCity}, ${order.shippingState}"

            // Send Email
            notificationService.queueNotification(
                QueueNotificationRequest(
                    recipientId = customer.id,
                    recipientEmail = customer.email,
                    recipientName = "${customer.firstName} ${customer.lastName}",
                    notificationType = com.emjay.backend.notifications.domain.entity.NotificationType.ORDER_CONFIRMATION,
                    channel = com.emjay.backend.notifications.domain.entity.NotificationChannel.EMAIL,
                    subject = "Order Confirmed - ${order.orderNumber}",
                    htmlContent = buildOrderConfirmationEmail(order, customer, shippingAddress),
                    relatedEntityType = "ORDER",
                    relatedEntityId = order.id
                )
            )

            // Send SMS if phone available
            customer.phone?.let { phone ->
                notificationService.queueNotification(
                    QueueNotificationRequest(
                        recipientId = customer.id,
                        recipientPhone = phone,
                        recipientName = "${customer.firstName} ${customer.lastName}",
                        notificationType = NotificationType.ORDER_CONFIRMATION,
                        channel = NotificationChannel.SMS,
                        message = "Hi ${customer.firstName}! Your order #${order.orderNumber} (₦${order.totalAmount}) has been confirmed. Track at emjay.com/orders/${order.orderNumber}",
                        relatedEntityType = "ORDER",
                        relatedEntityId = order.id
                    )
                )
            }
        } catch (e: Exception) {
            // Log but don't fail order creation
            println("Failed to send order confirmation notification: ${e.message}")
        }
    }

    private fun buildOrderConfirmationEmail(
        order: Order,
        customer: Customer,
        shippingAddress: String
    ): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; margin: 0; padding: 0; }
                    .header { background: #4F46E5; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .order-details { background: #F3F4F6; padding: 15px; margin: 20px 0; border-radius: 5px; }
                    .footer { background: #F3F4F6; padding: 20px; text-align: center; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>🎉 Order Confirmed!</h1>
                </div>
                <div class="content">
                    <p>Hi ${customer.firstName},</p>
                    <p>Thank you for your order! Your order <strong>#${order.orderNumber}</strong> has been confirmed.</p>
                    
                    <div class="order-details">
                        <h3>Order Details</h3>
                        <p><strong>Order Number:</strong> ${order.orderNumber}</p>
                        <p><strong>Order Date:</strong> ${order.orderedAt}</p>
                        <p><strong>Total Amount:</strong> ₦${order.totalAmount}</p>
                        <p><strong>Delivery Address:</strong> $shippingAddress</p>
                    </div>
                    
                    <p>We'll send you another email when your order ships.</p>
                    <p>Thank you for shopping with Emjay!</p>
                </div>
                <div class="footer">
                    <p>© 2026 Emjay Beauty. All rights reserved.</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    fun sendPaymentReceivedNotification(order: Order, payment: Payment, customer: Customer) {
        try {
            // Send Email
            notificationService.queueNotification(
                QueueNotificationRequest(
                    recipientId = customer.id,
                    recipientEmail = customer.email,
                    recipientName = "${customer.firstName} ${customer.lastName}",
                    notificationType = NotificationType.PAYMENT_RECEIVED,
                    channel = NotificationChannel.EMAIL,
                    subject = "Payment Received - ₦${payment.amount}",
                    htmlContent = """
                        <h1>Payment Received</h1>
                        <p>Hi ${customer.firstName},</p>
                        <p>We've received your payment of <strong>₦${payment.amount}</strong> for order #${order.orderNumber}.</p>
                        <p><strong>Transaction ID:</strong> ${payment.id ?: "Pending"}</p>
                        <p>Your order is now being processed.</p>
                        <p>Thank you!</p>
                    """,
                    relatedEntityType = "PAYMENT",
                    relatedEntityId = payment.id
                )
            )

            // Send SMS
            customer.phone?.let { phone ->
                notificationService.queueNotification(
                    QueueNotificationRequest(
                        recipientId = customer.id,
                        recipientPhone = phone,
                        notificationType = NotificationType.PAYMENT_RECEIVED,
                        channel = NotificationChannel.SMS,
                        message = "Payment of ₦${payment.amount} received for order #${order.orderNumber}. Thank you! -Emjay",
                        relatedEntityType = "PAYMENT",
                        relatedEntityId = payment.id
                    )
                )
            }
        } catch (e: Exception) {
            println("Failed to send payment notification: ${e.message}")
        }
    }
}