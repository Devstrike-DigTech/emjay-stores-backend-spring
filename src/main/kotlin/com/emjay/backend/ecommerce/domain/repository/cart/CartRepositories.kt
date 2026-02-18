package com.emjay.backend.ecommerce.domain.repository.cart

import com.emjay.backend.ecommerce.domain.entity.cart.CartItem
import com.emjay.backend.ecommerce.domain.entity.cart.CartStatus
import com.emjay.backend.ecommerce.domain.entity.cart.ShoppingCart
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime
import java.util.*

/**
 * Repository interface for ShoppingCart domain entity
 */
interface ShoppingCartRepository {

    fun save(cart: ShoppingCart): ShoppingCart

    fun findById(id: UUID): ShoppingCart?

    fun findByCustomerId(customerId: UUID): ShoppingCart?

    fun findByGuestSessionId(sessionId: String): ShoppingCart?

    fun findActiveByCustomerId(customerId: UUID): ShoppingCart?

    fun findActiveByGuestSessionId(sessionId: String): ShoppingCart?

    fun findExpiredCarts(before: LocalDateTime, pageable: Pageable): Page<ShoppingCart>

    fun findByStatus(status: CartStatus, pageable: Pageable): Page<ShoppingCart>

    fun findAll(pageable: Pageable): Page<ShoppingCart>

    fun delete(cart: ShoppingCart)
}

/**
 * Repository interface for CartItem domain entity
 */
interface CartItemRepository {

    fun save(item: CartItem): CartItem

    fun saveAll(items: List<CartItem>): List<CartItem>

    fun findById(id: UUID): CartItem?

    fun findByCartId(cartId: UUID): List<CartItem>

    fun findByCartAndProduct(cartId: UUID, productId: UUID): CartItem?

    fun existsByCartAndProduct(cartId: UUID, productId: UUID): Boolean

    fun countByCartId(cartId: UUID): Long

    fun delete(item: CartItem)

    fun deleteByCartId(cartId: UUID)
}