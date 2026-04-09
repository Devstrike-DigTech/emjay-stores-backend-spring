package com.emjay.backend.common.infrastructure.persistence.repository

import com.emjay.backend.common.infrastructure.persistence.entity.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface JpaRefreshTokenRepository : JpaRepository<RefreshTokenEntity, UUID> {
    
    fun findByToken(token: String): RefreshTokenEntity?
    
    fun findAllByUserId(userId: UUID): List<RefreshTokenEntity>
    
    @Query("SELECT t FROM RefreshTokenEntity t WHERE t.userId = :userId AND t.isRevoked = false AND t.expiresAt > :now")
    fun findValidByUserId(userId: UUID, now: LocalDateTime = LocalDateTime.now()): List<RefreshTokenEntity>
    
    fun deleteByToken(token: String)
    
    fun deleteAllByUserId(userId: UUID)
    
    @Modifying
    @Query("DELETE FROM RefreshTokenEntity t WHERE t.expiresAt < :now")
    fun deleteExpiredTokens(now: LocalDateTime = LocalDateTime.now())
    
    @Modifying
    @Query("UPDATE RefreshTokenEntity t SET t.isRevoked = true WHERE t.userId = :userId")
    fun revokeAllByUserId(userId: UUID)
}
