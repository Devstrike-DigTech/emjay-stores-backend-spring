package com.emjay.backend.common.infrastructure.persistence.repository

import com.emjay.backend.common.domain.entity.user.RefreshToken
import com.emjay.backend.common.domain.repository.user.RefreshTokenRepository
import com.emjay.backend.common.infrastructure.persistence.entity.RefreshTokenEntity
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class RefreshTokenRepositoryImpl(
    private val jpaRepository: JpaRefreshTokenRepository
) : RefreshTokenRepository {
    
    override fun save(refreshToken: RefreshToken): RefreshToken {
        val entity = toEntity(refreshToken)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }
    
    override fun findByToken(token: String): RefreshToken? {
        return jpaRepository.findByToken(token)?.let { toDomain(it) }
    }
    
    override fun findByUserId(userId: UUID): List<RefreshToken> {
        return jpaRepository.findAllByUserId(userId).map { toDomain(it) }
    }
    
    override fun findValidByUserId(userId: UUID): List<RefreshToken> {
        return jpaRepository.findValidByUserId(userId).map { toDomain(it) }
    }
    
    @Transactional
    override fun deleteByToken(token: String) {
        jpaRepository.deleteByToken(token)
    }
    
    @Transactional
    override fun deleteByUserId(userId: UUID) {
        jpaRepository.deleteAllByUserId(userId)
    }
    
    @Transactional
    override fun deleteExpiredTokens() {
        jpaRepository.deleteExpiredTokens()
    }
    
    @Transactional
    override fun revokeAllByUserId(userId: UUID) {
        jpaRepository.revokeAllByUserId(userId)
    }
    
    private fun toDomain(entity: RefreshTokenEntity): RefreshToken {
        return RefreshToken(
            id = entity.id,
            userId = entity.userId,
            token = entity.token,
            expiresAt = entity.expiresAt,
            isRevoked = entity.isRevoked,
            createdAt = entity.createdAt
        )
    }
    
    private fun toEntity(domain: RefreshToken): RefreshTokenEntity {
        return RefreshTokenEntity(
            id = domain.id,
            userId = domain.userId,
            token = domain.token,
            expiresAt = domain.expiresAt,
            isRevoked = domain.isRevoked,
            createdAt = domain.createdAt
        )
    }
}
