package com.emjay.backend.ecommerce.infrastructure.persistence.repository

import com.emjay.backend.ecommerce.domain.entity.cart.CartStatus
import com.emjay.backend.ecommerce.domain.entity.cart.ShoppingCart
import com.emjay.backend.ecommerce.domain.repository.cart.ShoppingCartRepository
import com.emjay.backend.ecommerce.infrastructure.persistence.entity.ShoppingCartEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class ShoppingCartRepositoryImpl(
    private val jpaRepository: JpaShoppingCartRepository
) : ShoppingCartRepository {

    override fun save(cart: ShoppingCart): ShoppingCart {
        val entity = toEntity(cart)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): ShoppingCart? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findByCustomerId(customerId: UUID): ShoppingCart? {
        return jpaRepository.findByCustomerId(customerId)?.let { toDomain(it) }
    }

    override fun findByGuestSessionId(sessionId: String): ShoppingCart? {
        return jpaRepository.findByGuestSessionId(sessionId)?.let { toDomain(it) }
    }

    override fun findActiveByCustomerId(customerId: UUID): ShoppingCart? {
        return jpaRepository.findActiveByCustomerId(customerId)?.let { toDomain(it) }
    }

    override fun findActiveByGuestSessionId(sessionId: String): ShoppingCart? {
        return jpaRepository.findActiveByGuestSessionId(sessionId)?.let { toDomain(it) }
    }

    override fun findExpiredCarts(before: LocalDateTime, pageable: Pageable): Page<ShoppingCart> {
        return jpaRepository.findExpiredCarts(before, pageable).map { toDomain(it) }
    }

    override fun findByStatus(status: CartStatus, pageable: Pageable): Page<ShoppingCart> {
        return jpaRepository.findByStatus(status, pageable).map { toDomain(it) }
    }

    override fun findAll(pageable: Pageable): Page<ShoppingCart> {
        return jpaRepository.findAll(pageable).map { toDomain(it) }
    }

    override fun delete(cart: ShoppingCart) {
        cart.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: ShoppingCartEntity): ShoppingCart {
        return ShoppingCart(
            id = entity.id,
            customerId = entity.customerId,
            guestSessionId = entity.guestSessionId,
            status = entity.status,
            subtotal = entity.subtotal,
            discountAmount = entity.discountAmount,
            taxAmount = entity.taxAmount,
            totalAmount = entity.totalAmount,
            couponCode = entity.couponCode,
            expiresAt = entity.expiresAt,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEntity(domain: ShoppingCart): ShoppingCartEntity {
        return ShoppingCartEntity(
            id = domain.id,
            customerId = domain.customerId,
            guestSessionId = domain.guestSessionId,
            status = domain.status,
            subtotal = domain.subtotal,
            discountAmount = domain.discountAmount,
            taxAmount = domain.taxAmount,
            totalAmount = domain.totalAmount,
            couponCode = domain.couponCode,
            expiresAt = domain.expiresAt,
            createdAt = domain.createdAt ?: LocalDateTime.now(),
            updatedAt = domain.updatedAt ?: LocalDateTime.now()
        )
    }
}