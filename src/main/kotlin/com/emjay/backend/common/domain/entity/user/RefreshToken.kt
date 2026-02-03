package com.emjay.backend.common.domain.entity.user

import java.time.LocalDateTime
import java.util.UUID

/**
 * RefreshToken domain entity for JWT refresh token management
 */
data class RefreshToken(
    val id: UUID? = null,
    val userId: UUID,
    val token: String,
    val expiresAt: LocalDateTime,
    val isRevoked: Boolean = false,
    val createdAt: LocalDateTime? = null
) {
    fun isValid(): Boolean = !isRevoked && expiresAt.isAfter(LocalDateTime.now())
    
    fun isExpired(): Boolean = expiresAt.isBefore(LocalDateTime.now())
    
    fun revoke(): RefreshToken = copy(isRevoked = true)
}
