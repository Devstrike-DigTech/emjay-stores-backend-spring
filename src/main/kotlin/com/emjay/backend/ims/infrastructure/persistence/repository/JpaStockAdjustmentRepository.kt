package com.emjay.backend.ims.infrastructure.persistence.repository

import com.emjay.backend.ims.domain.entity.inventory.AdjustmentType
import com.emjay.backend.ims.infrastructure.persistence.entity.StockAdjustmentEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface JpaStockAdjustmentRepository : JpaRepository<StockAdjustmentEntity, UUID> {

    fun findAllByProductId(productId: UUID, pageable: Pageable): Page<StockAdjustmentEntity>

    fun findAllByUserId(userId: UUID, pageable: Pageable): Page<StockAdjustmentEntity>

    fun findAllByAdjustmentType(adjustmentType: AdjustmentType, pageable: Pageable): Page<StockAdjustmentEntity>

    fun findByCreatedAtBetween(start: LocalDateTime, end: LocalDateTime, pageable: Pageable): Page<StockAdjustmentEntity>

    fun countByProductId(productId: UUID): Long


    @Query("SELECT SUM(s.quantity) FROM StockAdjustmentEntity s WHERE s.productId = :productId AND s.adjustmentType = :type")
    fun sumQuantityByProductIdAndType(@Param("productId") productId: UUID, @Param("type") type: AdjustmentType): Long?

    @Query(
        value = """
            SELECT EXTRACT(DOW FROM created_at)::integer AS day_of_week,
                   COALESCE(SUM(COALESCE(sale_price, 0) * quantity), 0) AS total_sales,
                   COUNT(*) AS units_sold
            FROM stock_adjustments
            WHERE adjustment_type = 'SALE'
            GROUP BY EXTRACT(DOW FROM created_at)
            ORDER BY day_of_week
        """,
        nativeQuery = true
    )
    fun getSalesByDayOfWeek(): List<Array<Any>>

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM StockAdjustmentEntity s WHERE s.productId = :productId AND s.adjustmentType = :type AND s.createdAt BETWEEN :start AND :end")
    fun countSalesByProductAndDateRange(
        @Param("productId") productId: UUID,
        @Param("type") type: AdjustmentType,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): Int
}