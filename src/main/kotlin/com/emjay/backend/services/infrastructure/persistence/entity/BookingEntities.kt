package com.emjay.backend.services.infrastructure.persistence.entity

import com.emjay.backend.services.domain.entity.BookingStatus
import com.emjay.backend.services.domain.entity.DayOfWeek
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

// ========== STAFF AVAILABILITY ENTITY ==========

@Entity
@Table(
    name = "staff_availability",
    uniqueConstraints = [UniqueConstraint(columnNames = ["staff_id", "day_of_week", "start_time"])]
)
data class StaffAvailabilityEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "staff_id", nullable = false)
    val staffId: UUID,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "day_of_week", nullable = false, columnDefinition = "day_of_week")
    val dayOfWeek: DayOfWeek,

    @Column(name = "start_time", nullable = false)
    val startTime: LocalTime,

    @Column(name = "end_time", nullable = false)
    val endTime: LocalTime,

    @Column(name = "is_active", nullable = false)
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

// ========== STAFF BREAK ENTITY ==========

@Entity
@Table(name = "staff_breaks")
data class StaffBreakEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "staff_id", nullable = false)
    val staffId: UUID,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "day_of_week", nullable = false, columnDefinition = "day_of_week")
    val dayOfWeek: DayOfWeek,

    @Column(name = "start_time", nullable = false)
    val startTime: LocalTime,

    @Column(name = "end_time", nullable = false)
    val endTime: LocalTime,

    @Column(name = "break_name", length = 100)
    val breakName: String? = null,

    @Column(name = "is_active", nullable = false)
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

// ========== BLOCKED DATE ENTITY ==========

@Entity
@Table(name = "blocked_dates")
data class BlockedDateEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "staff_id")
    val staffId: UUID? = null,

    @Column(name = "blocked_date", nullable = false)
    val blockedDate: LocalDate,

    @Column(name = "start_time")
    val startTime: LocalTime? = null,

    @Column(name = "end_time")
    val endTime: LocalTime? = null,

    @Column(length = 300)
    val reason: String? = null,

    @Column(name = "created_by")
    val createdBy: UUID? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}

// ========== BOOKING ENTITY ==========

@Entity
@Table(name = "bookings")
data class BookingEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "booking_number", nullable = false, unique = true, length = 50)
    val bookingNumber: String,

    @Column(name = "customer_id", nullable = false)
    val customerId: UUID,

    @Column(name = "service_id", nullable = false)
    val serviceId: UUID,

    @Column(name = "staff_id", nullable = false)
    val staffId: UUID,

    @Column(name = "booking_date", nullable = false)
    val bookingDate: LocalDate,

    @Column(name = "start_time", nullable = false)
    val startTime: LocalTime,

    @Column(name = "end_time", nullable = false)
    val endTime: LocalTime,

    @Column(name = "duration_minutes", nullable = false)
    val durationMinutes: Int,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "booking_status")
    val status: BookingStatus = BookingStatus.PENDING,

    @Column(name = "service_price", nullable = false, precision = 12, scale = 2)
    val servicePrice: BigDecimal,

    @Column(name = "addons_price", nullable = false, precision = 12, scale = 2)
    val addonsPrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    val totalAmount: BigDecimal,

    @Column(name = "payment_status", length = 50)
    val paymentStatus: String = "PENDING",

    @Column(name = "paid_at")
    val paidAt: LocalDateTime? = null,

    @Column(name = "customer_notes", columnDefinition = "TEXT")
    val customerNotes: String? = null,

    @Column(name = "staff_notes", columnDefinition = "TEXT")
    val staffNotes: String? = null,

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    val cancellationReason: String? = null,

    @Column(name = "booked_at", nullable = false)
    val bookedAt: LocalDateTime,

    @Column(name = "confirmed_at")
    val confirmedAt: LocalDateTime? = null,

    @Column(name = "started_at")
    val startedAt: LocalDateTime? = null,

    @Column(name = "completed_at")
    val completedAt: LocalDateTime? = null,

    @Column(name = "cancelled_at")
    val cancelledAt: LocalDateTime? = null,

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

// ========== BOOKING ADDON ENTITY ==========

@Entity
@Table(name = "booking_addons")
data class BookingAddonEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "booking_id", nullable = false)
    val bookingId: UUID,

    @Column(name = "addon_id", nullable = false)
    val addonId: UUID,

    @Column(name = "addon_name", nullable = false, length = 200)
    val addonName: String,

    @Column(nullable = false, precision = 12, scale = 2)
    val price: BigDecimal,

    @Column(name = "duration_minutes", nullable = false)
    val durationMinutes: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}

// ========== BOOKING STATUS HISTORY ENTITY ==========

@Entity
@Table(name = "booking_status_history")
data class BookingStatusHistoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "booking_id", nullable = false)
    val bookingId: UUID,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "from_status", columnDefinition = "booking_status")
    val fromStatus: BookingStatus? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "to_status", nullable = false, columnDefinition = "booking_status")
    val toStatus: BookingStatus,

    @Column(name = "changed_by")
    val changedBy: UUID? = null,

    @Column(columnDefinition = "TEXT")
    val reason: String? = null,

    @Column(name = "changed_at", nullable = false)
    val changedAt: LocalDateTime
)

// ========== BOOKING REMINDER ENTITY ==========

@Entity
@Table(name = "booking_reminders")
data class BookingReminderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "booking_id", nullable = false)
    val bookingId: UUID,

    @Column(name = "reminder_type", nullable = false, length = 50)
    val reminderType: String,

    @Column(name = "scheduled_for", nullable = false)
    val scheduledFor: LocalDateTime,

    @Column(name = "sent_at")
    val sentAt: LocalDateTime? = null,

    @Column(length = 50)
    val status: String = "PENDING",

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}