package com.emjay.backend.common.infrastructure.persistence.repository

import com.emjay.backend.common.domain.entity.user.UserRole
import com.emjay.backend.common.infrastructure.persistence.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface JpaUserRepository : JpaRepository<UserEntity, UUID> {
    
    fun findByEmail(email: String): UserEntity?
    
    fun findByUsername(username: String): UserEntity?

    @Query("SELECT u FROM UserEntity u WHERE u.email = :identifier OR u.username = :identifier")
    fun findByEmailOrUsername(@Param("identifier") identifier: String): UserEntity?

    fun existsByEmail(email: String): Boolean
    
    fun existsByUsername(username: String): Boolean
    
    fun findAllByRole(role: UserRole): List<UserEntity>
    
    fun findAllByIsActive(isActive: Boolean): List<UserEntity>
}
