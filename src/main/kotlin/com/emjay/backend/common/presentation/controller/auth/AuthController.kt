package com.emjay.backend.common.presentation.controller.auth

import com.emjay.backend.common.application.dto.auth.*
import com.emjay.backend.common.application.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
class AuthController(
    private val authService: AuthService
) {
    
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        val response = authService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    @PostMapping("/login")
    @Operation(summary = "Login with email/username and password")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<AuthResponse> {
        val response = authService.refreshToken(request)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Logout current user")
    fun logout(@AuthenticationPrincipal userId: UUID): ResponseEntity<MessageResponse> {
        authService.logout(userId)
        return ResponseEntity.ok(MessageResponse("Logged out successfully"))
    }
    
    @PostMapping("/change-password")
    @Operation(summary = "Change user password")
    fun changePassword(
        @AuthenticationPrincipal userId: UUID,
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<MessageResponse> {
        authService.changePassword(userId, request)
        return ResponseEntity.ok(MessageResponse("Password changed successfully"))
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    fun getCurrentUser(@AuthenticationPrincipal userId: UUID): ResponseEntity<UserResponse> {
        val response = authService.getCurrentUser(userId)
        return ResponseEntity.ok(response)
    }
}
