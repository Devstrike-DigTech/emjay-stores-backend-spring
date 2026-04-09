package com.emjay.backend.sms.infrastructure.persistence.entity

import com.emjay.backend.sms.domain.entity.leave.LeaveRequestStatus
import com.emjay.backend.sms.domain.entity.leave.LeaveType
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "leave_balances")
data class LeaveBalanceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "staff_profile_id", nullable = false)
    val staffProfileId: UUID,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "leave_type", nullable = false, columnDefinition = "leave_type")
    val leaveType: LeaveType,

    @Column(nullable = false)
    val year: Int,

    @Column(name = "total_days", nullable = false, precision = 5, scale = 2)
    val totalDays: BigDecimal,

    @Column(name = "used_days", nullable = false, precision = 5, scale = 2)
    val usedDays: BigDecimal = BigDecimal.ZERO,

    @Column(name = "pending_days", nullable = false, precision = 5, scale = 2)
    val pendingDays: BigDecimal = BigDecimal.ZERO,

    @Column(name = "carried_over_days", precision = 5, scale = 2)
    val carriedOverDays: BigDecimal = BigDecimal.ZERO,

    @Column(name = "allow_negative")
    val allowNegative: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

@Entity
@Table(name = "leave_requests")
data class LeaveRequestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "staff_profile_id", nullable = false)
    val staffProfileId: UUID,

    @Column(name = "leave_balance_id")
    val leaveBalanceId: UUID? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "leave_type", nullable = false, columnDefinition = "leave_type")
    val leaveType: LeaveType,

    @Column(name = "start_date", nullable = false)
    val startDate: LocalDate,

    @Column(name = "end_date", nullable = false)
    val endDate: LocalDate,

    @Column(name = "total_days", nullable = false, precision = 5, scale = 2)
    val totalDays: BigDecimal,

    @Column(columnDefinition = "TEXT")
    val reason: String? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "leave_request_status")
    val status: LeaveRequestStatus = LeaveRequestStatus.PENDING,

    @Column(name = "requested_by", nullable = false)
    val requestedBy: UUID,

    @Column(name = "requested_at", nullable = false)
    val requestedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "reviewed_by")
    val reviewedBy: UUID? = null,

    @Column(name = "reviewed_at")
    val reviewedAt: LocalDateTime? = null,

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    val rejectionReason: String? = null,

    @Column(name = "supporting_document_url", length = 500)
    val supportingDocumentUrl: String? = null,

    @Column(name = "staff_notes", columnDefinition = "TEXT")
    val staffNotes: String? = null,

    @Column(name = "manager_notes", columnDefinition = "TEXT")
    val managerNotes: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

@Entity
@Table(name = "leave_policies")
data class LeavePolicyEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "leave_type", nullable = false, unique = true, columnDefinition = "leave_type")
    val leaveType: LeaveType,

    @Column(name = "policy_name", nullable = false, length = 100)
    val policyName: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "days_per_year", nullable = false, precision = 5, scale = 2)
    val daysPerYear: BigDecimal,

    @Column(name = "max_consecutive_days")
    val maxConsecutiveDays: Int? = null,

    @Column(name = "min_days_notice")
    val minDaysNotice: Int = 0,

    @Column(name = "allow_carryover")
    val allowCarryover: Boolean = false,

    @Column(name = "max_carryover_days", precision = 5, scale = 2)
    val maxCarryoverDays: BigDecimal = BigDecimal.ZERO,

    @Column(name = "carryover_expiry_months")
    val carryoverExpiryMonths: Int? = null,

    @Column(name = "requires_documentation")
    val requiresDocumentation: Boolean = false,

    @Column(name = "requires_manager_approval")
    val requiresManagerApproval: Boolean = true,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}