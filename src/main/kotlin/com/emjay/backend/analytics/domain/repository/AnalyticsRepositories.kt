package com.emjay.backend.analytics.domain.repository

import com.emjay.backend.analytics.domain.entity.*
import java.time.LocalDate
import java.util.*

// ========== SALES ANALYTICS REPOSITORY ==========

interface SalesAnalyticsRepository {
    fun save(analytics: SalesAnalytics): SalesAnalytics
    fun findById(id: UUID): SalesAnalytics?
    fun findByDateAndPeriod(date: LocalDate, period: ReportPeriod): SalesAnalytics?
    fun findByDateRange(startDate: LocalDate, endDate: LocalDate): List<SalesAnalytics>
    fun findLatestByPeriod(period: ReportPeriod, limit: Int): List<SalesAnalytics>
}

// ========== PRODUCT PERFORMANCE REPOSITORY ==========

interface ProductPerformanceRepository {
    fun save(performance: ProductPerformance): ProductPerformance
    fun findById(id: UUID): ProductPerformance?
    fun findByProductIdAndPeriod(productId: UUID, period: ReportPeriod): List<ProductPerformance>
    fun findTopByRevenue(startDate: LocalDate, endDate: LocalDate, limit: Int): List<ProductPerformance>
    fun findByDateRange(startDate: LocalDate, endDate: LocalDate): List<ProductPerformance>
}

// ========== CUSTOMER ANALYTICS REPOSITORY ==========

interface AdminCustomerAnalyticsRepository {
    fun save(analytics: CustomerAnalytics): CustomerAnalytics
    fun findById(id: UUID): CustomerAnalytics?
    fun findByCustomerId(customerId: UUID): CustomerAnalytics?
    fun findAll(): List<CustomerAnalytics>
    fun findBySegment(segment: CustomerSegment): List<CustomerAnalytics>
    fun findTopByTotalSpent(limit: Int): List<CustomerAnalytics>
    fun findAtRiskCustomers(): List<CustomerAnalytics>
}

// ========== BOOKING ANALYTICS REPOSITORY ==========

interface BookingAnalyticsRepository {
    fun save(analytics: BookingAnalytics): BookingAnalytics
    fun findById(id: UUID): BookingAnalytics?
    fun findByDateAndPeriod(date: LocalDate, period: ReportPeriod): BookingAnalytics?
    fun findByDateRange(startDate: LocalDate, endDate: LocalDate): List<BookingAnalytics>
    fun findLatestByPeriod(period: ReportPeriod, limit: Int): List<BookingAnalytics>
}

// ========== INVENTORY ANALYTICS REPOSITORY ==========

interface InventoryAnalyticsRepository {
    fun save(analytics: InventoryAnalytics): InventoryAnalytics
    fun findById(id: UUID): InventoryAnalytics?
    fun findByDate(date: LocalDate): InventoryAnalytics?
    fun findLatest(limit: Int): List<InventoryAnalytics>
}

// ========== STAFF PERFORMANCE REPOSITORY ==========

interface StaffPerformanceRepository {
    fun save(performance: StaffPerformance): StaffPerformance
    fun findById(id: UUID): StaffPerformance?
    fun findByStaffIdAndPeriod(staffId: UUID, period: ReportPeriod): List<StaffPerformance>
    fun findTopByRevenue(startDate: LocalDate, endDate: LocalDate, limit: Int): List<StaffPerformance>
}

// ========== DASHBOARD METRICS REPOSITORY ==========

interface DashboardMetricsRepository {
    fun save(metric: DashboardMetric): DashboardMetric
    fun findByNameAndPeriod(name: String, periodLabel: String): DashboardMetric?
    fun findByType(type: MetricType): List<DashboardMetric>
    fun findLatest(): List<DashboardMetric>
}