package com.emjay.backend.analytics.presentation.controller
import com.emjay.backend.analytics.application.dto.*
import com.emjay.backend.analytics.application.service.AnalyticsService
import com.emjay.backend.analytics.domain.entity.ReportPeriod
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "Business intelligence and analytics")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@SecurityRequirement(name = "bearerAuth")
class AnalyticsController(
    private val analyticsService: AnalyticsService
) {

    // ========== DASHBOARD ==========

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard overview")
    fun getDashboard(): ResponseEntity<DashboardResponse> {
        val dashboard = analyticsService.getDashboard()
        return ResponseEntity.ok(dashboard)
    }

    // ========== SALES ANALYTICS ==========

    @GetMapping("/sales/by-day")
    @Operation(summary = "Get sales breakdown by day of week")
    fun getSalesByDayOfWeek(): ResponseEntity<List<DaySalesResponse>> {
        val data = analyticsService.getSalesByDayOfWeek()
        return ResponseEntity.ok(data)
    }

    @GetMapping("/sales/summary")
    @Operation(summary = "Get sales summary for date range")
    fun getSalesSummary(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<SalesReportResponse> {
        val report = analyticsService.getSalesReport(startDate, endDate)
        return ResponseEntity.ok(report)
    }

    @GetMapping("/sales/trend")
    @Operation(summary = "Get sales trend")
    fun getSalesTrend(
        @RequestParam(defaultValue = "MONTHLY") period: ReportPeriod,
        @RequestParam(defaultValue = "12") count: Int
    ): ResponseEntity<List<SalesSummaryResponse>> {
        val trend = analyticsService.getSalesTrend(period, count)
        return ResponseEntity.ok(trend)
    }

    // ========== PRODUCT ANALYTICS ==========

    @GetMapping("/products/top")
    @Operation(summary = "Get top performing products")
    fun getTopProducts(
        @RequestParam(defaultValue = "MONTHLY") period: ReportPeriod,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<TopProductsResponse> {
        val topProducts = analyticsService.getTopProducts(period, limit)
        return ResponseEntity.ok(topProducts)
    }

    // ========== CUSTOMER ANALYTICS ==========

    @GetMapping("/customers/segmentation")
    @Operation(summary = "Get customer segmentation")
    fun getCustomerSegmentation(): ResponseEntity<CustomerSegmentationResponse> {
        val segmentation = analyticsService.getCustomerSegmentation()
        return ResponseEntity.ok(segmentation)
    }

    @GetMapping("/customers/top")
    @Operation(summary = "Get top customers by lifetime value")
    fun getTopCustomers(
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<CustomerAnalyticsResponse>> {
        val topCustomers = analyticsService.getTopCustomers(limit)
        return ResponseEntity.ok(topCustomers)
    }
}