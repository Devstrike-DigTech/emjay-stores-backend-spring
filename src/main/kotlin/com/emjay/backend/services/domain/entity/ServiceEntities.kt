package com.emjay.backend.services.domain.entity

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

// ========== ENUMS ==========

enum class ServiceStatus {
    ACTIVE,
    INACTIVE,
    DISCONTINUED
}

enum class BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    RESCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    NO_SHOW
}

enum class DayOfWeek {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY
}

// ========== SERVICE CATEGORY ==========

data class ServiceCategory(
    val id: UUID? = null,
    val name: String,
    val slug: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val displayOrder: Int = 0,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun isAvailable(): Boolean = isActive
}

// ========== SERVICE SUBCATEGORY ==========

data class ServiceSubcategory(
    val id: UUID? = null,
    val categoryId: UUID,
    val name: String,
    val slug: String,
    val description: String? = null,
    val displayOrder: Int = 0,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun isAvailable(): Boolean = isActive
}

// ========== SERVICE ==========

data class Service(
    val id: UUID? = null,
    val categoryId: UUID,
    val subcategoryId: UUID? = null,
    val name: String,
    val slug: String,
    val description: String? = null,
    val shortDescription: String? = null,

    // Pricing
    val basePrice: BigDecimal,
    val discountedPrice: BigDecimal? = null,

    // Duration
    val durationMinutes: Int,
    val bufferTimeMinutes: Int = 15,

    // Service details
    val skillLevel: String? = null,
    val maxClientsPerSlot: Int = 1,

    // Status
    val status: ServiceStatus = ServiceStatus.ACTIVE,
    val isFeatured: Boolean = false,
    val requiresConsultation: Boolean = false,

    // SEO
    val metaTitle: String? = null,
    val metaDescription: String? = null,
    val metaKeywords: String? = null,

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun isActive(): Boolean = status == ServiceStatus.ACTIVE

    fun currentPrice(): BigDecimal = discountedPrice ?: basePrice

    fun hasDiscount(): Boolean = discountedPrice != null && discountedPrice < basePrice

    fun discountPercentage(): Double? {
        return if (hasDiscount() && discountedPrice != null) {
            ((basePrice - discountedPrice) / basePrice * BigDecimal(100)).toDouble()
        } else null
    }

    fun totalTimeMinutes(): Int = durationMinutes + bufferTimeMinutes
}

// ========== SERVICE IMAGE ==========

data class ServiceImage(
    val id: UUID? = null,
    val serviceId: UUID,
    val imageUrl: String,
    val altText: String? = null,
    val displayOrder: Int = 0,
    val isPrimary: Boolean = false,
    val createdAt: LocalDateTime? = null
)

// ========== SERVICE ADD-ON ==========

data class ServiceAddon(
    val id: UUID? = null,
    val serviceId: UUID,
    val name: String,
    val description: String? = null,
    val price: BigDecimal,
    val durationMinutes: Int = 0,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

// ========== SERVICE STAFF ASSIGNMENT ==========

data class ServiceStaff(
    val id: UUID? = null,
    val serviceId: UUID,
    val staffId: UUID,
    val isPrimary: Boolean = false,
    val assignedAt: LocalDateTime? = null
)

// ========== STAFF AVAILABILITY ==========

data class StaffAvailability(
    val id: UUID? = null,
    val staffId: UUID,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun isAvailableAt(time: LocalTime): Boolean {
        return isActive && time >= startTime && time < endTime
    }
}

// ========== STAFF BREAK ==========

data class StaffBreak(
    val id: UUID? = null,
    val staffId: UUID,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val breakName: String? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

// ========== BLOCKED DATE ==========

data class BlockedDate(
    val id: UUID? = null,
    val staffId: UUID? = null, // null = business closed
    val blockedDate: LocalDate,
    val startTime: LocalTime? = null, // null = all day
    val endTime: LocalTime? = null,
    val reason: String? = null,
    val createdBy: UUID? = null,
    val createdAt: LocalDateTime? = null
) {
    fun isAllDay(): Boolean = startTime == null && endTime == null

    fun isBusinessClosure(): Boolean = staffId == null
}

// ========== BOOKING ==========

data class Booking(
    val id: UUID? = null,
    val bookingNumber: String,

    // References
    val customerId: UUID,
    val serviceId: UUID,
    val staffId: UUID,

    // Scheduling
    val bookingDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val durationMinutes: Int,

    // Status
    val status: BookingStatus = BookingStatus.PENDING,

    // Pricing
    val servicePrice: BigDecimal,
    val addonsPrice: BigDecimal = BigDecimal.ZERO,
    val totalAmount: BigDecimal,

    // Payment
    val paymentStatus: String = "PENDING",
    val paidAt: LocalDateTime? = null,

    // Notes
    val customerNotes: String? = null,
    val staffNotes: String? = null,
    val cancellationReason: String? = null,

    // Timestamps
    val bookedAt: LocalDateTime,
    val confirmedAt: LocalDateTime? = null,
    val startedAt: LocalDateTime? = null,
    val completedAt: LocalDateTime? = null,
    val cancelledAt: LocalDateTime? = null,

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun isPending(): Boolean = status == BookingStatus.PENDING

    fun isConfirmed(): Boolean = status == BookingStatus.CONFIRMED

    fun isCompleted(): Boolean = status == BookingStatus.COMPLETED

    fun isCancelled(): Boolean = status == BookingStatus.CANCELLED

    fun canBeCancelled(): Boolean = status in listOf(BookingStatus.PENDING, BookingStatus.CONFIRMED)

    fun canBeRescheduled(): Boolean = status in listOf(BookingStatus.PENDING, BookingStatus.CONFIRMED)

    fun isPaid(): Boolean = paymentStatus == "PAID" || paidAt != null

    fun isUpcoming(): Boolean {
        val now = LocalDateTime.now()
        val bookingDateTime = LocalDateTime.of(bookingDate, startTime)
        return bookingDateTime.isAfter(now) && status in listOf(BookingStatus.PENDING, BookingStatus.CONFIRMED)
    }

    fun isPast(): Boolean {
        val now = LocalDateTime.now()
        val bookingDateTime = LocalDateTime.of(bookingDate, endTime)
        return bookingDateTime.isBefore(now)
    }
}

// ========== BOOKING ADD-ON ==========

data class BookingAddon(
    val id: UUID? = null,
    val bookingId: UUID,
    val addonId: UUID,
    val addonName: String, // Snapshot
    val price: BigDecimal, // Snapshot
    val durationMinutes: Int = 0,
    val createdAt: LocalDateTime? = null
)

// ========== BOOKING STATUS HISTORY ==========

data class BookingStatusHistory(
    val id: UUID? = null,
    val bookingId: UUID,
    val fromStatus: BookingStatus? = null,
    val toStatus: BookingStatus,
    val changedBy: UUID? = null,
    val reason: String? = null,
    val changedAt: LocalDateTime
)

// ========== BOOKING REMINDER ==========

data class BookingReminder(
    val id: UUID? = null,
    val bookingId: UUID,
    val reminderType: String, // SMS, EMAIL, PUSH
    val scheduledFor: LocalDateTime,
    val sentAt: LocalDateTime? = null,
    val status: String = "PENDING", // PENDING, SENT, FAILED
    val createdAt: LocalDateTime? = null
)