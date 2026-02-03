package com.emjay.backend.sms.presentation.controller.shift

import com.emjay.backend.sms.application.dto.shift.*
import com.emjay.backend.sms.application.service.ShiftSwapRequestService
import com.emjay.backend.sms.domain.entity.shift.SwapRequestStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/shifts/swap-requests")
@Tag(name = "Shift Swap Requests", description = "Endpoints for managing shift swap requests")
@SecurityRequirement(name = "bearerAuth")
class ShiftSwapRequestController(
    private val swapRequestService: ShiftSwapRequestService
) {

    @PostMapping
    @Operation(summary = "Create shift swap request")
    fun createShiftSwapRequest(
        @Valid @RequestBody request: CreateShiftSwapRequest
    ): ResponseEntity<ShiftSwapRequestResponse> {
        val response = swapRequestService.createShiftSwapRequest(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(summary = "Get all shift swap requests (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getAllShiftSwapRequests(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ShiftSwapRequestListResponse> {
        val response = swapRequestService.getAllShiftSwapRequests(page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending shift swap requests (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getPendingShiftSwapRequests(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ShiftSwapRequestListResponse> {
        val response = swapRequestService.getPendingShiftSwapRequests(page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get shift swap request by ID")
    fun getShiftSwapRequestById(@PathVariable id: UUID): ResponseEntity<ShiftSwapRequestResponse> {
        val response = swapRequestService.getShiftSwapRequestById(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get shift swap requests by status (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getShiftSwapRequestsByStatus(
        @PathVariable status: SwapRequestStatus,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ShiftSwapRequestListResponse> {
        val response = swapRequestService.getShiftSwapRequestsByStatus(status, page, size)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Approve shift swap request (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun approveShiftSwapRequest(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ApproveShiftSwapRequest
    ): ResponseEntity<ShiftSwapRequestResponse> {
        val response = swapRequestService.approveShiftSwapRequest(id, request)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Reject shift swap request (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun rejectShiftSwapRequest(
        @PathVariable id: UUID,
        @Valid @RequestBody request: RejectShiftSwapRequest
    ): ResponseEntity<ShiftSwapRequestResponse> {
        val response = swapRequestService.rejectShiftSwapRequest(id, request)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel shift swap request")
    fun cancelShiftSwapRequest(@PathVariable id: UUID): ResponseEntity<ShiftSwapRequestResponse> {
        val response = swapRequestService.cancelShiftSwapRequest(id)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete shift swap request (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteShiftSwapRequest(@PathVariable id: UUID): ResponseEntity<Void> {
        swapRequestService.deleteShiftSwapRequest(id)
        return ResponseEntity.noContent().build()
    }
}