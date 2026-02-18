package com.emjay.backend.ecommerce.domain.entity.customer

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Customer Analytics domain entity
 * Tracks spending, budget, and customer insights
 */
data class CustomerAnalytics(
    val id: UUID? = null,
    val customerId: UUID,
    val totalOrders: Int = 0,
    val totalSpent: BigDecimal = BigDecimal.ZERO,
    val averageOrderValue: BigDecimal = BigDecimal.ZERO,
    val monthlyBudgetCap: BigDecimal? = null,
    val currentMonthSpent: BigDecimal = BigDecimal.ZERO,
    val budgetAlertThreshold: BigDecimal = BigDecimal("80.0"),
    val lifetimeValue: BigDecimal = BigDecimal.ZERO,
    val lastPurchaseAt: LocalDateTime? = null,
    val firstPurchaseAt: LocalDateTime? = null,
    val daysSinceLastPurchase: Int? = null,
    val favoriteCategoryId: UUID? = null,
    val favoriteBrand: String? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun hasBudgetCap(): Boolean = monthlyBudgetCap != null

    fun budgetUtilizationPercentage(): Double? {
        return monthlyBudgetCap?.let {
            if (it > BigDecimal.ZERO) {
                (currentMonthSpent.toDouble() / it.toDouble()) * 100
            } else {
                null
            }
        }
    }

    fun isOverBudget(): Boolean {
        return monthlyBudgetCap?.let { currentMonthSpent > it } ?: false
    }

    fun isNearBudgetLimit(): Boolean {
        return budgetUtilizationPercentage()?.let { it >= budgetAlertThreshold.toDouble() } ?: false
    }

    fun remainingBudget(): BigDecimal? {
        return monthlyBudgetCap?.let { it - currentMonthSpent }
    }

    fun canAfford(amount: BigDecimal): Boolean {
        return if (hasBudgetCap()) {
            remainingBudget()?.let { it >= amount } ?: false
        } else {
            true // No budget limit
        }
    }

    fun isActiveCustomer(): Boolean {
        return daysSinceLastPurchase?.let { it <= 90 } ?: false
    }

    fun isNewCustomer(): Boolean {
        return totalOrders <= 1
    }

    fun isVIPCustomer(): Boolean {
        return lifetimeValue >= BigDecimal("100000") // Configurable threshold
    }
}