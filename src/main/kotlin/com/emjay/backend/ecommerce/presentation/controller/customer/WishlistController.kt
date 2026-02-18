package com.emjay.backend.ecommerce.presentation.controller.customer

import com.emjay.backend.ecommerce.application.dto.customer.*
import com.emjay.backend.ecommerce.application.service.CustomerAnalyticsService
import com.emjay.backend.ecommerce.application.service.WishlistService
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
@RequestMapping("/api/v1/customers/wishlist")
@Tag(name = "Customer Wishlist", description = "Customer wishlist management")
@SecurityRequirement(name = "bearerAuth")
class WishlistController(
    private val wishlistService: WishlistService
) {

    @PostMapping
    @Operation(summary = "Add product to wishlist")
    fun addToWishlist(
        @Valid @RequestBody request: AddToWishlistRequest
    ): ResponseEntity<WishlistItemResponse> {
        val customerId = getCurrentCustomerId()
        val response = wishlistService.addToWishlist(customerId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(summary = "Get customer wishlist")
    fun getWishlist(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<WishlistListResponse> {
        val customerId = getCurrentCustomerId()
        val response = wishlistService.getCustomerWishlist(customerId, page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/count")
    @Operation(summary = "Get wishlist item count")
    fun getWishlistCount(): ResponseEntity<Map<String, Long>> {
        val customerId = getCurrentCustomerId()
        val count = wishlistService.getWishlistItemCount(customerId)
        return ResponseEntity.ok(mapOf("count" to count))
    }

    @PutMapping("/{itemId}")
    @Operation(summary = "Update wishlist item")
    fun updateWishlistItem(
        @PathVariable itemId: UUID,
        @Valid @RequestBody request: UpdateWishlistItemRequest
    ): ResponseEntity<WishlistItemResponse> {
        val response = wishlistService.updateWishlistItem(itemId, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{itemId}")
    @Operation(summary = "Remove item from wishlist")
    fun removeFromWishlist(@PathVariable itemId: UUID): ResponseEntity<Void> {
        wishlistService.removeFromWishlist(itemId)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/product/{productId}")
    @Operation(summary = "Remove product from wishlist")
    fun removeProductFromWishlist(@PathVariable productId: UUID): ResponseEntity<Void> {
        val customerId = getCurrentCustomerId()
        wishlistService.removeProductFromWishlist(customerId, productId)
        return ResponseEntity.noContent().build()
    }

    private fun getCurrentCustomerId(): UUID {
        val authentication = SecurityContextHolder.getContext().authentication
        return UUID.fromString(authentication.name)
    }
}

@RestController
@RequestMapping("/api/v1/customers/analytics")
@Tag(name = "Customer Analytics", description = "Customer spending analytics and budget management")
@SecurityRequirement(name = "bearerAuth")
class CustomerAnalyticsController(
    private val analyticsService: CustomerAnalyticsService
) {

    @GetMapping("/me")
    @Operation(summary = "Get current customer analytics")
    fun getMyAnalytics(): ResponseEntity<CustomerAnalyticsResponse> {
        val customerId = getCurrentCustomerId()
        val response = analyticsService.getCustomerAnalytics(customerId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get customer dashboard")
    fun getDashboard(): ResponseEntity<CustomerDashboardResponse> {
        val customerId = getCurrentCustomerId()
        val response = analyticsService.getDashboard(customerId)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/budget")
    @Operation(summary = "Set monthly budget cap")
    fun setBudgetCap(
        @Valid @RequestBody request: SetBudgetCapRequest
    ): ResponseEntity<CustomerAnalyticsResponse> {
        val customerId = getCurrentCustomerId()
        val response = analyticsService.setBudgetCap(customerId, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/budget")
    @Operation(summary = "Remove budget cap")
    fun removeBudgetCap(): ResponseEntity<CustomerAnalyticsResponse> {
        val customerId = getCurrentCustomerId()
        val response = analyticsService.removeBudgetCap(customerId)
        return ResponseEntity.ok(response)
    }

    private fun getCurrentCustomerId(): UUID {
        val authentication = SecurityContextHolder.getContext().authentication
        return UUID.fromString(authentication.name)
    }
}