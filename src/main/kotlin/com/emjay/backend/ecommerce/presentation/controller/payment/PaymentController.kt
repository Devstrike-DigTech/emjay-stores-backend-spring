package com.emjay.backend.ecommerce.presentation.controller.payment

import com.emjay.backend.ecommerce.application.service.payment.PaymentGatewayService
import com.emjay.backend.ecommerce.application.service.payment.PaymentInitiationResponse
import com.emjay.backend.ecommerce.domain.entity.order.PaymentMethod
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Payment gateway integration")
class PaymentController(
    private val paymentGatewayService: PaymentGatewayService
) {

    // ========== Initiate Payment ==========

    @PostMapping("/initiate")
    @Operation(summary = "Initiate payment for order")
    @SecurityRequirement(name = "bearerAuth")
    fun initiatePayment(
        @Valid @RequestBody request: InitiatePaymentRequest
    ): ResponseEntity<PaymentInitiationResponse> {
        val response = paymentGatewayService.initiatePayment(request.orderId, request.paymentMethod)
        return ResponseEntity.ok(response)
    }


    @GetMapping("/paystack/callback")
    @Operation(summary = "Paystack payment callback")
    fun paystackCallback(
        @RequestParam trxref: String,
        @RequestParam reference: String
    ): ResponseEntity<String> {
        // Verify the payment with Paystack
        // For now, redirect to a success page or return success message
        return ResponseEntity.ok("""
        <html>
            <body>
                <h1>Payment Successful!</h1>
                <p>Reference: $reference</p>
                <p>Your order is being processed.</p>
                <script>
                    setTimeout(() => window.close(), 3000);
                </script>
            </body>
        </html>
    """.trimIndent())
    }

    @GetMapping("/flutterwave/callback")
    @Operation(summary = "Flutterwave payment callback")
    fun flutterwaveCallback(
        @RequestParam status: String,
        @RequestParam tx_ref: String,
        @RequestParam transaction_id: String
    ): ResponseEntity<String> {
        return ResponseEntity.ok("""
        <html>
            <body>
                <h1>Payment Successful!</h1>
                <p>Transaction: $transaction_id</p>
                <p>Your order is being processed.</p>
            </body>
        </html>
    """.trimIndent())
    }

    @GetMapping("/stripe/callback")
    @Operation(summary = "Stripe payment callback")
    fun stripeCallback(): ResponseEntity<String> {
        return ResponseEntity.ok("""
        <html>
            <body>
                <h1>Payment Successful!</h1>
                <p>Your order is being processed.</p>
            </body>
        </html>
    """.trimIndent())
    }

    // ========== Paystack Webhook ==========

    @PostMapping("/webhook/paystack")
    @Operation(summary = "Paystack webhook endpoint")
    fun paystackWebhook(@RequestBody event: Map<String, Any>): ResponseEntity<Map<String, String>> {
        val success = paymentGatewayService.handlePaystackWebhook(event)
        return if (success) {
            ResponseEntity.ok(mapOf("status" to "success"))
        } else {
            ResponseEntity.badRequest().body(mapOf("status" to "failed"))
        }
    }

    // ========== Flutterwave Webhook ==========

    @PostMapping("/webhook/flutterwave")
    @Operation(summary = "Flutterwave webhook endpoint")
    fun flutterwaveWebhook(@RequestBody event: Map<String, Any>): ResponseEntity<Map<String, String>> {
        val success = paymentGatewayService.handleFlutterwaveWebhook(event)
        return if (success) {
            ResponseEntity.ok(mapOf("status" to "success"))
        } else {
            ResponseEntity.badRequest().body(mapOf("status" to "failed"))
        }
    }

    // ========== Stripe Webhook ==========

    @PostMapping("/webhook/stripe")
    @Operation(summary = "Stripe webhook endpoint")
    fun stripeWebhook(@RequestBody event: Map<String, Any>): ResponseEntity<Map<String, String>> {
        val success = paymentGatewayService.handleStripeWebhook(event)
        return if (success) {
            ResponseEntity.ok(mapOf("status" to "success"))
        } else {
            ResponseEntity.badRequest().body(mapOf("status" to "failed"))
        }
    }
}

// ========== Request DTOs ==========

data class InitiatePaymentRequest(
    @field:NotNull(message = "Order ID is required")
    val orderId: UUID,

    @field:NotNull(message = "Payment method is required")
    val paymentMethod: PaymentMethod
)