package com.emjay.backend.ecommerce.application.dto.customer

import com.emjay.backend.ecommerce.domain.entity.customer.AuthProvider
import com.emjay.backend.ecommerce.domain.entity.customer.CustomerStatus
import com.emjay.backend.ecommerce.domain.entity.customer.CustomerType
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

// ========== Customer Registration & Authentication ==========

data class RegisterCustomerRequest(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,

    @field:NotBlank(message = "First name is required")
    val firstName: String,

    @field:NotBlank(message = "Last name is required")
    val lastName: String,

    val phone: String? = null,
    val newsletterSubscribed: Boolean = false
)

data class GoogleAuthRequest(
    @field:NotBlank(message = "Google ID token is required")
    val idToken: String
)

data class GoogleAuthResponse(
    val customerId: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val profileImageUrl: String?,
    val isNewCustomer: Boolean,
    val accessToken: String
)

data class LoginRequest(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)

data class LoginResponse(
    val customerId: String,
    val email: String,
    val fullName: String,
    val customerType: CustomerType,
    val accessToken: String,
    val refreshToken: String
)

data class GuestSessionRequest(
    val email: String? = null,
    val phone: String? = null
)

data class GuestSessionResponse(
    val sessionId: String,
    val customerId: String,
    val expiresAt: LocalDateTime
)

// ========== Customer Profile ==========

data class UpdateCustomerProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val dateOfBirth: LocalDate? = null,
    val gender: String? = null,
    val newsletterSubscribed: Boolean? = null,
    val smsNotifications: Boolean? = null
)

data class CustomerProfileResponse(
    val id: String,
    val customerType: CustomerType,
    val email: String?,
    val phone: String?,
    val firstName: String?,
    val lastName: String?,
    val fullName: String,
    val dateOfBirth: LocalDate?,
    val gender: String?,
    val profileImageUrl: String?,
    val authProvider: AuthProvider?,
    val status: CustomerStatus,
    val emailVerified: Boolean,
    val phoneVerified: Boolean,
    val newsletterSubscribed: Boolean,
    val smsNotifications: Boolean,
    val isGuest: Boolean,
    val isActive: Boolean,
    val lastLoginAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CustomerListResponse(
    val content: List<CustomerProfileResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)

// ========== Customer Address ==========

data class CreateCustomerAddressRequest(
    val addressLabel: String? = null,
    val recipientName: String? = null,
    val phone: String? = null,

    @field:NotBlank(message = "Address line 1 is required")
    val addressLine1: String,

    val addressLine2: String? = null,

    @field:NotBlank(message = "City is required")
    val city: String,

    val stateProvince: String? = null,
    val postalCode: String? = null,
    val country: String = "Nigeria",
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    val isDefault: Boolean = false,
    val isBillingAddress: Boolean = false,
    val isShippingAddress: Boolean = true,
    val deliveryInstructions: String? = null
)

data class UpdateCustomerAddressRequest(
    val addressLabel: String? = null,
    val recipientName: String? = null,
    val phone: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val city: String? = null,
    val stateProvince: String? = null,
    val postalCode: String? = null,
    val country: String? = null,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    val isDefault: Boolean? = null,
    val isBillingAddress: Boolean? = null,
    val isShippingAddress: Boolean? = null,
    val deliveryInstructions: String? = null
)

data class CustomerAddressResponse(
    val id: String,
    val customerId: String,
    val addressLabel: String?,
    val recipientName: String?,
    val phone: String?,
    val addressLine1: String,
    val addressLine2: String?,
    val city: String,
    val stateProvince: String?,
    val postalCode: String?,
    val country: String,
    val latitude: BigDecimal?,
    val longitude: BigDecimal?,
    val isDefault: Boolean,
    val isBillingAddress: Boolean,
    val isShippingAddress: Boolean,
    val deliveryInstructions: String?,
    val fullAddress: String,
    val shortAddress: String,
    val displayLabel: String,
    val hasCoordinates: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CustomerAddressListResponse(
    val content: List<CustomerAddressResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)

// ========== Wishlist ==========

data class AddToWishlistRequest(
    @field:NotBlank(message = "Product ID is required")
    val productId: UUID,

    val priority: Int = 0,
    val notes: String? = null,
    val notifyOnPriceDrop: Boolean = false,
    val targetPrice: BigDecimal? = null
)

data class UpdateWishlistItemRequest(
    val priority: Int? = null,
    val notes: String? = null,
    val notifyOnPriceDrop: Boolean? = null,
    val targetPrice: BigDecimal? = null
)

data class WishlistItemResponse(
    val id: String,
    val customerId: String,
    val productId: String,
    val productName: String?,
    val productImageUrl: String?,
    val currentPrice: BigDecimal?,
    val priority: Int,
    val notes: String?,
    val priceWhenAdded: BigDecimal?,
    val notifyOnPriceDrop: Boolean,
    val targetPrice: BigDecimal?,
    val priceDropPercentage: Double?,
    val isPriceAtTarget: Boolean,
    val addedAt: LocalDateTime
)

data class WishlistListResponse(
    val content: List<WishlistItemResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)

// ========== Customer Analytics ==========

data class SetBudgetCapRequest(
    @field:NotBlank(message = "Monthly budget cap is required")
    val monthlyBudgetCap: BigDecimal,

    val budgetAlertThreshold: BigDecimal = BigDecimal("80.0")
)

data class CustomerAnalyticsResponse(
    val id: String,
    val customerId: String,
    val totalOrders: Int,
    val totalSpent: BigDecimal,
    val averageOrderValue: BigDecimal,
    val monthlyBudgetCap: BigDecimal?,
    val currentMonthSpent: BigDecimal,
    val budgetAlertThreshold: BigDecimal,
    val budgetUtilizationPercentage: Double?,
    val remainingBudget: BigDecimal?,
    val isOverBudget: Boolean,
    val isNearBudgetLimit: Boolean,
    val lifetimeValue: BigDecimal,
    val lastPurchaseAt: LocalDateTime?,
    val firstPurchaseAt: LocalDateTime?,
    val daysSinceLastPurchase: Int?,
    val favoriteCategoryId: String?,
    val favoriteBrand: String?,
    val isActiveCustomer: Boolean,
    val isNewCustomer: Boolean,
    val isVIPCustomer: Boolean,
    val updatedAt: LocalDateTime
)

// ========== Customer Dashboard ==========

data class CustomerDashboardResponse(
    val profile: CustomerProfileResponse,
    val analytics: CustomerAnalyticsResponse,
    val recentOrders: List<OrderSummary>,
    val wishlistCount: Int,
    val addressCount: Int
)

data class OrderSummary(
    val orderId: String,
    val orderNumber: String,
    val orderDate: LocalDateTime,
    val totalAmount: BigDecimal,
    val status: String,
    val itemCount: Int
)

// ========== Password Management ==========

data class ChangePasswordRequest(
    @field:NotBlank(message = "Current password is required")
    val currentPassword: String,

    @field:NotBlank(message = "New password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val newPassword: String
)

data class ForgotPasswordRequest(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String
)

data class ResetPasswordRequest(
    @field:NotBlank(message = "Token is required")
    val token: String,

    @field:NotBlank(message = "New password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val newPassword: String
)