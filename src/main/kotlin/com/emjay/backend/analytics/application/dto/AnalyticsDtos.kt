package com.emjay.backend.analytics.application.dto

import com.emjay.backend.analytics.domain.entity.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

// ========== REQUEST DTOs ==========

data class AnalyticsDateRangeRequest(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val period: ReportPeriod = ReportPeriod.DAILY
)

// ========== RESPONSE DTOs ==========

// Sales Analytics
data class SalesReportResponse(
    val periodLabel: String,
    val totalRevenue: BigDecimal,
    val totalOrders: Int,
    val averageOrderValue: BigDecimal,
    val completionRate: BigDecimal,
    val cancellationRate: BigDecimal,
    val newCustomers: Int,
    val returningCustomers: Int,
    val trend: List<SalesTrendData>
)

data class SalesTrendData(
    val date: LocalDate,
    val revenue: BigDecimal,
    val orders: Int,
    val averageOrderValue: BigDecimal
)

data class SalesSummaryResponse(
    val totalRevenue: BigDecimal,
    val totalOrders: Int,
    val completedOrders: Int,
    val pendingOrders: Int,
    val cancelledOrders: Int,
    val averageOrderValue: BigDecimal,
    val completionRate: BigDecimal
)

// Product Analytics
data class ProductPerformanceResponse(
    val productId: String,
    val productName: String,
    val unitsSold: Int,
    val revenue: BigDecimal,
    val ordersCount: Int,
    val rank: Int?,
    val trend: String
)

data class TopProductsResponse(
    val period: String,
    val products: List<ProductPerformanceResponse>
)

// Customer Analytics
data class CustomerAnalyticsResponse(
    val customerId: String,
    val customerName: String,
    val totalSpent: BigDecimal,
    val totalOrders: Int,
    val totalBookings: Int,
    val averageOrderValue: BigDecimal,
    val segment: CustomerSegment?,
    val lifetimeValueTier: ValueTier?,
    val daysSinceLastPurchase: Int?,
    val isAtRisk: Boolean
)

data class CustomerSegmentationResponse(
    val vip: Int,
    val regular: Int,
    val occasional: Int,
    val atRisk: Int,
    val totalCustomers: Int
)

// Dashboard
data class DashboardResponse(
    val todayRevenue: BigDecimal,
    val todayOrders: Int,
    val todayBookings: Int,
    val activeCustomers: Int,

    val monthRevenue: BigDecimal,
    val monthOrders: Int,
    val monthGrowth: BigDecimal,

    val topProducts: List<ProductPerformanceResponse>,
    val recentOrders: List<OrderSummaryResponse>,
    val lowStockAlerts: List<ProductAlertResponse>,

    val metrics: List<DashboardMetricResponse>
)

data class DashboardMetricResponse(
    val name: String,
    val value: BigDecimal,
    val previousValue: BigDecimal?,
    val changePercentage: BigDecimal?,
    val trend: String, // UP, DOWN, STABLE
    val label: String?
)

data class OrderSummaryResponse(
    val orderId: String,
    val orderNumber: String,
    val customerName: String,
    val totalAmount: BigDecimal,
    val status: String,
    val orderedAt: String
)

data class ProductAlertResponse(
    val productId: String,
    val productName: String,
    val stockLevel: Int,
    val alertType: String // LOW_STOCK, OUT_OF_STOCK
)

// Real-time Metrics
data class RealTimeMetricsResponse(
    val currentRevenue: BigDecimal,
    val currentOrders: Int,
    val currentBookings: Int,
    val onlineCustomers: Int,
    val pendingPayments: Int
)