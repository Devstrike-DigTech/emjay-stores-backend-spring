package com.emjay.backend.ecommerce.presentation.controller.customer

import com.emjay.backend.ecommerce.application.dto.customer.*
import com.emjay.backend.ecommerce.application.service.CustomerService
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
@RequestMapping("/api/v1/customers")
@Tag(name = "Customer Management", description = "Customer registration, authentication, and profile management")
class CustomerController(
    private val customerService: CustomerService
) {

    // ========== Public Endpoints (No Auth Required) ==========

    @PostMapping("/register")
    @Operation(summary = "Register new customer account")
    fun registerCustomer(
        @Valid @RequestBody request: RegisterCustomerRequest
    ): ResponseEntity<CustomerProfileResponse> {
        val response = customerService.registerCustomer(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/login")
    @Operation(summary = "Customer login")
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): ResponseEntity<LoginResponse> {
        val response = customerService.login(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/auth/google")
    @Operation(summary = "Login/Register with Google")
    fun googleAuth(
        @Valid @RequestBody request: GoogleAuthRequest
    ): ResponseEntity<GoogleAuthResponse> {
        val response = customerService.registerWithGoogle(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/guest/session")
    @Operation(summary = "Create guest checkout session")
    fun createGuestSession(
        @Valid @RequestBody request: GuestSessionRequest
    ): ResponseEntity<GuestSessionResponse> {
        val response = customerService.createGuestSession(request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/guest/session/{sessionId}")
    @Operation(summary = "Get guest customer by session ID")
    fun getGuestBySession(
        @PathVariable sessionId: String
    ): ResponseEntity<CustomerProfileResponse> {
        val response = customerService.getGuestBySession(sessionId)
        return ResponseEntity.ok(response)
    }

    // ========== Customer Profile Endpoints ==========

    @GetMapping("/me")
    @Operation(summary = "Get current customer profile")
    @SecurityRequirement(name = "bearerAuth")
    fun getCurrentCustomer(): ResponseEntity<CustomerProfileResponse> {
        val customerId = getCurrentCustomerId()
        val response = customerService.getCustomerProfile(customerId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID (Admin/Manager)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getCustomerById(@PathVariable id: UUID): ResponseEntity<CustomerProfileResponse> {
        val response = customerService.getCustomerProfile(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping
    @Operation(summary = "Get all customers (Admin/Manager)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getAllCustomers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<CustomerListResponse> {
        val response = customerService.getAllCustomers(page, size)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/me")
    @Operation(summary = "Update current customer profile")
    @SecurityRequirement(name = "bearerAuth")
    fun updateCurrentCustomer(
        @Valid @RequestBody request: UpdateCustomerProfileRequest
    ): ResponseEntity<CustomerProfileResponse> {
        val customerId = getCurrentCustomerId()
        val response = customerService.updateCustomerProfile(customerId, request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/me/change-password")
    @Operation(summary = "Change customer password")
    @SecurityRequirement(name = "bearerAuth")
    fun changePassword(
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<Void> {
        val customerId = getCurrentCustomerId()
        customerService.changePassword(customerId, request)
        return ResponseEntity.noContent().build()
    }

    // ========== Customer Address Endpoints ==========

    @PostMapping("/me/addresses")
    @Operation(summary = "Add new address")
    @SecurityRequirement(name = "bearerAuth")
    fun addAddress(
        @Valid @RequestBody request: CreateCustomerAddressRequest
    ): ResponseEntity<CustomerAddressResponse> {
        val customerId = getCurrentCustomerId()
        val response = customerService.addCustomerAddress(customerId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/me/addresses")
    @Operation(summary = "Get customer addresses")
    @SecurityRequirement(name = "bearerAuth")
    fun getMyAddresses(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<CustomerAddressListResponse> {
        val customerId = getCurrentCustomerId()
        val response = customerService.getCustomerAddresses(customerId, page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/me/addresses/default")
    @Operation(summary = "Get default address")
    @SecurityRequirement(name = "bearerAuth")
    fun getDefaultAddress(): ResponseEntity<CustomerAddressResponse?> {
        val customerId = getCurrentCustomerId()
        val response = customerService.getDefaultAddress(customerId)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/addresses/{addressId}")
    @Operation(summary = "Update address")
    @SecurityRequirement(name = "bearerAuth")
    fun updateAddress(
        @PathVariable addressId: UUID,
        @Valid @RequestBody request: UpdateCustomerAddressRequest
    ): ResponseEntity<CustomerAddressResponse> {
        val response = customerService.updateCustomerAddress(addressId, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/addresses/{addressId}")
    @Operation(summary = "Delete address")
    @SecurityRequirement(name = "bearerAuth")
    fun deleteAddress(@PathVariable addressId: UUID): ResponseEntity<Void> {
        customerService.deleteCustomerAddress(addressId)
        return ResponseEntity.noContent().build()
    }

    // ========== Helper Methods ==========

    private fun getCurrentCustomerId(): UUID {
        val authentication = SecurityContextHolder.getContext().authentication
        return UUID.fromString(authentication.name)
    }
}