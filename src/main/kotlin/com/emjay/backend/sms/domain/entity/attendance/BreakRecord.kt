package com.emjay.backend.sms.domain.entity.attendance

import java.time.Duration
import java.time.LocalDateTime
import java.util.*

/**
 * Break Record domain entity
 * Represents a break period during a work shift
 */
data class BreakRecord(
    val id: UUID? = null,
    val attendanceRecordId: UUID,
    val breakStartTime: LocalDateTime,
    val breakEndTime: LocalDateTime? = null,
    val breakDurationMinutes: Int? = null,
    val breakType: BreakType = BreakType.REGULAR,
    val notes: String? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun isActive(): Boolean {
        return breakEndTime == null
    }

    fun isEnded(): Boolean {
        return breakEndTime != null
    }

    fun calculateDuration(): Int? {
        return if (breakEndTime != null) {
            Duration.between(breakStartTime, breakEndTime).toMinutes().toInt()
        } else {
            null
        }
    }

    fun canEndBreak(): Boolean {
        return isActive()
    }
}

/**
 * Break type enum
 */
enum class BreakType {
    REGULAR,
    LUNCH,
    EMERGENCY
}