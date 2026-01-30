package com.emjay.backend.domain.entity.user

import java.time.LocalDateTime
import java.util.UUID

/**
 * User domain entity representing a system user
 * This is a pure domain model independent of persistence framework
 */
data class User(
    val id: UUID? = null,
    val email: String,
    val username: String,
    val passwordHash: String,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val role: UserRole,
    val isActive: Boolean = true,
    val isVerified: Boolean = false,
    val createdAt: LocalDateTime? = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = LocalDateTime.now()
) {
    fun fullName(): String = "$firstName $lastName"
    
    fun isAdmin(): Boolean = role == UserRole.ADMIN
    
    fun isManager(): Boolean = role == UserRole.MANAGER
    
    fun isStaff(): Boolean = role == UserRole.STAFF
    
    fun hasRole(requiredRole: UserRole): Boolean = role == requiredRole
    
    fun hasAnyRole(vararg roles: UserRole): Boolean = roles.contains(role)
    
    fun canAccessResource(requiredRole: UserRole): Boolean {
        return when (requiredRole) {
            UserRole.ADMIN -> role == UserRole.ADMIN
            UserRole.MANAGER -> role == UserRole.ADMIN || role == UserRole.MANAGER
            UserRole.STAFF -> true // Everyone can access staff-level resources
        }
    }
}
