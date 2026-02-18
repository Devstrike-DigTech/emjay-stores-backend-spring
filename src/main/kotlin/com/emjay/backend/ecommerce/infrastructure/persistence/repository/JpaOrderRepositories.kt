package com.emjay.backend.ecommerce.infrastructure.persistence.repository

import com.emjay.backend.ecommerce.domain.entity.order.*
import com.emjay.backend.ecommerce.infrastructure.persistence.entity.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface JpaOrderRepository : JpaRepository<OrderEntity, UUID> {

    fun findByOrderNumber(orderNumber: String): OrderEntity?

    fun findByCustomerId(customerId: UUID, pageable: Pageable): Page<OrderEntity>

    @Query("SELECT o FROM OrderEntity o WHERE o.customerId = :customerId AND o.status = :status")
    fun findByCustomerIdAndStatus(
        @Param("customerId") customerId: UUID,
        @Param("status") status: OrderStatus,
        pageable: Pageable
    ): Page<OrderEntity>

    fun findByStatus(status: OrderStatus, pageable: Pageable): Page<OrderEntity>

    @Query("SELECT o FROM OrderEntity o WHERE o.orderedAt >= :startDate AND o.orderedAt <= :endDate")
    fun findByDateRange(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
        pageable: Pageable
    ): Page<OrderEntity>

    fun countByCustomerId(customerId: UUID): Long

    fun countByStatus(status: OrderStatus): Long
}

@Repository
interface JpaOrderItemRepository : JpaRepository<OrderItemEntity, UUID> {

    fun findByOrderId(orderId: UUID): List<OrderItemEntity>

    fun countByOrderId(orderId: UUID): Long
}

@Repository
interface JpaPaymentRepository : JpaRepository<PaymentEntity, UUID> {

    fun findByOrderId(orderId: UUID): List<PaymentEntity>

    fun findByGatewayReference(reference: String): PaymentEntity?

    fun findByPaymentStatus(status: PaymentStatus, pageable: Pageable): Page<PaymentEntity>
}

@Repository
interface JpaShipmentRepository : JpaRepository<ShipmentEntity, UUID> {

    fun findByOrderId(orderId: UUID): ShipmentEntity?

    fun findByTrackingNumber(trackingNumber: String): ShipmentEntity?

    fun findByStatus(status: ShipmentStatus, pageable: Pageable): Page<ShipmentEntity>
}

@Repository
interface JpaShipmentTrackingEventRepository : JpaRepository<ShipmentTrackingEventEntity, UUID> {

    fun findByShipmentId(shipmentId: UUID): List<ShipmentTrackingEventEntity>
}

@Repository
interface JpaOrderStatusHistoryRepository : JpaRepository<OrderStatusHistoryEntity, UUID> {

    fun findByOrderId(orderId: UUID): List<OrderStatusHistoryEntity>
}