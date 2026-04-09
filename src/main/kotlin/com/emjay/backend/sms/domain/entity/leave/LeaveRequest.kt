package com.emjay.backend.sms.domain.entity.leave

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Leave Request domain entity
 * Represents a leave request submitted by staff
 */
data class LeaveRequest(
    val id: UUID? = null,
    val staffProfileId: UUID,
    val leaveBalanceId: UUID? = null,
    val leaveType: LeaveType,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalDays: BigDecimal,
    val reason: String? = null,
    val status: LeaveRequestStatus = LeaveRequestStatus.PENDING,
    val requestedBy: UUID,
    val requestedAt: LocalDateTime? = null,
    val reviewedBy: UUID? = null,
    val reviewedAt: LocalDateTime? = null,
    val rejectionReason: String? = null,
    val supportingDocumentUrl: String? = null,
    val staffNotes: String? = null,
    val managerNotes: String? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun isPending(): Boolean = status == LeaveRequestStatus.PENDING

    fun isApproved(): Boolean = status == LeaveRequestStatus.APPROVED

    fun isRejected(): Boolean = status == LeaveRequestStatus.REJECTED

    fun isCancelled(): Boolean = status == LeaveRequestStatus.CANCELLED

    fun isCompleted(): Boolean = status == LeaveRequestStatus.COMPLETED

    fun canBeApproved(): Boolean = status == LeaveRequestStatus.PENDING

    fun canBeRejected(): Boolean = status == LeaveRequestStatus.PENDING

    fun canBeCancelled(): Boolean = status in listOf(LeaveRequestStatus.PENDING, LeaveRequestStatus.APPROVED)

    fun isActive(): Boolean = status in listOf(LeaveRequestStatus.PENDING, LeaveRequestStatus.APPROVED)

    fun calculateDays(): Long {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1
    }

    fun isInFuture(): Boolean {
        return startDate.isAfter(LocalDate.now())
    }

    fun isOngoing(): Boolean {
        val today = LocalDate.now()
        return !today.isBefore(startDate) && !today.isAfter(endDate)
    }

    fun hasEnded(): Boolean {
        return endDate.isBefore(LocalDate.now())
    }
}

/**
 * Leave request status enum
 */
enum class LeaveRequestStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED,
    COMPLETED
}