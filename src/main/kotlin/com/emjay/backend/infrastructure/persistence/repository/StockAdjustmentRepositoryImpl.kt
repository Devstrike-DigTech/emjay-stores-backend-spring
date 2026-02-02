package com.emjay.backend.infrastructure.persistence.repository

import com.emjay.backend.domain.entity.inventory.AdjustmentType
import com.emjay.backend.domain.entity.inventory.StockAdjustment
import com.emjay.backend.domain.repository.inventory.StockAdjustmentRepository
import com.emjay.backend.infrastructure.persistence.entity.StockAdjustmentEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class StockAdjustmentRepositoryImpl(
    private val jpaRepository: JpaStockAdjustmentRepository
) : StockAdjustmentRepository {

    override fun save(adjustment: StockAdjustment): StockAdjustment {
        val entity = toEntity(adjustment)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): StockAdjustment? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findByProductId(productId: UUID, pageable: Pageable): Page<StockAdjustment> {
        return jpaRepository.findAllByProductId(productId, pageable).map { toDomain(it) }
    }

    override fun findByUserId(userId: UUID, pageable: Pageable): Page<StockAdjustment> {
        return jpaRepository.findAllByUserId(userId, pageable).map { toDomain(it) }
    }

    override fun findByAdjustmentType(type: AdjustmentType, pageable: Pageable): Page<StockAdjustment> {
        return jpaRepository.findAllByAdjustmentType(type, pageable).map { toDomain(it) }
    }

    override fun findByDateRange(start: LocalDateTime, end: LocalDateTime, pageable: Pageable): Page<StockAdjustment> {
        return jpaRepository.findByCreatedAtBetween(start, end, pageable).map { toDomain(it) }
    }

    override fun findRecentByProductId(productId: UUID, limit: Int): List<StockAdjustment> {
        return jpaRepository.findAllByProductId(productId, PageRequest.of(0, limit))
            .content
            .map { toDomain(it) }
    }

    override fun count(): Long {
        return jpaRepository.count()
    }

    override fun countByProductId(productId: UUID): Long {
        return jpaRepository.countByProductId(productId)
    }

    override fun sumQuantityByProductIdAndType(productId: UUID, type: AdjustmentType): Long {
        return jpaRepository.sumQuantityByProductIdAndType(productId, type) ?: 0L
    }

    private fun toDomain(entity: StockAdjustmentEntity): StockAdjustment {
        return StockAdjustment(
            id = entity.id,
            productId = entity.productId,
            userId = entity.userId,
            adjustmentType = entity.adjustmentType,
            quantity = entity.quantity,
            previousQuantity = entity.previousQuantity,
            newQuantity = entity.newQuantity,
            reason = entity.reason,
            salePrice = entity.salePrice,
            createdAt = entity.createdAt
        )
    }

    private fun toEntity(domain: StockAdjustment): StockAdjustmentEntity {
        return StockAdjustmentEntity(
            id = domain.id,
            productId = domain.productId,
            userId = domain.userId,
            adjustmentType = domain.adjustmentType,
            quantity = domain.quantity,
            previousQuantity = domain.previousQuantity,
            newQuantity = domain.newQuantity,
            reason = domain.reason,
            salePrice = domain.salePrice,
            createdAt = domain.createdAt ?: java.time.LocalDateTime.now()
        )
    }
}