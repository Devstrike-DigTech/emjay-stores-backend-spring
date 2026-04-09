package com.emjay.backend.sms.domain.entity.shift

import java.time.LocalDateTime
import java.util.*

/**
 * Shift Swap Request domain entity
 * Represents a request to swap shifts between staff members
 */
data class ShiftSwapRequest(
    val id: UUID? = null,
    val requesterShiftId: UUID,
    val targetShiftId: UUID? = null,
    val targetStaffId: UUID? = null,
    val reason: String? = null,
    val status: SwapRequestStatus = SwapRequestStatus.PENDING,
    val approvedBy: UUID? = null,
    val approvedAt: LocalDateTime? = null,
    val rejectionReason: String? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun isPending(): Boolean = status == SwapRequestStatus.PENDING

    fun isApproved(): Boolean = status == SwapRequestStatus.APPROVED

    fun isRejected(): Boolean = status == SwapRequestStatus.REJECTED

    fun canBeApproved(): Boolean = status == SwapRequestStatus.PENDING

    fun canBeCancelled(): Boolean = status == SwapRequestStatus.PENDING
}

/**
 * Swap request status enum
 */
enum class SwapRequestStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED
}