package com.emjay.backend.ecommerce.presentation.controller.cart

import com.emjay.backend.ecommerce.application.dto.cart.*
import com.emjay.backend.ecommerce.application.service.ShoppingCartService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Shopping Cart", description = "Shopping cart management for guest and registered customers")
class ShoppingCartController(
    private val cartService: ShoppingCartService
) {

    // ========== Get or Create Cart ==========

    @GetMapping
    @Operation(summary = "Get current cart (guest or registered)")
    fun getCart(
        @RequestParam(required = false) guestSessionId: String?
    ): ResponseEntity<CartSummaryResponse> {
        val customerId = getCurrentCustomerIdOrNull()
        val response = cartService.getCart(customerId, guestSessionId)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/guest")
    @Operation(summary = "Create guest cart explicitly")
    fun createGuestCart(
        @Valid @RequestBody request: CreateGuestCartRequest
    ): ResponseEntity<CreateGuestCartResponse> {
        val response = cartService.createGuestCartExplicit(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    // ========== Add to Cart ==========

    @PostMapping("/items")
    @Operation(summary = "Add product to cart")
    fun addToCart(
        @Valid @RequestBody request: AddToCartRequest,
        @RequestParam(required = false) guestSessionId: String?
    ): ResponseEntity<AddToCartResponse> {
        val customerId = getCurrentCustomerIdOrNull()
        val response = cartService.addToCart(customerId, guestSessionId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    // ========== Update Cart Item ==========

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity")
    fun updateCartItem(
        @PathVariable itemId: UUID,
        @Valid @RequestBody request: UpdateCartItemRequest
    ): ResponseEntity<CartSummaryResponse> {
        val response = cartService.updateCartItem(itemId, request)
        return ResponseEntity.ok(response)
    }

    // ========== Remove from Cart ==========

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    fun removeCartItem(@PathVariable itemId: UUID): ResponseEntity<CartSummaryResponse> {
        val response = cartService.removeCartItem(itemId)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear entire cart")
    fun clearCart(
        @RequestParam(required = false) guestSessionId: String?
    ): ResponseEntity<CartOperationResponse> {
        val customerId = getCurrentCustomerIdOrNull()
        val response = cartService.clearCart(customerId, guestSessionId)
        return ResponseEntity.ok(response)
    }

    // ========== Cart Validation ==========

    @GetMapping("/{cartId}/validate")
    @Operation(summary = "Validate cart items (stock, prices, availability)")
    fun validateCart(@PathVariable cartId: UUID): ResponseEntity<CartValidationResponse> {
        val response = cartService.validateCart(cartId)
        return ResponseEntity.ok(response)
    }

    // ========== Merge Carts ==========

    @PostMapping("/merge")
    @Operation(summary = "Merge guest cart into customer cart (after login)")
    @SecurityRequirement(name = "bearerAuth")
    fun mergeCarts(
        @Valid @RequestBody request: MergeCartsRequest
    ): ResponseEntity<MergeCartsResponse> {
        val customerId = getCurrentCustomerId()
        val response = cartService.mergeCarts(customerId, request)
        return ResponseEntity.ok(response)
    }

    // ========== Helper Methods ==========

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