package com.emjay.backend.ecommerce.infrastructure.persistence.repository

import com.emjay.backend.ecommerce.domain.entity.order.*
import com.emjay.backend.ecommerce.domain.repository.order.*
import com.emjay.backend.ecommerce.infrastructure.persistence.entity.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

// ========== OrderItem Repository ==========

@Repository
class OrderItemRepositoryImpl(
    private val jpaRepository: JpaOrderItemRepository
) : OrderItemRepository {

    override fun save(item: OrderItem): OrderItem {
        val entity = toEntity(item)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun saveAll(items: List<OrderItem>): List<OrderItem> {
        val entities = items.map { toEntity(it) }
        val saved = jpaRepository.saveAll(entities)
        return saved.map { toDomain(it) }
    }

    override fun findById(id: UUID): OrderItem? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findByOrderId(orderId: UUID): List<OrderItem> {
        return jpaRepository.findByOrderId(orderId).map { toDomain(it) }
    }

    override fun countByOrderId(orderId: UUID): Long {
        return jpaRepository.countByOrderId(orderId)
    }

    override fun delete(item: OrderItem) {
        item.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: OrderItemEntity) = OrderItem(
        id = entity.id,
        orderId = entity.orderId,
        productId = entity.productId,
        quantity = entity.quantity,
        unitPrice = entity.unitPrice,
        subtotal = entity.subtotal,
        productName = entity.productName,
        productSku = entity.productSku,
        productImageUrl = entity.productImageUrl,
        createdAt = entity.createdAt
    )

    private fun toEntity(domain: OrderItem) = OrderItemEntity(
        id = domain.id,
        orderId = domain.orderId,
        productId = domain.productId,
        quantity = domain.quantity,
        unitPrice = domain.unitPrice,
        subtotal = domain.subtotal,
        productName = domain.productName,
        productSku = domain.productSku,
        productImageUrl = domain.productImageUrl,
        createdAt = domain.createdAt ?: LocalDateTime.now()
    )
}

// ========== Payment Repository ==========

@Repository
class PaymentRepositoryImpl(
    private val jpaRepository: JpaPaymentRepository
) : PaymentRepository {

    override fun save(payment: Payment): Payment {
        val entity = toEntity(payment)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): Payment? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findByOrderId(orderId: UUID): List<Payment> {
        return jpaRepository.findByOrderId(orderId).map { toDomain(it) }
    }

    override fun findByGatewayReference(reference: String): Payment? {
        return jpaRepository.findByGatewayReference(reference)?.let { toDomain(it) }
    }

    override fun findByStatus(status: PaymentStatus, pageable: Pageable): Page<Payment> {
        return jpaRepository.findByPaymentStatus(status, pageable).map { toDomain(it) }
    }

    override fun findAll(pageable: Pageable): Page<Payment> {
        return jpaRepository.findAll(pageable).map { toDomain(it) }
    }

    private fun toDomain(entity: PaymentEntity) = Payment(
        id = entity.id,
        orderId = entity.orderId,
        paymentMethod = entity.paymentMethod,
        paymentStatus = entity.paymentStatus,
        amount = entity.amount,
        currency = entity.currency,
        gatewayProvider = entity.gatewayProvider,
        gatewayTransactionId = entity.gatewayTransactionId,
        gatewayReference = entity.gatewayReference,
        metadata = entity.metadata,
        initiatedAt = entity.initiatedAt,
        authorizedAt = entity.authorizedAt,
        capturedAt = entity.capturedAt,
        failedAt = entity.failedAt,
        failureReason = entity.failureReason,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    private fun toEntity(domain: Payment) = PaymentEntity(
        id = domain.id,
        orderId = domain.orderId,
        paymentMethod = domain.paymentMethod,
        paymentStatus = domain.paymentStatus,
        amount = domain.amount,
        currency = domain.currency,
        gatewayProvider = domain.gatewayProvider,
        gatewayTransactionId = domain.gatewayTransactionId,
        gatewayReference = domain.gatewayReference,
        metadata = domain.metadata,
        initiatedAt = domain.initiatedAt,
        authorizedAt = domain.authorizedAt,
        capturedAt = domain.capturedAt,
        failedAt = domain.failedAt,
        failureReason = domain.failureReason,
        createdAt = domain.createdAt ?: LocalDateTime.now(),
        updatedAt = domain.updatedAt ?: LocalDateTime.now()
    )
}

// ========== Shipment Repository ==========

@Repository
class ShipmentRepositoryImpl(
    private val jpaRepository: JpaShipmentRepository
) : ShipmentRepository {

    override fun save(shipment: Shipment): Shipment {
        val entity = toEntity(shipment)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): Shipment? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findByOrderId(orderId: UUID): Shipment? {
        return jpaRepository.findByOrderId(orderId)?.let { toDomain(it) }
    }

    override fun findByTrackingNumber(trackingNumber: String): Shipment? {
        return jpaRepository.findByTrackingNumber(trackingNumber)?.let { toDomain(it) }
    }

    override fun findByStatus(status: ShipmentStatus, pageable: Pageable): Page<Shipment> {
        return jpaRepository.findByStatus(status, pageable).map { toDomain(it) }
    }

    override fun findAll(pageable: Pageable): Page<Shipment> {
        return jpaRepository.findAll(pageable).map { toDomain(it) }
    }

    private fun toDomain(entity: ShipmentEntity) = Shipment(
        id = entity.id,
        orderId = entity.orderId,
        trackingNumber = entity.trackingNumber,
        carrier = entity.carrier,
        status = entity.status,
        shippingMethod = entity.shippingMethod,
        estimatedDeliveryDate = entity.estimatedDeliveryDate,
        actualDeliveryDate = entity.actualDeliveryDate,
        currentLocation = entity.currentLocation,
        notes = entity.notes,
        shippedAt = entity.shippedAt,
        deliveredAt = entity.deliveredAt,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    private fun toEntity(domain: Shipment) = ShipmentEntity(
        id = domain.id,
        orderId = domain.orderId,
        trackingNumber = domain.trackingNumber,
        carrier = domain.carrier,
        status = domain.status,
        shippingMethod = domain.shippingMethod,
        estimatedDeliveryDate = domain.estimatedDeliveryDate,
        actualDeliveryDate = domain.actualDeliveryDate,
        currentLocation = domain.currentLocation,
        notes = domain.notes,
        shippedAt = domain.shippedAt,
        deliveredAt = domain.deliveredAt,
        createdAt = domain.createdAt ?: LocalDateTime.now(),
        updatedAt = domain.updatedAt ?: LocalDateTime.now()
    )
}

// ========== Shipment Tracking Event Repository ==========

@Repository
class ShipmentTrackingEventRepositoryImpl(
    private val jpaRepository: JpaShipmentTrackingEventRepository
) : ShipmentTrackingEventRepository {

    override fun save(event: ShipmentTrackingEvent): ShipmentTrackingEvent {
        val entity = toEntity(event)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findByShipmentId(shipmentId: UUID): List<ShipmentTrackingEvent> {
        return jpaRepository.findByShipmentId(shipmentId).map { toDomain(it) }
    }

    private fun toDomain(entity: ShipmentTrackingEventEntity) = ShipmentTrackingEvent(
        id = entity.id,
        shipmentId = entity.shipmentId,
        status = entity.status,
        location = entity.location,
        description = entity.description,
        eventTimestamp = entity.eventTimestamp,
        createdAt = entity.createdAt
    )

    private fun toEntity(domain: ShipmentTrackingEvent) = ShipmentTrackingEventEntity(
        id = domain.id,
        shipmentId = domain.shipmentId,
        status = domain.status,
        location = domain.location,
        description = domain.description,
        eventTimestamp = domain.eventTimestamp,
        createdAt = domain.createdAt ?: LocalDateTime.now()
    )
}

// ========== Order Status History Repository ==========

@Repository
class OrderStatusHistoryRepositoryImpl(
    private val jpaRepository: JpaOrderStatusHistoryRepository
) : OrderStatusHistoryRepository {

    override fun save(history: OrderStatusHistory): OrderStatusHistory {
        val entity = toEntity(history)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findByOrderId(orderId: UUID): List<OrderStatusHistory> {
        return jpaRepository.findByOrderId(orderId).map { toDomain(it) }
    }

    private fun toDomain(entity: OrderStatusHistoryEntity) = OrderStatusHistory(
        id = entity.id,
        orderId = entity.orderId,
        fromStatus = entity.fromStatus,
        toStatus = entity.toStatus,
        changedBy = entity.changedBy,
        reason = entity.reason,
        changedAt = entity.changedAt
    )

    private fun toEntity(domain: OrderStatusHistory) = OrderStatusHistoryEntity(
        id = domain.id,
        orderId = domain.orderId,
        fromStatus = domain.fromStatus,
        toStatus = domain.toStatus,
        changedBy = domain.changedBy,
        reason = domain.reason,
        changedAt = domain.changedAt
    )
}