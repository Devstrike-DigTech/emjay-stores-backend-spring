package com.emjay.backend.ecommerce.domain.repository.order

import com.emjay.backend.ecommerce.domain.entity.order.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime
import java.util.*

/**
 * Repository interface for Order domain entity
 */
interface OrderRepository {

    fun save(order: Order): Order

    fun findById(id: UUID): Order?

    fun findByOrderNumber(orderNumber: String): Order?

    fun findByCustomerId(customerId: UUID, pageable: Pageable): Page<Order>

    fun findByCustomerIdAndStatus(customerId: UUID, status: OrderStatus, pageable: Pageable): Page<Order>

    fun findByStatus(status: OrderStatus, pageable: Pageable): Page<Order>

    fun findByDateRange(startDate: LocalDateTime, endDate: LocalDateTime, pageable: Pageable): Page<Order>

    fun findAll(pageable: Pageable): Page<Order>

    fun countByCustomerId(customerId: UUID): Long

    fun countByStatus(status: OrderStatus): Long

    fun generateOrderNumber(): String
}

/**
 * Repository interface for OrderItem domain entity
 */
interface OrderItemRepository {

    fun save(item: OrderItem): OrderItem

    fun saveAll(items: List<OrderItem>): List<OrderItem>

    fun findById(id: UUID): OrderItem?

    fun findByOrderId(orderId: UUID): List<OrderItem>

    fun countByOrderId(orderId: UUID): Long

    fun delete(item: OrderItem)
}

/**
 * Repository interface for Payment domain entity
 */
interface PaymentRepository {

    fun save(payment: Payment): Payment

    fun findById(id: UUID): Payment?

    fun findByOrderId(orderId: UUID): List<Payment>

    fun findByGatewayReference(reference: String): Payment?

    fun findByStatus(status: PaymentStatus, pageable: Pageable): Page<Payment>

    fun findAll(pageable: Pageable): Page<Payment>
}

/**
 * Repository interface for Shipment domain entity
 */
interface ShipmentRepository {

    fun save(shipment: Shipment): Shipment

    fun findById(id: UUID): Shipment?

    fun findByOrderId(orderId: UUID): Shipment?

    fun findByTrackingNumber(trackingNumber: String): Shipment?

    fun findByStatus(status: ShipmentStatus, pageable: Pageable): Page<Shipment>

    fun findAll(pageable: Pageable): Page<Shipment>
}

/**
 * Repository interface for ShipmentTrackingEvent domain entity
 */
interface ShipmentTrackingEventRepository {

    fun save(event: ShipmentTrackingEvent): ShipmentTrackingEvent

    fun findByShipmentId(shipmentId: UUID): List<ShipmentTrackingEvent>
}

/**
 * Repository interface for OrderStatusHistory domain entity
 */
interface OrderStatusHistoryRepository {

    fun save(history: OrderStatusHistory): OrderStatusHistory

    fun findByOrderId(orderId: UUID): List<OrderStatusHistory>
}