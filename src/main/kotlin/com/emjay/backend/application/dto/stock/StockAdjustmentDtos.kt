package com.emjay.backend.application.dto.stock

import com.emjay.backend.domain.entity.inventory.AdjustmentType
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

// Request DTOs
data class CreateStockAdjustmentRequest(
    @field:NotNull(message = "Product ID is required")
    val productId: UUID,

    @field:NotNull(message = "Adjustment type is required")
    val adjustmentType: AdjustmentType,

    @field:NotNull(message = "Quantity is required")
    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int,

    val reason: String? = null,


    @field:DecimalMin(value = "0.0", inclusive = false, message = "Sale price must be greater than 0")
    val salePrice: BigDecimal? = null // Price per unit for sales
)

// Response DTOs
data class StockAdjustmentResponse(
    val id: String,
    val productId: String,
    val productName: String,
    val productSku: String,
    val userId: String,
    val userName: String,
    val adjustmentType: AdjustmentType,
    val quantity: Int,
    val previousQuantity: Int,
    val newQuantity: Int,
    val reason: String?,
    val salePrice: BigDecimal?,
    val totalSaleAmount: BigDecimal?,
    val createdAt: LocalDateTime
)

data class StockAdjustmentPageResponse(
    val content: List<StockAdjustmentResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)

data class StockHistorySummary(
    val productId: String,
    val productName: String,
    val currentStock: Int,
    val totalAdditions: Int,
    val totalDeductions: Int,
    val totalSales: Int,
    val totalReturns: Int,
    val totalRevenue: BigDecimal,
    val adjustments: List<StockAdjustmentResponse>
)