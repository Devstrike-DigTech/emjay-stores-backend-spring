package com.emjay.backend.sms.infrastructure.persistence.entity

import com.emjay.backend.sms.domain.entity.attendance.AttendanceStatus
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "attendance_records")
data class AttendanceRecordEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "staff_profile_id", nullable = false)
    val staffProfileId: UUID,

    @Column(name = "staff_shift_id")
    val staffShiftId: UUID? = null,

    // Clock-in details
    @Column(name = "clock_in_time", nullable = false)
    val clockInTime: LocalDateTime,

    @Column(name = "clock_in_location")
    val clockInLocation: String? = null,

    @Column(name = "clock_in_latitude", precision = 10, scale = 8)
    val clockInLatitude: BigDecimal? = null,

    @Column(name = "clock_in_longitude", precision = 11, scale = 8)
    val clockInLongitude: BigDecimal? = null,

    @Column(name = "clock_in_notes", columnDefinition = "TEXT")
    val clockInNotes: String? = null,

    // Clock-out details
    @Column(name = "clock_out_time")
    val clockOutTime: LocalDateTime? = null,

    @Column(name = "clock_out_location")
    val clockOutLocation: String? = null,

    @Column(name = "clock_out_latitude", precision = 10, scale = 8)
    val clockOutLatitude: BigDecimal? = null,

    @Column(name = "clock_out_longitude", precision = 11, scale = 8)
    val clockOutLongitude: BigDecimal? = null,

    @Column(name = "clock_out_notes", columnDefinition = "TEXT")
    val clockOutNotes: String? = null,

    // Break tracking
    @Column(name = "total_break_minutes")
    val totalBreakMinutes: Int = 0,

    // Scheduled times
    @Column(name = "scheduled_start_time")
    val scheduledStartTime: LocalDateTime? = null,

    @Column(name = "scheduled_end_time")
    val scheduledEndTime: LocalDateTime? = null,

    // Calculated fields
    @Column(name = "actual_work_minutes")
    val actualWorkMinutes: Int? = null,

    @Column(name = "is_late")
    val isLate: Boolean = false,

    @Column(name = "late_minutes")
    val lateMinutes: Int = 0,

    @Column(name = "is_early_departure")
    val isEarlyDeparture: Boolean = false,

    @Column(name = "early_departure_minutes")
    val earlyDepartureMinutes: Int = 0,

    // Status
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "attendance_status")
    val status: AttendanceStatus = AttendanceStatus.PRESENT,

    // Approval
    @Column(name = "approved_by")
    val approvedBy: UUID? = null,

    @Column(name = "approval_notes", columnDefinition = "TEXT")
    val approvalNotes: String? = null,

    // Audit
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