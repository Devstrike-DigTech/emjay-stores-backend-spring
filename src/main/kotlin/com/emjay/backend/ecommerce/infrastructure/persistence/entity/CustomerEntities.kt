package com.emjay.backend.ecommerce.infrastructure.persistence.entity

import com.emjay.backend.ecommerce.domain.entity.customer.*
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "customers")
data class CustomerEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "customer_type", nullable = false, columnDefinition = "customer_type")
    val customerType: CustomerType,

    @Column(unique = true)
    val email: String? = null,

    @Column(length = 20)
    val phone: String? = null,

    @Column(name = "first_name", length = 100)
    val firstName: String? = null,

    @Column(name = "last_name", length = 100)
    val lastName: String? = null,

    @Column(name = "password_hash")
    val passwordHash: String? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "auth_provider", columnDefinition = "auth_provider")
    val authProvider: AuthProvider? = null,

    @Column(name = "google_id", unique = true)
    val googleId: String? = null,

    @Column(name = "facebook_id", unique = true)
    val facebookId: String? = null,

    @Column(name = "apple_id", unique = true)
    val appleId: String? = null,

    @Column(name = "date_of_birth")
    val dateOfBirth: LocalDate? = null,

    @Column(length = 20)
    val gender: String? = null,

    @Column(name = "profile_image_url", length = 500)
    val profileImageUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "customer_status")
    val status: CustomerStatus = CustomerStatus.ACTIVE,

    @Column(name = "email_verified")
    val emailVerified: Boolean = false,

    @Column(name = "phone_verified")
    val phoneVerified: Boolean = false,

    @Column(name = "newsletter_subscribed")
    val newsletterSubscribed: Boolean = false,

    @Column(name = "sms_notifications")
    val smsNotifications: Boolean = false,

    @Column(name = "guest_session_id", unique = true, length = 100)
    val guestSessionId: String? = null,

    @Column(name = "guest_session_expires_at")
    val guestSessionExpiresAt: LocalDateTime? = null,

    @Column(name = "last_login_at")
    val lastLoginAt: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

@Entity
@Table(name = "customer_addresses")
data class CustomerAddressEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "customer_id", nullable = false)
    val customerId: UUID,

    @Column(name = "address_label", length = 50)
    val addressLabel: String? = null,

    @Column(name = "recipient_name", length = 200)
    val recipientName: String? = null,

    @Column(length = 20)
    val phone: String? = null,

    @Column(name = "address_line1", nullable = false)
    val addressLine1: String,

    @Column(name = "address_line2")
    val addressLine2: String? = null,

    @Column(nullable = false, length = 100)
    val city: String,

    @Column(name = "state_province", length = 100)
    val stateProvince: String? = null,

    @Column(name = "postal_code", length = 20)
    val postalCode: String? = null,

    @Column(nullable = false, length = 100)
    val country: String = "Nigeria",

    @Column(precision = 10, scale = 8)
    val latitude: BigDecimal? = null,

    @Column(precision = 11, scale = 8)
    val longitude: BigDecimal? = null,

    @Column(name = "is_default")
    val isDefault: Boolean = false,

    @Column(name = "is_billing_address")
    val isBillingAddress: Boolean = false,

    @Column(name = "is_shipping_address")
    val isShippingAddress: Boolean = true,

    @Column(name = "delivery_instructions", columnDefinition = "TEXT")
    val deliveryInstructions: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

@Entity
@Table(name = "wishlist_items")
data class WishlistItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "customer_id", nullable = false)
    val customerId: UUID,

    @Column(name = "product_id", nullable = false)
    val productId: UUID,

    val priority: Int = 0,

    @Column(columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(name = "price_when_added", precision = 12, scale = 2)
    val priceWhenAdded: BigDecimal? = null,

    @Column(name = "notify_on_price_drop")
    val notifyOnPriceDrop: Boolean = false,

    @Column(name = "target_price", precision = 12, scale = 2)
    val targetPrice: BigDecimal? = null,

    @Column(name = "added_at", nullable = false, updatable = false)
    var addedAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        addedAt = LocalDateTime.now()
    }
}

@Entity
@Table(name = "customer_analytics")
data class CustomerAnalyticsEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "customer_id", nullable = false, unique = true)
    val customerId: UUID,

    @Column(name = "total_orders")
    val totalOrders: Int = 0,

    @Column(name = "total_spent", precision = 12, scale = 2)
    val totalSpent: BigDecimal = BigDecimal.ZERO,

    @Column(name = "average_order_value", precision = 12, scale = 2)
    val averageOrderValue: BigDecimal = BigDecimal.ZERO,

    @Column(name = "monthly_budget_cap", precision = 12, scale = 2)
    val monthlyBudgetCap: BigDecimal? = null,

    @Column(name = "current_month_spent", precision = 12, scale = 2)
    val currentMonthSpent: BigDecimal = BigDecimal.ZERO,

    @Column(name = "budget_alert_threshold", precision = 5, scale = 2)
    val budgetAlertThreshold: BigDecimal = BigDecimal("80.0"),

    @Column(name = "lifetime_value", precision = 12, scale = 2)
    val lifetimeValue: BigDecimal = BigDecimal.ZERO,

    @Column(name = "last_purchase_at")
    val lastPurchaseAt: LocalDateTime? = null,

    @Column(name = "first_purchase_at")
    val firstPurchaseAt: LocalDateTime? = null,

    @Column(name = "days_since_last_purchase")
    val daysSinceLastPurchase: Int? = null,

    @Column(name = "favorite_category_id")
    val favoriteCategoryId: UUID? = null,

    @Column(name = "favorite_brand", length = 100)
    val favoriteBrand: String? = null,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        updatedAt = LocalDateTime.now()
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}