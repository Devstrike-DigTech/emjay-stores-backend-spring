package com.emjay.backend.application.dto.user

import com.emjay.backend.domain.entity.user.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

// Admin creates user with specific role
data class CreateUserRequest(
    @field:Email(message = "Email must be valid")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,

    @field:NotBlank(message = "First name is required")
    val firstName: String,

    @field:NotBlank(message = "Last name is required")
    val lastName: String,

    val phone: String? = null,

    @field:NotNull(message = "Role is required")
    val role: UserRole
)

data class UpdateUserRequest(
    val firstName: String?,
    val lastName: String?,
    val phone: String?,
    val isActive: Boolean?
)

data class UpdateUserRoleRequest(
    @field:NotNull(message = "Role is required")
    val role: UserRole
)