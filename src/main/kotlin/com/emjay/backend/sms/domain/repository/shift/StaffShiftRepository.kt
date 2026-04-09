package com.emjay.backend.sms.domain.repository.shift

import com.emjay.backend.sms.domain.entity.shift.StaffShift
import com.emjay.backend.sms.domain.entity.shift.ShiftStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.util.*

/**
 * Repository interface for StaffShift domain entity
 */
interface StaffShiftRepository {

    fun save(staffShift: StaffShift): StaffShift

    fun saveAll(staffShifts: List<StaffShift>): List<StaffShift>

    fun findById(id: UUID): StaffShift?

    fun findAll(pageable: Pageable): Page<StaffShift>

    fun findByStaffProfileId(staffProfileId: UUID, pageable: Pageable): Page<StaffShift>

    fun findByShiftDate(date: LocalDate, pageable: Pageable): Page<StaffShift>

    fun findByDateRange(startDate: LocalDate, endDate: LocalDate, pageable: Pageable): Page<StaffShift>

    fun findByStaffAndDateRange(
        staffProfileId: UUID,
        startDate: LocalDate,
        endDate: LocalDate,
        pageable: Pageable
    ): Page<StaffShift>

    fun findByStatus(status: ShiftStatus, pageable: Pageable): Page<StaffShift>

    fun findUpcomingShifts(staffProfileId: UUID, fromDate: LocalDate, pageable: Pageable): Page<StaffShift>

    fun countByStaffAndDate(staffProfileId: UUID, date: LocalDate): Long

    fun countByDateAndStatus(date: LocalDate, status: ShiftStatus): Long

    fun delete(staffShift: StaffShift)
}