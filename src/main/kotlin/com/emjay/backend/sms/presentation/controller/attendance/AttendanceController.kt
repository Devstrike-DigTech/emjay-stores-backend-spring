package com.emjay.backend.sms.presentation.controller.attendance

import com.emjay.backend.sms.application.dto.attendance.*
import com.emjay.backend.sms.application.service.AttendanceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/api/v1/attendance")
@Tag(name = "Attendance", description = "Endpoints for attendance tracking and clock-in/out")
@SecurityRequirement(name = "bearerAuth")
class AttendanceController(
    private val attendanceService: AttendanceService
) {

    // ========== Clock-In/Out ==========

    @PostMapping("/clock-in")
    @Operation(summary = "Clock in (Staff)")
    fun clockIn(@Valid @RequestBody request: ClockInRequest): ResponseEntity<AttendanceRecordResponse> {
        val response = attendanceService.clockIn(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PatchMapping("/{attendanceId}/clock-out")
    @Operation(summary = "Clock out (Staff)")
    fun clockOut(
        @PathVariable attendanceId: UUID,
        @Valid @RequestBody request: ClockOutRequest
    ): ResponseEntity<AttendanceRecordResponse> {
        val response = attendanceService.clockOut(attendanceId, request)
        return ResponseEntity.ok(response)
    }

    // ========== Break Management ==========

    @PostMapping("/{attendanceId}/start-break")
    @Operation(summary = "Start break (Staff)")
    fun startBreak(
        @PathVariable attendanceId: UUID,
        @Valid @RequestBody request: StartBreakRequest
    ): ResponseEntity<BreakRecordResponse> {
        val response = attendanceService.startBreak(attendanceId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PatchMapping("/breaks/{breakId}/end")
    @Operation(summary = "End break (Staff)")
    fun endBreak(
        @PathVariable breakId: UUID,
        @Valid @RequestBody request: EndBreakRequest
    ): ResponseEntity<BreakRecordResponse> {
        val response = attendanceService.endBreak(breakId, request)
        return ResponseEntity.ok(response)
    }

    // ========== Attendance Records ==========

    @GetMapping("/{id}")
    @Operation(summary = "Get attendance record by ID")
    fun getAttendanceRecordById(@PathVariable id: UUID): ResponseEntity<AttendanceRecordResponse> {
        val response = attendanceService.getAttendanceRecordById(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/staff/{staffProfileId}/active")
    @Operation(summary = "Get active attendance record for staff")
    fun getActiveAttendance(@PathVariable staffProfileId: UUID): ResponseEntity<AttendanceRecordResponse?> {
        val response = attendanceService.getActiveAttendance(staffProfileId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/staff/{staffProfileId}")
    @Operation(summary = "Get attendance records for staff member")
    fun getStaffAttendanceRecords(
        @PathVariable staffProfileId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<AttendanceRecordListResponse> {
        val response = attendanceService.getStaffAttendanceRecords(staffProfileId, page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get attendance records for a specific date (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getAttendanceByDate(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<AttendanceRecordListResponse> {
        val response = attendanceService.getAttendanceByDate(date, page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get attendance records within date range (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getAttendanceByDateRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
        @RequestParam(required = false) staffProfileId: UUID?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<AttendanceRecordListResponse> {
        val response = attendanceService.getAttendanceByDateRange(startDate, endDate, staffProfileId, page, size)
        return ResponseEntity.ok(response)
    }

    // ========== Reports ==========

    @PostMapping("/reports/daily")
    @Operation(summary = "Get daily attendance report (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getDailyAttendanceReport(
        @Valid @RequestBody request: DailyAttendanceReportRequest
    ): ResponseEntity<DailyAttendanceReportResponse> {
        val response = attendanceService.getDailyAttendanceReport(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/reports/staff")
    @Operation(summary = "Get staff attendance report (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getStaffAttendanceReport(
        @Valid @RequestBody request: StaffAttendanceReportRequest
    ): ResponseEntity<StaffAttendanceReportResponse> {
        val response = attendanceService.getStaffAttendanceReport(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/reports/timesheet")
    @Operation(summary = "Get timesheet report (Admin/Manager/Staff)")
    fun getTimesheet(
        @Valid @RequestBody request: TimesheetRequest
    ): ResponseEntity<TimesheetResponse> {
        val response = attendanceService.getTimesheet(request)
        return ResponseEntity.ok(response)
    }
}