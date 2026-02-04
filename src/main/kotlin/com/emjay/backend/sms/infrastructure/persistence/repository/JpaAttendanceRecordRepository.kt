package com.emjay.backend.sms.infrastructure.persistence.repository

import com.emjay.backend.sms.domain.entity.attendance.AttendanceStatus
import com.emjay.backend.sms.infrastructure.persistence.entity.AttendanceRecordEntity
import com.emjay.backend.sms.infrastructure.persistence.entity.BreakRecordEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface JpaAttendanceRecordRepository : JpaRepository<AttendanceRecordEntity, UUID> {

    fun findByStaffProfileId(staffProfileId: UUID, pageable: Pageable): Page<AttendanceRecordEntity>

    fun findByStaffShiftId(shiftId: UUID): AttendanceRecordEntity?

    @Query("SELECT a FROM AttendanceRecordEntity a WHERE a.staffProfileId = :staffProfileId AND DATE(a.clockInTime) = :date")
    fun findByStaffAndDate(
        @Param("staffProfileId") staffProfileId: UUID,
        @Param("date") date: java.time.LocalDate,
        pageable: Pageable
    ): Page<AttendanceRecordEntity>

    @Query("SELECT a FROM AttendanceRecordEntity a WHERE a.clockInTime BETWEEN :startDate AND :endDate")
    fun findByDateRange(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
        pageable: Pageable
    ): Page<AttendanceRecordEntity>

    @Query("SELECT a FROM AttendanceRecordEntity a WHERE a.staffProfileId = :staffProfileId AND a.clockInTime BETWEEN :startDate AND :endDate")
    fun findByStaffAndDateRange(
        @Param("staffProfileId") staffProfileId: UUID,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
        pageable: Pageable
    ): Page<AttendanceRecordEntity>

    @Query("SELECT a FROM AttendanceRecordEntity a WHERE a.staffProfileId = :staffProfileId AND a.clockOutTime IS NULL ORDER BY a.clockInTime DESC")
    fun findActiveAttendance(@Param("staffProfileId") staffProfileId: UUID): AttendanceRecordEntity?

    fun findByStatus(status: AttendanceStatus, pageable: Pageable): Page<AttendanceRecordEntity>

    @Query("SELECT a FROM AttendanceRecordEntity a WHERE DATE(a.clockInTime) = :date AND a.isLate = true")
    fun findLateArrivals(
        @Param("date") date: java.time.LocalDate,
        pageable: Pageable
    ): Page<AttendanceRecordEntity>

    @Query("SELECT a FROM AttendanceRecordEntity a WHERE DATE(a.clockInTime) = :date AND a.isEarlyDeparture = true")
    fun findEarlyDepartures(
        @Param("date") date: java.time.LocalDate,
        pageable: Pageable
    ): Page<AttendanceRecordEntity>

    @Query("SELECT COUNT(a) FROM AttendanceRecordEntity a WHERE a.staffProfileId = :staffProfileId AND YEAR(a.clockInTime) = :year AND MONTH(a.clockInTime) = :month")
    fun countByStaffAndMonth(
        @Param("staffProfileId") staffProfileId: UUID,
        @Param("year") year: Int,
        @Param("month") month: Int
    ): Long
}

@Repository
interface JpaBreakRecordRepository : JpaRepository<BreakRecordEntity, UUID> {

    fun findByAttendanceRecordId(attendanceRecordId: UUID, pageable: Pageable): Page<BreakRecordEntity>

    @Query("SELECT b FROM BreakRecordEntity b WHERE b.attendanceRecordId = :attendanceRecordId AND b.breakEndTime IS NULL")
    fun findActiveBreak(@Param("attendanceRecordId") attendanceRecordId: UUID): BreakRecordEntity?
}