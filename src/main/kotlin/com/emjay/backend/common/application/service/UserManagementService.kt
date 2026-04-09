package com.emjay.backend.common.application.service

import com.emjay.backend.common.application.dto.auth.UserResponse
import com.emjay.backend.common.application.dto.user.CreateUserRequest
import com.emjay.backend.common.application.dto.user.UpdateUserRequest
import com.emjay.backend.common.application.dto.user.UpdateUserRoleRequest
import com.emjay.backend.common.domain.entity.user.User
import com.emjay.backend.common.domain.entity.user.UserRole
import com.emjay.backend.domain.exception.ResourceAlreadyExistsException
import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.common.domain.repository.user.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserManagementService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun createUser(request: CreateUserRequest): UserResponse {
        // Check if email already exists
        if (userRepository.existsByEmail(request.email)) {
            throw ResourceAlreadyExistsException("Email already registered")
        }

        // Check if username already exists
        if (userRepository.existsByUsername(request.username)) {
            throw ResourceAlreadyExistsException("Username already taken")
        }

        // Create new user
        val user = User(
            email = request.email,
            username = request.username,
            passwordHash = passwordEncoder.encode(request.password),
            firstName = request.firstName,
            lastName = request.lastName,
            phone = request.phone,
            role = request.role,
            isActive = true,
            isVerified = true // Admin-created users are auto-verified
        )

        val savedUser = userRepository.save(user)
        return toUserResponse(savedUser)
    }

    fun getAllUsers(): List<UserResponse> {
        return userRepository.findAll().map { toUserResponse(it) }
    }

    fun getUserById(userId: UUID): UserResponse {
        val user = userRepository.findById(userId)
            ?: throw ResourceNotFoundException("User not found")
        return toUserResponse(user)
    }

    fun getUsersByRole(role: UserRole): List<UserResponse> {
        return userRepository.findAllByRole(role).map { toUserResponse(it) }
    }

    @Transactional
    fun updateUser(userId: UUID, request: UpdateUserRequest): UserResponse {
        val user = userRepository.findById(userId)
            ?: throw ResourceNotFoundException("User not found")

        val updatedUser = user.copy(
            firstName = request.firstName ?: user.firstName,
            lastName = request.lastName ?: user.lastName,
            phone = request.phone ?: user.phone,
            isActive = request.isActive ?: user.isActive
        )

        val saved = userRepository.save(updatedUser)
        return toUserResponse(saved)
    }

    @Transactional
    fun updateUserRole(userId: UUID, request: UpdateUserRoleRequest): UserResponse {
        val user = userRepository.findById(userId)
            ?: throw ResourceNotFoundException("User not found")

        val updatedUser = user.copy(role = request.role)
        val saved = userRepository.save(updatedUser)
        return toUserResponse(saved)
    }

    @Transactional
    fun deleteUser(userId: UUID) {
        if (!userRepository.findById(userId)?.let { true }!! ?: false) {
            throw ResourceNotFoundException("User not found")
        }
        userRepository.deleteById(userId)
    }

    private fun toUserResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id.toString(),
            email = user.email,
            username = user.username,
            firstName = user.firstName,
            lastName = user.lastName,
            phone = user.phone,
            role = user.role,
            isActive = user.isActive,
            isVerified = user.isVerified
        )
    }
}