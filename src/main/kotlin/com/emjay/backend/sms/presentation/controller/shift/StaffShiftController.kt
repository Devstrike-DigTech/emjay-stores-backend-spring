package com.emjay.backend.sms.presentation.controller.shift

import com.emjay.backend.sms.application.dto.shift.*
import com.emjay.backend.sms.application.service.StaffShiftService
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
@RequestMapping("/api/v1/shifts/staff-shifts")
@Tag(name = "Staff Shifts", description = "Endpoints for managing staff shift assignments")
@SecurityRequirement(name = "bearerAuth")
class StaffShiftController(
    private val staffShiftService: StaffShiftService
) {

    @PostMapping
    @Operation(summary = "Create staff shift assignment (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun createStaffShift(
        @Valid @RequestBody request: CreateStaffShiftRequest
    ): ResponseEntity<StaffShiftResponse> {
        val response = staffShiftService.createStaffShift(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/bulk")
    @Operation(summary = "Create multiple staff shifts (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun createBulkStaffShifts(
        @Valid @RequestBody request: BulkCreateStaffShiftsRequest
    ): ResponseEntity<StaffShiftListResponse> {
        val response = staffShiftService.createBulkStaffShifts(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(summary = "Get all staff shifts (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getAllStaffShifts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<StaffShiftListResponse> {
        val response = staffShiftService.getAllStaffShifts(page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get staff shift by ID")
    fun getStaffShiftById(@PathVariable id: UUID): ResponseEntity<StaffShiftResponse> {
        val response = staffShiftService.getStaffShiftById(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/staff/{staffProfileId}")
    @Operation(summary = "Get shifts for a staff member")
    fun getStaffShiftsByStaff(
        @PathVariable staffProfileId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<StaffShiftListResponse> {
        val response = staffShiftService.getStaffShiftsByStaff(staffProfileId, page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/staff/{staffProfileId}/upcoming")
    @Operation(summary = "Get upcoming shifts for a staff member")
    fun getUpcomingShifts(
        @PathVariable staffProfileId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<StaffShiftListResponse> {
        val response = staffShiftService.getUpcomingShifts(staffProfileId, page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get all shifts for a specific date (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getStaffShiftsByDate(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<StaffShiftListResponse> {
        val response = staffShiftService.getStaffShiftsByDate(date, page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get shifts within a date range (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getStaffShiftsByDateRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<StaffShiftListResponse> {
        val response = staffShiftService.getStaffShiftsByDateRange(startDate, endDate, page, size)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/weekly-roster")
    @Operation(summary = "Get weekly roster (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getWeeklyRoster(
        @Valid @RequestBody request: WeeklyRosterRequest
    ): ResponseEntity<WeeklyRosterResponse> {
        val response = staffShiftService.getWeeklyRoster(request)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update staff shift (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun updateStaffShift(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateStaffShiftRequest
    ): ResponseEntity<StaffShiftResponse> {
        val response = staffShiftService.updateStaffShift(id, request)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel staff shift (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun cancelStaffShift(@PathVariable id: UUID): ResponseEntity<StaffShiftResponse> {
        val response = staffShiftService.cancelStaffShift(id)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete staff shift (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteStaffShift(@PathVariable id: UUID): ResponseEntity<Void> {
        staffShiftService.deleteStaffShift(id)
        return ResponseEntity.noContent().build()
    }
}