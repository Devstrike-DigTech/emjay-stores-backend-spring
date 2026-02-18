package com.emjay.backend.ecommerce.infrastructure.persistence.repository

import com.emjay.backend.ecommerce.domain.entity.order.Order
import com.emjay.backend.ecommerce.domain.entity.order.OrderStatus
import com.emjay.backend.ecommerce.domain.repository.order.OrderRepository
import com.emjay.backend.ecommerce.infrastructure.persistence.entity.OrderEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.Year
import java.util.*

@Repository
class OrderRepositoryImpl(
    private val jpaRepository: JpaOrderRepository,
    private val jdbcTemplate: JdbcTemplate
) : OrderRepository {

    override fun save(order: Order): Order {
        val entity = toEntity(order)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): Order? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findByOrderNumber(orderNumber: String): Order? {
        return jpaRepository.findByOrderNumber(orderNumber)?.let { toDomain(it) }
    }

    override fun findByCustomerId(customerId: UUID, pageable: Pageable): Page<Order> {
        return jpaRepository.findByCustomerId(customerId, pageable).map { toDomain(it) }
    }

    override fun findByCustomerIdAndStatus(
        customerId: UUID,
        status: OrderStatus,
        pageable: Pageable
    ): Page<Order> {
        return jpaRepository.findByCustomerIdAndStatus(customerId, status, pageable).map { toDomain(it) }
    }

    override fun findByStatus(status: OrderStatus, pageable: Pageable): Page<Order> {
        return jpaRepository.findByStatus(status, pageable).map { toDomain(it) }
    }

    override fun findByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        pageable: Pageable
    ): Page<Order> {
        return jpaRepository.findByDateRange(startDate, endDate, pageable).map { toDomain(it) }
    }

    override fun findAll(pageable: Pageable): Page<Order> {
        return jpaRepository.findAll(pageable).map { toDomain(it) }
    }

    override fun countByCustomerId(customerId: UUID): Long {
        return jpaRepository.countByCustomerId(customerId)
    }

    override fun countByStatus(status: OrderStatus): Long {
        return jpaRepository.countByStatus(status)
    }

    override fun generateOrderNumber(): String {
        val year = Year.now().value
        val sequence = jdbcTemplate.queryForObject(
            "SELECT nextval('order_number_seq')",
            Long::class.java
        ) ?: 1L

        return String.format("ORD-%d-%05d", year, sequence)
    }

    private fun toDomain(entity: OrderEntity): Order {
        return Order(
            id = entity.id,
            orderNumber = entity.orderNumber,
            customerId = entity.customerId,
            status = entity.status,
            subtotal = entity.subtotal,
            discountAmount = entity.discountAmount,
            taxAmount = entity.taxAmount,
            shippingCost = entity.shippingCost,
            totalAmount = entity.totalAmount,
            couponCode = entity.couponCode,
            shippingAddressId = entity.shippingAddressId,
            shippingAddressLine1 = entity.shippingAddressLine1,
            shippingAddressLine2 = entity.shippingAddressLine2,
            shippingCity = entity.shippingCity,
            shippingState = entity.shippingState,
            shippingPostalCode = entity.shippingPostalCode,
            shippingCountry = entity.shippingCountry,
            recipientName = entity.recipientName,
            recipientPhone = entity.recipientPhone,
            billingAddressId = entity.billingAddressId,
            billingAddressLine1 = entity.billingAddressLine1,
            billingAddressLine2 = entity.billingAddressLine2,
            billingCity = entity.billingCity,
            billingState = entity.billingState,
            billingPostalCode = entity.billingPostalCode,
            billingCountry = entity.billingCountry,
            customerNotes = entity.customerNotes,
            adminNotes = entity.adminNotes,
            orderedAt = entity.orderedAt,
            paidAt = entity.paidAt,
            shippedAt = entity.shippedAt,
            deliveredAt = entity.deliveredAt,
            cancelledAt = entity.cancelledAt,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEntity(domain: Order): OrderEntity {
        return OrderEntity(
            id = domain.id,
            orderNumber = domain.orderNumber,
            customerId = domain.customerId,
            status = domain.status,
            subtotal = domain.subtotal,
            discountAmount = domain.discountAmount,
            taxAmount = domain.taxAmount,
            shippingCost = domain.shippingCost,
            totalAmount = domain.totalAmount,
            couponCode = domain.couponCode,
            shippingAddressId = domain.shippingAddressId,
            shippingAddressLine1 = domain.shippingAddressLine1,
            shippingAddressLine2 = domain.shippingAddressLine2,
            shippingCity = domain.shippingCity,
            shippingState = domain.shippingState,
            shippingPostalCode = domain.shippingPostalCode,
            shippingCountry = domain.shippingCountry,
            recipientName = domain.recipientName,
            recipientPhone = domain.recipientPhone,
            billingAddressId = domain.billingAddressId,
            billingAddressLine1 = domain.billingAddressLine1,
            billingAddressLine2 = domain.billingAddressLine2,
            billingCity = domain.billingCity,
            billingState = domain.billingState,
            billingPostalCode = domain.billingPostalCode,
            billingCountry = domain.billingCountry,
            customerNotes = domain.customerNotes,
            adminNotes = domain.adminNotes,
            orderedAt = domain.orderedAt,
            paidAt = domain.paidAt,
            shippedAt = domain.shippedAt,
            deliveredAt = domain.deliveredAt,
            cancelledAt = domain.cancelledAt,
            createdAt = domain.createdAt ?: LocalDateTime.now(),
            updatedAt = domain.updatedAt ?: LocalDateTime.now()
        )
    }
}