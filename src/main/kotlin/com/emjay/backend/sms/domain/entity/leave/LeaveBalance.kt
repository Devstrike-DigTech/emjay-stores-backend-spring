package com.emjay.backend.sms.domain.entity.leave

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Leave Balance domain entity
 * Tracks available leave days for a staff member
 */
data class LeaveBalance(
    val id: UUID? = null,
    val staffProfileId: UUID,
    val leaveType: LeaveType,
    val year: Int,
    val totalDays: BigDecimal,
    val usedDays: BigDecimal = BigDecimal.ZERO,
    val pendingDays: BigDecimal = BigDecimal.ZERO,
    val carriedOverDays: BigDecimal = BigDecimal.ZERO,
    val allowNegative: Boolean = false,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun availableDays(): BigDecimal {
        return totalDays - usedDays - pendingDays
    }

    fun hasAvailableDays(): Boolean {
        return availableDays() > BigDecimal.ZERO
    }

    fun canTakeDays(days: BigDecimal): Boolean {
        return if (allowNegative) {
            true
        } else {
            availableDays() >= days
        }
    }

    fun utilizationRate(): Double {
        return if (totalDays > BigDecimal.ZERO) {
            (usedDays.toDouble() / totalDays.toDouble()) * 100
        } else {
            0.0
        }
    }
}

/**
 * Leave type enum
 */
enum class LeaveType {
    ANNUAL,
    SICK,
    EMERGENCY,
    UNPAID,
    MATERNITY,
    PATERNITY,
    COMPASSIONATE,
    STUDY
}