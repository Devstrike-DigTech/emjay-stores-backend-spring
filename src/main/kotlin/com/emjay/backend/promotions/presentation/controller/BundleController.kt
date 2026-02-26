package com.emjay.backend.promotions.presentation.controller


import com.emjay.backend.common.infrastructure.security.jwt.JwtTokenProvider
import com.emjay.backend.promotions.application.dto.*
import com.emjay.backend.promotions.application.service.BundleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

// ========== BUNDLE CONTROLLER ==========

@RestController
@RequestMapping("/api/v1/bundles")
@Tag(name = "Product Bundles", description = "Manage product bundles with discounted pricing")
@SecurityRequirement(name = "bearerAuth")
class BundleController(
    private val bundleService: BundleService,
    private val jwtUtil: JwtTokenProvider
) {

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")

    @Operation(summary = "Create product bundle")
    fun createBundle(
        @Valid @RequestBody request: CreateBundleRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<BundleResponse> {
        val userId = extractUserIdFromToken(token)
        val response = bundleService.createBundle(request, userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(summary = "Get all bundles (public catalog)")
    fun getBundles(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<BundleSummaryResponse>> {
        val bundles = bundleService.getBundles(page, size)
        return ResponseEntity.ok(bundles)
    }

    @GetMapping("/active")
    @Operation(summary = "Get active bundles (public)")
    fun getActiveBundles(): ResponseEntity<List<BundleSummaryResponse>> {
        val bundles = bundleService.getActiveBundles()
        return ResponseEntity.ok(bundles)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bundle details (public)")
    fun getBundle(@PathVariable id: UUID): ResponseEntity<BundleResponse> {
        val bundle = bundleService.getBundle(id)
        return ResponseEntity.ok(bundle)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update bundle")
    fun updateBundle(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateBundleRequest
    ): ResponseEntity<BundleResponse> {
        val bundle = bundleService.updateBundle(id, request)
        return ResponseEntity.ok(bundle)
    }

    @PostMapping("/{bundleId}/images")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add bundle image")
    fun addBundleImage(
        @PathVariable bundleId: UUID,
        @Valid @RequestBody request: AddBundleImageRequest
    ): ResponseEntity<BundleImageResponse> {
        val image = bundleService.addBundleImage(bundleId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(image)
    }

    @PostMapping("/calculate-price")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Calculate bundle pricing automatically")
    fun calculateBundlePrice(
        @Valid @RequestBody request: CalculateBundlePriceRequest
    ): ResponseEntity<CalculateBundlePriceResponse> {
        val response = bundleService.calculateBundlePrice(request)
        return ResponseEntity.ok(response)
    }

    private fun extractUserIdFromToken(token: String): UUID {
        val jwt = token.removePrefix("Bearer ")
        val userId = jwtUtil.getUserIdFromToken(jwt)
        return UUID.fromString(userId.toString())
    }
}
