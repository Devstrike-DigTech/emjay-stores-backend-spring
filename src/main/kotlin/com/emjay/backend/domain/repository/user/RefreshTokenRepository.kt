package com.emjay.backend.domain.repository.user

import com.emjay.backend.domain.entity.user.RefreshToken
import java.util.UUID

/**
 * RefreshTokenRepository port for refresh token persistence
 */
interface RefreshTokenRepository {
    fun save(refreshToken: RefreshToken): RefreshToken
    
    fun findByToken(token: String): RefreshToken?
    
    fun findByUserId(userId: UUID): List<RefreshToken>
    
    fun findValidByUserId(userId: UUID): List<RefreshToken>
    
    fun deleteByToken(token: String)
    
    fun deleteByUserId(userId: UUID)
    
    fun deleteExpiredTokens()
    
    fun revokeAllByUserId(userId: UUID)
}
