package com.emjay.backend.ecommerce.infrastructure.persistence.repository

import com.emjay.backend.ecommerce.domain.entity.cart.CartItem
import com.emjay.backend.ecommerce.domain.repository.cart.CartItemRepository
import com.emjay.backend.ecommerce.infrastructure.persistence.entity.CartItemEntity
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class CartItemRepositoryImpl(
    private val jpaRepository: JpaCartItemRepository
) : CartItemRepository {

    override fun save(item: CartItem): CartItem {
        val entity = toEntity(item)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun saveAll(items: List<CartItem>): List<CartItem> {
        val entities = items.map { toEntity(it) }
        val saved = jpaRepository.saveAll(entities)
        return saved.map { toDomain(it) }
    }

    override fun findById(id: UUID): CartItem? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findByCartId(cartId: UUID): List<CartItem> {
        return jpaRepository.findByCartId(cartId).map { toDomain(it) }
    }

    override fun findByCartAndProduct(cartId: UUID, productId: UUID): CartItem? {
        return jpaRepository.findByCartIdAndProductId(cartId, productId)?.let { toDomain(it) }
    }

    override fun existsByCartAndProduct(cartId: UUID, productId: UUID): Boolean {
        return jpaRepository.existsByCartIdAndProductId(cartId, productId)
    }

    override fun countByCartId(cartId: UUID): Long {
        return jpaRepository.countByCartId(cartId)
    }

    override fun delete(item: CartItem) {
        item.id?.let { jpaRepository.deleteById(it) }
    }

    override fun deleteByCartId(cartId: UUID) {
        jpaRepository.deleteByCartId(cartId)
    }

    private fun toDomain(entity: CartItemEntity): CartItem {
        return CartItem(
            id = entity.id,
            cartId = entity.cartId,
            productId = entity.productId,
            quantity = entity.quantity,
            unitPrice = entity.unitPrice,
            subtotal = entity.subtotal,
            productName = entity.productName,
            productSku = entity.productSku,
            productImageUrl = entity.productImageUrl,
            addedAt = entity.addedAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEntity(domain: CartItem): CartItemEntity {
        return CartItemEntity(
            id = domain.id,
            cartId = domain.cartId,
            productId = domain.productId,
            quantity = domain.quantity,
            unitPrice = domain.unitPrice,
            subtotal = domain.subtotal,
            productName = domain.productName,
            productSku = domain.productSku,
            productImageUrl = domain.productImageUrl,
            addedAt = domain.addedAt ?: LocalDateTime.now(),
            updatedAt = domain.updatedAt ?: LocalDateTime.now()
        )
    }
}