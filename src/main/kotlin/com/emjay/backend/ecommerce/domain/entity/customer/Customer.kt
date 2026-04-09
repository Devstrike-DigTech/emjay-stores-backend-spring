package com.emjay.backend.ecommerce.domain.entity.customer

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Customer domain entity
 * Supports both guest checkout and registered users
 */
data class Customer(
    val id: UUID? = null,
    val customerType: CustomerType,
    val email: String? = null,
    val phone: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val passwordHash: String? = null,
    val authProvider: AuthProvider? = null,
    val googleId: String? = null,
    val facebookId: String? = null,
    val appleId: String? = null,
    val dateOfBirth: LocalDate? = null,
    val gender: String? = null,
    val profileImageUrl: String? = null,
    val status: CustomerStatus = CustomerStatus.ACTIVE,
    val emailVerified: Boolean = false,
    val phoneVerified: Boolean = false,
    val newsletterSubscribed: Boolean = false,
    val smsNotifications: Boolean = false,
    val guestSessionId: String? = null,
    val guestSessionExpiresAt: LocalDateTime? = null,
    val lastLoginAt: LocalDateTime? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun fullName(): String {
        return when {
            firstName != null && lastName != null -> "$firstName $lastName"
            firstName != null -> firstName
            lastName != null -> lastName
            else -> email ?: "Guest Customer"
        }
    }

    fun isGuest(): Boolean = customerType == CustomerType.GUEST

    fun isRegistered(): Boolean = customerType == CustomerType.REGISTERED

    fun isActive(): Boolean = status == CustomerStatus.ACTIVE

    fun isSuspended(): Boolean = status == CustomerStatus.SUSPENDED

    fun hasVerifiedEmail(): Boolean = emailVerified

    fun hasVerifiedPhone(): Boolean = phoneVerified

    fun isGoogleAuth(): Boolean = authProvider == AuthProvider.GOOGLE && googleId != null

    fun isLocalAuth(): Boolean = authProvider == AuthProvider.LOCAL

    fun guestSessionValid(): Boolean {
        return if (isGuest() && guestSessionExpiresAt != null) {
            guestSessionExpiresAt.isAfter(LocalDateTime.now())
        } else {
            false
        }
    }

    fun canLogin(): Boolean {
        return isRegistered() && isActive() && (passwordHash != null || googleId != null || facebookId != null || appleId != null)
    }
}

/**
 * Customer type enum
 */
enum class CustomerType {
    GUEST,
    REGISTERED
}

/**
 * Auth provider enum
 */
enum class AuthProvider {
    LOCAL,
    GOOGLE,
    FACEBOOK,
    APPLE
}

/**
 * Customer status enum
 */
enum class CustomerStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    DELETED
}