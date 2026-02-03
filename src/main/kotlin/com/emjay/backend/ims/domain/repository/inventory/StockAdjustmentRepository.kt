package com.emjay.backend.ims.domain.repository.inventory

import com.emjay.backend.ims.domain.entity.inventory.AdjustmentType
import com.emjay.backend.ims.domain.entity.inventory.StockAdjustment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime
import java.util.UUID

/**
 * StockAdjustmentRepository port for stock adjustment persistence
 */
interface StockAdjustmentRepository {
    fun save(adjustment: StockAdjustment): StockAdjustment
    
    fun findById(id: UUID): StockAdjustment?
    
    fun findByProductId(productId: UUID, pageable: Pageable): Page<StockAdjustment>
    
    fun findByUserId(userId: UUID, pageable: Pageable): Page<StockAdjustment>
    
    fun findByAdjustmentType(type: AdjustmentType, pageable: Pageable): Page<StockAdjustment>
    
    fun findByDateRange(start: LocalDateTime, end: LocalDateTime, pageable: Pageable): Page<StockAdjustment>
    
    fun findRecentByProductId(productId: UUID, limit: Int): List<StockAdjustment>
    
    fun count(): Long
    
    fun countByProductId(productId: UUID): Long

    fun sumQuantityByProductIdAndType(productId: UUID, type: AdjustmentType): Long
}
