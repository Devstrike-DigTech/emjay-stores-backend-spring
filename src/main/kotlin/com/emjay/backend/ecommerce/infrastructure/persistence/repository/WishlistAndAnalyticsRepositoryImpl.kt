package com.emjay.backend.ecommerce.infrastructure.persistence.repository

import com.emjay.backend.ecommerce.domain.entity.customer.CustomerAnalytics
import com.emjay.backend.ecommerce.domain.entity.customer.WishlistItem
import com.emjay.backend.ecommerce.domain.repository.customer.CustomerAnalyticsRepository
import com.emjay.backend.ecommerce.domain.repository.customer.WishlistItemRepository
import com.emjay.backend.ecommerce.infrastructure.persistence.entity.CustomerAnalyticsEntity
import com.emjay.backend.ecommerce.infrastructure.persistence.entity.WishlistItemEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class WishlistItemRepositoryImpl(
    private val jpaRepository: JpaWishlistItemRepository
) : WishlistItemRepository {

    override fun save(item: WishlistItem): WishlistItem {
        val entity = toEntity(item)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): WishlistItem? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findByCustomerId(customerId: UUID, pageable: Pageable): Page<WishlistItem> {
        return jpaRepository.findByCustomerId(customerId, pageable).map { toDomain(it) }
    }

    override fun findByCustomerAndProduct(customerId: UUID, productId: UUID): WishlistItem? {
        return jpaRepository.findByCustomerIdAndProductId(customerId, productId)?.let { toDomain(it) }
    }

    override fun existsByCustomerAndProduct(customerId: UUID, productId: UUID): Boolean {
        return jpaRepository.existsByCustomerIdAndProductId(customerId, productId)
    }

    override fun countByCustomerId(customerId: UUID): Long {
        return jpaRepository.countByCustomerId(customerId)
    }

    override fun delete(item: WishlistItem) {
        item.id?.let { jpaRepository.deleteById(it) }
    }

    override fun deleteByCustomerAndProduct(customerId: UUID, productId: UUID) {
        jpaRepository.deleteByCustomerIdAndProductId(customerId, productId)
    }

    private fun toDomain(entity: WishlistItemEntity): WishlistItem {
        return WishlistItem(
            id = entity.id,
            customerId = entity.customerId,
            productId = entity.productId,
            priority = entity.priority,
            notes = entity.notes,
            priceWhenAdded = entity.priceWhenAdded,
            notifyOnPriceDrop = entity.notifyOnPriceDrop,
            targetPrice = entity.targetPrice,
            addedAt = entity.addedAt
        )
    }

    private fun toEntity(domain: WishlistItem): WishlistItemEntity {
        return WishlistItemEntity(
            id = domain.id,
            customerId = domain.customerId,
            productId = domain.productId,
            priority = domain.priority,
            notes = domain.notes,
            priceWhenAdded = domain.priceWhenAdded,
            notifyOnPriceDrop = domain.notifyOnPriceDrop,
            targetPrice = domain.targetPrice,
            addedAt = domain.addedAt ?: LocalDateTime.now()
        )
    }
}

@Repository
class CustomerAnalyticsRepositoryImpl(
    private val jpaRepository: JpaCustomerAnalyticsRepository
) : CustomerAnalyticsRepository {

    override fun save(analytics: CustomerAnalytics): CustomerAnalytics {
        val entity = toEntity(analytics)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): CustomerAnalytics? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findByCustomerId(customerId: UUID): CustomerAnalytics? {
        return jpaRepository.findByCustomerId(customerId)?.let { toDomain(it) }
    }

    override fun findAll(pageable: Pageable): Page<CustomerAnalytics> {
        return jpaRepository.findAll(pageable).map { toDomain(it) }
    }

    override fun delete(analytics: CustomerAnalytics) {
        analytics.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: CustomerAnalyticsEntity): CustomerAnalytics {
        return CustomerAnalytics(
            id = entity.id,
            customerId = entity.customerId,
            totalOrders = entity.totalOrders,
            totalSpent = entity.totalSpent,
            averageOrderValue = entity.averageOrderValue,
            monthlyBudgetCap = entity.monthlyBudgetCap,
            currentMonthSpent = entity.currentMonthSpent,
            budgetAlertThreshold = entity.budgetAlertThreshold,
            lifetimeValue = entity.lifetimeValue,
            lastPurchaseAt = entity.lastPurchaseAt,
            firstPurchaseAt = entity.firstPurchaseAt,
            daysSinceLastPurchase = entity.daysSinceLastPurchase,
            favoriteCategoryId = entity.favoriteCategoryId,
            favoriteBrand = entity.favoriteBrand,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEntity(domain: CustomerAnalytics): CustomerAnalyticsEntity {
        return CustomerAnalyticsEntity(
            id = domain.id,
            customerId = domain.customerId,
            totalOrders = domain.totalOrders,
            totalSpent = domain.totalSpent,
            averageOrderValue = domain.averageOrderValue,
            monthlyBudgetCap = domain.monthlyBudgetCap,
            currentMonthSpent = domain.currentMonthSpent,
            budgetAlertThreshold = domain.budgetAlertThreshold,
            lifetimeValue = domain.lifetimeValue,
            lastPurchaseAt = domain.lastPurchaseAt,
            firstPurchaseAt = domain.firstPurchaseAt,
            daysSinceLastPurchase = domain.daysSinceLastPurchase,
            favoriteCategoryId = domain.favoriteCategoryId,
            favoriteBrand = domain.favoriteBrand,
            updatedAt = domain.updatedAt ?: LocalDateTime.now()
        )
    }
}