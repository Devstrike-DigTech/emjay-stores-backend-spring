package com.emjay.backend.sms.application.dto.attendance

import com.emjay.backend.sms.domain.entity.attendance.AttendanceStatus
import com.emjay.backend.sms.domain.entity.attendance.BreakType
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

// ========== Clock-In/Out DTOs ==========

data class ClockInRequest(
    @field:NotNull(message = "Staff profile ID is required")
    val staffProfileId: UUID,

    val shiftId: UUID? = null,
    val location: String? = null,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    val notes: String? = null
)

data class ClockOutRequest(
    val location: String? = null,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    val notes: String? = null
)

data class AttendanceRecordResponse(
    val id: String,
    val staffProfileId: String,
    val staffName: String?,
    val staffShiftId: String?,

    // Clock-in details
    val clockInTime: LocalDateTime,
    val clockInLocation: String?,
    val clockInLatitude: BigDecimal?,
    val clockInLongitude: BigDecimal?,
    val clockInNotes: String?,

    // Clock-out details
    val clockOutTime: LocalDateTime?,
    val clockOutLocation: String?,
    val clockOutLatitude: BigDecimal?,
    val clockOutLongitude: BigDecimal?,
    val clockOutNotes: String?,

    // Break tracking
    val totalBreakMinutes: Int,

    // Scheduled vs actual
    val scheduledStartTime: LocalDateTime?,
    val scheduledEndTime: LocalDateTime?,
    val actualWorkMinutes: Int?,
    val actualWorkHours: Double?,

    // Tardiness tracking
    val isLate: Boolean,
    val lateMinutes: Int,
    val isEarlyDeparture: Boolean,
    val earlyDepartureMinutes: Int,

    // Status
    val status: AttendanceStatus,
    val isActive: Boolean,
    val isClockedOut: Boolean,

    // Approval
    val approvedBy: String?,
    val approvalNotes: String?,

    // Timestamps
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class AttendanceRecordListResponse(
    val content: List<AttendanceRecordResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)

// ========== Break DTOs ==========

data class StartBreakRequest(
    @field:NotNull(message = "Break type is required")
    val breakType: BreakType = BreakType.REGULAR,

    val notes: String? = null
)

data class EndBreakRequest(
    val notes: String? = null
)

data class BreakRecordResponse(
    val id: String,
    val attendanceRecordId: String,
    val breakStartTime: LocalDateTime,
    val breakEndTime: LocalDateTime?,
    val breakDurationMinutes: Int?,
    val breakType: BreakType,
    val notes: String?,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class BreakRecordListResponse(
    val content: List<BreakRecordResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)

// ========== Attendance Reports DTOs ==========

data class DailyAttendanceReportRequest(
    @field:NotNull(message = "Date is required")
    val date: LocalDate
)

data class DailyAttendanceReportResponse(
    val date: LocalDate,
    val totalStaff: Int,
    val present: Int,
    val late: Int,
    val absent: Int,
    val onBreak: Int,
    val earlyDepartures: Int,
    val attendanceRecords: List<AttendanceRecordResponse>
)

data class StaffAttendanceReportRequest(
    @field:NotNull(message = "Staff profile ID is required")
    val staffProfileId: UUID,

    @field:NotNull(message = "Start date is required")
    val startDate: LocalDate,

    @field:NotNull(message = "End date is required")
    val endDate: LocalDate
)

data class StaffAttendanceReportResponse(
    val staffProfileId: String,
    val staffName: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalDays: Int,
    val daysPresent: Int,
    val daysLate: Int,
    val daysAbsent: Int,
    val totalHoursWorked: Double,
    val averageHoursPerDay: Double,
    val attendanceRate: Double,
    val punctualityRate: Double,
    val attendanceRecords: List<AttendanceRecordResponse>
)

data class MonthlyAttendanceSummaryRequest(
    @field:NotNull(message = "Year is required")
    val year: Int,

    @field:NotNull(message = "Month is required")
    val month: Int,

    val staffProfileId: UUID? = null
)

data class MonthlyAttendanceSummaryResponse(
    val year: Int,
    val month: Int,
    val summaries: List<StaffMonthlySummary>
)

data class StaffMonthlySummary(
    val staffProfileId: String,
    val staffName: String,
    val totalDays: Int,
    val daysPresent: Int,
    val daysLate: Int,
    val daysAbsent: Int,
    val totalHoursWorked: Double,
    val averageHoursPerDay: Double,
    val attendanceRate: Double
)

// ========== Timesheet DTOs ==========

data class TimesheetRequest(
    @field:NotNull(message = "Staff profile ID is required")
    val staffProfileId: UUID,

    @field:NotNull(message = "Start date is required")
    val startDate: LocalDate,

    @field:NotNull(message = "End date is required")
    val endDate: LocalDate
)

data class TimesheetResponse(
    val staffProfileId: String,
    val staffName: String,
    val period: String,
    val entries: List<TimesheetEntry>,
    val totalHours: Double,
    val totalRegularHours: Double,
    val totalOvertimeHours: Double
)

data class TimesheetEntry(
    val date: LocalDate,
    val clockIn: LocalDateTime?,
    val clockOut: LocalDateTime?,
    val breakMinutes: Int,
    val hoursWorked: Double,
    val status: AttendanceStatus,
    val isLate: Boolean,
    val notes: String?
)