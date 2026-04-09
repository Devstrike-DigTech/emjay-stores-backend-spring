package com.emjay.backend.ecommerce.presentation.controller.order

import com.emjay.backend.ecommerce.application.dto.order.*
import com.emjay.backend.ecommerce.application.service.OrderService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders & Checkout", description = "Order management and checkout")
@SecurityRequirement(name = "bearerAuth")
class OrderController(
    private val orderService: OrderService
) {

    @PostMapping("/checkout")
    @Operation(summary = "Checkout and create order from cart")
    fun checkout(
        @Valid @RequestBody request: CheckoutRequest,
        @RequestParam(required = false) guestSessionId: String?
    ): ResponseEntity<CheckoutResponse> {
        val customerId = getCurrentCustomerIdOrNull() // Returns null for guests
        val response = orderService.checkout(customerId, guestSessionId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    fun getOrder(@PathVariable id: UUID): ResponseEntity<OrderResponse> {
        val response = orderService.getOrder(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Get current customer orders")
    fun getMyOrders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<OrderResponse>> {
        val customerId = getCurrentCustomerId()
        val response = orderService.getCustomerOrders(customerId, page, size)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun updateOrderStatus(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateOrderStatusRequest
    ): ResponseEntity<OrderResponse> {
        val userId = getCurrentCustomerId()
        val response = orderService.updateOrderStatus(id, request, userId)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel order")
    fun cancelOrder(
        @PathVariable id: UUID,
        @RequestParam(required = false) reason: String?
    ): ResponseEntity<OrderResponse> {
        val userId = getCurrentCustomerId()
        val response = orderService.cancelOrder(id, userId, reason)
        return ResponseEntity.ok(response)
    }

    private fun getCurrentCustomerId(): UUID {
        val authentication = SecurityContextHolder.getContext().authentication
        return UUID.fromString(authentication.name)
    }


    private fun getCurrentCustomerIdOrNull(): UUID? {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication != null && authentication.isAuthenticated && authentication.name != "anonymousUser") {
                UUID.fromString(authentication.name)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}