package com.emjay.backend.sms.domain.repository.attendance

import com.emjay.backend.sms.domain.entity.attendance.AttendanceRecord
import com.emjay.backend.sms.domain.entity.attendance.AttendanceStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Repository interface for AttendanceRecord domain entity
 */
interface AttendanceRecordRepository {

    fun save(attendanceRecord: AttendanceRecord): AttendanceRecord

    fun findById(id: UUID): AttendanceRecord?

    fun findAll(pageable: Pageable): Page<AttendanceRecord>

    fun findByStaffProfileId(staffProfileId: UUID, pageable: Pageable): Page<AttendanceRecord>

    fun findByStaffShiftId(shiftId: UUID): AttendanceRecord?

    fun findByStaffAndDate(staffProfileId: UUID, date: LocalDate, pageable: Pageable): Page<AttendanceRecord>

    fun findByDateRange(startDate: LocalDateTime, endDate: LocalDateTime, pageable: Pageable): Page<AttendanceRecord>

    fun findByStaffAndDateRange(
        staffProfileId: UUID,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        pageable: Pageable
    ): Page<AttendanceRecord>

    fun findActiveAttendance(staffProfileId: UUID): AttendanceRecord?

    fun findByStatus(status: AttendanceStatus, pageable: Pageable): Page<AttendanceRecord>

    fun findLateArrivals(date: LocalDate, pageable: Pageable): Page<AttendanceRecord>

    fun findEarlyDepartures(date: LocalDate, pageable: Pageable): Page<AttendanceRecord>

    fun countByStaffAndMonth(staffProfileId: UUID, year: Int, month: Int): Long

    fun delete(attendanceRecord: AttendanceRecord)
}