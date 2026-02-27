package com.emjay.backend.analytics.application.service

import com.emjay.backend.analytics.application.dto.*
import com.emjay.backend.analytics.domain.entity.*
import com.emjay.backend.analytics.domain.repository.*
import com.emjay.backend.ecommerce.domain.repository.order.OrderRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

@Service
class AnalyticsService(
    private val salesAnalyticsRepository: SalesAnalyticsRepository,
    private val productPerformanceRepository: ProductPerformanceRepository,
    private val adminCustomerAnalyticsRepository: AdminCustomerAnalyticsRepository,
    private val dashboardMetricsRepository: DashboardMetricsRepository,
    private val orderRepository: OrderRepository
) {

    // ========== SALES ANALYTICS ==========

    fun getSalesReport(startDate: LocalDate, endDate: LocalDate): SalesReportResponse {
        val analytics = salesAnalyticsRepository.findByDateRange(startDate, endDate)

        val totalRevenue = analytics.sumOf { it.totalRevenue }
        val totalOrders = analytics.sumOf { it.totalOrders }
        val completedOrders = analytics.sumOf { it.completedOrders }
        val cancelledOrders = analytics.sumOf { it.cancelledOrders }
        val newCustomers = analytics.sumOf { it.newCustomers }
        val returningCustomers = analytics.sumOf { it.returningCustomers }

        val avgOrderValue = if (totalOrders > 0) {
            totalRevenue.divide(BigDecimal(totalOrders), 2, RoundingMode.HALF_UP)
        } else BigDecimal.ZERO

        val completionRate = if (totalOrders > 0) {
            (BigDecimal(completedOrders).divide(BigDecimal(totalOrders), 4, RoundingMode.HALF_UP)) * BigDecimal(100)
        } else BigDecimal.ZERO

        val cancellationRate = if (totalOrders > 0) {
            (BigDecimal(cancelledOrders).divide(BigDecimal(totalOrders), 4, RoundingMode.HALF_UP)) * BigDecimal(100)
        } else BigDecimal.ZERO

        val trend = analytics.map {
            SalesTrendData(
                date = it.periodDate,
                revenue = it.totalRevenue,
                orders = it.totalOrders,
                averageOrderValue = it.averageOrderValue
            )
        }

        return SalesReportResponse(
            periodLabel = "${startDate} to ${endDate}",
            totalRevenue = totalRevenue,
            totalOrders = totalOrders,
            averageOrderValue = avgOrderValue,
            completionRate = completionRate,
            cancellationRate = cancellationRate,
            newCustomers = newCustomers,
            returningCustomers = returningCustomers,
            trend = trend
        )
    }

    fun getSalesTrend(period: ReportPeriod, count: Int): List<SalesSummaryResponse> {
        val analytics = salesAnalyticsRepository.findLatestByPeriod(period, count)

        return analytics.map {
            SalesSummaryResponse(
                totalRevenue = it.totalRevenue,
                totalOrders = it.totalOrders,
                completedOrders = it.completedOrders,
                pendingOrders = it.pendingOrders,
                cancelledOrders = it.cancelledOrders,
                averageOrderValue = it.averageOrderValue,
                completionRate = it.getCompletionRate()
            )
        }
    }

    // ========== PRODUCT ANALYTICS ==========

    fun getTopProducts(period: ReportPeriod, limit: Int = 10): TopProductsResponse {
        val startDate = getStartDateForPeriod(period)
        val endDate = LocalDate.now()

        val products = productPerformanceRepository
            .findTopByRevenue(startDate, endDate, limit)

        return TopProductsResponse(
            period = period.name,
            products = products.map {
                ProductPerformanceResponse(
                    productId = it.productId.toString(),
                    productName = "Product ${it.productId}", // TODO: Fetch actual name
                    unitsSold = it.unitsSold,
                    revenue = it.revenue,
                    ordersCount = it.ordersCount,
                    rank = it.revenueRank,
                    trend = "STABLE" // TODO: Calculate trend
                )
            }
        )
    }

    // ========== CUSTOMER ANALYTICS ==========

    fun getCustomerSegmentation(): CustomerSegmentationResponse {
        val analytics = adminCustomerAnalyticsRepository.findAll()

        return CustomerSegmentationResponse(
            vip = analytics.count { it.customerSegment == CustomerSegment.VIP },
            regular = analytics.count { it.customerSegment == CustomerSegment.REGULAR },
            occasional = analytics.count { it.customerSegment == CustomerSegment.OCCASIONAL },
            atRisk = analytics.count { it.customerSegment == CustomerSegment.AT_RISK },
            totalCustomers = analytics.size
        )
    }

    fun getTopCustomers(limit: Int = 10): List<CustomerAnalyticsResponse> {
        return adminCustomerAnalyticsRepository.findTopByTotalSpent(limit).map {
            CustomerAnalyticsResponse(
                customerId = it.customerId.toString(),
                customerName = "Customer ${it.customerId}", // TODO: Fetch actual name
                totalSpent = it.totalSpent,
                totalOrders = it.totalOrders,
                totalBookings = it.totalBookings,
                averageOrderValue = it.averageOrderValue,
                segment = it.customerSegment,
                lifetimeValueTier = it.lifetimeValueTier,
                daysSinceLastPurchase = it.daysSinceLastPurchase,
                isAtRisk = it.isAtRisk()
            )
        }
    }

    // ========== DASHBOARD ==========

    fun getDashboard(): DashboardResponse {
        val today = LocalDate.now()

        // Calculate today's metrics
        val todayMetrics = calculateDailyMetrics(today)
        val monthMetrics = calculateMonthlyMetrics(today)

        return DashboardResponse(
            todayRevenue = todayMetrics.revenue,
            todayOrders = todayMetrics.orders,
            todayBookings = 0, // TODO: Fetch from booking service
            activeCustomers = adminCustomerAnalyticsRepository.findAll().count { it.isActive() },

            monthRevenue = monthMetrics.revenue,
            monthOrders = monthMetrics.orders,
            monthGrowth = calculateGrowth(monthMetrics.revenue, monthMetrics.previousRevenue),

            topProducts = getTopProducts(ReportPeriod.MONTHLY, 5).products,
            recentOrders = emptyList(), // TODO: Fetch recent orders
            lowStockAlerts = emptyList(), // TODO: Fetch low stock products

            metrics = buildDashboardMetrics()
        )
    }

    // ========== SCHEDULED JOBS ==========

    @Scheduled(cron = "0 0 1 * * *") // Daily at 1 AM
    @Transactional
    fun calculateDailySalesAnalytics() {
        val yesterday = LocalDate.now().minusDays(1)

        try {
            val salesData = aggregateSalesData(yesterday, ReportPeriod.DAILY)
            salesAnalyticsRepository.save(salesData)
        } catch (e: Exception) {
            println("Failed to calculate daily sales analytics: ${e.message}")
        }
    }

    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    @Transactional
    fun calculateCustomerAnalytics() {
        try {
            // TODO: Fetch all customers and calculate analytics
            println("Customer analytics calculation scheduled")
        } catch (e: Exception) {
            println("Failed to calculate customer analytics: ${e.message}")
        }
    }

    // ========== HELPER METHODS ==========

    private fun aggregateSalesData(date: LocalDate, period: ReportPeriod): SalesAnalytics {
        // TODO: Implement actual aggregation from orders
        return SalesAnalytics(
            periodDate = date,
            periodType = period,
            totalRevenue = BigDecimal.ZERO,
            totalOrders = 0,
            totalItemsSold = 0,
            averageOrderValue = BigDecimal.ZERO,
            pendingOrders = 0,
            completedOrders = 0,
            cancelledOrders = 0,
            cashPayments = BigDecimal.ZERO,
            cardPayments = BigDecimal.ZERO,
            transferPayments = BigDecimal.ZERO,
            newCustomers = 0,
            returningCustomers = 0,
            uniqueCustomers = 0
        )
    }

    private fun calculateDailyMetrics(date: LocalDate): DailyMetrics {
        // TODO: Calculate actual metrics from database
        return DailyMetrics(
            revenue = BigDecimal.ZERO,
            orders = 0
        )
    }

    private fun calculateMonthlyMetrics(date: LocalDate): MonthlyMetrics {
        // TODO: Calculate actual metrics from database
        return MonthlyMetrics(
            revenue = BigDecimal.ZERO,
            orders = 0,
            previousRevenue = BigDecimal.ZERO
        )
    }

    private fun calculateGrowth(current: BigDecimal, previous: BigDecimal): BigDecimal {
        return if (previous > BigDecimal.ZERO) {
            ((current - previous) / previous) * BigDecimal(100)
        } else BigDecimal.ZERO
    }

    private fun buildDashboardMetrics(): List<DashboardMetricResponse> {
        return dashboardMetricsRepository.findLatest().map {
            DashboardMetricResponse(
                name = it.metricName,
                value = it.metricValue,
                previousValue = it.previousValue,
                changePercentage = it.changePercentage,
                trend = it.getTrend(),
                label = it.periodLabel
            )
        }
    }

    private fun getStartDateForPeriod(period: ReportPeriod): LocalDate {
        val now = LocalDate.now()
        return when (period) {
            ReportPeriod.DAILY -> now.minusDays(30)
            ReportPeriod.WEEKLY -> now.minusWeeks(12)
            ReportPeriod.MONTHLY -> now.minusMonths(12)
            ReportPeriod.QUARTERLY -> now.minusMonths(24)
            ReportPeriod.YEARLY -> now.minusYears(5)
            ReportPeriod.CUSTOM -> now.minusMonths(1)
        }
    }

    // Helper data classes
    private data class DailyMetrics(val revenue: BigDecimal, val orders: Int)
    private data class MonthlyMetrics(val revenue: BigDecimal, val orders: Int, val previousRevenue: BigDecimal)
}