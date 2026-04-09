package com.emjay.backend.promotions.presentation.controller

import com.emjay.backend.common.infrastructure.security.jwt.JwtTokenProvider
import com.emjay.backend.promotions.application.dto.*
import com.emjay.backend.promotions.application.service.BundleService
import com.emjay.backend.promotions.application.service.PromotionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

// ========== PROMOTION CONTROLLER ==========

@RestController
@RequestMapping("/api/v1/promotions")
@Tag(name = "Promotions", description = "Manage promotional campaigns and discount codes")
class PromotionController(
    private val promotionService: PromotionService,
    private val jwtUtil: JwtTokenProvider
) {

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create promotion")
    fun createPromotion(
        @Valid @RequestBody request: CreatePromotionRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<PromotionResponse> {
        val userId = extractUserIdFromToken(token)
        val response = promotionService.createPromotion(request, userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all promotions (admin)")
    fun getPromotions(): ResponseEntity<List<PromotionSummaryResponse>> {
        val promotions = promotionService.getActivePromotions()
        return ResponseEntity.ok(promotions)
    }

    @GetMapping("/active")
    @Operation(summary = "Get active promotions (public)")
    fun getActivePromotions(): ResponseEntity<List<PromotionSummaryResponse>> {
        val promotions = promotionService.getActivePromotions()
        return ResponseEntity.ok(promotions)
    }

    @PostMapping("/validate-code")
    @Operation(summary = "Validate promo code (public)")
    fun validatePromoCode(
        @Valid @RequestBody request: ValidatePromoCodeRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<ValidatePromoCodeResponse> {
        val customerId = extractUserIdFromToken(token)
        val response = promotionService.validatePromoCode(request, customerId)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{promotionId}/record-usage")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Record promotion usage (internal)")
    fun recordUsage(
        @PathVariable promotionId: UUID,
        @RequestParam orderId: UUID,
        @RequestParam discountAmount: java.math.BigDecimal,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<Map<String, String>> {
        val customerId = extractUserIdFromToken(token)
        promotionService.recordUsage(promotionId, customerId, orderId, discountAmount)
        return ResponseEntity.ok(mapOf("message" to "Usage recorded successfully"))
    }

    private fun extractUserIdFromToken(token: String): UUID {
        val jwt = token.removePrefix("Bearer ")
        val userId = jwtUtil.getUserIdFromToken(jwt)
        return UUID.fromString(userId.toString())
    }
}