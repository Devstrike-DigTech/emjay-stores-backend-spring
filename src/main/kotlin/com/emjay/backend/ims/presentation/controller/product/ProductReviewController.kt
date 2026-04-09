package com.emjay.backend.ims.presentation.controller.product

import com.emjay.backend.ims.application.dto.product.CreateReviewRequest
import com.emjay.backend.ims.application.dto.product.ReviewResponse
import com.emjay.backend.ims.application.dto.product.ReviewSummaryResponse
import com.emjay.backend.ims.application.service.ProductReviewService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/products/{productId}/reviews")
@Tag(name = "Product Reviews", description = "Manage product reviews and ratings")
class ProductReviewController(
    private val productReviewService: ProductReviewService
) {

    @GetMapping
    @Operation(summary = "Get approved reviews for a product")
    fun getReviews(@PathVariable productId: UUID): ResponseEntity<List<ReviewResponse>> =
        ResponseEntity.ok(productReviewService.getApprovedReviews(productId))

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all reviews (including pending) — admin only")
    fun getAllReviews(@PathVariable productId: UUID): ResponseEntity<List<ReviewResponse>> =
        ResponseEntity.ok(productReviewService.getAllReviews(productId))

    @GetMapping("/summary")
    @Operation(summary = "Get rating summary and breakdown for a product")
    fun getReviewSummary(@PathVariable productId: UUID): ResponseEntity<ReviewSummaryResponse> =
        ResponseEntity.ok(productReviewService.getReviewSummary(productId))

    @PostMapping
    @Operation(summary = "Submit a review for a product (customer-facing, used by e-commerce side)")
    fun createReview(
        @PathVariable productId: UUID,
        @Valid @RequestBody request: CreateReviewRequest
    ): ResponseEntity<ReviewResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(productReviewService.createReview(productId, request))

    @PatchMapping("/{reviewId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Approve a pending review")
    fun approveReview(
        @PathVariable productId: UUID,
        @PathVariable reviewId: UUID
    ): ResponseEntity<ReviewResponse> =
        ResponseEntity.ok(productReviewService.approveReview(productId, reviewId))

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a review")
    fun deleteReview(
        @PathVariable productId: UUID,
        @PathVariable reviewId: UUID
    ): ResponseEntity<Map<String, String>> {
        productReviewService.deleteReview(productId, reviewId)
        return ResponseEntity.ok(mapOf("message" to "Review deleted successfully"))
    }
}
