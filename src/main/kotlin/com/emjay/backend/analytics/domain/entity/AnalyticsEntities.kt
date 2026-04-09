package com.emjay.backend.analytics.domain.entity

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

// ========== ENUMS ==========

enum class ReportType {
    SALES_SUMMARY,
    REVENUE_ANALYSIS,
    PRODUCT_PERFORMANCE,
    CUSTOMER_INSIGHTS,
    BOOKING_ANALYTICS,
    INVENTORY_STATUS,
    STAFF_PERFORMANCE,
    CUSTOM
}

enum class ReportPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    YEARLY,
    CUSTOM
}

enum class MetricType {
    REVENUE,
    ORDERS,
    BOOKINGS,
    CUSTOMERS,
    PRODUCTS_SOLD,
    AVERAGE_ORDER_VALUE,
    CONVERSION_RATE,
    CUSTOMER_LIFETIME_VALUE
}

enum class CustomerSegment {
    VIP,        // High value, frequent purchases
    REGULAR,    // Moderate purchases
    OCCASIONAL, // Infrequent purchases
    AT_RISK     // Haven't purchased recently
}

enum class ValueTier {
    HIGH,
    MEDIUM,
    LOW
}

// ========== SALES ANALYTICS ==========

data class SalesAnalytics(
    val id: UUID? = null,
    val periodDate: LocalDate,
    val periodType: ReportPeriod,

    // Sales metrics
    val totalRevenue: BigDecimal,
    val totalOrders: Int,
    val totalItemsSold: Int,
    val averageOrderValue: BigDecimal,

    // Order breakdown
    val pendingOrders: Int,
    val completedOrders: Int,
    val cancelledOrders: Int,

    // Payment breakdown
    val cashPayments: BigDecimal,
    val cardPayments: BigDecimal,
    val transferPayments: BigDecimal,

    // Customer metrics
    val newCustomers: Int,
    val returningCustomers: Int,
    val uniqueCustomers: Int,

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun getCompletionRate(): BigDecimal {
        return if (totalOrders > 0) {
            (completedOrders.toBigDecimal() / totalOrders.toBigDecimal()) * BigDecimal(100)
        } else BigDecimal.ZERO
    }

    fun getCancellationRate(): BigDecimal {
        return if (totalOrders > 0) {
            (cancelledOrders.toBigDecimal() / totalOrders.toBigDecimal()) * BigDecimal(100)
        } else BigDecimal.ZERO
    }
}

// ========== PRODUCT PERFORMANCE ==========

data class ProductPerformance(
    val id: UUID? = null,
    val productId: UUID,
    val periodDate: LocalDate,
    val periodType: ReportPeriod,

    val unitsSold: Int,
    val revenue: BigDecimal,
    val ordersCount: Int,
    val averageUnitPrice: BigDecimal,

    val revenueRank: Int? = null,
    val unitsRank: Int? = null,

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun isTopPerformer(): Boolean = revenueRank != null && revenueRank <= 10
}

// ========== CUSTOMER ANALYTICS ==========

data class CustomerAnalytics(
    val id: UUID? = null,
    val customerId: UUID,

    // Lifetime metrics
    val totalOrders: Int,
    val totalBookings: Int,
    val totalSpent: BigDecimal,
    val averageOrderValue: BigDecimal,

    // Engagement
    val firstPurchaseDate: LocalDate? = null,
    val lastPurchaseDate: LocalDate? = null,
    val daysSinceLastPurchase: Int? = null,
    val purchaseFrequency: BigDecimal? = null,

    // Segmentation
    val customerSegment: CustomerSegment? = null,
    val lifetimeValueTier: ValueTier? = null,

    val calculatedAt: LocalDateTime? = null
) {
    fun isActive(): Boolean {
        return daysSinceLastPurchase != null && daysSinceLastPurchase < 90
    }

    fun isAtRisk(): Boolean {
        return daysSinceLastPurchase != null && daysSinceLastPurchase > 180
    }

    fun getLifetimeValue(): BigDecimal = totalSpent
}

// ========== BOOKING ANALYTICS ==========

data class BookingAnalytics(
    val id: UUID? = null,
    val periodDate: LocalDate,
    val periodType: ReportPeriod,

    // Booking metrics
    val totalBookings: Int,
    val completedBookings: Int,
    val cancelledBookings: Int,
    val noShowBookings: Int,

    // Revenue
    val totalRevenue: BigDecimal,
    val averageBookingValue: BigDecimal,

    // Popular service
    val mostPopularServiceId: UUID? = null,
    val mostPopularServiceName: String? = null,
    val mostPopularServiceBookings: Int? = null,

    // Top staff
    val mostProductiveStaffId: UUID? = null,
    val mostProductiveStaffBookings: Int? = null,

    // Efficiency
    val occupancyRate: BigDecimal? = null,
    val cancellationRate: BigDecimal? = null,
    val noShowRate: BigDecimal? = null,

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun getCompletionRate(): BigDecimal {
        return if (totalBookings > 0) {
            (completedBookings.toBigDecimal() / totalBookings.toBigDecimal()) * BigDecimal(100)
        } else BigDecimal.ZERO
    }
}

// ========== INVENTORY ANALYTICS ==========

data class InventoryAnalytics(
    val id: UUID? = null,
    val snapshotDate: LocalDate,

    // Inventory metrics
    val totalProducts: Int,
    val totalStockValue: BigDecimal,
    val lowStockProducts: Int,
    val outOfStockProducts: Int,

    // Stock movement
    val productsAdded: Int,
    val productsSold: Int,
    val stockAdjustments: Int,

    // Fast mover
    val fastestMovingProductId: UUID? = null,
    val fastestMovingProductName: String? = null,
    val fastestMovingUnits: Int? = null,

    // Slow mover
    val slowestMovingProductId: UUID? = null,
    val slowestMovingProductName: String? = null,
    val slowestMovingDaysInStock: Int? = null,

    val createdAt: LocalDateTime? = null
) {
    fun getStockHealthScore(): Int {
        val outOfStockPenalty = outOfStockProducts * 10
        val lowStockPenalty = lowStockProducts * 5
        return maxOf(0, 100 - outOfStockPenalty - lowStockPenalty)
    }
}

// ========== STAFF PERFORMANCE ==========

data class StaffPerformance(
    val id: UUID? = null,
    val staffId: UUID,
    val periodDate: LocalDate,
    val periodType: ReportPeriod,

    val totalBookings: Int,
    val completedBookings: Int,
    val cancelledBookings: Int,

    val totalRevenue: BigDecimal,
    val averageBookingValue: BigDecimal,

    val completionRate: BigDecimal? = null,
    val customerSatisfactionScore: BigDecimal? = null,
    val revenueRank: Int? = null,

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun isTopPerformer(): Boolean = revenueRank != null && revenueRank <= 3
}

// ========== DASHBOARD METRICS ==========

data class DashboardMetric(
    val id: UUID? = null,
    val metricName: String,
    val metricValue: BigDecimal,
    val metricType: MetricType,

    val previousValue: BigDecimal? = null,
    val changePercentage: BigDecimal? = null,

    val periodLabel: String? = null,
    val calculatedAt: LocalDateTime? = null
) {
    fun getTrend(): String {
        return when {
            changePercentage == null -> "STABLE"
            changePercentage > BigDecimal.ZERO -> "UP"
            changePercentage < BigDecimal.ZERO -> "DOWN"
            else -> "STABLE"
        }
    }

    fun isImproving(): Boolean {
        return changePercentage != null && changePercentage > BigDecimal.ZERO
    }
}