package com.emjay.backend.ims.presentation.controller.product

import com.emjay.backend.ims.application.dto.product.ReviewResponse
import com.emjay.backend.ims.application.service.ProductReviewService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "Review Moderation", description = "Admin review moderation across all products")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@SecurityRequirement(name = "bearerAuth")
class ReviewModerationController(
    private val productReviewService: ProductReviewService
) {
    @GetMapping("/pending")
    @Operation(summary = "Get all pending reviews awaiting approval")
    fun getPendingReviews(): ResponseEntity<List<ReviewResponse>> =
        ResponseEntity.ok(productReviewService.getPendingReviews())
}
