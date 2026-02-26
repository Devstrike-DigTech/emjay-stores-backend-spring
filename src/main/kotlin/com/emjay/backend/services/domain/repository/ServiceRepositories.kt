package com.emjay.backend.services.domain.repository

import com.emjay.backend.services.domain.entity.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

// ========== SERVICE CATEGORY REPOSITORY ==========

interface ServiceCategoryRepository {
    fun save(category: ServiceCategory): ServiceCategory
    fun findById(id: UUID): ServiceCategory?
    fun findBySlug(slug: String): ServiceCategory?
    fun findAll(pageable: Pageable): Page<ServiceCategory>
    fun findAllActive(): List<ServiceCategory>
    fun existsBySlug(slug: String): Boolean
    fun delete(category: ServiceCategory)
}

// ========== SERVICE SUBCATEGORY REPOSITORY ==========

interface ServiceSubcategoryRepository {
    fun save(subcategory: ServiceSubcategory): ServiceSubcategory
    fun findById(id: UUID): ServiceSubcategory?
    fun findBySlug(slug: String): ServiceSubcategory?
    fun findByCategoryId(categoryId: UUID): List<ServiceSubcategory>
    fun findByCategoryIdAndActive(categoryId: UUID): List<ServiceSubcategory>
    fun delete(subcategory: ServiceSubcategory)
}

// ========== SERVICE REPOSITORY ==========

interface ServiceRepository {
    fun save(service: Service): Service
    fun findById(id: UUID): Service?
    fun findBySlug(slug: String): Service?
    fun findAll(pageable: Pageable): Page<Service>
    fun findByStatus(status: ServiceStatus, pageable: Pageable): Page<Service>
    fun findByCategoryId(categoryId: UUID, pageable: Pageable): Page<Service>
    fun findBySubcategoryId(subcategoryId: UUID, pageable: Pageable): Page<Service>
    fun findFeatured(): List<Service>
    fun searchByName(query: String, pageable: Pageable): Page<Service>
    fun delete(service: Service)
}

// ========== SERVICE IMAGE REPOSITORY ==========

interface ServiceImageRepository {
    fun save(image: ServiceImage): ServiceImage
    fun saveAll(images: List<ServiceImage>): List<ServiceImage>
    fun findById(id: UUID): ServiceImage?
    fun findByServiceId(serviceId: UUID): List<ServiceImage>
    fun findPrimaryImage(serviceId: UUID): ServiceImage?
    fun delete(image: ServiceImage)
    fun deleteByServiceId(serviceId: UUID)
}

// ========== SERVICE ADDON REPOSITORY ==========

interface ServiceAddonRepository {
    fun save(addon: ServiceAddon): ServiceAddon
    fun findById(id: UUID): ServiceAddon?
    fun findByServiceId(serviceId: UUID): List<ServiceAddon>
    fun findActiveByServiceId(serviceId: UUID): List<ServiceAddon>
    fun delete(addon: ServiceAddon)
}

// ========== SERVICE STAFF REPOSITORY ==========

interface ServiceStaffRepository {
    fun save(assignment: ServiceStaff): ServiceStaff
    fun findById(id: UUID): ServiceStaff?
    fun findByServiceId(serviceId: UUID): List<ServiceStaff>
    fun findByStaffId(staffId: UUID): List<ServiceStaff>
    fun findByServiceAndStaff(serviceId: UUID, staffId: UUID): ServiceStaff?
    fun existsByServiceAndStaff(serviceId: UUID, staffId: UUID): Boolean
    fun delete(assignment: ServiceStaff)
}

// ========== STAFF AVAILABILITY REPOSITORY ==========

interface StaffAvailabilityRepository {
    fun save(availability: StaffAvailability): StaffAvailability
    fun findById(id: UUID): StaffAvailability?
    fun findByStaffId(staffId: UUID): List<StaffAvailability>
    fun findByStaffIdAndDay(staffId: UUID, day: DayOfWeek): List<StaffAvailability>
    fun findActiveByStaffId(staffId: UUID): List<StaffAvailability>
    fun delete(availability: StaffAvailability)
}

// ========== STAFF BREAK REPOSITORY ==========

interface StaffBreakRepository {
    fun save(staffBreak: StaffBreak): StaffBreak
    fun findById(id: UUID): StaffBreak?
    fun findByStaffId(staffId: UUID): List<StaffBreak>
    fun findByStaffIdAndDay(staffId: UUID, day: DayOfWeek): List<StaffBreak>
    fun delete(staffBreak: StaffBreak)
}

// ========== BLOCKED DATE REPOSITORY ==========

interface BlockedDateRepository {
    fun save(blockedDate: BlockedDate): BlockedDate
    fun findById(id: UUID): BlockedDate?
    fun findByDate(date: LocalDate): List<BlockedDate>
    fun findByStaffIdAndDate(staffId: UUID, date: LocalDate): List<BlockedDate>
    fun findBusinessClosures(date: LocalDate): List<BlockedDate>
    fun isDateBlocked(staffId: UUID?, date: LocalDate): Boolean
    fun delete(blockedDate: BlockedDate)
}

// ========== BOOKING REPOSITORY ==========

interface BookingRepository {
    fun save(booking: Booking): Booking
    fun findById(id: UUID): Booking?
    fun findByBookingNumber(bookingNumber: String): Booking?
    fun findByCustomerId(customerId: UUID, pageable: Pageable): Page<Booking>
    fun findByStaffId(staffId: UUID, pageable: Pageable): Page<Booking>
    fun findByServiceId(serviceId: UUID, pageable: Pageable): Page<Booking>
    fun findByStatus(status: BookingStatus, pageable: Pageable): Page<Booking>
    fun findByDateRange(startDate: LocalDate, endDate: LocalDate, pageable: Pageable): Page<Booking>
    fun findByStaffAndDate(staffId: UUID, date: LocalDate): List<Booking>
    fun findUpcomingByCustomer(customerId: UUID): List<Booking>
    fun findPastByCustomer(customerId: UUID, pageable: Pageable): Page<Booking>
    fun existsConflict(staffId: UUID, date: LocalDate, startTime: LocalTime, endTime: LocalTime, excludeBookingId: UUID?): Boolean
    fun generateBookingNumber(): String
    fun countByStatus(status: BookingStatus): Long
}

// ========== BOOKING ADDON REPOSITORY ==========

interface BookingAddonRepository {
    fun save(addon: BookingAddon): BookingAddon
    fun saveAll(addons: List<BookingAddon>): List<BookingAddon>
    fun findById(id: UUID): BookingAddon?
    fun findByBookingId(bookingId: UUID): List<BookingAddon>
    fun delete(addon: BookingAddon)
}

// ========== BOOKING STATUS HISTORY REPOSITORY ==========

interface BookingStatusHistoryRepository {
    fun save(history: BookingStatusHistory): BookingStatusHistory
    fun findByBookingId(bookingId: UUID): List<BookingStatusHistory>
}

// ========== BOOKING REMINDER REPOSITORY ==========

interface BookingReminderRepository {
    fun save(reminder: BookingReminder): BookingReminder
    fun findById(id: UUID): BookingReminder?
    fun findByBookingId(bookingId: UUID): List<BookingReminder>
    fun findPendingReminders(before: java.time.LocalDateTime): List<BookingReminder>
    fun delete(reminder: BookingReminder)
}