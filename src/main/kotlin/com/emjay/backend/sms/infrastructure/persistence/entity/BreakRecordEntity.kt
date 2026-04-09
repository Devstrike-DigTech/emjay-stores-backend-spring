package com.emjay.backend.sms.infrastructure.persistence.entity

import com.emjay.backend.sms.domain.entity.attendance.BreakType
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "break_records")
data class BreakRecordEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "attendance_record_id", nullable = false)
    val attendanceRecordId: UUID,

    @Column(name = "break_start_time", nullable = false)
    val breakStartTime: LocalDateTime,

    @Column(name = "break_end_time")
    val breakEndTime: LocalDateTime? = null,

    @Column(name = "break_duration_minutes")
    val breakDurationMinutes: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "break_type", length = 50)
    val breakType: BreakType = BreakType.REGULAR,

    @Column(columnDefinition = "TEXT")
    val notes: String? = null,

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