package com.emjay.backend.sms.application.service

import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.sms.application.dto.shift.*
import com.emjay.backend.sms.domain.entity.shift.ShiftSwapRequest
import com.emjay.backend.sms.domain.entity.shift.SwapRequestStatus
import com.emjay.backend.sms.domain.repository.shift.ShiftSwapRequestRepository
import com.emjay.backend.sms.domain.repository.shift.StaffShiftRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class ShiftSwapRequestService(
    private val swapRequestRepository: ShiftSwapRequestRepository,
    private val staffShiftRepository: StaffShiftRepository,
    private val staffShiftService: StaffShiftService
) {

    @Transactional
    fun createShiftSwapRequest(request: CreateShiftSwapRequest): ShiftSwapRequestResponse {
        // Validate requester shift exists
        val requesterShift = staffShiftRepository.findById(request.requesterShiftId)
            ?: throw ResourceNotFoundException("Requester shift not found")

        // Validate target shift if provided
        request.targetShiftId?.let { targetId ->
            staffShiftRepository.findById(targetId)
                ?: throw ResourceNotFoundException("Target shift not found")
        }

        // Check if there's already a pending swap request for this shift
        swapRequestRepository.findByRequesterShiftId(request.requesterShiftId)?.let {
            if (it.isPending()) {
                throw IllegalStateException("A pending swap request already exists for this shift")
            }
        }

        // Ensure shift can be swapped (not in progress or completed)
        if (!requesterShift.canBeCancelled()) {
            throw IllegalStateException("Shift cannot be swapped in current status: ${requesterShift.status}")
        }

        val swapRequest = ShiftSwapRequest(
            requesterShiftId = request.requesterShiftId,
            targetShiftId = request.targetShiftId,
            targetStaffId = request.targetStaffId,
            reason = request.reason
        )

        val saved = swapRequestRepository.save(swapRequest)
        return toShiftSwapRequestResponse(saved)
    }

    fun getShiftSwapRequestById(id: UUID): ShiftSwapRequestResponse {
        val swapRequest = swapRequestRepository.findById(id)
            ?: throw ResourceNotFoundException("Shift swap request not found")
        return toShiftSwapRequestResponse(swapRequest)
    }

    fun getAllShiftSwapRequests(page: Int = 0, size: Int = 20): ShiftSwapRequestListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val swapRequestsPage = swapRequestRepository.findAll(pageable)

        val responses = swapRequestsPage.content.map { toShiftSwapRequestResponse(it) }

        return ShiftSwapRequestListResponse(
            content = responses,
            totalElements = swapRequestsPage.totalElements,
            totalPages = swapRequestsPage.totalPages,
            currentPage = swapRequestsPage.number,
            pageSize = swapRequestsPage.size
        )
    }

    fun getPendingShiftSwapRequests(page: Int = 0, size: Int = 20): ShiftSwapRequestListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"))
        val swapRequestsPage = swapRequestRepository.findPendingRequests(pageable)

        val responses = swapRequestsPage.content.map { toShiftSwapRequestResponse(it) }

        return ShiftSwapRequestListResponse(
            content = responses,
            totalElements = swapRequestsPage.totalElements,
            totalPages = swapRequestsPage.totalPages,
            currentPage = swapRequestsPage.number,
            pageSize = swapRequestsPage.size
        )
    }

    fun getShiftSwapRequestsByStatus(
        status: SwapRequestStatus,
        page: Int = 0,
        size: Int = 20
    ): ShiftSwapRequestListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val swapRequestsPage = swapRequestRepository.findByStatus(status, pageable)

        val responses = swapRequestsPage.content.map { toShiftSwapRequestResponse(it) }

        return ShiftSwapRequestListResponse(
            content = responses,
            totalElements = swapRequestsPage.totalElements,
            totalPages = swapRequestsPage.totalPages,
            currentPage = swapRequestsPage.number,
            pageSize = swapRequestsPage.size
        )
    }

    @Transactional
    fun approveShiftSwapRequest(id: UUID, request: ApproveShiftSwapRequest): ShiftSwapRequestResponse {
        val swapRequest = swapRequestRepository.findById(id)
            ?: throw ResourceNotFoundException("Shift swap request not found")

        if (!swapRequest.canBeApproved()) {
            throw IllegalStateException("Swap request cannot be approved in current status: ${swapRequest.status}")
        }

        // Get the shifts involved
        val requesterShift = staffShiftRepository.findById(swapRequest.requesterShiftId)
            ?: throw ResourceNotFoundException("Requester shift not found")

        val targetShift = swapRequest.targetShiftId?.let { targetId ->
            staffShiftRepository.findById(targetId)
                ?: throw ResourceNotFoundException("Target shift not found")
        }

        // If there's a target shift, swap the staff assignments
        if (targetShift != null) {
            // Swap staff IDs
            val updatedRequesterShift = requesterShift.copy(
                staffProfileId = targetShift.staffProfileId
            )
            val updatedTargetShift = targetShift.copy(
                staffProfileId = requesterShift.staffProfileId
            )

            staffShiftRepository.save(updatedRequesterShift)
            staffShiftRepository.save(updatedTargetShift)
        } else {
            // If no target shift, just cancel the requester's shift
            staffShiftService.cancelStaffShift(requesterShift.id!!)
        }

        // Update swap request status
        val approved = swapRequest.copy(
            status = SwapRequestStatus.APPROVED,
            approvedBy = request.approvedBy,
            approvedAt = LocalDateTime.now()
        )

        val saved = swapRequestRepository.save(approved)
        return toShiftSwapRequestResponse(saved)
    }

    @Transactional
    fun rejectShiftSwapRequest(id: UUID, request: RejectShiftSwapRequest): ShiftSwapRequestResponse {
        val swapRequest = swapRequestRepository.findById(id)
            ?: throw ResourceNotFoundException("Shift swap request not found")

        if (!swapRequest.canBeApproved()) {
            throw IllegalStateException("Swap request cannot be rejected in current status: ${swapRequest.status}")
        }

        val rejected = swapRequest.copy(
            status = SwapRequestStatus.REJECTED,
            approvedBy = request.approvedBy,
            rejectionReason = request.rejectionReason,
            approvedAt = LocalDateTime.now()
        )

        val saved = swapRequestRepository.save(rejected)
        return toShiftSwapRequestResponse(saved)
    }

    @Transactional
    fun cancelShiftSwapRequest(id: UUID): ShiftSwapRequestResponse {
        val swapRequest = swapRequestRepository.findById(id)
            ?: throw ResourceNotFoundException("Shift swap request not found")

        if (!swapRequest.canBeCancelled()) {
            throw IllegalStateException("Swap request cannot be cancelled in current status: ${swapRequest.status}")
        }

        val cancelled = swapRequest.copy(status = SwapRequestStatus.CANCELLED)
        val saved = swapRequestRepository.save(cancelled)
        return toShiftSwapRequestResponse(saved)
    }

    @Transactional
    fun deleteShiftSwapRequest(id: UUID) {
        val swapRequest = swapRequestRepository.findById(id)
            ?: throw ResourceNotFoundException("Shift swap request not found")
        swapRequestRepository.delete(swapRequest)
    }

    private fun toShiftSwapRequestResponse(swapRequest: ShiftSwapRequest): ShiftSwapRequestResponse {
        val requesterShift = staffShiftRepository.findById(swapRequest.requesterShiftId)
            ?: throw ResourceNotFoundException("Requester shift not found")

        val targetShift = swapRequest.targetShiftId?.let { targetId ->
            staffShiftRepository.findById(targetId)
        }

        return ShiftSwapRequestResponse(
            id = swapRequest.id.toString(),
            requesterShiftId = swapRequest.requesterShiftId.toString(),
            requesterShiftDetails = staffShiftService.getStaffShiftById(swapRequest.requesterShiftId),
            targetShiftId = swapRequest.targetShiftId?.toString(),
            targetShiftDetails = targetShift?.let { staffShiftService.getStaffShiftById(it.id!!) },
            targetStaffId = swapRequest.targetStaffId?.toString(),
            reason = swapRequest.reason,
            status = swapRequest.status,
            approvedBy = swapRequest.approvedBy?.toString(),
            approvedAt = swapRequest.approvedAt,
            rejectionReason = swapRequest.rejectionReason,
            createdAt = swapRequest.createdAt!!,
            updatedAt = swapRequest.updatedAt!!
        )
    }
}