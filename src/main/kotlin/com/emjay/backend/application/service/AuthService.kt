package com.emjay.backend.application.service

import com.emjay.backend.application.dto.auth.*
import com.emjay.backend.domain.entity.user.RefreshToken
import com.emjay.backend.domain.entity.user.User
import com.emjay.backend.domain.entity.user.UserRole
import com.emjay.backend.domain.exception.*
import com.emjay.backend.domain.repository.user.RefreshTokenRepository
import com.emjay.backend.domain.repository.user.UserRepository
import com.emjay.backend.infrastructure.security.jwt.JwtProperties
import com.emjay.backend.infrastructure.security.jwt.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtProperties: JwtProperties
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
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
            role = UserRole.STAFF, // Default role
            isActive = true,
            isVerified = false,
//            createdAt = LocalDateTime.now(),
//            updatedAt = LocalDateTime.now()
        )

        val savedUser = userRepository.save(user)

        // Generate tokens
        return generateAuthResponse(savedUser)
    }

    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        // Find user by email or username
        val user = userRepository.findByEmailOrUsername(request.emailOrUsername)
            ?: throw InvalidCredentialsException()

        // Check if account is active
        if (!user.isActive) {
            throw AccountNotActiveException()
        }

        // Verify password
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }

        // Generate tokens
        return generateAuthResponse(user)
    }

    @Transactional
    fun refreshToken(request: RefreshTokenRequest): AuthResponse {
        val token = request.refreshToken

        // Validate token
        if (!jwtTokenProvider.validateToken(token) || !jwtTokenProvider.isRefreshToken(token)) {
            throw InvalidTokenException()
        }

        // Get user ID from token
        val userId = jwtTokenProvider.getUserIdFromToken(token)

        // Check if refresh token exists and is valid
        val refreshToken = refreshTokenRepository.findByToken(token)
            ?: throw InvalidTokenException("Refresh token not found")

        if (!refreshToken.isValid()) {
            throw InvalidTokenException("Refresh token is expired or revoked")
        }

        // Get user
        val user = userRepository.findById(userId)
            ?: throw ResourceNotFoundException("User not found")

        // Check if account is still active
        if (!user.isActive) {
            throw AccountNotActiveException()
        }

        // Revoke old refresh token
        val revokedToken = refreshToken.revoke()
        refreshTokenRepository.save(revokedToken)

        // Generate new tokens
        return generateAuthResponse(user)
    }

    @Transactional
    fun logout(userId: UUID) {
        refreshTokenRepository.revokeAllByUserId(userId)
    }

    @Transactional
    fun changePassword(userId: UUID, request: ChangePasswordRequest) {
        val user = userRepository.findById(userId)
            ?: throw ResourceNotFoundException("User not found")

        // Verify current password
        if (!passwordEncoder.matches(request.currentPassword, user.passwordHash)) {
            throw InvalidCredentialsException("Current password is incorrect")
        }

        // Update password
        val updatedUser = user.copy(
            passwordHash = passwordEncoder.encode(request.newPassword)
        )

        userRepository.save(updatedUser)

        // Revoke all refresh tokens
        refreshTokenRepository.revokeAllByUserId(userId)
    }

    fun getCurrentUser(userId: UUID): UserResponse {
        val user = userRepository.findById(userId)
            ?: throw ResourceNotFoundException("User not found")

        return toUserResponse(user)
    }

    private fun generateAuthResponse(user: User): AuthResponse {
        val userId = user.id ?: throw IllegalStateException("User ID cannot be null")

        // Generate JWT tokens
        val accessToken = jwtTokenProvider.generateAccessToken(userId, user.email, user.role)
        val refreshToken = jwtTokenProvider.generateRefreshToken(userId)

        // Save refresh token
        val refreshTokenEntity = RefreshToken(
            userId = userId,
            token = refreshToken,
            createdAt = LocalDateTime.now(),
            expiresAt = LocalDateTime.now().plusSeconds(jwtProperties.refreshTokenExpiration / 1000)
        )
        refreshTokenRepository.save(refreshTokenEntity)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = "Bearer",
            expiresIn = jwtProperties.accessTokenExpiration,
            user = toUserResponse(user)
        )
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
