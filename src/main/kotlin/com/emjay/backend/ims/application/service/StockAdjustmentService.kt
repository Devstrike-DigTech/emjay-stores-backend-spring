package com.emjay.backend.ims.application.service

import com.emjay.backend.ims.application.dto.stock.*
import com.emjay.backend.ims.domain.entity.inventory.AdjustmentType
import com.emjay.backend.ims.domain.entity.inventory.StockAdjustment
import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.ims.domain.repository.inventory.StockAdjustmentRepository
import com.emjay.backend.ims.domain.repository.product.ProductRepository
import com.emjay.backend.common.domain.repository.user.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class StockAdjustmentService(
    private val stockAdjustmentRepository: StockAdjustmentRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createStockAdjustment(request: CreateStockAdjustmentRequest): StockAdjustmentResponse {
        // Get product
        val product = productRepository.findById(request.productId)
            ?: throw ResourceNotFoundException("Product not found")

        // Validate sale price for SALE adjustments
        if (request.adjustmentType == AdjustmentType.SALE && request.salePrice == null) {
            throw IllegalArgumentException("Sale price is required for sales")
        }

        // Get current user - JWT subject is user ID
        val authentication = SecurityContextHolder.getContext().authentication
        val userIdString = authentication.name
        val userId = UUID.fromString(userIdString)
        val user = userRepository.findById(userId)
            ?: throw ResourceNotFoundException("User not found")

        // Calculate new quantity based on adjustment type
        val previousQuantity = product.stockQuantity
        val newQuantity = when (request.adjustmentType) {
            AdjustmentType.ADDITION, AdjustmentType.RETURN -> previousQuantity + request.quantity
            AdjustmentType.DEDUCTION, AdjustmentType.SALE -> {
                if (previousQuantity < request.quantity) {
                    throw IllegalArgumentException("Insufficient stock. Available: $previousQuantity, Requested: ${request.quantity}")
                }
                previousQuantity - request.quantity
            }
        }

        // Create stock adjustment record
        val stockAdjustment = StockAdjustment(
            productId = product.id!!,
            userId = user.id!!,
            adjustmentType = request.adjustmentType,
            quantity = request.quantity,
            previousQuantity = previousQuantity,
            newQuantity = newQuantity,
            reason = request.reason,
            salePrice = request.salePrice
        )

        val savedAdjustment = stockAdjustmentRepository.save(stockAdjustment)

        // Update product stock quantity
        val updatedProduct = product.copy(stockQuantity = newQuantity)
        productRepository.save(updatedProduct)

        return toStockAdjustmentResponse(savedAdjustment, product.name, product.sku, user.username)
    }

    fun getStockAdjustmentById(adjustmentId: UUID): StockAdjustmentResponse {
        val adjustment = stockAdjustmentRepository.findById(adjustmentId)
            ?: throw ResourceNotFoundException("Stock adjustment not found")

        val product = productRepository.findById(adjustment.productId)
            ?: throw ResourceNotFoundException("Product not found")

        val user = userRepository.findById(adjustment.userId)
            ?: throw ResourceNotFoundException("User not found")

        return toStockAdjustmentResponse(adjustment, product.name, product.sku, user.username)
    }

    fun getAllStockAdjustments(page: Int = 0, size: Int = 20): StockAdjustmentPageResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        // Get all adjustments from the beginning of time to now
        val now = java.time.LocalDateTime.now()
        val farPast = java.time.LocalDateTime.of(2000, 1, 1, 0, 0)
        val adjustmentsPage = stockAdjustmentRepository.findByDateRange(farPast, now, pageable)
        return toStockAdjustmentPageResponse(adjustmentsPage)
    }

    fun getProductStockHistory(productId: UUID, page: Int = 0, size: Int = 20): StockAdjustmentPageResponse {
        // Verify product exists
        productRepository.findById(productId)
            ?: throw ResourceNotFoundException("Product not found")

        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val adjustmentsPage = stockAdjustmentRepository.findByProductId(productId, pageable)
        return toStockAdjustmentPageResponse(adjustmentsPage)
    }

    fun getProductStockSummary(productId: UUID): StockHistorySummary {
        val product = productRepository.findById(productId)
            ?: throw ResourceNotFoundException("Product not found")

        val totalAdditions = stockAdjustmentRepository.sumQuantityByProductIdAndType(productId, AdjustmentType.ADDITION).toInt()
        val totalDeductions = stockAdjustmentRepository.sumQuantityByProductIdAndType(productId, AdjustmentType.DEDUCTION).toInt()
        val totalSales = stockAdjustmentRepository.sumQuantityByProductIdAndType(productId, AdjustmentType.SALE).toInt()
        val totalReturns = stockAdjustmentRepository.sumQuantityByProductIdAndType(productId, AdjustmentType.RETURN).toInt()

        val adjustments = stockAdjustmentRepository.findByProductId(productId, PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt")))
        val adjustmentResponses = adjustments.content.map {
            val user = userRepository.findById(it.userId) ?: throw ResourceNotFoundException("User not found")
            toStockAdjustmentResponse(it, product.name, product.sku, user.username)
        }

        // Calculate total revenue from sales
        val totalRevenue = adjustments.content
            .filter { it.adjustmentType == AdjustmentType.SALE }
            .mapNotNull { it.totalSaleAmount() }
            .fold(java.math.BigDecimal.ZERO) { acc, amount -> acc.add(amount) }

        return StockHistorySummary(
            productId = productId.toString(),
            productName = product.name,
            currentStock = product.stockQuantity,
            totalAdditions = totalAdditions,
            totalDeductions = totalDeductions,
            totalSales = totalSales,
            totalReturns = totalReturns,
            totalRevenue = totalRevenue,
            adjustments = adjustmentResponses
        )
    }

    fun getUserStockAdjustments(userId: UUID, page: Int = 0, size: Int = 20): StockAdjustmentPageResponse {
        // Verify user exists
        userRepository.findById(userId)
            ?: throw ResourceNotFoundException("User not found")

        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val adjustmentsPage = stockAdjustmentRepository.findByUserId(userId, pageable)
        return toStockAdjustmentPageResponse(adjustmentsPage)
    }

    fun getAdjustmentsByType(type: AdjustmentType, page: Int = 0, size: Int = 20): StockAdjustmentPageResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val adjustmentsPage = stockAdjustmentRepository.findByAdjustmentType(type, pageable)
        return toStockAdjustmentPageResponse(adjustmentsPage)
    }

    private fun toStockAdjustmentResponse(
        adjustment: StockAdjustment,
        productName: String,
        productSku: String,
        userName: String
    ): StockAdjustmentResponse {
        return StockAdjustmentResponse(
            id = adjustment.id.toString(),
            productId = adjustment.productId.toString(),
            productName = productName,
            productSku = productSku,
            userId = adjustment.userId.toString(),
            userName = userName,
            adjustmentType = adjustment.adjustmentType,
            quantity = adjustment.quantity,
            previousQuantity = adjustment.previousQuantity,
            newQuantity = adjustment.newQuantity,
            reason = adjustment.reason,
            salePrice = adjustment.salePrice,
            totalSaleAmount = adjustment.totalSaleAmount(),
            createdAt = adjustment.createdAt!!
        )
    }

    private fun toStockAdjustmentPageResponse(page: Page<StockAdjustment>): StockAdjustmentPageResponse {
        val responses = page.content.map { adjustment ->
            val product = productRepository.findById(adjustment.productId)
                ?: throw ResourceNotFoundException("Product not found")
            val user = userRepository.findById(adjustment.userId)
                ?: throw ResourceNotFoundException("User not found")
            toStockAdjustmentResponse(adjustment, product.name, product.sku, user.username)
        }

        return StockAdjustmentPageResponse(
            content = responses,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
            pageSize = page.size
        )
    }
}