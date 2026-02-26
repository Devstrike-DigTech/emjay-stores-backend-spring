package com.emjay.backend.services.application.dto

import com.emjay.backend.services.domain.entity.BookingStatus
import com.emjay.backend.services.domain.entity.DayOfWeek
import com.emjay.backend.services.domain.entity.ServiceStatus
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

// ========== SERVICE CATEGORY DTOs ==========

data class CreateServiceCategoryRequest(
    @field:NotBlank(message = "Category name is required")
    @field:Size(max = 200)
    val name: String,

    val description: String? = null,
    val imageUrl: String? = null,
    val displayOrder: Int = 0
)

data class UpdateServiceCategoryRequest(
    val name: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val displayOrder: Int? = null,
    val isActive: Boolean? = null
)

data class ServiceCategoryResponse(
    val id: String,
    val name: String,
    val slug: String,
    val description: String?,
    val imageUrl: String?,
    val displayOrder: Int,
    val isActive: Boolean,
    val subcategoryCount: Int = 0,
    val serviceCount: Int = 0,
    val createdAt: LocalDateTime
)

// ========== SERVICE SUBCATEGORY DTOs ==========

data class CreateServiceSubcategoryRequest(
    @field:NotBlank(message = "Subcategory name is required")
    @field:Size(max = 200)
    val name: String,

    val description: String? = null,
    val displayOrder: Int = 0
)

data class ServiceSubcategoryResponse(
    val id: String,
    val categoryId: String,
    val name: String,
    val slug: String,
    val description: String?,
    val displayOrder: Int,
    val isActive: Boolean,
    val serviceCount: Int = 0
)

// ========== SERVICE DTOs ==========

data class CreateServiceRequest(
    @field:NotBlank(message = "Service name is required")
    @field:Size(max = 300)
    val name: String,

    @field:NotNull(message = "Category ID is required")
    val categoryId: UUID,

    val subcategoryId: UUID? = null,

    val description: String? = null,
    val shortDescription: String? = null,

    @field:NotNull(message = "Base price is required")
    @field:DecimalMin(value = "0.0", message = "Price must be positive")
    val basePrice: BigDecimal,

    @field:DecimalMin(value = "0.0", message = "Discounted price must be positive")
    val discountedPrice: BigDecimal? = null,

    @field:NotNull(message = "Duration is required")
    @field:Min(value = 15, message = "Duration must be at least 15 minutes")
    val durationMinutes: Int,

    @field:Min(value = 0, message = "Buffer time cannot be negative")
    val bufferTimeMinutes: Int = 15,

    val skillLevel: String? = null,
    val maxClientsPerSlot: Int = 1,
    val requiresConsultation: Boolean = false,
    val isFeatured: Boolean = false,

    val metaTitle: String? = null,
    val metaDescription: String? = null,
    val metaKeywords: String? = null
)

data class UpdateServiceRequest(
    val name: String? = null,
    val categoryId: UUID? = null,
    val subcategoryId: UUID? = null,
    val description: String? = null,
    val shortDescription: String? = null,
    val basePrice: BigDecimal? = null,
    val discountedPrice: BigDecimal? = null,
    val durationMinutes: Int? = null,
    val bufferTimeMinutes: Int? = null,
    val skillLevel: String? = null,
    val status: ServiceStatus? = null,
    val isFeatured: Boolean? = null,
    val requiresConsultation: Boolean? = null
)

data class ServiceResponse(
    val id: String,
    val categoryId: String,
    val categoryName: String,
    val subcategoryId: String?,
    val subcategoryName: String?,
    val name: String,
    val slug: String,
    val description: String?,
    val shortDescription: String?,
    val basePrice: BigDecimal,
    val discountedPrice: BigDecimal?,
    val currentPrice: BigDecimal,
    val hasDiscount: Boolean,
    val discountPercentage: Double?,
    val durationMinutes: Int,
    val bufferTimeMinutes: Int,
    val totalTimeMinutes: Int,
    val skillLevel: String?,
    val status: ServiceStatus,
    val isFeatured: Boolean,
    val requiresConsultation: Boolean,
    val images: List<ServiceImageResponse>,
    val addons: List<ServiceAddonResponse>,
    val assignedStaff: List<StaffAssignmentResponse>,
    val createdAt: LocalDateTime
)

data class ServiceListResponse(
    val content: List<ServiceSummaryResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)

data class ServiceSummaryResponse(
    val id: String,
    val name: String,
    val slug: String,
    val categoryName: String,
    val shortDescription: String?,
    val currentPrice: BigDecimal,
    val hasDiscount: Boolean,
    val durationMinutes: Int,
    val primaryImage: String?,
    val isFeatured: Boolean,
    val status: ServiceStatus
)

// ========== SERVICE IMAGE DTOs ==========

data class AddServiceImageRequest(
    @field:NotBlank(message = "Image URL is required")
    val imageUrl: String,

    val altText: String? = null,
    val displayOrder: Int = 0,
    val isPrimary: Boolean = false
)

data class ServiceImageResponse(
    val id: String,
    val imageUrl: String,
    val altText: String?,
    val displayOrder: Int,
    val isPrimary: Boolean
)

// ========== SERVICE ADDON DTOs ==========

data class CreateServiceAddonRequest(
    @field:NotBlank(message = "Add-on name is required")
    val name: String,

    val description: String? = null,

    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "0.0")
    val price: BigDecimal,

    @field:Min(value = 0)
    val durationMinutes: Int = 0
)

data class ServiceAddonResponse(
    val id: String,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val durationMinutes: Int,
    val isActive: Boolean
)

// ========== STAFF ASSIGNMENT DTOs ==========

data class AssignStaffToServiceRequest(
    @field:NotNull(message = "Staff ID is required")
    val staffId: UUID,

    val isPrimary: Boolean = false
)

data class StaffAssignmentResponse(
    val id: String,
    val staffId: String,
    val staffName: String,
    val isPrimary: Boolean,
    val assignedAt: LocalDateTime
)

// ========== STAFF AVAILABILITY DTOs ==========

data class SetStaffAvailabilityRequest(
    @field:NotNull(message = "Day of week is required")
    val dayOfWeek: DayOfWeek,

    @field:NotNull(message = "Start time is required")
    val startTime: LocalTime,

    @field:NotNull(message = "End time is required")
    val endTime: LocalTime
)

data class StaffAvailabilityResponse(
    val id: String,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val isActive: Boolean
)

data class AddStaffBreakRequest(
    @field:NotNull(message = "Day of week is required")
    val dayOfWeek: DayOfWeek,

    @field:NotNull(message = "Start time is required")
    val startTime: LocalTime,

    @field:NotNull(message = "End time is required")
    val endTime: LocalTime,

    val breakName: String? = null
)

data class BlockDateRequest(
    val staffId: UUID? = null, // null = business closure

    @field:NotNull(message = "Date is required")
    val blockedDate: LocalDate,

    val startTime: LocalTime? = null, // null = all day
    val endTime: LocalTime? = null,
    val reason: String? = null
)

// ========== AVAILABLE SLOTS DTOs ==========

data class GetAvailableSlotsRequest(
    @field:NotNull(message = "Service ID is required")
    val serviceId: UUID,

    @field:NotNull(message = "Date is required")
    @field:FutureOrPresent(message = "Date must be today or in the future")
    val date: LocalDate,

    val staffId: UUID? = null // Optional: specific staff, otherwise any available
)

data class TimeSlot(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val availableStaff: List<StaffSlotInfo>
)

data class StaffSlotInfo(
    val staffId: String,
    val staffName: String,
    val isPrimary: Boolean
)

data class AvailableSlotsResponse(
    val date: LocalDate,
    val serviceId: String,
    val serviceName: String,
    val durationMinutes: Int,
    val slots: List<TimeSlot>
)

// ========== BOOKING DTOs ==========

data class CreateBookingRequest(
    @field:NotNull(message = "Service ID is required")
    val serviceId: UUID,

    @field:NotNull(message = "Staff ID is required")
    val staffId: UUID,

    @field:NotNull(message = "Booking date is required")
    @field:FutureOrPresent(message = "Date must be today or in the future")
    val bookingDate: LocalDate,

    @field:NotNull(message = "Start time is required")
    val startTime: LocalTime,

    val addonIds: List<UUID> = emptyList(),
    val customerNotes: String? = null,

    @field:NotNull(message = "Payment method is required")
    val paymentMethod: String // Will integrate with existing payment system
)

data class BookingResponse(
    val id: String,
    val bookingNumber: String,
    val customerId: String,
    val customerName: String,
    val service: ServiceSummaryResponse,
    val staff: StaffInfo,
    val bookingDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val durationMinutes: Int,
    val status: BookingStatus,
    val servicePrice: BigDecimal,
    val addonsPrice: BigDecimal,
    val totalAmount: BigDecimal,
    val addons: List<BookingAddonResponse>,
    val paymentStatus: String,
    val isPaid: Boolean,
    val canCancel: Boolean,
    val canReschedule: Boolean,
    val customerNotes: String?,
    val staffNotes: String?,
    val bookedAt: LocalDateTime,
    val confirmedAt: LocalDateTime?,
    val completedAt: LocalDateTime?
)

data class BookingListResponse(
    val content: List<BookingSummaryResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)

data class BookingSummaryResponse(
    val id: String,
    val bookingNumber: String,
    val serviceName: String,
    val staffName: String,
    val bookingDate: LocalDate,
    val startTime: LocalTime,
    val status: BookingStatus,
    val totalAmount: BigDecimal,
    val isPaid: Boolean
)

data class BookingAddonResponse(
    val id: String,
    val name: String,
    val price: BigDecimal,
    val durationMinutes: Int
)

data class StaffInfo(
    val id: String,
    val name: String,
    val imageUrl: String?
)

data class RescheduleBookingRequest(
    @field:NotNull(message = "New date is required")
    @field:FutureOrPresent
    val newDate: LocalDate,

    @field:NotNull(message = "New start time is required")
    val newStartTime: LocalTime,

    val reason: String? = null
)

data class CancelBookingRequest(
    val reason: String? = null
)

data class UpdateBookingStatusRequest(
    @field:NotNull(message = "Status is required")
    val status: BookingStatus,

    val staffNotes: String? = null
)

// ========== STAFF SCHEDULE DTOs ==========

data class StaffScheduleResponse(
    val staffId: String,
    val staffName: String,
    val date: LocalDate,
    val availability: StaffAvailabilityResponse?,
    val breaks: List<StaffBreakResponse>,
    val bookings: List<BookingSummaryResponse>,
    val blockedSlots: List<BlockedSlotResponse>
)

data class StaffBreakResponse(
    val id: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val breakName: String?
)

data class BlockedSlotResponse(
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val reason: String?,
    val isAllDay: Boolean
)