package com.emjay.backend.sms.application.service

import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.sms.application.dto.leave.*
import com.emjay.backend.sms.domain.entity.leave.*
import com.emjay.backend.sms.domain.repository.leave.LeaveBalanceRepository
import com.emjay.backend.sms.domain.repository.leave.LeavePolicyRepository
import com.emjay.backend.sms.domain.repository.leave.LeaveRequestRepository
import com.emjay.backend.sms.domain.repository.staff.StaffProfileRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class LeaveManagementService(
    private val leaveBalanceRepository: LeaveBalanceRepository,
    private val leaveRequestRepository: LeaveRequestRepository,
    private val leavePolicyRepository: LeavePolicyRepository,
    private val staffProfileRepository: StaffProfileRepository
) {

    // ========== Leave Balance Management ==========

    @Transactional
    fun createLeaveBalance(request: CreateLeaveBalanceRequest): LeaveBalanceResponse {
        // Validate staff profile exists
        staffProfileRepository.findById(request.staffProfileId)
            ?: throw ResourceNotFoundException("Staff profile not found")

        // Check if balance already exists
        leaveBalanceRepository.findByStaffAndLeaveTypeAndYear(
            request.staffProfileId,
            request.leaveType,
            request.year
        )?.let {
            throw IllegalStateException("Leave balance already exists for this staff, leave type, and year")
        }

        val leaveBalance = LeaveBalance(
            staffProfileId = request.staffProfileId,
            leaveType = request.leaveType,
            year = request.year,
            totalDays = request.totalDays,
            carriedOverDays = request.carriedOverDays,
            allowNegative = request.allowNegative
        )

        val saved = leaveBalanceRepository.save(leaveBalance)
        return toLeaveBalanceResponse(saved)
    }

    @Transactional
    fun initializeLeaveBalances(request: InitializeLeaveBalancesRequest): InitializeLeaveBalancesResponse {
        val year = request.year
        val policies = leavePolicyRepository.findActivePolicy(PageRequest.of(0, 100)).content

        // Get staff to initialize
        val staffProfiles = if (request.staffProfileIds != null) {
            request.staffProfileIds.mapNotNull { staffProfileRepository.findById(it) }
        } else {
            staffProfileRepository.findAll(PageRequest.of(0, 10000)).content
        }

        var balancesCreated = 0

        staffProfiles.forEach { staff ->
            policies.forEach { policy ->
                // Check if balance already exists
                val existing = leaveBalanceRepository.findByStaffAndLeaveTypeAndYear(
                    staff.id!!,
                    policy.leaveType,
                    year
                )

                if (existing == null) {
                    val balance = LeaveBalance(
                        staffProfileId = staff.id,
                        leaveType = policy.leaveType,
                        year = year,
                        totalDays = policy.daysPerYear,
                        allowNegative = false
                    )
                    leaveBalanceRepository.save(balance)
                    balancesCreated++
                }
            }
        }

        return InitializeLeaveBalancesResponse(
            year = year,
            balancesCreated = balancesCreated,
            staffProcessed = staffProfiles.size,
            message = "Initialized $balancesCreated leave balances for ${staffProfiles.size} staff members"
        )
    }

    fun getLeaveBalanceById(id: UUID): LeaveBalanceResponse {
        val balance = leaveBalanceRepository.findById(id)
            ?: throw ResourceNotFoundException("Leave balance not found")
        return toLeaveBalanceResponse(balance)
    }

    fun getStaffLeaveBalances(
        staffProfileId: UUID,
        year: Int? = null,
        page: Int = 0,
        size: Int = 20
    ): LeaveBalanceListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "leaveType"))

        val balancesPage = if (year != null) {
            leaveBalanceRepository.findByStaffAndYear(staffProfileId, year, pageable)
        } else {
            leaveBalanceRepository.findByStaffProfileId(staffProfileId, pageable)
        }

        val responses = balancesPage.content.map { toLeaveBalanceResponse(it) }

        return LeaveBalanceListResponse(
            content = responses,
            totalElements = balancesPage.totalElements,
            totalPages = balancesPage.totalPages,
            currentPage = balancesPage.number,
            pageSize = balancesPage.size
        )
    }

    @Transactional
    fun updateLeaveBalance(id: UUID, request: UpdateLeaveBalanceRequest): LeaveBalanceResponse {
        val existing = leaveBalanceRepository.findById(id)
            ?: throw ResourceNotFoundException("Leave balance not found")

        val updated = existing.copy(
            totalDays = request.totalDays ?: existing.totalDays,
            usedDays = request.usedDays ?: existing.usedDays,
            pendingDays = request.pendingDays ?: existing.pendingDays,
            carriedOverDays = request.carriedOverDays ?: existing.carriedOverDays,
            allowNegative = request.allowNegative ?: existing.allowNegative
        )

        val saved = leaveBalanceRepository.save(updated)
        return toLeaveBalanceResponse(saved)
    }

    // ========== Leave Request Management ==========

    @Transactional
    fun createLeaveRequest(request: CreateLeaveRequestRequest, requestedBy: UUID): LeaveRequestResponse {
        // Validate staff profile exists
        val staffProfile = staffProfileRepository.findById(request.staffProfileId)
            ?: throw ResourceNotFoundException("Staff profile not found")

        // Validate dates
        if (request.endDate.isBefore(request.startDate)) {
            throw IllegalArgumentException("End date cannot be before start date")
        }

        // Calculate total days
        val totalDays = BigDecimal(ChronoUnit.DAYS.between(request.startDate, request.endDate) + 1)

        // Get policy for validation
        val policy = leavePolicyRepository.findByLeaveType(request.leaveType)
            ?: throw ResourceNotFoundException("Leave policy not found for type: ${request.leaveType}")

        // Check min days notice
        if (policy.minDaysNotice > 0) {
            val daysUntilLeave = ChronoUnit.DAYS.between(LocalDate.now(), request.startDate)
            if (daysUntilLeave < policy.minDaysNotice) {
                throw IllegalArgumentException("Leave must be requested at least ${policy.minDaysNotice} days in advance")
            }
        }

        // Check max consecutive days
        if (policy.maxConsecutiveDays != null && totalDays.toInt() > policy.maxConsecutiveDays) {
            throw IllegalArgumentException("Cannot request more than ${policy.maxConsecutiveDays} consecutive days")
        }

        // Check for overlapping requests
        val overlapping = leaveRequestRepository.findOverlappingRequests(
            request.staffProfileId,
            request.startDate,
            request.endDate
        )
        if (overlapping.isNotEmpty()) {
            throw IllegalArgumentException("Overlapping leave request already exists")
        }

        // Get or create leave balance for current year
        val year = request.startDate.year
        val balance = leaveBalanceRepository.findByStaffAndLeaveTypeAndYear(
            request.staffProfileId,
            request.leaveType,
            year
        ) ?: run {
            // Auto-create balance if it doesn't exist
            val newBalance = LeaveBalance(
                staffProfileId = request.staffProfileId,
                leaveType = request.leaveType,
                year = year,
                totalDays = policy.daysPerYear
            )
            leaveBalanceRepository.save(newBalance)
        }

        // Check if sufficient balance
        if (!balance.canTakeDays(totalDays)) {
            throw IllegalArgumentException("Insufficient leave balance. Available: ${balance.availableDays()}, Requested: $totalDays")
        }

        // Create leave request
        val leaveRequest = LeaveRequest(
            staffProfileId = request.staffProfileId,
            leaveBalanceId = balance.id,
            leaveType = request.leaveType,
            startDate = request.startDate,
            endDate = request.endDate,
            totalDays = totalDays,
            reason = request.reason,
            requestedBy = requestedBy,
            staffNotes = request.staffNotes
        )

        val saved = leaveRequestRepository.save(leaveRequest)

        // Update pending days in balance
        val updatedBalance = balance.copy(
            pendingDays = balance.pendingDays + totalDays
        )
        leaveBalanceRepository.save(updatedBalance)

        return toLeaveRequestResponse(saved)
    }

    @Transactional
    fun approveLeaveRequest(id: UUID, request: ApproveLeaveRequestRequest): LeaveRequestResponse {
        val existing = leaveRequestRepository.findById(id)
            ?: throw ResourceNotFoundException("Leave request not found")

        if (!existing.canBeApproved()) {
            throw IllegalStateException("Leave request cannot be approved in current status: ${existing.status}")
        }

        // Update request status
        val approved = existing.copy(
            status = LeaveRequestStatus.APPROVED,
            reviewedBy = request.reviewedBy,
            reviewedAt = LocalDateTime.now(),
            managerNotes = request.managerNotes
        )

        val saved = leaveRequestRepository.save(approved)

        // Update balance: move from pending to used
        existing.leaveBalanceId?.let { balanceId ->
            val balance = leaveBalanceRepository.findById(balanceId)
            balance?.let {
                val updated = it.copy(
                    pendingDays = it.pendingDays - existing.totalDays,
                    usedDays = it.usedDays + existing.totalDays
                )
                leaveBalanceRepository.save(updated)
            }
        }

        return toLeaveRequestResponse(saved)
    }

    @Transactional
    fun rejectLeaveRequest(id: UUID, request: RejectLeaveRequestRequest): LeaveRequestResponse {
        val existing = leaveRequestRepository.findById(id)
            ?: throw ResourceNotFoundException("Leave request not found")

        if (!existing.canBeRejected()) {
            throw IllegalStateException("Leave request cannot be rejected in current status: ${existing.status}")
        }

        // Update request status
        val rejected = existing.copy(
            status = LeaveRequestStatus.REJECTED,
            reviewedBy = request.reviewedBy,
            reviewedAt = LocalDateTime.now(),
            rejectionReason = request.rejectionReason,
            managerNotes = request.managerNotes
        )

        val saved = leaveRequestRepository.save(rejected)

        // Update balance: remove from pending
        existing.leaveBalanceId?.let { balanceId ->
            val balance = leaveBalanceRepository.findById(balanceId)
            balance?.let {
                val updated = it.copy(
                    pendingDays = it.pendingDays - existing.totalDays
                )
                leaveBalanceRepository.save(updated)
            }
        }

        return toLeaveRequestResponse(saved)
    }

    @Transactional
    fun cancelLeaveRequest(id: UUID): LeaveRequestResponse {
        val existing = leaveRequestRepository.findById(id)
            ?: throw ResourceNotFoundException("Leave request not found")

        if (!existing.canBeCancelled()) {
            throw IllegalStateException("Leave request cannot be cancelled in current status: ${existing.status}")
        }

        val cancelled = existing.copy(status = LeaveRequestStatus.CANCELLED)
        val saved = leaveRequestRepository.save(cancelled)

        // Update balance based on previous status
        existing.leaveBalanceId?.let { balanceId ->
            val balance = leaveBalanceRepository.findById(balanceId)
            balance?.let {
                val updated = when (existing.status) {
                    LeaveRequestStatus.PENDING -> it.copy(
                        pendingDays = it.pendingDays - existing.totalDays
                    )
                    LeaveRequestStatus.APPROVED -> it.copy(
                        usedDays = it.usedDays - existing.totalDays
                    )
                    else -> it
                }
                leaveBalanceRepository.save(updated)
            }
        }

        return toLeaveRequestResponse(saved)
    }

    fun getLeaveRequestById(id: UUID): LeaveRequestResponse {
        val request = leaveRequestRepository.findById(id)
            ?: throw ResourceNotFoundException("Leave request not found")
        return toLeaveRequestResponse(request)
    }

    fun getStaffLeaveRequests(
        staffProfileId: UUID,
        status: LeaveRequestStatus? = null,
        page: Int = 0,
        size: Int = 20
    ): LeaveRequestListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"))

        val requestsPage = if (status != null) {
            leaveRequestRepository.findByStaffAndStatus(staffProfileId, status, pageable)
        } else {
            leaveRequestRepository.findByStaffProfileId(staffProfileId, pageable)
        }

        val responses = requestsPage.content.map { toLeaveRequestResponse(it) }

        return LeaveRequestListResponse(
            content = responses,
            totalElements = requestsPage.totalElements,
            totalPages = requestsPage.totalPages,
            currentPage = requestsPage.number,
            pageSize = requestsPage.size
        )
    }

    fun getPendingLeaveRequests(page: Int = 0, size: Int = 20): LeaveRequestListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "requestedAt"))
        val requestsPage = leaveRequestRepository.findPendingRequests(pageable)

        val responses = requestsPage.content.map { toLeaveRequestResponse(it) }

        return LeaveRequestListResponse(
            content = responses,
            totalElements = requestsPage.totalElements,
            totalPages = requestsPage.totalPages,
            currentPage = requestsPage.number,
            pageSize = requestsPage.size
        )
    }

    fun getLeaveSummary(request: LeaveSummaryRequest): LeaveSummaryResponse {
        val staffProfile = staffProfileRepository.findById(request.staffProfileId)
            ?: throw ResourceNotFoundException("Staff profile not found")

        val year = request.year ?: LocalDate.now().year

        val balances = getStaffLeaveBalances(request.staffProfileId, year, 0, 100).content
        val pendingRequests = getStaffLeaveRequests(request.staffProfileId, LeaveRequestStatus.PENDING, 0, 100).content
        val approvedRequests = getStaffLeaveRequests(request.staffProfileId, LeaveRequestStatus.APPROVED, 0, 100).content

        return LeaveSummaryResponse(
            staffProfileId = request.staffProfileId.toString(),
            staffName = staffProfile.fullName(),
            year = year,
            balances = balances,
            pendingRequests = pendingRequests,
            approvedRequests = approvedRequests,
            totalAvailableDays = balances.sumOf { it.availableDays },
            totalUsedDays = balances.sumOf { it.usedDays },
            totalPendingDays = balances.sumOf { it.pendingDays }
        )
    }

    // ========== Helper Methods ==========

    private fun toLeaveBalanceResponse(balance: LeaveBalance): LeaveBalanceResponse {
        val staffProfile = staffProfileRepository.findById(balance.staffProfileId)

        return LeaveBalanceResponse(
            id = balance.id.toString(),
            staffProfileId = balance.staffProfileId.toString(),
            staffName = staffProfile?.fullName(),
            leaveType = balance.leaveType,
            year = balance.year,
            totalDays = balance.totalDays,
            usedDays = balance.usedDays,
            pendingDays = balance.pendingDays,
            availableDays = balance.availableDays(),
            carriedOverDays = balance.carriedOverDays,
            utilizationRate = balance.utilizationRate(),
            allowNegative = balance.allowNegative,
            hasAvailableDays = balance.hasAvailableDays(),
            createdAt = balance.createdAt!!,
            updatedAt = balance.updatedAt!!
        )
    }

    private fun toLeaveRequestResponse(request: LeaveRequest): LeaveRequestResponse {
        val staffProfile = staffProfileRepository.findById(request.staffProfileId)

        return LeaveRequestResponse(
            id = request.id.toString(),
            staffProfileId = request.staffProfileId.toString(),
            staffName = staffProfile?.fullName(),
            leaveBalanceId = request.leaveBalanceId?.toString(),
            leaveType = request.leaveType,
            startDate = request.startDate,
            endDate = request.endDate,
            totalDays = request.totalDays,
            reason = request.reason,
            status = request.status,
            requestedBy = request.requestedBy.toString(),
            requestedAt = request.requestedAt!!,
            reviewedBy = request.reviewedBy?.toString(),
            reviewedAt = request.reviewedAt,
            rejectionReason = request.rejectionReason,
            supportingDocumentUrl = request.supportingDocumentUrl,
            staffNotes = request.staffNotes,
            managerNotes = request.managerNotes,
            isPending = request.isPending(),
            isApproved = request.isApproved(),
            isActive = request.isActive(),
            isInFuture = request.isInFuture(),
            isOngoing = request.isOngoing(),
            hasEnded = request.hasEnded(),
            createdAt = request.createdAt!!,
            updatedAt = request.updatedAt!!
        )
    }
}