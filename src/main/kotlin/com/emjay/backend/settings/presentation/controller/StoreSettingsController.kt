package com.emjay.backend.settings.presentation.controller

import com.emjay.backend.settings.application.dto.*
import com.emjay.backend.settings.application.service.StoreSettingsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/settings")
@Tag(name = "Store Settings", description = "Manage store profile, contact info and website metadata")
class StoreSettingsController(
    private val storeSettingsService: StoreSettingsService
) {

    // ===== STORE ACCOUNT =====

    @GetMapping("/store")
    @Operation(summary = "Get store account info")
    fun getStoreAccount(): ResponseEntity<StoreAccountResponse> =
        ResponseEntity.ok(storeSettingsService.getStoreAccount())

    @PutMapping("/store")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update store account info")
    fun updateStoreAccount(@RequestBody request: UpdateStoreAccountRequest): ResponseEntity<StoreAccountResponse> =
        ResponseEntity.ok(storeSettingsService.updateStoreAccount(request))

    // ===== CONTACT INFO =====

    @GetMapping("/contact")
    @Operation(summary = "Get store contact info")
    fun getContactInfo(): ResponseEntity<ContactInfoResponse> =
        ResponseEntity.ok(storeSettingsService.getContactInfo())

    @PutMapping("/contact")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update store contact info")
    fun updateContactInfo(@RequestBody request: UpdateContactInfoRequest): ResponseEntity<ContactInfoResponse> =
        ResponseEntity.ok(storeSettingsService.updateContactInfo(request))

    // ===== WEBSITE INFO =====

    @GetMapping("/website")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get website metadata")
    fun getWebsiteInfo(): ResponseEntity<WebsiteInfoResponse> =
        ResponseEntity.ok(storeSettingsService.getWebsiteInfo())

    @PutMapping("/website")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update website metadata")
    fun updateWebsiteInfo(@RequestBody request: UpdateWebsiteInfoRequest): ResponseEntity<WebsiteInfoResponse> =
        ResponseEntity.ok(storeSettingsService.updateWebsiteInfo(request))
}
