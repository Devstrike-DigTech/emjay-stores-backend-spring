package com.emjay.backend.sms.domain.repository.leave

import com.emjay.backend.sms.domain.entity.leave.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.util.*

/**
 * Repository interface for LeaveBalance domain entity
 */
interface LeaveBalanceRepository {

    fun save(leaveBalance: LeaveBalance): LeaveBalance

    fun findById(id: UUID): LeaveBalance?

    fun findByStaffProfileId(staffProfileId: UUID, pageable: Pageable): Page<LeaveBalance>

    fun findByStaffAndYear(staffProfileId: UUID, year: Int, pageable: Pageable): Page<LeaveBalance>

    fun findByStaffAndLeaveTypeAndYear(
        staffProfileId: UUID,
        leaveType: LeaveType,
        year: Int
    ): LeaveBalance?

    fun findByYear(year: Int, pageable: Pageable): Page<LeaveBalance>

    fun findAll(pageable: Pageable): Page<LeaveBalance>

    fun delete(leaveBalance: LeaveBalance)
}

/**
 * Repository interface for LeaveRequest domain entity
 */
interface LeaveRequestRepository {

    fun save(leaveRequest: LeaveRequest): LeaveRequest

    fun findById(id: UUID): LeaveRequest?

    fun findAll(pageable: Pageable): Page<LeaveRequest>

    fun findByStaffProfileId(staffProfileId: UUID, pageable: Pageable): Page<LeaveRequest>

    fun findByStatus(status: LeaveRequestStatus, pageable: Pageable): Page<LeaveRequest>

    fun findByStaffAndStatus(
        staffProfileId: UUID,
        status: LeaveRequestStatus,
        pageable: Pageable
    ): Page<LeaveRequest>

    fun findPendingRequests(pageable: Pageable): Page<LeaveRequest>

    fun findByDateRange(startDate: LocalDate, endDate: LocalDate, pageable: Pageable): Page<LeaveRequest>

    fun findByStaffAndDateRange(
        staffProfileId: UUID,
        startDate: LocalDate,
        endDate: LocalDate,
        pageable: Pageable
    ): Page<LeaveRequest>

    fun findOverlappingRequests(
        staffProfileId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<LeaveRequest>

    fun countPendingByStaff(staffProfileId: UUID): Long

    fun delete(leaveRequest: LeaveRequest)
}

/**
 * Repository interface for LeavePolicy domain entity
 */
interface LeavePolicyRepository {

    fun save(leavePolicy: LeavePolicy): LeavePolicy

    fun findById(id: UUID): LeavePolicy?

    fun findByLeaveType(leaveType: LeaveType): LeavePolicy?

    fun findAll(pageable: Pageable): Page<LeavePolicy>

    fun findActivePolicy(pageable: Pageable): Page<LeavePolicy>

    fun delete(leavePolicy: LeavePolicy)
}