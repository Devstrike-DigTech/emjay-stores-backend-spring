package com.emjay.backend.sms.domain.entity.attendance

import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

/**
 * Attendance Record domain entity
 * Represents a clock-in/out event for a staff member
 */
data class AttendanceRecord(
    val id: UUID? = null,
    val staffProfileId: UUID,
    val staffShiftId: UUID? = null,

    // Clock-in details
    val clockInTime: LocalDateTime,
    val clockInLocation: String? = null,
    val clockInLatitude: BigDecimal? = null,
    val clockInLongitude: BigDecimal? = null,
    val clockInNotes: String? = null,

    // Clock-out details
    val clockOutTime: LocalDateTime? = null,
    val clockOutLocation: String? = null,
    val clockOutLatitude: BigDecimal? = null,
    val clockOutLongitude: BigDecimal? = null,
    val clockOutNotes: String? = null,

    // Break tracking
    val totalBreakMinutes: Int = 0,

    // Scheduled times (for comparison)
    val scheduledStartTime: LocalDateTime? = null,
    val scheduledEndTime: LocalDateTime? = null,

    // Calculated fields
    val actualWorkMinutes: Int? = null,
    val isLate: Boolean = false,
    val lateMinutes: Int = 0,
    val isEarlyDeparture: Boolean = false,
    val earlyDepartureMinutes: Int = 0,

    // Status
    val status: AttendanceStatus = AttendanceStatus.PRESENT,

    // Approval
    val approvedBy: UUID? = null,
    val approvalNotes: String? = null,

    // Audit
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun isActive(): Boolean {
        return clockOutTime == null
    }

    fun isClockedOut(): Boolean {
        return clockOutTime != null
    }

    fun calculateActualWorkMinutes(): Int? {
        return if (clockOutTime != null) {
            val totalMinutes = Duration.between(clockInTime, clockOutTime).toMinutes().toInt()
            totalMinutes - totalBreakMinutes
        } else {
            null
        }
    }

    fun calculateTotalMinutes(): Int? {
        return if (clockOutTime != null) {
            Duration.between(clockInTime, clockOutTime).toMinutes().toInt()
        } else {
            null
        }
    }

    fun calculateWorkHours(): Double? {
        return calculateActualWorkMinutes()?.let { it / 60.0 }
    }

    fun hasLocation(): Boolean {
        return clockInLatitude != null && clockInLongitude != null
    }

    fun canClockOut(): Boolean {
        return isActive()
    }

    fun wasOnTime(): Boolean {
        return !isLate
    }
}

/**
 * Attendance status enum
 */
enum class AttendanceStatus {
    PRESENT,
    LATE,
    EARLY_DEPARTURE,
    ABSENT,
    ON_BREAK
}