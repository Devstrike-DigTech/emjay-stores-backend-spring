package com.emjay.backend.sms.application.service

import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.sms.application.dto.attendance.*
import com.emjay.backend.sms.domain.entity.attendance.AttendanceRecord
import com.emjay.backend.sms.domain.entity.attendance.AttendanceStatus
import com.emjay.backend.sms.domain.entity.attendance.BreakRecord
import com.emjay.backend.sms.domain.repository.attendance.AttendanceRecordRepository
import com.emjay.backend.sms.domain.repository.attendance.BreakRecordRepository
import com.emjay.backend.sms.domain.repository.shift.StaffShiftRepository
import com.emjay.backend.sms.domain.repository.staff.StaffProfileRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@Service
class AttendanceService(
    private val attendanceRecordRepository: AttendanceRecordRepository,
    private val breakRecordRepository: BreakRecordRepository,
    private val staffProfileRepository: StaffProfileRepository,
    private val staffShiftRepository: StaffShiftRepository
) {
    
    @Transactional
    fun clockIn(request: ClockInRequest): AttendanceRecordResponse {
        // Validate staff profile exists
        val staffProfile = staffProfileRepository.findById(request.staffProfileId)
            ?: throw ResourceNotFoundException("Staff profile not found")
        
        // Check if already clocked in
        attendanceRecordRepository.findActiveAttendance(request.staffProfileId)?.let {
            throw IllegalStateException("Staff member is already clocked in. Please clock out first.")
        }
        
        val now = LocalDateTime.now()
        
        // Find scheduled shift for today if shift ID provided
        val shift = request.shiftId?.let {
            staffShiftRepository.findById(it)
                ?: throw ResourceNotFoundException("Shift not found")
        }
        
        // Calculate if late
        var isLate = false
        var lateMinutes = 0
        var scheduledStartTime: LocalDateTime? = null
        var scheduledEndTime: LocalDateTime? = null
        
        if (shift != null) {
            scheduledStartTime = LocalDateTime.of(shift.shiftDate, shift.startTime)
            scheduledEndTime = LocalDateTime.of(shift.shiftDate, shift.endTime)
            
            if (now.isAfter(scheduledStartTime)) {
                isLate = true
                lateMinutes = Duration.between(scheduledStartTime, now).toMinutes().toInt()
            }
        }
        
        // Determine status
        val status = when {
            isLate -> AttendanceStatus.LATE
            else -> AttendanceStatus.PRESENT
        }
        
        val attendanceRecord = AttendanceRecord(
            staffProfileId = request.staffProfileId,
            staffShiftId = request.shiftId,
            clockInTime = now,
            clockInLocation = request.location,
            clockInLatitude = request.latitude,
            clockInLongitude = request.longitude,
            clockInNotes = request.notes,
            scheduledStartTime = scheduledStartTime,
            scheduledEndTime = scheduledEndTime,
            isLate = isLate,
            lateMinutes = lateMinutes,
            status = status
        )
        
        val saved = attendanceRecordRepository.save(attendanceRecord)
        return toAttendanceRecordResponse(saved)
    }
    
    @Transactional
    fun clockOut(attendanceId: UUID, request: ClockOutRequest): AttendanceRecordResponse {
        val existing = attendanceRecordRepository.findById(attendanceId)
            ?: throw ResourceNotFoundException("Attendance record not found")
        
        if (!existing.canClockOut()) {
            throw IllegalStateException("Cannot clock out - attendance record is not active")
        }
        
        val now = LocalDateTime.now()
        
        // Calculate total break time
        val pageable = PageRequest.of(0, 100)
        val breaks = breakRecordRepository.findByAttendanceRecordId(attendanceId, pageable)
        val totalBreakMinutes = breaks.content
            .filter { it.breakEndTime != null }
            .sumOf { it.calculateDuration() ?: 0 }
        
        // Calculate if early departure
        var isEarlyDeparture = false
        var earlyDepartureMinutes = 0
        
        if (existing.scheduledEndTime != null && now.isBefore(existing.scheduledEndTime)) {
            isEarlyDeparture = true
            earlyDepartureMinutes = Duration.between(now, existing.scheduledEndTime).toMinutes().toInt()
        }
        
        // Calculate actual work minutes
        val totalMinutes = Duration.between(existing.clockInTime, now).toMinutes().toInt()
        val actualWorkMinutes = totalMinutes - totalBreakMinutes
        
        // Update status
        val status = when {
            isEarlyDeparture -> AttendanceStatus.EARLY_DEPARTURE
            existing.isLate -> AttendanceStatus.LATE
            else -> AttendanceStatus.PRESENT
        }
        
        val updated = existing.copy(
            clockOutTime = now,
            clockOutLocation = request.location,
            clockOutLatitude = request.latitude,
            clockOutLongitude = request.longitude,
            clockOutNotes = request.notes,
            totalBreakMinutes = totalBreakMinutes,
            actualWorkMinutes = actualWorkMinutes,
            isEarlyDeparture = isEarlyDeparture,
            earlyDepartureMinutes = earlyDepartureMinutes,
            status = status
        )
        
        val saved = attendanceRecordRepository.save(updated)
        return toAttendanceRecordResponse(saved)
    }
    
    @Transactional
    fun startBreak(attendanceId: UUID, request: StartBreakRequest): BreakRecordResponse {
        val attendance = attendanceRecordRepository.findById(attendanceId)
            ?: throw ResourceNotFoundException("Attendance record not found")
        
        if (!attendance.isActive()) {
            throw IllegalStateException("Cannot start break - not clocked in")
        }
        
        // Check if already on break
        breakRecordRepository.findActiveBreak(attendanceId)?.let {
            throw IllegalStateException("Already on break")
        }
        
        val breakRecord = BreakRecord(
            attendanceRecordId = attendanceId,
            breakStartTime = LocalDateTime.now(),
            breakType = request.breakType,
            notes = request.notes
        )
        
        // Update attendance status
        val updatedAttendance = attendance.copy(status = AttendanceStatus.ON_BREAK)
        attendanceRecordRepository.save(updatedAttendance)
        
        val saved = breakRecordRepository.save(breakRecord)
        return toBreakRecordResponse(saved)
    }
    
    @Transactional
    fun endBreak(breakId: UUID, request: EndBreakRequest): BreakRecordResponse {
        val existing = breakRecordRepository.findById(breakId)
            ?: throw ResourceNotFoundException("Break record not found")
        
        if (!existing.canEndBreak()) {
            throw IllegalStateException("Break is not active")
        }
        
        val now = LocalDateTime.now()
        val duration = Duration.between(existing.breakStartTime, now).toMinutes().toInt()
        
        val updated = existing.copy(
            breakEndTime = now,
            breakDurationMinutes = duration,
            notes = request.notes ?: existing.notes
        )
        
        // Update attendance status back to present/late
        val attendance = attendanceRecordRepository.findById(existing.attendanceRecordId)
            ?: throw ResourceNotFoundException("Attendance record not found")
        
        val updatedAttendance = attendance.copy(
            status = if (attendance.isLate) AttendanceStatus.LATE else AttendanceStatus.PRESENT
        )
        attendanceRecordRepository.save(updatedAttendance)
        
        val saved = breakRecordRepository.save(updated)
        return toBreakRecordResponse(saved)
    }
    
    fun getAttendanceRecordById(id: UUID): AttendanceRecordResponse {
        val record = attendanceRecordRepository.findById(id)
            ?: throw ResourceNotFoundException("Attendance record not found")
        return toAttendanceRecordResponse(record)
    }
    
    fun getActiveAttendance(staffProfileId: UUID): AttendanceRecordResponse? {
        return attendanceRecordRepository.findActiveAttendance(staffProfileId)
            ?.let { toAttendanceRecordResponse(it) }
    }
    
    fun getStaffAttendanceRecords(staffProfileId: UUID, page: Int = 0, size: Int = 20): AttendanceRecordListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "clockInTime"))
        val recordsPage = attendanceRecordRepository.findByStaffProfileId(staffProfileId, pageable)
        
        val responses = recordsPage.content.map { toAttendanceRecordResponse(it) }
        
        return AttendanceRecordListResponse(
            content = responses,
            totalElements = recordsPage.totalElements,
            totalPages = recordsPage.totalPages,
            currentPage = recordsPage.number,
            pageSize = recordsPage.size
        )
    }
    
    fun getAttendanceByDate(date: LocalDate, page: Int = 0, size: Int = 20): AttendanceRecordListResponse {
        val startOfDay = date.atStartOfDay()
        val endOfDay = date.atTime(LocalTime.MAX)
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "clockInTime"))
        
        val recordsPage = attendanceRecordRepository.findByDateRange(startOfDay, endOfDay, pageable)
        
        val responses = recordsPage.content.map { toAttendanceRecordResponse(it) }
        
        return AttendanceRecordListResponse(
            content = responses,
            totalElements = recordsPage.totalElements,
            totalPages = recordsPage.totalPages,
            currentPage = recordsPage.number,
            pageSize = recordsPage.size
        )
    }

    fun getAttendanceByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
        staffProfileId: UUID? = null,
        page: Int = 0,
        size: Int = 20
    ): AttendanceRecordListResponse {
        val start = startDate.atStartOfDay()
        val end = endDate.atTime(LocalTime.MAX)
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "clockInTime"))

        val recordsPage = if (staffProfileId != null) {
            attendanceRecordRepository.findByStaffAndDateRange(staffProfileId, start, end, pageable)
        } else {
            attendanceRecordRepository.findByDateRange(start, end, pageable)
        }

        val responses = recordsPage.content.map { toAttendanceRecordResponse(it) }

        return AttendanceRecordListResponse(
            content = responses,
            totalElements = recordsPage.totalElements,
            totalPages = recordsPage.totalPages,
            currentPage = recordsPage.number,
            pageSize = recordsPage.size
        )
    }
    
    fun getDailyAttendanceReport(request: DailyAttendanceReportRequest): DailyAttendanceReportResponse {
        val startOfDay = request.date.atStartOfDay()
        val endOfDay = request.date.atTime(LocalTime.MAX)
        val pageable = PageRequest.of(0, 1000)
        
        val recordsPage = attendanceRecordRepository.findByDateRange(startOfDay, endOfDay, pageable)
        val records = recordsPage.content
        
        val responses = records.map { toAttendanceRecordResponse(it) }
        
        return DailyAttendanceReportResponse(
            date = request.date,
            totalStaff = records.size,
            present = records.count { it.status == AttendanceStatus.PRESENT },
            late = records.count { it.status == AttendanceStatus.LATE },
            absent = 0, // Would need scheduled shifts to calculate
            onBreak = records.count { it.status == AttendanceStatus.ON_BREAK },
            earlyDepartures = records.count { it.status == AttendanceStatus.EARLY_DEPARTURE },
            attendanceRecords = responses
        )
    }
    
    fun getStaffAttendanceReport(request: StaffAttendanceReportRequest): StaffAttendanceReportResponse {
        val staffProfile = staffProfileRepository.findById(request.staffProfileId)
            ?: throw ResourceNotFoundException("Staff profile not found")
        
        val startDateTime = request.startDate.atStartOfDay()
        val endDateTime = request.endDate.atTime(LocalTime.MAX)
        val pageable = PageRequest.of(0, 1000)
        
        val recordsPage = attendanceRecordRepository.findByStaffAndDateRange(
            request.staffProfileId,
            startDateTime,
            endDateTime,
            pageable
        )
        
        val records = recordsPage.content
        val responses = records.map { toAttendanceRecordResponse(it) }
        
        val daysPresent = records.size
        val daysLate = records.count { it.isLate }
        val totalHours = records.mapNotNull { it.calculateWorkHours() }.sum()
        val totalDays = Duration.between(startDateTime, endDateTime).toDays().toInt() + 1
        
        return StaffAttendanceReportResponse(
            staffProfileId = request.staffProfileId.toString(),
            staffName = staffProfile.fullName(),
            startDate = request.startDate,
            endDate = request.endDate,
            totalDays = totalDays,
            daysPresent = daysPresent,
            daysLate = daysLate,
            daysAbsent = totalDays - daysPresent,
            totalHoursWorked = totalHours,
            averageHoursPerDay = if (daysPresent > 0) totalHours / daysPresent else 0.0,
            attendanceRate = if (totalDays > 0) (daysPresent.toDouble() / totalDays) * 100 else 0.0,
            punctualityRate = if (daysPresent > 0) ((daysPresent - daysLate).toDouble() / daysPresent) * 100 else 0.0,
            attendanceRecords = responses
        )
    }
    
    fun getTimesheet(request: TimesheetRequest): TimesheetResponse {
        val staffProfile = staffProfileRepository.findById(request.staffProfileId)
            ?: throw ResourceNotFoundException("Staff profile not found")
        
        val startDateTime = request.startDate.atStartOfDay()
        val endDateTime = request.endDate.atTime(LocalTime.MAX)
        val pageable = PageRequest.of(0, 1000, Sort.by(Sort.Direction.ASC, "clockInTime"))
        
        val recordsPage = attendanceRecordRepository.findByStaffAndDateRange(
            request.staffProfileId,
            startDateTime,
            endDateTime,
            pageable
        )
        
        val entries = recordsPage.content.map { record ->
            TimesheetEntry(
                date = record.clockInTime.toLocalDate(),
                clockIn = record.clockInTime,
                clockOut = record.clockOutTime,
                breakMinutes = record.totalBreakMinutes,
                hoursWorked = record.calculateWorkHours() ?: 0.0,
                status = record.status,
                isLate = record.isLate,
                notes = record.clockInNotes ?: record.clockOutNotes
            )
        }
        
        val totalHours = entries.sumOf { it.hoursWorked }
        
        return TimesheetResponse(
            staffProfileId = request.staffProfileId.toString(),
            staffName = staffProfile.fullName(),
            period = "${request.startDate} to ${request.endDate}",
            entries = entries,
            totalHours = totalHours,
            totalRegularHours = totalHours, // Could add overtime logic
            totalOvertimeHours = 0.0
        )
    }
    
    @Transactional
    fun deleteAttendanceRecord(id: UUID) {
        val record = attendanceRecordRepository.findById(id)
            ?: throw ResourceNotFoundException("Attendance record not found")
        attendanceRecordRepository.delete(record)
    }
    
    private fun toAttendanceRecordResponse(record: AttendanceRecord): AttendanceRecordResponse {
        val staffProfile = staffProfileRepository.findById(record.staffProfileId)
        
        return AttendanceRecordResponse(
            id = record.id.toString(),
            staffProfileId = record.staffProfileId.toString(),
            staffName = staffProfile?.fullName(),
            staffShiftId = record.staffShiftId?.toString(),
            clockInTime = record.clockInTime,
            clockInLocation = record.clockInLocation,
            clockInLatitude = record.clockInLatitude,
            clockInLongitude = record.clockInLongitude,
            clockInNotes = record.clockInNotes,
            clockOutTime = record.clockOutTime,
            clockOutLocation = record.clockOutLocation,
            clockOutLatitude = record.clockOutLatitude,
            clockOutLongitude = record.clockOutLongitude,
            clockOutNotes = record.clockOutNotes,
            totalBreakMinutes = record.totalBreakMinutes,
            scheduledStartTime = record.scheduledStartTime,
            scheduledEndTime = record.scheduledEndTime,
            actualWorkMinutes = record.actualWorkMinutes,
            actualWorkHours = record.calculateWorkHours(),
            isLate = record.isLate,
            lateMinutes = record.lateMinutes,
            isEarlyDeparture = record.isEarlyDeparture,
            earlyDepartureMinutes = record.earlyDepartureMinutes,
            status = record.status,
            isActive = record.isActive(),
            isClockedOut = record.isClockedOut(),
            approvedBy = record.approvedBy?.toString(),
            approvalNotes = record.approvalNotes,
            createdAt = record.createdAt!!,
            updatedAt = record.updatedAt!!
        )
    }
    
    private fun toBreakRecordResponse(record: BreakRecord): BreakRecordResponse {
        return BreakRecordResponse(
            id = record.id.toString(),
            attendanceRecordId = record.attendanceRecordId.toString(),
            breakStartTime = record.breakStartTime,
            breakEndTime = record.breakEndTime,
            breakDurationMinutes = record.calculateDuration(),
            breakType = record.breakType,
            notes = record.notes,
            isActive = record.isActive(),
            createdAt = record.createdAt!!,
            updatedAt = record.updatedAt!!
        )
    }
}
