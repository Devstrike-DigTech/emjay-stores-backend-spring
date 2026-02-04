package com.emjay.backend.sms.infrastructure.persistence.repository

import com.emjay.backend.sms.domain.entity.attendance.AttendanceRecord
import com.emjay.backend.sms.domain.entity.attendance.AttendanceStatus
import com.emjay.backend.sms.domain.repository.attendance.AttendanceRecordRepository
import com.emjay.backend.sms.infrastructure.persistence.entity.AttendanceRecordEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Repository
class AttendanceRecordRepositoryImpl(
    private val jpaRepository: JpaAttendanceRecordRepository
) : AttendanceRecordRepository {

    override fun save(attendanceRecord: AttendanceRecord): AttendanceRecord {
        val entity = toEntity(attendanceRecord)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): AttendanceRecord? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findAll(pageable: Pageable): Page<AttendanceRecord> {
        return jpaRepository.findAll(pageable).map { toDomain(it) }
    }

    override fun findByStaffProfileId(staffProfileId: UUID, pageable: Pageable): Page<AttendanceRecord> {
        return jpaRepository.findByStaffProfileId(staffProfileId, pageable).map { toDomain(it) }
    }

    override fun findByStaffShiftId(shiftId: UUID): AttendanceRecord? {
        return jpaRepository.findByStaffShiftId(shiftId)?.let { toDomain(it) }
    }

    override fun findByStaffAndDate(staffProfileId: UUID, date: LocalDate, pageable: Pageable): Page<AttendanceRecord> {
        return jpaRepository.findByStaffAndDate(staffProfileId, date, pageable).map { toDomain(it) }
    }

    override fun findByDateRange(startDate: LocalDateTime, endDate: LocalDateTime, pageable: Pageable): Page<AttendanceRecord> {
        return jpaRepository.findByDateRange(startDate, endDate, pageable).map { toDomain(it) }
    }

    override fun findByStaffAndDateRange(
        staffProfileId: UUID,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        pageable: Pageable
    ): Page<AttendanceRecord> {
        return jpaRepository.findByStaffAndDateRange(staffProfileId, startDate, endDate, pageable)
            .map { toDomain(it) }
    }

    override fun findActiveAttendance(staffProfileId: UUID): AttendanceRecord? {
        return jpaRepository.findActiveAttendance(staffProfileId)?.let { toDomain(it) }
    }

    override fun findByStatus(status: AttendanceStatus, pageable: Pageable): Page<AttendanceRecord> {
        return jpaRepository.findByStatus(status, pageable).map { toDomain(it) }
    }

    override fun findLateArrivals(date: LocalDate, pageable: Pageable): Page<AttendanceRecord> {
        return jpaRepository.findLateArrivals(date, pageable).map { toDomain(it) }
    }

    override fun findEarlyDepartures(date: LocalDate, pageable: Pageable): Page<AttendanceRecord> {
        return jpaRepository.findEarlyDepartures(date, pageable).map { toDomain(it) }
    }

    override fun countByStaffAndMonth(staffProfileId: UUID, year: Int, month: Int): Long {
        return jpaRepository.countByStaffAndMonth(staffProfileId, year, month)
    }

    override fun delete(attendanceRecord: AttendanceRecord) {
        attendanceRecord.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: AttendanceRecordEntity): AttendanceRecord {
        return AttendanceRecord(
            id = entity.id,
            staffProfileId = entity.staffProfileId,
            staffShiftId = entity.staffShiftId,
            clockInTime = entity.clockInTime,
            clockInLocation = entity.clockInLocation,
            clockInLatitude = entity.clockInLatitude,
            clockInLongitude = entity.clockInLongitude,
            clockInNotes = entity.clockInNotes,
            clockOutTime = entity.clockOutTime,
            clockOutLocation = entity.clockOutLocation,
            clockOutLatitude = entity.clockOutLatitude,
            clockOutLongitude = entity.clockOutLongitude,
            clockOutNotes = entity.clockOutNotes,
            totalBreakMinutes = entity.totalBreakMinutes,
            scheduledStartTime = entity.scheduledStartTime,
            scheduledEndTime = entity.scheduledEndTime,
            actualWorkMinutes = entity.actualWorkMinutes,
            isLate = entity.isLate,
            lateMinutes = entity.lateMinutes,
            isEarlyDeparture = entity.isEarlyDeparture,
            earlyDepartureMinutes = entity.earlyDepartureMinutes,
            status = entity.status,
            approvedBy = entity.approvedBy,
            approvalNotes = entity.approvalNotes,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEntity(domain: AttendanceRecord): AttendanceRecordEntity {
        return AttendanceRecordEntity(
            id = domain.id,
            staffProfileId = domain.staffProfileId,
            staffShiftId = domain.staffShiftId,
            clockInTime = domain.clockInTime,
            clockInLocation = domain.clockInLocation,
            clockInLatitude = domain.clockInLatitude,
            clockInLongitude = domain.clockInLongitude,
            clockInNotes = domain.clockInNotes,
            clockOutTime = domain.clockOutTime,
            clockOutLocation = domain.clockOutLocation,
            clockOutLatitude = domain.clockOutLatitude,
            clockOutLongitude = domain.clockOutLongitude,
            clockOutNotes = domain.clockOutNotes,
            totalBreakMinutes = domain.totalBreakMinutes,
            scheduledStartTime = domain.scheduledStartTime,
            scheduledEndTime = domain.scheduledEndTime,
            actualWorkMinutes = domain.actualWorkMinutes,
            isLate = domain.isLate,
            lateMinutes = domain.lateMinutes,
            isEarlyDeparture = domain.isEarlyDeparture,
            earlyDepartureMinutes = domain.earlyDepartureMinutes,
            status = domain.status,
            approvedBy = domain.approvedBy,
            approvalNotes = domain.approvalNotes,
            createdAt = domain.createdAt ?: LocalDateTime.now(),
            updatedAt = domain.updatedAt ?: LocalDateTime.now()
        )
    }
}