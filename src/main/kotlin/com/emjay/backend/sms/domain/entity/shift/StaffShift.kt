package com.emjay.backend.sms.domain.entity.shift

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * Staff Shift domain entity
 * Represents an actual shift assignment for a staff member
 */
data class StaffShift(
    val id: UUID? = null,
    val staffProfileId: UUID,
    val shiftTemplateId: UUID? = null,
    val shiftDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val breakDurationMinutes: Int = 30,
    val status: ShiftStatus = ShiftStatus.SCHEDULED,
    val notes: String? = null,
    val assignedBy: UUID? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun totalDurationMinutes(): Int {
        val duration = java.time.Duration.between(startTime, endTime)
        return duration.toMinutes().toInt()
    }

    fun workDurationMinutes(): Int {
        return totalDurationMinutes() - breakDurationMinutes
    }

    fun workDurationHours(): Double {
        return workDurationMinutes() / 60.0
    }

    fun isInProgress(): Boolean {
        return status == ShiftStatus.IN_PROGRESS
    }

    fun isCompleted(): Boolean {
        return status == ShiftStatus.COMPLETED
    }

    fun canBeCancelled(): Boolean {
        return status in listOf(ShiftStatus.SCHEDULED, ShiftStatus.CONFIRMED)
    }

    fun isOvernight(): Boolean {
        return endTime < startTime
    }
}

/**
 * Shift status enum
 */
enum class ShiftStatus {
    SCHEDULED,      // Shift has been scheduled
    CONFIRMED,      // Staff has confirmed they'll work
    IN_PROGRESS,    // Shift is currently happening (clocked in)
    COMPLETED,      // Shift is done (clocked out)
    CANCELLED,      // Shift was cancelled
    NO_SHOW         // Staff didn't show up
}