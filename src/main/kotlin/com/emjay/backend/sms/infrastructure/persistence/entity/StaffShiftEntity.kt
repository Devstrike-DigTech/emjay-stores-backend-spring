package com.emjay.backend.sms.infrastructure.persistence.entity

import com.emjay.backend.sms.domain.entity.shift.ShiftStatus
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@Entity
@Table(name = "staff_shifts")
data class StaffShiftEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "staff_profile_id", nullable = false)
    val staffProfileId: UUID,

    @Column(name = "shift_template_id")
    val shiftTemplateId: UUID? = null,

    @Column(name = "shift_date", nullable = false)
    val shiftDate: LocalDate,

    @Column(name = "start_time", nullable = false)
    val startTime: LocalTime,

    @Column(name = "end_time", nullable = false)
    val endTime: LocalTime,

    @Column(name = "break_duration_minutes")
    val breakDurationMinutes: Int = 30,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "shift_status")
    val status: ShiftStatus = ShiftStatus.SCHEDULED,

    @Column(columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(name = "assigned_by")
    val assignedBy: UUID? = null,

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