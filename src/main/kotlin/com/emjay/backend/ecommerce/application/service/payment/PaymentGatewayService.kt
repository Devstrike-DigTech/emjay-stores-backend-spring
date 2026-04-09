package com.emjay.backend.ecommerce.application.service.payment

import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.ecommerce.domain.entity.order.*
import com.emjay.backend.ecommerce.domain.repository.order.OrderRepository
import com.emjay.backend.ecommerce.domain.repository.order.OrderStatusHistoryRepository
import com.emjay.backend.ecommerce.domain.repository.order.PaymentRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Payment Gateway Integration Service
 * Supports Paystack, Flutterwave, and Stripe
 */
@Service
class PaymentGatewayService(
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository,
    private val orderStatusHistoryRepository: OrderStatusHistoryRepository,
    private val restTemplate: RestTemplate,
    @Value("\${payment.paystack.secret-key}") private val paystackSecretKey: String,
    @Value("\${payment.flutterwave.secret-key}") private val flutterwaveSecretKey: String,
    @Value("\${payment.stripe.secret-key}") private val stripeSecretKey: String,
    @Value("\${payment.callback-url}") private val callbackUrl: String
) {

    companion object {
        private const val PAYSTACK_BASE_URL = "https://api.paystack.co"
        private const val FLUTTERWAVE_BASE_URL = "https://api.flutterwave.com/v3"
        private const val STRIPE_BASE_URL = "https://api.stripe.com/v1"
    }

    // ========== Initialize Payment ==========

    @Transactional
    fun initiatePayment(orderId: UUID, paymentMethod: PaymentMethod): PaymentInitiationResponse {
        val order = orderRepository.findById(orderId)
            ?: throw ResourceNotFoundException("Order not found")

        if (order.status != OrderStatus.PENDING_PAYMENT) {
            throw IllegalStateException("Order is not pending payment")
        }

        // Create payment record
        val payment = Payment(
            orderId = orderId,
            paymentMethod = paymentMethod,
            paymentStatus = PaymentStatus.PENDING,
            amount = order.totalAmount,
            currency = determineCurrency(paymentMethod),
            initiatedAt = LocalDateTime.now()
        )
        val savedPayment = paymentRepository.save(payment)

        // Route to appropriate gateway
        return when {
            paymentMethod.name.startsWith("PAYSTACK") -> initiatePaystackPayment(order, savedPayment)
            paymentMethod.name.startsWith("FLUTTERWAVE") -> initiateFlutterwavePayment(order, savedPayment)
            paymentMethod.name.startsWith("STRIPE") -> initiateStripePayment(order, savedPayment)
            else -> throw IllegalArgumentException("Unsupported payment method")
        }
    }

    // ========== Paystack Integration ==========

    private fun initiatePaystackPayment(order: Order, payment: Payment): PaymentInitiationResponse {
        val url = "$PAYSTACK_BASE_URL/transaction/initialize"

        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $paystackSecretKey")
            contentType = MediaType.APPLICATION_JSON
        }

        val requestBody = mapOf(
            "email" to getCustomerEmail(order.customerId),
            "amount" to (order.totalAmount * BigDecimal(100)).toLong(), // Convert to kobo
            "reference" to payment.id.toString(),
            "callback_url" to "$callbackUrl/paystack/callback",
            "metadata" to mapOf(
                "order_id" to order.id.toString(),
                "order_number" to order.orderNumber,
                "payment_id" to payment.id.toString()
            ),
            "channels" to getPaystackChannels(payment.paymentMethod)
        )

        val request = HttpEntity(requestBody, headers)

        try {
            val response = restTemplate.postForEntity(url, request, Map::class.java)
            val data = response.body?.get("data") as? Map<*, *>

            if (data != null) {
                val authorizationUrl = data["authorization_url"] as String
                val accessCode = data["access_code"] as String
                val reference = data["reference"] as String

                // Update payment with gateway details
                val updated = payment.copy(
                    gatewayProvider = "PAYSTACK",
                    gatewayReference = reference
                )
                paymentRepository.save(updated)

                return PaymentInitiationResponse(
                    paymentId = payment.id.toString(),
                    provider = "PAYSTACK",
                    authorizationUrl = authorizationUrl,
                    reference = reference,
                    accessCode = accessCode
                )
            } else {
                throw RuntimeException("Invalid Paystack response")
            }
        } catch (e: Exception) {
            handlePaymentFailure(payment, "Paystack initialization failed: ${e.message}")
            throw RuntimeException("Payment initialization failed", e)
        }
    }

    private fun getPaystackChannels(method: PaymentMethod): List<String> {
        return when (method) {
            PaymentMethod.PAYSTACK_CARD -> listOf("card")
            PaymentMethod.PAYSTACK_BANK_TRANSFER -> listOf("bank_transfer")
            PaymentMethod.PAYSTACK_USSD -> listOf("ussd")
            else -> listOf("card", "bank", "ussd")
        }
    }

    // ========== Flutterwave Integration ==========

    private fun initiateFlutterwavePayment(order: Order, payment: Payment): PaymentInitiationResponse {
        val url = "$FLUTTERWAVE_BASE_URL/payments"

        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $flutterwaveSecretKey")
            contentType = MediaType.APPLICATION_JSON
        }

        val requestBody = mapOf(
            "tx_ref" to payment.id.toString(),
            "amount" to order.totalAmount.toString(),
            "currency" to "NGN",
            "redirect_url" to "$callbackUrl/flutterwave/callback",
            "payment_options" to getFlutterwavePaymentOptions(payment.paymentMethod),
            "customer" to mapOf(
                "email" to getCustomerEmail(order.customerId),
                "name" to order.recipientName
            ),
            "customizations" to mapOf(
                "title" to "Emjay Order Payment",
                "description" to "Payment for order ${order.orderNumber}"
            ),
            "meta" to mapOf(
                "order_id" to order.id.toString(),
                "order_number" to order.orderNumber
            )
        )

        val request = HttpEntity(requestBody, headers)

        try {
            val response = restTemplate.postForEntity(url, request, Map::class.java)
            val data = response.body?.get("data") as? Map<*, *>

            if (data != null) {
                val paymentLink = data["link"] as String

                // Update payment
                val updated = payment.copy(
                    gatewayProvider = "FLUTTERWAVE",
                    gatewayReference = payment.id.toString()
                )
                paymentRepository.save(updated)

                return PaymentInitiationResponse(
                    paymentId = payment.id.toString(),
                    provider = "FLUTTERWAVE",
                    authorizationUrl = paymentLink,
                    reference = payment.id.toString()
                )
            } else {
                throw RuntimeException("Invalid Flutterwave response")
            }
        } catch (e: Exception) {
            handlePaymentFailure(payment, "Flutterwave initialization failed: ${e.message}")
            throw RuntimeException("Payment initialization failed", e)
        }
    }

    private fun getFlutterwavePaymentOptions(method: PaymentMethod): String {
        return when (method) {
            PaymentMethod.FLUTTERWAVE_CARD -> "card"
            PaymentMethod.FLUTTERWAVE_BANK_TRANSFER -> "banktransfer"
            PaymentMethod.FLUTTERWAVE_MOBILE_MONEY -> "mobilemoney"
            else -> "card,banktransfer,ussd"
        }
    }

    // ========== Stripe Integration ==========

    private fun initiateStripePayment(order: Order, payment: Payment): PaymentInitiationResponse {
        val url = "$STRIPE_BASE_URL/payment_intents"

        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $stripeSecretKey")
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }

        // Convert NGN to USD for Stripe (approximate rate)
        val amountInCents = (order.totalAmount * BigDecimal("0.0012") * BigDecimal(100)).toLong()

        val requestBody = "amount=$amountInCents" +
                "&currency=usd" +
                "&payment_method_types[]=card" +
                "&metadata[order_id]=${order.id}" +
                "&metadata[order_number]=${order.orderNumber}"

        val request = HttpEntity(requestBody, headers)

        try {
            val response = restTemplate.postForEntity(url, request, Map::class.java)
            val clientSecret = response.body?.get("client_secret") as? String
            val paymentIntentId = response.body?.get("id") as? String

            if (clientSecret != null && paymentIntentId != null) {
                // Update payment
                val updated = payment.copy(
                    gatewayProvider = "STRIPE",
                    gatewayReference = paymentIntentId,
                    currency = "USD"
                )
                paymentRepository.save(updated)

                return PaymentInitiationResponse(
                    paymentId = payment.id.toString(),
                    provider = "STRIPE",
                    authorizationUrl = null, // Stripe uses client-side confirmation
                    reference = paymentIntentId,
                    clientSecret = clientSecret
                )
            } else {
                throw RuntimeException("Invalid Stripe response")
            }
        } catch (e: Exception) {
            handlePaymentFailure(payment, "Stripe initialization failed: ${e.message}")
            throw RuntimeException("Payment initialization failed", e)
        }
    }

    // ========== Webhook Handlers ==========

    @Transactional
    fun handlePaystackWebhook(event: Map<String, Any>): Boolean {
        val eventType = event["event"] as? String

        if (eventType == "charge.success") {
            val data = event["data"] as? Map<*, *> ?: return false
            val reference = data["reference"] as? String ?: return false
            val status = data["status"] as? String

            if (status == "success") {
                val payment = paymentRepository.findByGatewayReference(reference)
                    ?: return false

                completePayment(payment)
                return true
            }
        }

        return false
    }

    @Transactional
    fun handleFlutterwaveWebhook(event: Map<String, Any>): Boolean {
        val eventType = event["event"] as? String

        if (eventType == "charge.completed") {
            val data = event["data"] as? Map<*, *> ?: return false
            val txRef = data["tx_ref"] as? String ?: return false
            val status = data["status"] as? String

            if (status == "successful") {
                val paymentId = UUID.fromString(txRef)
                val payment = paymentRepository.findById(paymentId)
                    ?: return false

                completePayment(payment)
                return true
            }
        }

        return false
    }

    @Transactional
    fun handleStripeWebhook(event: Map<String, Any>): Boolean {
        val eventType = event["type"] as? String

        if (eventType == "payment_intent.succeeded") {
            val data = event["data"] as? Map<*, *> ?: return false
            val paymentIntent = data["object"] as? Map<*, *> ?: return false
            val paymentIntentId = paymentIntent["id"] as? String ?: return false

            val payment = paymentRepository.findByGatewayReference(paymentIntentId)
                ?: return false

            completePayment(payment)
            return true
        }

        return false
    }

    // ========== Helper Methods ==========

    private fun completePayment(payment: Payment) {
        // Update payment status
        val updated = payment.copy(
            paymentStatus = PaymentStatus.CAPTURED,
            capturedAt = LocalDateTime.now()
        )
        paymentRepository.save(updated)

        // Update order status
        val order = orderRepository.findById(payment.orderId)
            ?: throw ResourceNotFoundException("Order not found")

        val paidOrder = order.copy(
            status = OrderStatus.PAID,
            paidAt = LocalDateTime.now()
        )
        orderRepository.save(paidOrder)

        // Record status change
        val history = OrderStatusHistory(
            orderId = order.id!!,
            fromStatus = OrderStatus.PENDING_PAYMENT,
            toStatus = OrderStatus.PAID,
            changedBy = null,
            reason = "Payment captured via ${payment.gatewayProvider}",
            changedAt = LocalDateTime.now()
        )
        orderStatusHistoryRepository.save(history)
    }

    private fun handlePaymentFailure(payment: Payment, reason: String) {
        val failed = payment.copy(
            paymentStatus = PaymentStatus.FAILED,
            failedAt = LocalDateTime.now(),
            failureReason = reason
        )
        paymentRepository.save(failed)
    }

    private fun determineCurrency(method: PaymentMethod): String {
        return when {
            method.name.startsWith("STRIPE") -> "USD"
            else -> "NGN"
        }
    }

    private fun getCustomerEmail(customerId: UUID): String {
        // TODO: Fetch from customer repository
        return "customer@example.com"
    }
}

// ========== DTOs ==========

data class PaymentInitiationResponse(
    val paymentId: String,
    val provider: String,
    val authorizationUrl: String?,
    val reference: String,
    val accessCode: String? = null,
    val clientSecret: String? = null
)