package com.emjay.backend.ims.presentation.controller.product

import com.emjay.backend.ims.application.dto.product.ProductTargetResponse
import com.emjay.backend.ims.application.dto.product.SetProductTargetRequest
import com.emjay.backend.ims.application.service.ProductTargetService
import com.emjay.backend.common.infrastructure.security.jwt.JwtTokenProvider
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/products/{productId}/target")
@Tag(name = "Product Targets", description = "Manage monthly sales targets per product")
@SecurityRequirement(name = "bearerAuth")
class ProductTargetController(
    private val productTargetService: ProductTargetService,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Set or update a monthly sales target for a product")
    fun setTarget(
        @PathVariable productId: UUID,
        @Valid @RequestBody request: SetProductTargetRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<ProductTargetResponse> {
        val userId = jwtTokenProvider.getUserIdFromToken(token.removePrefix("Bearer "))
        val response = productTargetService.setTarget(productId, request, userId)
        return ResponseEntity.ok(response)
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get the current month's target and progress for a product")
    fun getTarget(
        @PathVariable productId: UUID,
        @RequestParam(required = false) year: Int?,
        @RequestParam(required = false) month: Int?
    ): ResponseEntity<ProductTargetResponse?> {
        val response = productTargetService.getTarget(productId, year, month)
        return ResponseEntity.ok(response)
    }
}
