package com.emjay.backend.services.infrastructure.persistence.repository

import com.emjay.backend.services.domain.entity.BookingStatus
import com.emjay.backend.services.domain.entity.DayOfWeek
import com.emjay.backend.services.domain.entity.ServiceStatus
import com.emjay.backend.services.infrastructure.persistence.entity.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

// ========== SERVICE CATEGORY ==========

@Repository
interface JpaServiceCategoryRepository : JpaRepository<ServiceCategoryEntity, UUID> {
    fun findBySlug(slug: String): ServiceCategoryEntity?
    fun findByIsActiveTrue(): List<ServiceCategoryEntity>
    fun existsBySlug(slug: String): Boolean
}

// ========== SERVICE SUBCATEGORY ==========

@Repository
interface JpaServiceSubcategoryRepository : JpaRepository<ServiceSubcategoryEntity, UUID> {
    fun findBySlug(slug: String): ServiceSubcategoryEntity?
    fun findByCategoryId(categoryId: UUID): List<ServiceSubcategoryEntity>

    @Query("SELECT s FROM ServiceSubcategoryEntity s WHERE s.categoryId = :categoryId AND s.isActive = true")
    fun findByCategoryIdAndIsActiveTrue(@Param("categoryId") categoryId: UUID): List<ServiceSubcategoryEntity>
}

// ========== SERVICE ==========

@Repository
interface JpaServiceRepository : JpaRepository<ServiceEntity, UUID> {
    fun findBySlug(slug: String): ServiceEntity?
    fun findByStatus(status: ServiceStatus, pageable: Pageable): Page<ServiceEntity>
    fun findByCategoryId(categoryId: UUID, pageable: Pageable): Page<ServiceEntity>
    fun findBySubcategoryId(subcategoryId: UUID, pageable: Pageable): Page<ServiceEntity>
    fun findByIsFeaturedTrue(): List<ServiceEntity>

    @Query("SELECT s FROM ServiceEntity s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun searchByName(@Param("query") query: String, pageable: Pageable): Page<ServiceEntity>
}

// ========== SERVICE IMAGE ==========

@Repository
interface JpaServiceImageRepository : JpaRepository<ServiceImageEntity, UUID> {
    fun findByServiceId(serviceId: UUID): List<ServiceImageEntity>

    @Query("SELECT i FROM ServiceImageEntity i WHERE i.serviceId = :serviceId AND i.isPrimary = true")
    fun findPrimaryByServiceId(@Param("serviceId") serviceId: UUID): ServiceImageEntity?

    fun deleteByServiceId(serviceId: UUID)
}

// ========== SERVICE ADDON ==========

@Repository
interface JpaServiceAddonRepository : JpaRepository<ServiceAddonEntity, UUID> {
    fun findByServiceId(serviceId: UUID): List<ServiceAddonEntity>

    @Query("SELECT a FROM ServiceAddonEntity a WHERE a.serviceId = :serviceId AND a.isActive = true")
    fun findByServiceIdAndIsActiveTrue(@Param("serviceId") serviceId: UUID): List<ServiceAddonEntity>
}

// ========== SERVICE STAFF ==========

@Repository
interface JpaServiceStaffRepository : JpaRepository<ServiceStaffEntity, UUID> {
    fun findByServiceId(serviceId: UUID): List<ServiceStaffEntity>
    fun findByStaffId(staffId: UUID): List<ServiceStaffEntity>

    @Query("SELECT s FROM ServiceStaffEntity s WHERE s.serviceId = :serviceId AND s.staffId = :staffId")
    fun findByServiceIdAndStaffId(
        @Param("serviceId") serviceId: UUID,
        @Param("staffId") staffId: UUID
    ): ServiceStaffEntity?

    fun existsByServiceIdAndStaffId(serviceId: UUID, staffId: UUID): Boolean
}

// ========== STAFF AVAILABILITY ==========

@Repository
interface JpaStaffAvailabilityRepository : JpaRepository<StaffAvailabilityEntity, UUID> {
    fun findByStaffId(staffId: UUID): List<StaffAvailabilityEntity>
    fun findByStaffIdAndDayOfWeek(staffId: UUID, dayOfWeek: DayOfWeek): List<StaffAvailabilityEntity>

    @Query("SELECT a FROM StaffAvailabilityEntity a WHERE a.staffId = :staffId AND a.isActive = true")
    fun findByStaffIdAndIsActiveTrue(@Param("staffId") staffId: UUID): List<StaffAvailabilityEntity>
}

// ========== STAFF BREAK ==========

@Repository
interface JpaStaffBreakRepository : JpaRepository<StaffBreakEntity, UUID> {
    fun findByStaffId(staffId: UUID): List<StaffBreakEntity>
    fun findByStaffIdAndDayOfWeek(staffId: UUID, dayOfWeek: DayOfWeek): List<StaffBreakEntity>
}

// ========== BLOCKED DATE ==========

@Repository
interface JpaBlockedDateRepository : JpaRepository<BlockedDateEntity, UUID> {
    fun findByBlockedDate(date: LocalDate): List<BlockedDateEntity>

    @Query("SELECT b FROM BlockedDateEntity b WHERE b.staffId = :staffId AND b.blockedDate = :date")
    fun findByStaffIdAndBlockedDate(
        @Param("staffId") staffId: UUID,
        @Param("date") date: LocalDate
    ): List<BlockedDateEntity>

    @Query("SELECT b FROM BlockedDateEntity b WHERE b.staffId IS NULL AND b.blockedDate = :date")
    fun findBusinessClosuresByDate(@Param("date") date: LocalDate): List<BlockedDateEntity>

    @Query("""
        SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
        FROM BlockedDateEntity b
        WHERE (b.staffId = :staffId OR b.staffId IS NULL)
        AND b.blockedDate = :date
    """)
    fun existsByStaffIdAndDate(
        @Param("staffId") staffId: UUID?,
        @Param("date") date: LocalDate
    ): Boolean
}

// ========== BOOKING ==========

@Repository
interface JpaBookingRepository : JpaRepository<BookingEntity, UUID> {
    fun findByBookingNumber(bookingNumber: String): BookingEntity?
    fun findByCustomerId(customerId: UUID, pageable: Pageable): Page<BookingEntity>
    fun findByStaffId(staffId: UUID, pageable: Pageable): Page<BookingEntity>
    fun findByServiceId(serviceId: UUID, pageable: Pageable): Page<BookingEntity>
    fun findByStatus(status: BookingStatus, pageable: Pageable): Page<BookingEntity>

    @Query("SELECT b FROM BookingEntity b WHERE b.bookingDate BETWEEN :startDate AND :endDate")
    fun findByBookingDateBetween(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
        pageable: Pageable
    ): Page<BookingEntity>

    @Query("SELECT b FROM BookingEntity b WHERE b.staffId = :staffId AND b.bookingDate = :date")
    fun findByStaffIdAndBookingDate(
        @Param("staffId") staffId: UUID,
        @Param("date") date: LocalDate
    ): List<BookingEntity>

    @Query("""
        SELECT b FROM BookingEntity b 
        WHERE b.customerId = :customerId 
        AND b.status IN ('PENDING', 'CONFIRMED')
        AND (b.bookingDate > CURRENT_DATE OR (b.bookingDate = CURRENT_DATE AND b.startTime > CURRENT_TIME))
        ORDER BY b.bookingDate, b.startTime
    """)
    fun findUpcomingByCustomerId(@Param("customerId") customerId: UUID): List<BookingEntity>

    @Query("""
        SELECT b FROM BookingEntity b 
        WHERE b.customerId = :customerId 
        AND (b.bookingDate < CURRENT_DATE OR (b.bookingDate = CURRENT_DATE AND b.endTime < CURRENT_TIME))
        ORDER BY b.bookingDate DESC, b.startTime DESC
    """)
    fun findPastByCustomerId(
        @Param("customerId") customerId: UUID,
        pageable: Pageable
    ): Page<BookingEntity>

    @Query("""
        SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
        FROM BookingEntity b
        WHERE b.staffId = :staffId
        AND b.bookingDate = :date
        AND b.status NOT IN ('CANCELLED', 'NO_SHOW')
        AND (
            (:startTime >= b.startTime AND :startTime < b.endTime) OR
            (:endTime > b.startTime AND :endTime <= b.endTime) OR
            (:startTime <= b.startTime AND :endTime >= b.endTime)
        )
        AND (:excludeId IS NULL OR b.id != :excludeId)
    """)
    fun existsConflict(
        @Param("staffId") staffId: UUID,
        @Param("date") date: LocalDate,
        @Param("startTime") startTime: LocalTime,
        @Param("endTime") endTime: LocalTime,
        @Param("excludeId") excludeId: UUID?
    ): Boolean

    fun countByStatus(status: BookingStatus): Long
}

// ========== BOOKING ADDON ==========

@Repository
interface JpaBookingAddonRepository : JpaRepository<BookingAddonEntity, UUID> {
    fun findByBookingId(bookingId: UUID): List<BookingAddonEntity>
}

// ========== BOOKING STATUS HISTORY ==========

@Repository
interface JpaBookingStatusHistoryRepository : JpaRepository<BookingStatusHistoryEntity, UUID> {
    fun findByBookingId(bookingId: UUID): List<BookingStatusHistoryEntity>
}

// ========== BOOKING REMINDER ==========

@Repository
interface JpaBookingReminderRepository : JpaRepository<BookingReminderEntity, UUID> {
    fun findByBookingId(bookingId: UUID): List<BookingReminderEntity>

    @Query("""
        SELECT r FROM BookingReminderEntity r 
        WHERE r.status = 'PENDING' 
        AND r.scheduledFor <= :before
    """)
    fun findPendingBeforeTime(@Param("before") before: LocalDateTime): List<BookingReminderEntity>
}