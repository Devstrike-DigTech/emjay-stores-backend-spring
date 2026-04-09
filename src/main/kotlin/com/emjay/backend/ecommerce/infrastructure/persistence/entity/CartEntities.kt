package com.emjay.backend.ecommerce.infrastructure.persistence.entity

import com.emjay.backend.ecommerce.domain.entity.cart.CartStatus
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "shopping_carts")
data class ShoppingCartEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "customer_id")
    val customerId: UUID? = null,

    @Column(name = "guest_session_id", length = 100)
    val guestSessionId: String? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "cart_status")
    val status: CartStatus = CartStatus.ACTIVE,

    @Column(nullable = false, precision = 12, scale = 2)
    val subtotal: BigDecimal = BigDecimal.ZERO,

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    val discountAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    val taxAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    val totalAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "coupon_code", length = 50)
    val couponCode: String? = null,

    @Column(name = "expires_at")
    val expiresAt: LocalDateTime? = null,

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
@Table(name = "cart_items")
data class CartItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "cart_id", nullable = false)
    val cartId: UUID,

    @Column(name = "product_id", nullable = false)
    val productId: UUID,

    @Column(nullable = false)
    val quantity: Int,

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    val unitPrice: BigDecimal,

    @Column(nullable = false, precision = 12, scale = 2)
    val subtotal: BigDecimal,

    @Column(name = "product_name")
    val productName: String? = null,

    @Column(name = "product_sku", length = 100)
    val productSku: String? = null,

    @Column(name = "product_image_url", length = 500)
    val productImageUrl: String? = null,

    @Column(name = "added_at", nullable = false, updatable = false)
    var addedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        addedAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}