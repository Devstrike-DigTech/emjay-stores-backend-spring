package com.emjay.backend.analytics.infrastructure.persistence.repository

import com.emjay.backend.analytics.domain.entity.*
import com.emjay.backend.analytics.domain.repository.*
import com.emjay.backend.analytics.infrastructure.persistence.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

// ========== JPA REPOSITORIES ==========

@Repository
interface JpaSalesAnalyticsRepository : JpaRepository<SalesAnalyticsEntity, UUID> {
    fun findByPeriodDateAndPeriodType(date: LocalDate, period: ReportPeriod): SalesAnalyticsEntity?

    @Query("SELECT s FROM SalesAnalyticsEntity s WHERE s.periodDate >= :startDate AND s.periodDate <= :endDate ORDER BY s.periodDate")
    fun findByDateRange(@Param("startDate") startDate: LocalDate, @Param("endDate") endDate: LocalDate): List<SalesAnalyticsEntity>

    fun findByPeriodTypeOrderByPeriodDateDesc(period: ReportPeriod): List<SalesAnalyticsEntity>
}

@Repository
interface JpaProductPerformanceRepository : JpaRepository<ProductPerformanceEntity, UUID> {
    fun findByProductIdAndPeriodType(productId: UUID, period: ReportPeriod): List<ProductPerformanceEntity>

    @Query("SELECT p FROM ProductPerformanceEntity p WHERE p.periodDate >= :startDate AND p.periodDate <= :endDate ORDER BY p.revenue DESC")
    fun findTopByRevenue(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<ProductPerformanceEntity>

    @Query("SELECT p FROM ProductPerformanceEntity p WHERE p.periodDate >= :startDate AND p.periodDate <= :endDate")
    fun findByDateRange(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<ProductPerformanceEntity>
}

@Repository
interface JpaAdminCustomerAnalyticsRepository : JpaRepository<AdminCustomerAnalyticsEntity, UUID> {
    fun findByCustomerId(customerId: UUID): AdminCustomerAnalyticsEntity?

    @Query("SELECT c FROM AdminCustomerAnalyticsEntity c WHERE c.customerSegment = :segment")
    fun findBySegment(@Param("segment") segment: String): List<AdminCustomerAnalyticsEntity>

    @Query("SELECT c FROM AdminCustomerAnalyticsEntity c ORDER BY c.totalSpent DESC")
    fun findTopByTotalSpent(): List<AdminCustomerAnalyticsEntity>

    @Query("SELECT c FROM AdminCustomerAnalyticsEntity c WHERE c.daysSinceLastPurchase > 180")
    fun findAtRisk(): List<AdminCustomerAnalyticsEntity>
}

@Repository
interface JpaDashboardMetricsRepository : JpaRepository<DashboardMetricEntity, UUID> {
    fun findByMetricNameAndPeriodLabel(name: String, periodLabel: String): DashboardMetricEntity?
    fun findByMetricType(type: MetricType): List<DashboardMetricEntity>
    fun findTop10ByOrderByCalculatedAtDesc(): List<DashboardMetricEntity>
}

// ========== REPOSITORY IMPLEMENTATIONS ==========

@Repository
class SalesAnalyticsRepositoryImpl(
    private val jpaRepository: JpaSalesAnalyticsRepository
) : SalesAnalyticsRepository {

    override fun save(analytics: SalesAnalytics) = toDomain(jpaRepository.save(toEntity(analytics)))
    override fun findById(id: UUID) = jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    override fun findByDateAndPeriod(date: LocalDate, period: ReportPeriod) =
        jpaRepository.findByPeriodDateAndPeriodType(date, period)?.let { toDomain(it) }
    override fun findByDateRange(startDate: LocalDate, endDate: LocalDate) =
        jpaRepository.findByDateRange(startDate, endDate).map { toDomain(it) }
    override fun findLatestByPeriod(period: ReportPeriod, limit: Int) =
        jpaRepository.findByPeriodTypeOrderByPeriodDateDesc(period).take(limit).map { toDomain(it) }

    private fun toDomain(entity: SalesAnalyticsEntity) = SalesAnalytics(
        id = entity.id, periodDate = entity.periodDate, periodType = entity.periodType,
        totalRevenue = entity.totalRevenue, totalOrders = entity.totalOrders,
        totalItemsSold = entity.totalItemsSold, averageOrderValue = entity.averageOrderValue,
        pendingOrders = entity.pendingOrders, completedOrders = entity.completedOrders,
        cancelledOrders = entity.cancelledOrders, cashPayments = entity.cashPayments,
        cardPayments = entity.cardPayments, transferPayments = entity.transferPayments,
        newCustomers = entity.newCustomers, returningCustomers = entity.returningCustomers,
        uniqueCustomers = entity.uniqueCustomers, createdAt = entity.createdAt, updatedAt = entity.updatedAt
    )

    private fun toEntity(domain: SalesAnalytics) = SalesAnalyticsEntity(
        id = domain.id, periodDate = domain.periodDate, periodType = domain.periodType,
        totalRevenue = domain.totalRevenue, totalOrders = domain.totalOrders,
        totalItemsSold = domain.totalItemsSold, averageOrderValue = domain.averageOrderValue,
        pendingOrders = domain.pendingOrders, completedOrders = domain.completedOrders,
        cancelledOrders = domain.cancelledOrders, cashPayments = domain.cashPayments,
        cardPayments = domain.cardPayments, transferPayments = domain.transferPayments,
        newCustomers = domain.newCustomers, returningCustomers = domain.returningCustomers,
        uniqueCustomers = domain.uniqueCustomers
    )
}

@Repository
class ProductPerformanceRepositoryImpl(
    private val jpaRepository: JpaProductPerformanceRepository
) : ProductPerformanceRepository {

    override fun save(performance: ProductPerformance) = toDomain(jpaRepository.save(toEntity(performance)))
    override fun findById(id: UUID) = jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    override fun findByProductIdAndPeriod(productId: UUID, period: ReportPeriod) =
        jpaRepository.findByProductIdAndPeriodType(productId, period).map { toDomain(it) }
    override fun findTopByRevenue(startDate: LocalDate, endDate: LocalDate, limit: Int) =
        jpaRepository.findTopByRevenue(startDate, endDate).take(limit).map { toDomain(it) }
    override fun findByDateRange(startDate: LocalDate, endDate: LocalDate) =
        jpaRepository.findByDateRange(startDate, endDate).map { toDomain(it) }

    private fun toDomain(entity: ProductPerformanceEntity) = ProductPerformance(
        id = entity.id, productId = entity.productId, periodDate = entity.periodDate,
        periodType = entity.periodType, unitsSold = entity.unitsSold, revenue = entity.revenue,
        ordersCount = entity.ordersCount, averageUnitPrice = entity.averageUnitPrice,
        revenueRank = entity.revenueRank, unitsRank = entity.unitsRank,
        createdAt = entity.createdAt, updatedAt = entity.updatedAt
    )

    private fun toEntity(domain: ProductPerformance) = ProductPerformanceEntity(
        id = domain.id, productId = domain.productId, periodDate = domain.periodDate,
        periodType = domain.periodType, unitsSold = domain.unitsSold, revenue = domain.revenue,
        ordersCount = domain.ordersCount, averageUnitPrice = domain.averageUnitPrice,
        revenueRank = domain.revenueRank, unitsRank = domain.unitsRank
    )
}

@Repository
class AdminAdminCustomerAnalyticsRepositoryImpl(
    private val jpaRepository: JpaAdminCustomerAnalyticsRepository
) : AdminCustomerAnalyticsRepository {

    override fun save(analytics: CustomerAnalytics) = toDomain(jpaRepository.save(toEntity(analytics)))
    override fun findById(id: UUID) = jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    override fun findByCustomerId(customerId: UUID) =
        jpaRepository.findByCustomerId(customerId)?.let { toDomain(it) }
    override fun findAll() = jpaRepository.findAll().map { toDomain(it) }
    override fun findBySegment(segment: CustomerSegment) =
        jpaRepository.findBySegment(segment.name).map { toDomain(it) }
    override fun findTopByTotalSpent(limit: Int) =
        jpaRepository.findTopByTotalSpent().take(limit).map { toDomain(it) }
    override fun findAtRiskCustomers() = jpaRepository.findAtRisk().map { toDomain(it) }

    private fun toDomain(entity: AdminCustomerAnalyticsEntity) = CustomerAnalytics(
        id = entity.id, customerId = entity.customerId, totalOrders = entity.totalOrders,
        totalBookings = entity.totalBookings, totalSpent = entity.totalSpent,
        averageOrderValue = entity.averageOrderValue, firstPurchaseDate = entity.firstPurchaseDate,
        lastPurchaseDate = entity.lastPurchaseDate, daysSinceLastPurchase = entity.daysSinceLastPurchase,
        purchaseFrequency = entity.purchaseFrequency,
        customerSegment = entity.customerSegment?.let { CustomerSegment.valueOf(it) },
        lifetimeValueTier = entity.lifetimeValueTier?.let { ValueTier.valueOf(it) },
        calculatedAt = entity.calculatedAt
    )

    private fun toEntity(domain: CustomerAnalytics) = AdminCustomerAnalyticsEntity(
        id = domain.id, customerId = domain.customerId, totalOrders = domain.totalOrders,
        totalBookings = domain.totalBookings, totalSpent = domain.totalSpent,
        averageOrderValue = domain.averageOrderValue, firstPurchaseDate = domain.firstPurchaseDate,
        lastPurchaseDate = domain.lastPurchaseDate, daysSinceLastPurchase = domain.daysSinceLastPurchase,
        purchaseFrequency = domain.purchaseFrequency, customerSegment = domain.customerSegment?.name,
        lifetimeValueTier = domain.lifetimeValueTier?.name
    )
}

@Repository
class DashboardMetricsRepositoryImpl(
    private val jpaRepository: JpaDashboardMetricsRepository
) : DashboardMetricsRepository {

    override fun save(metric: DashboardMetric) = toDomain(jpaRepository.save(toEntity(metric)))
    override fun findByNameAndPeriod(name: String, periodLabel: String) =
        jpaRepository.findByMetricNameAndPeriodLabel(name, periodLabel)?.let { toDomain(it) }
    override fun findByType(type: MetricType) = jpaRepository.findByMetricType(type).map { toDomain(it) }
    override fun findLatest() = jpaRepository.findTop10ByOrderByCalculatedAtDesc().map { toDomain(it) }

    private fun toDomain(entity: DashboardMetricEntity) = DashboardMetric(
        id = entity.id, metricName = entity.metricName, metricValue = entity.metricValue,
        metricType = entity.metricType, previousValue = entity.previousValue,
        changePercentage = entity.changePercentage, periodLabel = entity.periodLabel,
        calculatedAt = entity.calculatedAt
    )

    private fun toEntity(domain: DashboardMetric) = DashboardMetricEntity(
        id = domain.id, metricName = domain.metricName, metricValue = domain.metricValue,
        metricType = domain.metricType, previousValue = domain.previousValue,
        changePercentage = domain.changePercentage, periodLabel = domain.periodLabel
    )
}

// Placeholder implementations for remaining repositories
@Repository
class BookingAnalyticsRepositoryImpl : BookingAnalyticsRepository {
    override fun save(analytics: BookingAnalytics) = analytics
    override fun findById(id: UUID) = null
    override fun findByDateAndPeriod(date: LocalDate, period: ReportPeriod) = null
    override fun findByDateRange(startDate: LocalDate, endDate: LocalDate) = emptyList<BookingAnalytics>()
    override fun findLatestByPeriod(period: ReportPeriod, limit: Int) = emptyList<BookingAnalytics>()
}

@Repository
class InventoryAnalyticsRepositoryImpl : InventoryAnalyticsRepository {
    override fun save(analytics: InventoryAnalytics) = analytics
    override fun findById(id: UUID) = null
    override fun findByDate(date: LocalDate) = null
    override fun findLatest(limit: Int) = emptyList<InventoryAnalytics>()
}

@Repository
class StaffPerformanceRepositoryImpl : StaffPerformanceRepository {
    override fun save(performance: StaffPerformance) = performance
    override fun findById(id: UUID) = null
    override fun findByStaffIdAndPeriod(staffId: UUID, period: ReportPeriod) = emptyList<StaffPerformance>()
    override fun findTopByRevenue(startDate: LocalDate, endDate: LocalDate, limit: Int) = emptyList<StaffPerformance>()
}