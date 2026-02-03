package com.emjay.backend.sms.infrastructure.persistence.repository

import com.emjay.backend.sms.domain.entity.shift.ShiftStatus
import com.emjay.backend.sms.domain.entity.shift.ShiftType
import com.emjay.backend.sms.domain.entity.shift.SwapRequestStatus
import com.emjay.backend.sms.infrastructure.persistence.entity.ShiftSwapRequestEntity
import com.emjay.backend.sms.infrastructure.persistence.entity.ShiftTemplateEntity
import com.emjay.backend.sms.infrastructure.persistence.entity.StaffShiftEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface JpaShiftTemplateRepository : JpaRepository<ShiftTemplateEntity, UUID> {

    fun findByShiftType(type: ShiftType, pageable: Pageable): Page<ShiftTemplateEntity>

    fun findByIsActive(isActive: Boolean, pageable: Pageable): Page<ShiftTemplateEntity>

    fun existsByName(name: String): Boolean
}

@Repository
interface JpaStaffShiftRepository : JpaRepository<StaffShiftEntity, UUID> {

    fun findByStaffProfileId(staffProfileId: UUID, pageable: Pageable): Page<StaffShiftEntity>

    fun findByShiftDate(date: LocalDate, pageable: Pageable): Page<StaffShiftEntity>

    @Query("SELECT s FROM StaffShiftEntity s WHERE s.shiftDate BETWEEN :startDate AND :endDate")
    fun findByDateRange(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
        pageable: Pageable
    ): Page<StaffShiftEntity>

    @Query("SELECT s FROM StaffShiftEntity s WHERE s.staffProfileId = :staffProfileId AND s.shiftDate BETWEEN :startDate AND :endDate")
    fun findByStaffAndDateRange(
        @Param("staffProfileId") staffProfileId: UUID,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
        pageable: Pageable
    ): Page<StaffShiftEntity>

    fun findByStatus(status: ShiftStatus, pageable: Pageable): Page<StaffShiftEntity>

    @Query("SELECT s FROM StaffShiftEntity s WHERE s.staffProfileId = :staffProfileId AND s.shiftDate >= :fromDate ORDER BY s.shiftDate, s.startTime")
    fun findUpcomingShifts(
        @Param("staffProfileId") staffProfileId: UUID,
        @Param("fromDate") fromDate: LocalDate,
        pageable: Pageable
    ): Page<StaffShiftEntity>

    fun countByStaffProfileIdAndShiftDate(staffProfileId: UUID, date: LocalDate): Long

    fun countByShiftDateAndStatus(date: LocalDate, status: ShiftStatus): Long
}

@Repository
interface JpaShiftSwapRequestRepository : JpaRepository<ShiftSwapRequestEntity, UUID> {

    fun findByRequesterShiftId(requesterShiftId: UUID): ShiftSwapRequestEntity?

    fun findByTargetShiftId(targetShiftId: UUID, pageable: Pageable): Page<ShiftSwapRequestEntity>

    fun findByStatus(status: SwapRequestStatus, pageable: Pageable): Page<ShiftSwapRequestEntity>
}