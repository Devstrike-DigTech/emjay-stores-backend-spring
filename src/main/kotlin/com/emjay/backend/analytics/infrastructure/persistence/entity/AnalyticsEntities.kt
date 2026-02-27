package com.emjay.backend.analytics.infrastructure.persistence.entity

import com.emjay.backend.analytics.domain.entity.*
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

// ========== SALES ANALYTICS ENTITY ==========

@Entity
@Table(
    name = "sales_analytics",
    uniqueConstraints = [UniqueConstraint(columnNames = ["period_date", "period_type"])]
)
data class SalesAnalyticsEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "period_date", nullable = false)
    val periodDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "period_type", nullable = false, columnDefinition = "report_period")
    val periodType: ReportPeriod,

    @Column(name = "total_revenue", nullable = false, precision = 15, scale = 2)
    val totalRevenue: BigDecimal,

    @Column(name = "total_orders", nullable = false)
    val totalOrders: Int,

    @Column(name = "total_items_sold", nullable = false)
    val totalItemsSold: Int,

    @Column(name = "average_order_value", nullable = false, precision = 15, scale = 2)
    val averageOrderValue: BigDecimal,

    @Column(name = "pending_orders", nullable = false)
    val pendingOrders: Int,

    @Column(name = "completed_orders", nullable = false)
    val completedOrders: Int,

    @Column(name = "cancelled_orders", nullable = false)
    val cancelledOrders: Int,

    @Column(name = "cash_payments", nullable = false, precision = 15, scale = 2)
    val cashPayments: BigDecimal,

    @Column(name = "card_payments", nullable = false, precision = 15, scale = 2)
    val cardPayments: BigDecimal,

    @Column(name = "transfer_payments", nullable = false, precision = 15, scale = 2)
    val transferPayments: BigDecimal,

    @Column(name = "new_customers", nullable = false)
    val newCustomers: Int,

    @Column(name = "returning_customers", nullable = false)
    val returningCustomers: Int,

    @Column(name = "unique_customers", nullable = false)
    val uniqueCustomers: Int,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

// ========== PRODUCT PERFORMANCE ENTITY ==========

@Entity
@Table(
    name = "product_performance",
    uniqueConstraints = [UniqueConstraint(columnNames = ["product_id", "period_date", "period_type"])]
)
data class ProductPerformanceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "product_id", nullable = false)
    val productId: UUID,

    @Column(name = "period_date", nullable = false)
    val periodDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "period_type", nullable = false, columnDefinition = "report_period")
    val periodType: ReportPeriod,

    @Column(name = "units_sold", nullable = false)
    val unitsSold: Int,

    @Column(nullable = false, precision = 15, scale = 2)
    val revenue: BigDecimal,

    @Column(name = "orders_count", nullable = false)
    val ordersCount: Int,

    @Column(name = "average_unit_price", nullable = false, precision = 15, scale = 2)
    val averageUnitPrice: BigDecimal,

    @Column(name = "revenue_rank")
    val revenueRank: Int? = null,

    @Column(name = "units_rank")
    val unitsRank: Int? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

// ========== CUSTOMER ANALYTICS ENTITY ==========

@Entity
@Table(
    name = "admin_customer_analytics",
    uniqueConstraints = [UniqueConstraint(columnNames = ["customer_id"])]
)
data class AdminCustomerAnalyticsEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "customer_id", nullable = false, unique = true)
    val customerId: UUID,

    @Column(name = "total_orders", nullable = false)
    val totalOrders: Int,

    @Column(name = "total_bookings", nullable = false)
    val totalBookings: Int,

    @Column(name = "total_spent", nullable = false, precision = 15, scale = 2)
    val totalSpent: BigDecimal,

    @Column(name = "average_order_value", nullable = false, precision = 15, scale = 2)
    val averageOrderValue: BigDecimal,

    @Column(name = "first_purchase_date")
    val firstPurchaseDate: LocalDate? = null,

    @Column(name = "last_purchase_date")
    val lastPurchaseDate: LocalDate? = null,

    @Column(name = "days_since_last_purchase")
    val daysSinceLastPurchase: Int? = null,

    @Column(name = "purchase_frequency", precision = 10, scale = 2)
    val purchaseFrequency: BigDecimal? = null,

    @Column(name = "customer_segment", length = 50)
    val customerSegment: String? = null,

    @Column(name = "lifetime_value_tier", length = 20)
    val lifetimeValueTier: String? = null,

    @Column(name = "calculated_at", nullable = false)
    var calculatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        calculatedAt = LocalDateTime.now()
    }
}

// ========== DASHBOARD METRIC ENTITY ==========

@Entity
@Table(
    name = "dashboard_metrics",
    uniqueConstraints = [UniqueConstraint(columnNames = ["metric_name", "period_label"])]
)
data class DashboardMetricEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "metric_name", nullable = false, length = 100)
    val metricName: String,

    @Column(name = "metric_value", nullable = false, precision = 15, scale = 2)
    val metricValue: BigDecimal,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "metric_type", nullable = false, columnDefinition = "metric_type")
    val metricType: MetricType,

    @Column(name = "previous_value", precision = 15, scale = 2)
    val previousValue: BigDecimal? = null,

    @Column(name = "change_percentage", precision = 5, scale = 2)
    val changePercentage: BigDecimal? = null,

    @Column(name = "period_label", length = 50)
    val periodLabel: String? = null,

    @Column(name = "calculated_at", nullable = false)
    var calculatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        calculatedAt = LocalDateTime.now()
    }
}