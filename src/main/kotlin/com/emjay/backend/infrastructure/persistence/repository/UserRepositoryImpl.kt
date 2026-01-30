package com.emjay.backend.infrastructure.persistence.repository

import com.emjay.backend.domain.entity.user.User
import com.emjay.backend.domain.entity.user.UserRole
import com.emjay.backend.domain.repository.user.UserRepository
import com.emjay.backend.infrastructure.persistence.entity.UserEntity
import org.springframework.stereotype.Component
import java.util.*

@Component
class UserRepositoryImpl(
    private val jpaRepository: JpaUserRepository
) : UserRepository {
    
    override fun save(user: User): User {
        val entity = toEntity(user)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }
    
    override fun findById(id: UUID): User? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }
    
    override fun findByEmail(email: String): User? {
        return jpaRepository.findByEmail(email)?.let { toDomain(it) }
    }
    
    override fun findByUsername(username: String): User? {
        return jpaRepository.findByUsername(username)?.let { toDomain(it) }
    }

    override fun findByEmailOrUsername(identifier: String): User? {
        return jpaRepository.findByEmailOrUsername(identifier)?.let { toDomain(it) }
    }
    override fun existsByEmail(email: String): Boolean {
        return jpaRepository.existsByEmail(email)
    }
    
    override fun existsByUsername(username: String): Boolean {
        return jpaRepository.existsByUsername(username)
    }
    
    override fun findAll(): List<User> {
        return jpaRepository.findAll().map { toDomain(it) }
    }
    
    override fun findAllByRole(role: UserRole): List<User> {
        return jpaRepository.findAllByRole(role).map { toDomain(it) }
    }
    
    override fun findAllActive(): List<User> {
        return jpaRepository.findAllByIsActive(true).map { toDomain(it) }
    }
    
    override fun deleteById(id: UUID) {
        jpaRepository.deleteById(id)
    }
    
    override fun count(): Long {
        return jpaRepository.count()
    }
    
    private fun toDomain(entity: UserEntity): User {
        return User(
            id = entity.id,
            email = entity.email,
            username = entity.username,
            passwordHash = entity.passwordHash,
            firstName = entity.firstName,
            lastName = entity.lastName,
            phone = entity.phone,
            role = entity.role,
            isActive = entity.isActive,
            isVerified = entity.isVerified,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
    
    private fun toEntity(domain: User): UserEntity {
        return UserEntity(
            id = domain.id,
            email = domain.email,
            username = domain.username,
            passwordHash = domain.passwordHash,
            firstName = domain.firstName,
            lastName = domain.lastName,
            phone = domain.phone,
            role = domain.role,
            isActive = domain.isActive,
            isVerified = domain.isVerified,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}
