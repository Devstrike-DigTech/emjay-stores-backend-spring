package com.emjay.backend.sms.domain.entity.shift

import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * Shift Template domain entity
 * Represents a reusable shift definition (e.g., Morning Shift: 8AM-4PM)
 */
data class ShiftTemplate(
    val id: UUID? = null,
    val name: String,
    val shiftType: ShiftType,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val description: String? = null,
    val colorCode: String = "#3B82F6",
    val isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun durationInHours(): Double {
        val duration = java.time.Duration.between(startTime, endTime)
        return duration.toMinutes() / 60.0
    }

    fun isOvernight(): Boolean {
        return endTime < startTime
    }
}

/**
 * Shift type enum
 */
enum class ShiftType {
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT,
    CUSTOM
}