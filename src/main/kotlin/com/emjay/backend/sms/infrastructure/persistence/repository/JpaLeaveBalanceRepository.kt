package com.emjay.backend.sms.infrastructure.persistence.repository

import com.emjay.backend.sms.domain.entity.leave.LeaveRequestStatus
import com.emjay.backend.sms.domain.entity.leave.LeaveType
import com.emjay.backend.sms.infrastructure.persistence.entity.LeaveBalanceEntity
import com.emjay.backend.sms.infrastructure.persistence.entity.LeavePolicyEntity
import com.emjay.backend.sms.infrastructure.persistence.entity.LeaveRequestEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface JpaLeaveBalanceRepository : JpaRepository<LeaveBalanceEntity, UUID> {

    fun findByStaffProfileId(staffProfileId: UUID, pageable: Pageable): Page<LeaveBalanceEntity>

    @Query("SELECT lb FROM LeaveBalanceEntity lb WHERE lb.staffProfileId = :staffProfileId AND lb.year = :year")
    fun findByStaffAndYear(
        @Param("staffProfileId") staffProfileId: UUID,
        @Param("year") year: Int,
        pageable: Pageable
    ): Page<LeaveBalanceEntity>

    fun findByStaffProfileIdAndLeaveTypeAndYear(
        staffProfileId: UUID,
        leaveType: LeaveType,
        year: Int
    ): LeaveBalanceEntity?

    fun findByYear(year: Int, pageable: Pageable): Page<LeaveBalanceEntity>
}

@Repository
interface JpaLeaveRequestRepository : JpaRepository<LeaveRequestEntity, UUID> {

    fun findByStaffProfileId(staffProfileId: UUID, pageable: Pageable): Page<LeaveRequestEntity>

    fun findByStatus(status: LeaveRequestStatus, pageable: Pageable): Page<LeaveRequestEntity>

    @Query("SELECT lr FROM LeaveRequestEntity lr WHERE lr.staffProfileId = :staffProfileId AND lr.status = :status")
    fun findByStaffAndStatus(
        @Param("staffProfileId") staffProfileId: UUID,
        @Param("status") status: LeaveRequestStatus,
        pageable: Pageable
    ): Page<LeaveRequestEntity>

    @Query("SELECT lr FROM LeaveRequestEntity lr WHERE lr.startDate <= :endDate AND lr.endDate >= :startDate")
    fun findByDateRange(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
        pageable: Pageable
    ): Page<LeaveRequestEntity>

    @Query("SELECT lr FROM LeaveRequestEntity lr WHERE lr.staffProfileId = :staffProfileId AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    fun findByStaffAndDateRange(
        @Param("staffProfileId") staffProfileId: UUID,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
        pageable: Pageable
    ): Page<LeaveRequestEntity>

    @Query("SELECT lr FROM LeaveRequestEntity lr WHERE lr.staffProfileId = :staffProfileId AND lr.status IN ('PENDING', 'APPROVED') AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    fun findOverlappingRequests(
        @Param("staffProfileId") staffProfileId: UUID,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<LeaveRequestEntity>

    fun countByStaffProfileIdAndStatus(staffProfileId: UUID, status: LeaveRequestStatus): Long
}

@Repository
interface JpaLeavePolicyRepository : JpaRepository<LeavePolicyEntity, UUID> {

    fun findByLeaveType(leaveType: LeaveType): LeavePolicyEntity?

    fun findByIsActive(isActive: Boolean, pageable: Pageable): Page<LeavePolicyEntity>
}