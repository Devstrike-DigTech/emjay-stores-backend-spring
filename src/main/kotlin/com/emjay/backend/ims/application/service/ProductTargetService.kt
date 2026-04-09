package com.emjay.backend.ims.application.service

import com.emjay.backend.common.domain.exception.ResourceNotFoundException
import com.emjay.backend.ims.application.dto.product.ProductTargetResponse
import com.emjay.backend.ims.application.dto.product.SetProductTargetRequest
import com.emjay.backend.ims.domain.entity.inventory.AdjustmentType
import com.emjay.backend.ims.domain.entity.product.ProductTarget
import com.emjay.backend.ims.domain.repository.product.ProductTargetRepository
import com.emjay.backend.ims.infrastructure.persistence.repository.JpaStockAdjustmentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.UUID

@Service
class ProductTargetService(
    private val productTargetRepository: ProductTargetRepository,
    private val jpaStockAdjustmentRepository: JpaStockAdjustmentRepository
) {

    @Transactional
    fun setTarget(productId: UUID, request: SetProductTargetRequest, createdBy: UUID): ProductTargetResponse {
        val existing = productTargetRepository.findByProductIdAndYearMonth(productId, request.year, request.month)

        val target = if (existing != null) {
            existing.copy(targetUnits = request.targetUnits, updatedAt = LocalDateTime.now())
        } else {
            ProductTarget(
                productId = productId,
                targetYear = request.year,
                targetMonth = request.month,
                targetUnits = request.targetUnits,
                createdBy = createdBy
            )
        }

        val saved = productTargetRepository.save(target)
        val actualSold = getActualSold(productId, request.year, request.month)
        return toResponse(saved, actualSold)
    }

    fun getTarget(productId: UUID, year: Int? = null, month: Int? = null): ProductTargetResponse? {
        val now = YearMonth.now()
        val targetYear = year ?: now.year
        val targetMonth = month ?: now.monthValue

        val target = productTargetRepository.findByProductIdAndYearMonth(productId, targetYear, targetMonth)
            ?: return null

        val actualSold = getActualSold(productId, targetYear, targetMonth)
        return toResponse(target, actualSold)
    }

    private fun getActualSold(productId: UUID, year: Int, month: Int): Int {
        val ym = YearMonth.of(year, month)
        val start = ym.atDay(1).atStartOfDay()
        val end = ym.atEndOfMonth().atTime(23, 59, 59)
        return jpaStockAdjustmentRepository.countSalesByProductAndDateRange(productId, AdjustmentType.SALE, start, end)
    }

    private fun toResponse(target: ProductTarget, actualSold: Int): ProductTargetResponse {
        val progress = if (target.targetUnits > 0) {
            BigDecimal(actualSold).divide(BigDecimal(target.targetUnits), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)
        } else BigDecimal.ZERO

        return ProductTargetResponse(
            id = target.id.toString(),
            productId = target.productId.toString(),
            year = target.targetYear,
            month = target.targetMonth,
            targetUnits = target.targetUnits,
            actualUnitsSold = actualSold,
            progressPercentage = progress,
            createdBy = target.createdBy.toString()
        )
    }
}
