package com.emjay.backend.sms.presentation.controller.leave

import com.emjay.backend.sms.application.dto.leave.*
import com.emjay.backend.sms.application.service.LeaveManagementService
import com.emjay.backend.sms.domain.entity.leave.LeaveRequestStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/leave")
@Tag(name = "Leave Management", description = "Endpoints for managing leave requests and balances")
@SecurityRequirement(name = "bearerAuth")
class LeaveManagementController(
    private val leaveManagementService: LeaveManagementService
) {

    // ========== Leave Balance Endpoints ==========

    @PostMapping("/balances")
    @Operation(summary = "Create leave balance (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun createLeaveBalance(
        @Valid @RequestBody request: CreateLeaveBalanceRequest
    ): ResponseEntity<LeaveBalanceResponse> {
        val response = leaveManagementService.createLeaveBalance(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/balances/initialize")
    @Operation(summary = "Initialize leave balances for year (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun initializeLeaveBalances(
        @Valid @RequestBody request: InitializeLeaveBalancesRequest
    ): ResponseEntity<InitializeLeaveBalancesResponse> {
        val response = leaveManagementService.initializeLeaveBalances(request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/balances/{id}")
    @Operation(summary = "Get leave balance by ID")
    fun getLeaveBalanceById(@PathVariable id: UUID): ResponseEntity<LeaveBalanceResponse> {
        val response = leaveManagementService.getLeaveBalanceById(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/balances/staff/{staffProfileId}")
    @Operation(summary = "Get staff leave balances")
    fun getStaffLeaveBalances(
        @PathVariable staffProfileId: UUID,
        @RequestParam(required = false) year: Int?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<LeaveBalanceListResponse> {
        val response = leaveManagementService.getStaffLeaveBalances(staffProfileId, year, page, size)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/balances/{id}")
    @Operation(summary = "Update leave balance (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun updateLeaveBalance(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateLeaveBalanceRequest
    ): ResponseEntity<LeaveBalanceResponse> {
        val response = leaveManagementService.updateLeaveBalance(id, request)
        return ResponseEntity.ok(response)
    }

    // ========== Leave Request Endpoints ==========

    @PostMapping("/requests")
    @Operation(summary = "Create leave request (Staff)")
    fun createLeaveRequest(
        @Valid @RequestBody request: CreateLeaveRequestRequest
    ): ResponseEntity<LeaveRequestResponse> {
        val currentUserId = getCurrentUserId()
        val response = leaveManagementService.createLeaveRequest(request, currentUserId)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/requests/{id}")
    @Operation(summary = "Get leave request by ID")
    fun getLeaveRequestById(@PathVariable id: UUID): ResponseEntity<LeaveRequestResponse> {
        val response = leaveManagementService.getLeaveRequestById(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/requests/staff/{staffProfileId}")
    @Operation(summary = "Get staff leave requests")
    fun getStaffLeaveRequests(
        @PathVariable staffProfileId: UUID,
        @RequestParam(required = false) status: LeaveRequestStatus?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<LeaveRequestListResponse> {
        val response = leaveManagementService.getStaffLeaveRequests(staffProfileId, status, page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/requests/pending")
    @Operation(summary = "Get pending leave requests (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getPendingLeaveRequests(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<LeaveRequestListResponse> {
        val response = leaveManagementService.getPendingLeaveRequests(page, size)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/requests/{id}/approve")
    @Operation(summary = "Approve leave request (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun approveLeaveRequest(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ApproveLeaveRequestRequest
    ): ResponseEntity<LeaveRequestResponse> {
        val response = leaveManagementService.approveLeaveRequest(id, request)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/requests/{id}/reject")
    @Operation(summary = "Reject leave request (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun rejectLeaveRequest(
        @PathVariable id: UUID,
        @Valid @RequestBody request: RejectLeaveRequestRequest
    ): ResponseEntity<LeaveRequestResponse> {
        val response = leaveManagementService.rejectLeaveRequest(id, request)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/requests/{id}/cancel")
    @Operation(summary = "Cancel leave request (Staff/Admin/Manager)")
    fun cancelLeaveRequest(@PathVariable id: UUID): ResponseEntity<LeaveRequestResponse> {
        val response = leaveManagementService.cancelLeaveRequest(id)
        return ResponseEntity.ok(response)
    }

    // ========== Summary & Reports ==========

    @PostMapping("/summary")
    @Operation(summary = "Get leave summary for staff")
    fun getLeaveSummary(
        @Valid @RequestBody request: LeaveSummaryRequest
    ): ResponseEntity<LeaveSummaryResponse> {
        val response = leaveManagementService.getLeaveSummary(request)
        return ResponseEntity.ok(response)
    }

    // ========== Helper Methods ==========

    private fun getCurrentUserId(): UUID {
        val authentication = SecurityContextHolder.getContext().authentication
        return UUID.fromString(authentication.name)
    }
}