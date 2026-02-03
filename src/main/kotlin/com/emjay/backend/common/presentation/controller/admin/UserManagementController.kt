package com.emjay.backend.common.presentation.controller.admin

import com.emjay.backend.common.application.dto.auth.MessageResponse
import com.emjay.backend.common.application.dto.auth.UserResponse
import com.emjay.backend.common.application.dto.user.CreateUserRequest
import com.emjay.backend.common.application.dto.user.UpdateUserRequest
import com.emjay.backend.common.application.dto.user.UpdateUserRoleRequest
import com.emjay.backend.common.application.service.UserManagementService
import com.emjay.backend.common.domain.entity.user.UserRole
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "User Management", description = "Admin endpoints for managing users")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
class UserManagementController(
    private val userManagementService: UserManagementService
) {

    @PostMapping
    @Operation(summary = "Create a new user (Admin only)")
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
        val response = userManagementService.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(summary = "Get all users")
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        val users = userManagementService.getAllUsers()
        return ResponseEntity.ok(users)
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID")
    fun getUserById(@PathVariable userId: UUID): ResponseEntity<UserResponse> {
        val user = userManagementService.getUserById(userId)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Get users by role")
    fun getUsersByRole(@PathVariable role: String): ResponseEntity<List<UserResponse>> {
        val userRole = UserRole.valueOf(role.uppercase())
        val users = userManagementService.getUsersByRole(userRole)
        return ResponseEntity.ok(users)
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user details")
    fun updateUser(
        @PathVariable userId: UUID,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserResponse> {
        val user = userManagementService.updateUser(userId, request)
        return ResponseEntity.ok(user)
    }

    @PatchMapping("/{userId}/role")
    @Operation(summary = "Update user role")
    fun updateUserRole(
        @PathVariable userId: UUID,
        @Valid @RequestBody request: UpdateUserRoleRequest
    ): ResponseEntity<UserResponse> {
        val user = userManagementService.updateUserRole(userId, request)
        return ResponseEntity.ok(user)
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user")
    fun deleteUser(@PathVariable userId: UUID): ResponseEntity<MessageResponse> {
        userManagementService.deleteUser(userId)
        return ResponseEntity.ok(MessageResponse("User deleted successfully"))
    }
}