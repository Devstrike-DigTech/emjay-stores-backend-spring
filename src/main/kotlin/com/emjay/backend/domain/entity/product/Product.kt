package com.emjay.backend.domain.entity.product

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Product domain entity representing an inventory item
 */
data class Product(
    val id: UUID? = null,
    val sku: String,
    val name: String,
    val description: String?,
    val categoryId: UUID,
    val supplierId: UUID?,
    val retailPrice: BigDecimal,
    val wholesalePrice: BigDecimal?,
    val costPrice: BigDecimal,
    val stockQuantity: Int,
    val minStockThreshold: Int,
    val brand: String?,
    val status: ProductStatus,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun isLowStock(): Boolean = stockQuantity <= minStockThreshold
    
    fun isOutOfStock(): Boolean = stockQuantity <= 0 || status == ProductStatus.OUT_OF_STOCK
    
    fun isActive(): Boolean = status == ProductStatus.ACTIVE
    
    fun isDiscontinued(): Boolean = status == ProductStatus.DISCONTINUED
    
    fun profitMargin(): BigDecimal {
        return if (costPrice > BigDecimal.ZERO) {
            ((retailPrice - costPrice) / costPrice) * BigDecimal(100)
        } else {
            BigDecimal.ZERO
        }
    }
    
    fun totalValue(): BigDecimal = costPrice * BigDecimal(stockQuantity)
    
    fun canBeSold(): Boolean = isActive() && !isOutOfStock()
    
    fun updateStock(quantity: Int): Product {
        val newQuantity = stockQuantity + quantity
        val newStatus = when {
            newQuantity <= 0 -> ProductStatus.OUT_OF_STOCK
            status == ProductStatus.OUT_OF_STOCK && newQuantity > 0 -> ProductStatus.ACTIVE
            else -> status
        }
        return copy(stockQuantity = newQuantity, status = newStatus)
    }
}
