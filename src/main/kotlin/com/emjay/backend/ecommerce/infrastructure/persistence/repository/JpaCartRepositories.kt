package com.emjay.backend.ecommerce.infrastructure.persistence.repository

import com.emjay.backend.ecommerce.domain.entity.cart.CartStatus
import com.emjay.backend.ecommerce.infrastructure.persistence.entity.CartItemEntity
import com.emjay.backend.ecommerce.infrastructure.persistence.entity.ShoppingCartEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface JpaShoppingCartRepository : JpaRepository<ShoppingCartEntity, UUID> {

    fun findByCustomerId(customerId: UUID): ShoppingCartEntity?

    fun findByGuestSessionId(sessionId: String): ShoppingCartEntity?

    @Query("SELECT c FROM ShoppingCartEntity c WHERE c.customerId = :customerId AND c.status = 'ACTIVE'")
    fun findActiveByCustomerId(@Param("customerId") customerId: UUID): ShoppingCartEntity?

    @Query("SELECT c FROM ShoppingCartEntity c WHERE c.guestSessionId = :sessionId AND c.status = 'ACTIVE'")
    fun findActiveByGuestSessionId(@Param("sessionId") sessionId: String): ShoppingCartEntity?

    @Query("SELECT c FROM ShoppingCartEntity c WHERE c.expiresAt < :before AND c.status = 'ACTIVE'")
    fun findExpiredCarts(@Param("before") before: LocalDateTime, pageable: Pageable): Page<ShoppingCartEntity>

    fun findByStatus(status: CartStatus, pageable: Pageable): Page<ShoppingCartEntity>
}

@Repository
interface JpaCartItemRepository : JpaRepository<CartItemEntity, UUID> {

    fun findByCartId(cartId: UUID): List<CartItemEntity>

    fun findByCartIdAndProductId(cartId: UUID, productId: UUID): CartItemEntity?

    fun existsByCartIdAndProductId(cartId: UUID, productId: UUID): Boolean

    fun countByCartId(cartId: UUID): Long

    fun deleteByCartId(cartId: UUID)
}