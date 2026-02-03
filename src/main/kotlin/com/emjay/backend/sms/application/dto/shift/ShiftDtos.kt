package com.emjay.backend.sms.application.dto.shift

import com.emjay.backend.sms.domain.entity.shift.ShiftStatus
import com.emjay.backend.sms.domain.entity.shift.ShiftType
import com.emjay.backend.sms.domain.entity.shift.SwapRequestStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

// ========== Shift Template DTOs ==========

data class CreateShiftTemplateRequest(
    @field:NotBlank(message = "Shift name is required")
    val name: String,

    @field:NotNull(message = "Shift type is required")
    val shiftType: ShiftType,

    @field:NotNull(message = "Start time is required")
    val startTime: LocalTime,

    @field:NotNull(message = "End time is required")
    val endTime: LocalTime,

    val description: String? = null,
    val colorCode: String = "#3B82F6"
)

data class UpdateShiftTemplateRequest(
    val name: String? = null,
    val shiftType: ShiftType? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val description: String? = null,
    val colorCode: String? = null,
    val isActive: Boolean? = null
)

data class ShiftTemplateResponse(
    val id: String,
    val name: String,
    val shiftType: ShiftType,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val description: String?,
    val colorCode: String,
    val isActive: Boolean,
    val durationHours: Double,
    val isOvernight: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class ShiftTemplateListResponse(
    val content: List<ShiftTemplateResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)

// ========== Staff Shift DTOs ==========

data class CreateStaffShiftRequest(
    @field:NotNull(message = "Staff profile ID is required")
    val staffProfileId: UUID,

    val shiftTemplateId: UUID? = null,

    @field:NotNull(message = "Shift date is required")
    val shiftDate: LocalDate,

    @field:NotNull(message = "Start time is required")
    val startTime: LocalTime,

    @field:NotNull(message = "End time is required")
    val endTime: LocalTime,

    @field:Positive(message = "Break duration must be positive")
    val breakDurationMinutes: Int = 30,

    val notes: String? = null
)

data class BulkCreateStaffShiftsRequest(
    @field:NotNull(message = "Staff profile IDs are required")
    val staffProfileIds: List<UUID>,

    val shiftTemplateId: UUID? = null,

    @field:NotNull(message = "Shift dates are required")
    val shiftDates: List<LocalDate>,

    @field:NotNull(message = "Start time is required")
    val startTime: LocalTime,

    @field:NotNull(message = "End time is required")
    val endTime: LocalTime,

    val breakDurationMinutes: Int = 30,
    val notes: String? = null
)

data class UpdateStaffShiftRequest(
    val shiftDate: LocalDate? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val breakDurationMinutes: Int? = null,
    val status: ShiftStatus? = null,
    val notes: String? = null
)

data class StaffShiftResponse(
    val id: String,
    val staffProfileId: String,
    val staffName: String?,
    val shiftTemplateId: String?,
    val shiftTemplateName: String?,
    val shiftDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val breakDurationMinutes: Int,
    val totalDurationMinutes: Int,
    val workDurationMinutes: Int,
    val workDurationHours: Double,
    val status: ShiftStatus,
    val notes: String?,
    val assignedBy: String?,
    val isOvernight: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class StaffShiftListResponse(
    val content: List<StaffShiftResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)

data class WeeklyRosterRequest(
    @field:NotNull(message = "Start date is required")
    val startDate: LocalDate,

    val staffProfileIds: List<UUID>? = null
)

data class WeeklyRosterResponse(
    val weekStartDate: LocalDate,
    val weekEndDate: LocalDate,
    val shifts: List<StaffShiftResponse>,
    val staffSummaries: List<StaffWeeklySummary>
)

data class StaffWeeklySummary(
    val staffProfileId: String,
    val staffName: String,
    val totalShifts: Int,
    val totalHours: Double,
    val shiftsByDay: Map<String, Int>
)

// ========== Shift Swap Request DTOs ==========

data class CreateShiftSwapRequest(
    @field:NotNull(message = "Requester shift ID is required")
    val requesterShiftId: UUID,

    val targetShiftId: UUID? = null,
    val targetStaffId: UUID? = null,
    val reason: String? = null
)

data class ApproveShiftSwapRequest(
    @field:NotNull(message = "Approved by is required")
    val approvedBy: UUID
)

data class RejectShiftSwapRequest(
    @field:NotNull(message = "Approved by is required")
    val approvedBy: UUID,

    val rejectionReason: String? = null
)

data class ShiftSwapRequestResponse(
    val id: String,
    val requesterShiftId: String,
    val requesterShiftDetails: StaffShiftResponse,
    val targetShiftId: String?,
    val targetShiftDetails: StaffShiftResponse?,
    val targetStaffId: String?,
    val reason: String?,
    val status: SwapRequestStatus,
    val approvedBy: String?,
    val approvedAt: LocalDateTime?,
    val rejectionReason: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class ShiftSwapRequestListResponse(
    val content: List<ShiftSwapRequestResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)

// ========== Schedule Statistics DTOs ==========

data class ScheduleStatisticsResponse(
    val totalScheduledShifts: Long,
    val totalConfirmedShifts: Long,
    val totalInProgressShifts: Long,
    val totalCompletedShifts: Long,
    val totalCancelledShifts: Long,
    val totalNoShowShifts: Long,
    val totalStaffScheduled: Long,
    val averageShiftDuration: Double,
    val totalScheduledHours: Double
)

data class StaffScheduleStatisticsResponse(
    val staffProfileId: String,
    val staffName: String,
    val totalShifts: Int,
    val completedShifts: Int,
    val upcomingShifts: Int,
    val cancelledShifts: Int,
    val noShowShifts: Int,
    val totalHoursWorked: Double,
    val averageShiftDuration: Double
)