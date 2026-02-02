package com.emjay.backend.domain.entity.inventory

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * StockAdjustment domain entity for tracking inventory changes
 */
data class StockAdjustment(
    val id: UUID? = null,
    val productId: UUID,
    val userId: UUID,
    val adjustmentType: AdjustmentType,
    val quantity: Int,
    val previousQuantity: Int,
    val newQuantity: Int,
    val reason: String?,
    val salePrice: BigDecimal?,
    val createdAt: LocalDateTime? = null
) {
    fun isAddition(): Boolean = adjustmentType == AdjustmentType.ADDITION
    
    fun isDeduction(): Boolean = adjustmentType == AdjustmentType.DEDUCTION
    
    fun isSale(): Boolean = adjustmentType == AdjustmentType.SALE
    
    fun isReturn(): Boolean = adjustmentType == AdjustmentType.RETURN
    
    fun quantityChanged(): Int = newQuantity - previousQuantity

    fun totalSaleAmount(): BigDecimal? = salePrice?.multiply(BigDecimal(quantity))

}

/**
 * Types of stock adjustments
 */
enum class AdjustmentType {
    ADDITION,   // Manual stock addition
    DEDUCTION,  // Manual stock deduction
    SALE,       // Stock reduced due to sale
    RETURN      // Stock increased due to return
}
