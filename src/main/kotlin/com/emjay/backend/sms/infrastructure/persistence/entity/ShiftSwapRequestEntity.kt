package com.emjay.backend.sms.infrastructure.persistence.entity

import com.emjay.backend.sms.domain.entity.shift.SwapRequestStatus
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "shift_swap_requests")
data class ShiftSwapRequestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "requester_shift_id", nullable = false)
    val requesterShiftId: UUID,

    @Column(name = "target_shift_id")
    val targetShiftId: UUID? = null,

    @Column(name = "target_staff_id")
    val targetStaffId: UUID? = null,

    @Column(columnDefinition = "TEXT")
    val reason: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val status: SwapRequestStatus = SwapRequestStatus.PENDING,

    @Column(name = "approved_by")
    val approvedBy: UUID? = null,

    @Column(name = "approved_at")
    val approvedAt: LocalDateTime? = null,

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    val rejectionReason: String? = null,

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