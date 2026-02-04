package com.emjay.backend.sms.domain.entity.leave

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Leave Policy domain entity
 * Defines company-wide leave policies
 */
data class LeavePolicy(
    val id: UUID? = null,
    val leaveType: LeaveType,
    val policyName: String,
    val description: String? = null,
    val daysPerYear: BigDecimal,
    val maxConsecutiveDays: Int? = null,
    val minDaysNotice: Int = 0,
    val allowCarryover: Boolean = false,
    val maxCarryoverDays: BigDecimal = BigDecimal.ZERO,
    val carryoverExpiryMonths: Int? = null,
    val requiresDocumentation: Boolean = false,
    val requiresManagerApproval: Boolean = true,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun canCarryover(): Boolean = allowCarryover

    fun requiresApproval(): Boolean = requiresManagerApproval

    fun requiresDocs(): Boolean = requiresDocumentation

    fun hasNoticeRequirement(): Boolean = minDaysNotice > 0

    fun hasConsecutiveDayLimit(): Boolean = maxConsecutiveDays != null
}