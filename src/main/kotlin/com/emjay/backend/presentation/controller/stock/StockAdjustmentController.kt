package com.emjay.backend.presentation.controller.stock

import com.emjay.backend.application.dto.stock.*
import com.emjay.backend.application.service.StockAdjustmentService
import com.emjay.backend.domain.entity.inventory.AdjustmentType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/stock-adjustments")
@Tag(name = "Stock Adjustments", description = "Endpoints for managing inventory stock adjustments")
@SecurityRequirement(name = "bearerAuth")
class StockAdjustmentController(
    private val stockAdjustmentService: StockAdjustmentService
) {

    @PostMapping
    @Operation(summary = "Create stock adjustment (Admin/Manager/Staff)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    fun createStockAdjustment(
        @Valid @RequestBody request: CreateStockAdjustmentRequest
    ): ResponseEntity<StockAdjustmentResponse> {
        val response = stockAdjustmentService.createStockAdjustment(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(summary = "Get all stock adjustments (paginated)")
    fun getAllStockAdjustments(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<StockAdjustmentPageResponse> {
        val response = stockAdjustmentService.getAllStockAdjustments(page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{adjustmentId}")
    @Operation(summary = "Get stock adjustment by ID")
    fun getStockAdjustmentById(@PathVariable adjustmentId: UUID): ResponseEntity<StockAdjustmentResponse> {
        val response = stockAdjustmentService.getStockAdjustmentById(adjustmentId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get stock adjustment history for a product")
    fun getProductStockHistory(
        @PathVariable productId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<StockAdjustmentPageResponse> {
        val response = stockAdjustmentService.getProductStockHistory(productId, page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/product/{productId}/summary")
    @Operation(summary = "Get stock summary for a product")
    fun getProductStockSummary(@PathVariable productId: UUID): ResponseEntity<StockHistorySummary> {
        val response = stockAdjustmentService.getProductStockSummary(productId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get stock adjustments by user")
    fun getUserStockAdjustments(
        @PathVariable userId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<StockAdjustmentPageResponse> {
        val response = stockAdjustmentService.getUserStockAdjustments(userId, page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get stock adjustments by type")
    fun getAdjustmentsByType(
        @PathVariable type: AdjustmentType,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<StockAdjustmentPageResponse> {
        val response = stockAdjustmentService.getAdjustmentsByType(type, page, size)
        return ResponseEntity.ok(response)
    }
}