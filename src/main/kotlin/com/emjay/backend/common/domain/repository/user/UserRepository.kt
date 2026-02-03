package com.emjay.backend.common.domain.repository.user

import com.emjay.backend.common.domain.entity.user.User
import com.emjay.backend.common.domain.entity.user.UserRole
import java.util.UUID

/**
 * UserRepository port - defines contract for user persistence
 * Implementation will be in infrastructure layer
 */
interface UserRepository {
    fun save(user: User): User
    
    fun findById(id: UUID): User?
    
    fun findByEmail(email: String): User?
    
    fun findByUsername(username: String): User?
    
//    fun findByEmailOrUsername(email: String, username: String): User?
    fun findByEmailOrUsername(identifier: String): User?
    
    fun existsByEmail(email: String): Boolean
    
    fun existsByUsername(username: String): Boolean
    
    fun findAll(): List<User>
    
    fun findAllByRole(role: UserRole): List<User>
    
    fun findAllActive(): List<User>
    
    fun deleteById(id: UUID)
    
    fun count(): Long
}
