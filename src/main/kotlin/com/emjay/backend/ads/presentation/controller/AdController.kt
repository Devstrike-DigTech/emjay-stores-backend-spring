package com.emjay.backend.ads.presentation.controller

import com.emjay.backend.ads.application.dto.AdResponse
import com.emjay.backend.ads.application.dto.CreateAdRequest
import com.emjay.backend.ads.application.dto.UpdateAdRequest
import com.emjay.backend.ads.application.service.AdService
import com.emjay.backend.common.infrastructure.security.jwt.JwtTokenProvider
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
@RequestMapping("/api/v1/ads")
@Tag(name = "Ads", description = "Manage marketing advertisements")
class AdController(
    private val adService: AdService,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @GetMapping
    @Operation(summary = "Get all ads")
    fun getAllAds(): ResponseEntity<List<AdResponse>> =
        ResponseEntity.ok(adService.getAllAds())

    @GetMapping("/active")
    @Operation(summary = "Get currently active ads")
    fun getActiveAds(): ResponseEntity<List<AdResponse>> =
        ResponseEntity.ok(adService.getActiveAds())

    @GetMapping("/{id}")
    @Operation(summary = "Get ad by ID")
    fun getAdById(@PathVariable id: UUID): ResponseEntity<AdResponse> =
        ResponseEntity.ok(adService.getAdById(id))

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new ad")
    fun createAd(
        @Valid @RequestBody request: CreateAdRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<AdResponse> {
        val userId = jwtTokenProvider.getUserIdFromToken(token.removePrefix("Bearer "))
        return ResponseEntity.status(HttpStatus.CREATED).body(adService.createAd(request, userId))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update an ad")
    fun updateAd(
        @PathVariable id: UUID,
        @RequestBody request: UpdateAdRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<AdResponse> =
        ResponseEntity.ok(adService.updateAd(id, request))

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete an ad")
    fun deleteAd(@PathVariable id: UUID): ResponseEntity<Map<String, String>> {
        adService.deleteAd(id)
        return ResponseEntity.ok(mapOf("message" to "Ad deleted successfully"))
    }
}
