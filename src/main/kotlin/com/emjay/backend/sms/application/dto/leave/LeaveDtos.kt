package com.emjay.backend.sms.application.dto.leave

import com.emjay.backend.sms.domain.entity.leave.LeaveRequestStatus
import com.emjay.backend.sms.domain.entity.leave.LeaveType
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

// ========== Leave Balance DTOs ==========

data class CreateLeaveBalanceRequest(
    @field:NotNull(message = "Staff profile ID is required")
    val staffProfileId: UUID,

    @field:NotNull(message = "Leave type is required")
    val leaveType: LeaveType,

    @field:NotNull(message = "Year is required")
    val year: Int,

    @field:Positive(message = "Total days must be positive")
    val totalDays: BigDecimal,

    val carriedOverDays: BigDecimal = BigDecimal.ZERO,
    val allowNegative: Boolean = false
)

data class UpdateLeaveBalanceRequest(
    val totalDays: BigDecimal? = null,
    val usedDays: BigDecimal? = null,
    val pendingDays: BigDecimal? = null,
    val carriedOverDays: BigDecimal? = null,
    val allowNegative: Boolean? = null
)

data class LeaveBalanceResponse(
    val id: String,
    val staffProfileId: String,
    val staffName: String?,
    val leaveType: LeaveType,
    val year: Int,
    val totalDays: BigDecimal,
    val usedDays: BigDecimal,
    val pendingDays: BigDecimal,
    val availableDays: BigDecimal,
    val carriedOverDays: BigDecimal,
    val utilizationRate: Double,
    val allowNegative: Boolean,
    val hasAvailableDays: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class LeaveBalanceListResponse(
    val content: List<LeaveBalanceResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)

// ========== Leave Request DTOs ==========

data class CreateLeaveRequestRequest(
    @field:NotNull(message = "Staff profile ID is required")
    val staffProfileId: UUID,

    @field:NotNull(message = "Leave type is required")
    val leaveType: LeaveType,

    @field:NotNull(message = "Start date is required")
    val startDate: LocalDate,

    @field:NotNull(message = "End date is required")
    val endDate: LocalDate,

    val reason: String? = null,
    val staffNotes: String? = null
)

data class ApproveLeaveRequestRequest(
    @field:NotNull(message = "Reviewer ID is required")
    val reviewedBy: UUID,

    val managerNotes: String? = null
)

data class RejectLeaveRequestRequest(
    @field:NotNull(message = "Reviewer ID is required")
    val reviewedBy: UUID,

    @field:NotNull(message = "Rejection reason is required")
    val rejectionReason: String,

    val managerNotes: String? = null
)

data class LeaveRequestResponse(
    val id: String,
    val staffProfileId: String,
    val staffName: String?,
    val leaveBalanceId: String?,
    val leaveType: LeaveType,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalDays: BigDecimal,
    val reason: String?,
    val status: LeaveRequestStatus,
    val requestedBy: String,
    val requestedAt: LocalDateTime,
    val reviewedBy: String?,
    val reviewedAt: LocalDateTime?,
    val rejectionReason: String?,
    val supportingDocumentUrl: String?,
    val staffNotes: String?,
    val managerNotes: String?,
    val isPending: Boolean,
    val isApproved: Boolean,
    val isActive: Boolean,
    val isInFuture: Boolean,
    val isOngoing: Boolean,
    val hasEnded: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class LeaveRequestListResponse(
    val content: List<LeaveRequestResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)

// ========== Leave Policy DTOs ==========

data class CreateLeavePolicyRequest(
    @field:NotNull(message = "Leave type is required")
    val leaveType: LeaveType,

    @field:NotNull(message = "Policy name is required")
    val policyName: String,

    val description: String? = null,

    @field:Positive(message = "Days per year must be positive")
    val daysPerYear: BigDecimal,

    val maxConsecutiveDays: Int? = null,
    val minDaysNotice: Int = 0,
    val allowCarryover: Boolean = false,
    val maxCarryoverDays: BigDecimal = BigDecimal.ZERO,
    val carryoverExpiryMonths: Int? = null,
    val requiresDocumentation: Boolean = false,
    val requiresManagerApproval: Boolean = true
)

data class UpdateLeavePolicyRequest(
    val policyName: String? = null,
    val description: String? = null,
    val daysPerYear: BigDecimal? = null,
    val maxConsecutiveDays: Int? = null,
    val minDaysNotice: Int? = null,
    val allowCarryover: Boolean? = null,
    val maxCarryoverDays: BigDecimal? = null,
    val carryoverExpiryMonths: Int? = null,
    val requiresDocumentation: Boolean? = null,
    val requiresManagerApproval: Boolean? = null,
    val isActive: Boolean? = null
)

data class LeavePolicyResponse(
    val id: String,
    val leaveType: LeaveType,
    val policyName: String,
    val description: String?,
    val daysPerYear: BigDecimal,
    val maxConsecutiveDays: Int?,
    val minDaysNotice: Int,
    val allowCarryover: Boolean,
    val maxCarryoverDays: BigDecimal,
    val carryoverExpiryMonths: Int?,
    val requiresDocumentation: Boolean,
    val requiresManagerApproval: Boolean,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class LeavePolicyListResponse(
    val content: List<LeavePolicyResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)

// ========== Leave Calendar DTOs ==========

data class LeaveCalendarRequest(
    @field:NotNull(message = "Start date is required")
    val startDate: LocalDate,

    @field:NotNull(message = "End date is required")
    val endDate: LocalDate,

    val staffProfileIds: List<UUID>? = null,
    val leaveType: LeaveType? = null
)

data class LeaveCalendarResponse(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val leaves: List<LeaveCalendarEntry>
)

data class LeaveCalendarEntry(
    val date: LocalDate,
    val staffProfileId: String,
    val staffName: String,
    val leaveType: LeaveType,
    val leaveRequestId: String
)

// ========== Leave Summary DTOs ==========

data class LeaveSummaryRequest(
    @field:NotNull(message = "Staff profile ID is required")
    val staffProfileId: UUID,

    val year: Int? = null
)

data class LeaveSummaryResponse(
    val staffProfileId: String,
    val staffName: String,
    val year: Int,
    val balances: List<LeaveBalanceResponse>,
    val pendingRequests: List<LeaveRequestResponse>,
    val approvedRequests: List<LeaveRequestResponse>,
    val totalAvailableDays: BigDecimal,
    val totalUsedDays: BigDecimal,
    val totalPendingDays: BigDecimal
)

// ========== Team Leave Report DTOs ==========

data class TeamLeaveReportRequest(
    @field:NotNull(message = "Start date is required")
    val startDate: LocalDate,

    @field:NotNull(message = "End date is required")
    val endDate: LocalDate,

    val department: String? = null
)

data class TeamLeaveReportResponse(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalStaff: Int,
    val staffOnLeave: Int,
    val pendingRequests: Int,
    val approvedRequests: Int,
    val leavesByType: Map<LeaveType, Int>,
    val staffLeaveDetails: List<StaffLeaveDetail>
)

data class StaffLeaveDetail(
    val staffProfileId: String,
    val staffName: String,
    val department: String?,
    val totalLeaveDays: BigDecimal,
    val leaveRequests: List<LeaveRequestResponse>
)

// ========== Initialize Balances DTO ==========

data class InitializeLeaveBalancesRequest(
    @field:NotNull(message = "Year is required")
    val year: Int,

    val staffProfileIds: List<UUID>? = null
)

data class InitializeLeaveBalancesResponse(
    val year: Int,
    val balancesCreated: Int,
    val staffProcessed: Int,
    val message: String
)