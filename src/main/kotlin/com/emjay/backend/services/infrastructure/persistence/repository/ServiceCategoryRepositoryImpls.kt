package com.emjay.backend.services.infrastructure.persistence.repository

import com.emjay.backend.services.domain.entity.*
import com.emjay.backend.services.domain.repository.*
import com.emjay.backend.services.infrastructure.persistence.entity.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Year
import java.util.*

// ========== SERVICE CATEGORY REPOSITORY IMPL ==========

@Repository
class ServiceCategoryRepositoryImpl(
    private val jpaRepository: JpaServiceCategoryRepository
) : ServiceCategoryRepository {

    override fun save(category: ServiceCategory): ServiceCategory {
        val entity = toEntity(category)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): ServiceCategory? =
        jpaRepository.findById(id).map { toDomain(it) }.orElse(null)

    override fun findBySlug(slug: String): ServiceCategory? =
        jpaRepository.findBySlug(slug)?.let { toDomain(it) }

    override fun findAll(pageable: Pageable): Page<ServiceCategory> =
        jpaRepository.findAll(pageable).map { toDomain(it) }

    override fun findAllActive(): List<ServiceCategory> =
        jpaRepository.findByIsActiveTrue().map { toDomain(it) }

    override fun existsBySlug(slug: String): Boolean = jpaRepository.existsBySlug(slug)

    override fun delete(category: ServiceCategory) {
        category.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: ServiceCategoryEntity) = ServiceCategory(
        id = entity.id,
        name = entity.name,
        slug = entity.slug,
        description = entity.description,
        imageUrl = entity.imageUrl,
        displayOrder = entity.displayOrder,
        isActive = entity.isActive,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    private fun toEntity(domain: ServiceCategory) = ServiceCategoryEntity(
        id = domain.id,
        name = domain.name,
        slug = domain.slug,
        description = domain.description,
        imageUrl = domain.imageUrl,
        displayOrder = domain.displayOrder,
        isActive = domain.isActive,
        createdAt = domain.createdAt ?: LocalDateTime.now(),
        updatedAt = domain.updatedAt ?: LocalDateTime.now()
    )
}

// ========== SERVICE REPOSITORY IMPL (Key Methods Only) ==========

@Repository
class ServiceRepositoryImpl(
    private val jpaRepository: JpaServiceRepository
) : ServiceRepository {

    override fun save(service: Service): Service {
        val entity = toServiceEntity(service)
        val saved = jpaRepository.save(entity)
        return toServiceDomain(saved)
    }

    override fun findById(id: UUID): Service? =
        jpaRepository.findById(id).map { toServiceDomain(it) }.orElse(null)

    override fun findBySlug(slug: String): Service? =
        jpaRepository.findBySlug(slug)?.let { toServiceDomain(it) }

    override fun findAll(pageable: Pageable): Page<Service> =
        jpaRepository.findAll(pageable).map { toServiceDomain(it) }

    override fun findByStatus(status: ServiceStatus, pageable: Pageable): Page<Service> =
        jpaRepository.findByStatus(status, pageable).map { toServiceDomain(it) }

    override fun findByCategoryId(categoryId: UUID, pageable: Pageable): Page<Service> =
        jpaRepository.findByCategoryId(categoryId, pageable).map { toServiceDomain(it) }

    override fun findBySubcategoryId(subcategoryId: UUID, pageable: Pageable): Page<Service> =
        jpaRepository.findBySubcategoryId(subcategoryId, pageable).map { toServiceDomain(it) }

    override fun findFeatured(): List<Service> =
        jpaRepository.findByIsFeaturedTrue().map { toServiceDomain(it) }

    override fun searchByName(query: String, pageable: Pageable): Page<Service> =
        jpaRepository.searchByName(query, pageable).map { toServiceDomain(it) }

    override fun delete(service: Service) {
        service.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toServiceDomain(entity: ServiceEntity) = Service(
        id = entity.id,
        categoryId = entity.categoryId,
        subcategoryId = entity.subcategoryId,
        name = entity.name,
        slug = entity.slug,
        description = entity.description,
        shortDescription = entity.shortDescription,
        basePrice = entity.basePrice,
        discountedPrice = entity.discountedPrice,
        durationMinutes = entity.durationMinutes,
        bufferTimeMinutes = entity.bufferTimeMinutes,
        skillLevel = entity.skillLevel,
        maxClientsPerSlot = entity.maxClientsPerSlot,
        status = entity.status,
        isFeatured = entity.isFeatured,
        requiresConsultation = entity.requiresConsultation,
        metaTitle = entity.metaTitle,
        metaDescription = entity.metaDescription,
        metaKeywords = entity.metaKeywords,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    private fun toServiceEntity(domain: Service) = ServiceEntity(
        id = domain.id,
        categoryId = domain.categoryId,
        subcategoryId = domain.subcategoryId,
        name = domain.name,
        slug = domain.slug,
        description = domain.description,
        shortDescription = domain.shortDescription,
        basePrice = domain.basePrice,
        discountedPrice = domain.discountedPrice,
        durationMinutes = domain.durationMinutes,
        bufferTimeMinutes = domain.bufferTimeMinutes,
        skillLevel = domain.skillLevel,
        maxClientsPerSlot = domain.maxClientsPerSlot,
        status = domain.status,
        isFeatured = domain.isFeatured,
        requiresConsultation = domain.requiresConsultation,
        metaTitle = domain.metaTitle,
        metaDescription = domain.metaDescription,
        metaKeywords = domain.metaKeywords,
        createdAt = domain.createdAt ?: LocalDateTime.now(),
        updatedAt = domain.updatedAt ?: LocalDateTime.now()
    )
}

// ========== BOOKING REPOSITORY IMPL ==========

@Repository
class BookingRepositoryImpl(
    private val jpaRepository: JpaBookingRepository,
    private val jdbcTemplate: JdbcTemplate
) : BookingRepository {

    override fun save(booking: Booking): Booking {
        val entity = toBookingEntity(booking)
        val saved = jpaRepository.save(entity)
        return toBookingDomain(saved)
    }

    override fun findById(id: UUID): Booking? =
        jpaRepository.findById(id).map { toBookingDomain(it) }.orElse(null)

    override fun findByBookingNumber(bookingNumber: String): Booking? =
        jpaRepository.findByBookingNumber(bookingNumber)?.let { toBookingDomain(it) }

    override fun findByCustomerId(customerId: UUID, pageable: Pageable): Page<Booking> =
        jpaRepository.findByCustomerId(customerId, pageable).map { toBookingDomain(it) }

    override fun findByStaffId(staffId: UUID, pageable: Pageable): Page<Booking> =
        jpaRepository.findByStaffId(staffId, pageable).map { toBookingDomain(it) }

    override fun findByServiceId(serviceId: UUID, pageable: Pageable): Page<Booking> =
        jpaRepository.findByServiceId(serviceId, pageable).map { toBookingDomain(it) }

    override fun findByStatus(status: BookingStatus, pageable: Pageable): Page<Booking> =
        jpaRepository.findByStatus(status, pageable).map { toBookingDomain(it) }

    override fun findByDateRange(startDate: LocalDate, endDate: LocalDate, pageable: Pageable): Page<Booking> =
        jpaRepository.findByBookingDateBetween(startDate, endDate, pageable).map { toBookingDomain(it) }

    override fun findByStaffAndDate(staffId: UUID, date: LocalDate): List<Booking> =
        jpaRepository.findByStaffIdAndBookingDate(staffId, date).map { toBookingDomain(it) }

    override fun findUpcomingByCustomer(customerId: UUID): List<Booking> =
        jpaRepository.findUpcomingByCustomerId(customerId).map { toBookingDomain(it) }

    override fun findPastByCustomer(customerId: UUID, pageable: Pageable): Page<Booking> =
        jpaRepository.findPastByCustomerId(customerId, pageable).map { toBookingDomain(it) }

    override fun existsConflict(
        staffId: UUID,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        excludeBookingId: UUID?
    ): Boolean = jpaRepository.existsConflict(staffId, date, startTime, endTime, excludeBookingId)

    override fun generateBookingNumber(): String {
        val year = Year.now().value
        val sequence = jdbcTemplate.queryForObject(
            "SELECT nextval('booking_number_seq')",
            Long::class.java
        ) ?: 1L
        return String.format("BK-%d-%05d", year, sequence)
    }

    override fun countByStatus(status: BookingStatus): Long =
        jpaRepository.countByStatus(status)

    private fun toBookingDomain(entity: BookingEntity) = Booking(
        id = entity.id,
        bookingNumber = entity.bookingNumber,
        customerId = entity.customerId,
        serviceId = entity.serviceId,
        staffId = entity.staffId,
        bookingDate = entity.bookingDate,
        startTime = entity.startTime,
        endTime = entity.endTime,
        durationMinutes = entity.durationMinutes,
        status = entity.status,
        servicePrice = entity.servicePrice,
        addonsPrice = entity.addonsPrice,
        totalAmount = entity.totalAmount,
        paymentStatus = entity.paymentStatus,
        paidAt = entity.paidAt,
        customerNotes = entity.customerNotes,
        staffNotes = entity.staffNotes,
        cancellationReason = entity.cancellationReason,
        bookedAt = entity.bookedAt,
        confirmedAt = entity.confirmedAt,
        startedAt = entity.startedAt,
        completedAt = entity.completedAt,
        cancelledAt = entity.cancelledAt,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    private fun toBookingEntity(domain: Booking) = BookingEntity(
        id = domain.id,
        bookingNumber = domain.bookingNumber,
        customerId = domain.customerId,
        serviceId = domain.serviceId,
        staffId = domain.staffId,
        bookingDate = domain.bookingDate,
        startTime = domain.startTime,
        endTime = domain.endTime,
        durationMinutes = domain.durationMinutes,
        status = domain.status,
        servicePrice = domain.servicePrice,
        addonsPrice = domain.addonsPrice,
        totalAmount = domain.totalAmount,
        paymentStatus = domain.paymentStatus,
        paidAt = domain.paidAt,
        customerNotes = domain.customerNotes,
        staffNotes = domain.staffNotes,
        cancellationReason = domain.cancellationReason,
        bookedAt = domain.bookedAt,
        confirmedAt = domain.confirmedAt,
        startedAt = domain.startedAt,
        completedAt = domain.completedAt,
        cancelledAt = domain.cancelledAt,
        createdAt = domain.createdAt ?: LocalDateTime.now(),
        updatedAt = domain.updatedAt ?: LocalDateTime.now()
    )
}


// ========== SERVICE SUBCATEGORY REPOSITORY ==========

@Repository
class ServiceSubcategoryRepositoryImpl(
    private val jpaRepository: JpaServiceSubcategoryRepository
) : ServiceSubcategoryRepository {

    override fun save(subcategory: ServiceSubcategory): ServiceSubcategory {
        val entity = toEntity(subcategory)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): ServiceSubcategory? =
        jpaRepository.findById(id).map { toDomain(it) }.orElse(null)

    override fun findBySlug(slug: String): ServiceSubcategory? =
        jpaRepository.findBySlug(slug)?.let { toDomain(it) }

    override fun findByCategoryId(categoryId: UUID): List<ServiceSubcategory> =
        jpaRepository.findByCategoryId(categoryId).map { toDomain(it) }

    override fun findByCategoryIdAndActive(categoryId: UUID): List<ServiceSubcategory> =
        jpaRepository.findByCategoryIdAndIsActiveTrue(categoryId).map { toDomain(it) }

    override fun delete(subcategory: ServiceSubcategory) {
        subcategory.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: ServiceSubcategoryEntity) = ServiceSubcategory(
        id = entity.id,
        categoryId = entity.categoryId,
        name = entity.name,
        slug = entity.slug,
        description = entity.description,
        displayOrder = entity.displayOrder,
        isActive = entity.isActive,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    private fun toEntity(domain: ServiceSubcategory) = ServiceSubcategoryEntity(
        id = domain.id,
        categoryId = domain.categoryId,
        name = domain.name,
        slug = domain.slug,
        description = domain.description,
        displayOrder = domain.displayOrder,
        isActive = domain.isActive,
        createdAt = domain.createdAt ?: LocalDateTime.now(),
        updatedAt = domain.updatedAt ?: LocalDateTime.now()
    )
}

// ========== SERVICE IMAGE REPOSITORY ==========

@Repository
class ServiceImageRepositoryImpl(
    private val jpaRepository: JpaServiceImageRepository
) : ServiceImageRepository {

    override fun save(image: ServiceImage): ServiceImage {
        val entity = toEntity(image)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun saveAll(images: List<ServiceImage>): List<ServiceImage> {
        val entities = images.map { toEntity(it) }
        val saved = jpaRepository.saveAll(entities)
        return saved.map { toDomain(it) }
    }

    override fun findById(id: UUID): ServiceImage? =
        jpaRepository.findById(id).map { toDomain(it) }.orElse(null)

    override fun findByServiceId(serviceId: UUID): List<ServiceImage> =
        jpaRepository.findByServiceId(serviceId).map { toDomain(it) }

    override fun findPrimaryImage(serviceId: UUID): ServiceImage? =
        jpaRepository.findPrimaryByServiceId(serviceId)?.let { toDomain(it) }

    override fun delete(image: ServiceImage) {
        image.id?.let { jpaRepository.deleteById(it) }
    }

    override fun deleteByServiceId(serviceId: UUID) {
        jpaRepository.deleteByServiceId(serviceId)
    }

    private fun toDomain(entity: ServiceImageEntity) = ServiceImage(
        id = entity.id,
        serviceId = entity.serviceId,
        imageUrl = entity.imageUrl,
        altText = entity.altText,
        displayOrder = entity.displayOrder,
        isPrimary = entity.isPrimary,
        createdAt = entity.createdAt
    )

    private fun toEntity(domain: ServiceImage) = ServiceImageEntity(
        id = domain.id,
        serviceId = domain.serviceId,
        imageUrl = domain.imageUrl,
        altText = domain.altText,
        displayOrder = domain.displayOrder,
        isPrimary = domain.isPrimary,
        createdAt = domain.createdAt ?: LocalDateTime.now()
    )
}

// ========== SERVICE ADDON REPOSITORY ==========

@Repository
class ServiceAddonRepositoryImpl(
    private val jpaRepository: JpaServiceAddonRepository
) : ServiceAddonRepository {

    override fun save(addon: ServiceAddon): ServiceAddon {
        val entity = toEntity(addon)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): ServiceAddon? =
        jpaRepository.findById(id).map { toDomain(it) }.orElse(null)

    override fun findByServiceId(serviceId: UUID): List<ServiceAddon> =
        jpaRepository.findByServiceId(serviceId).map { toDomain(it) }

    override fun findActiveByServiceId(serviceId: UUID): List<ServiceAddon> =
        jpaRepository.findByServiceIdAndIsActiveTrue(serviceId).map { toDomain(it) }

    override fun delete(addon: ServiceAddon) {
        addon.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: ServiceAddonEntity) = ServiceAddon(
        id = entity.id,
        serviceId = entity.serviceId,
        name = entity.name,
        description = entity.description,
        price = entity.price,
        durationMinutes = entity.durationMinutes,
        isActive = entity.isActive,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    private fun toEntity(domain: ServiceAddon) = ServiceAddonEntity(
        id = domain.id,
        serviceId = domain.serviceId,
        name = domain.name,
        description = domain.description,
        price = domain.price,
        durationMinutes = domain.durationMinutes,
        isActive = domain.isActive,
        createdAt = domain.createdAt ?: LocalDateTime.now(),
        updatedAt = domain.updatedAt ?: LocalDateTime.now()
    )
}

// ========== SERVICE STAFF REPOSITORY ==========

@Repository
class ServiceStaffRepositoryImpl(
    private val jpaRepository: JpaServiceStaffRepository
) : ServiceStaffRepository {

    override fun save(assignment: ServiceStaff): ServiceStaff {
        val entity = toEntity(assignment)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): ServiceStaff? =
        jpaRepository.findById(id).map { toDomain(it) }.orElse(null)

    override fun findByServiceId(serviceId: UUID): List<ServiceStaff> =
        jpaRepository.findByServiceId(serviceId).map { toDomain(it) }

    override fun findByStaffId(staffId: UUID): List<ServiceStaff> =
        jpaRepository.findByStaffId(staffId).map { toDomain(it) }

    override fun findByServiceAndStaff(serviceId: UUID, staffId: UUID): ServiceStaff? =
        jpaRepository.findByServiceIdAndStaffId(serviceId, staffId)?.let { toDomain(it) }

    override fun existsByServiceAndStaff(serviceId: UUID, staffId: UUID): Boolean =
        jpaRepository.existsByServiceIdAndStaffId(serviceId, staffId)

    override fun delete(assignment: ServiceStaff) {
        assignment.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: ServiceStaffEntity) = ServiceStaff(
        id = entity.id,
        serviceId = entity.serviceId,
        staffId = entity.staffId,
        isPrimary = entity.isPrimary,
        assignedAt = entity.assignedAt
    )

    private fun toEntity(domain: ServiceStaff) = ServiceStaffEntity(
        id = domain.id,
        serviceId = domain.serviceId,
        staffId = domain.staffId,
        isPrimary = domain.isPrimary,
        assignedAt = domain.assignedAt ?: LocalDateTime.now()
    )
}

// ========== STAFF AVAILABILITY REPOSITORY ==========

@Repository
class StaffAvailabilityRepositoryImpl(
    private val jpaRepository: JpaStaffAvailabilityRepository
) : StaffAvailabilityRepository {

    override fun save(availability: StaffAvailability): StaffAvailability {
        val entity = toEntity(availability)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): StaffAvailability? =
        jpaRepository.findById(id).map { toDomain(it) }.orElse(null)

    override fun findByStaffId(staffId: UUID): List<StaffAvailability> =
        jpaRepository.findByStaffId(staffId).map { toDomain(it) }

    override fun findByStaffIdAndDay(staffId: UUID, day: DayOfWeek): List<StaffAvailability> =
        jpaRepository.findByStaffIdAndDayOfWeek(staffId, day).map { toDomain(it) }

    override fun findActiveByStaffId(staffId: UUID): List<StaffAvailability> =
        jpaRepository.findByStaffIdAndIsActiveTrue(staffId).map { toDomain(it) }

    override fun delete(availability: StaffAvailability) {
        availability.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: StaffAvailabilityEntity) = StaffAvailability(
        id = entity.id,
        staffId = entity.staffId,
        dayOfWeek = entity.dayOfWeek,
        startTime = entity.startTime,
        endTime = entity.endTime,
        isActive = entity.isActive,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    private fun toEntity(domain: StaffAvailability) = StaffAvailabilityEntity(
        id = domain.id,
        staffId = domain.staffId,
        dayOfWeek = domain.dayOfWeek,
        startTime = domain.startTime,
        endTime = domain.endTime,
        isActive = domain.isActive,
        createdAt = domain.createdAt ?: LocalDateTime.now(),
        updatedAt = domain.updatedAt ?: LocalDateTime.now()
    )
}

// ========== STAFF BREAK REPOSITORY ==========

@Repository
class StaffBreakRepositoryImpl(
    private val jpaRepository: JpaStaffBreakRepository
) : StaffBreakRepository {

    override fun save(staffBreak: StaffBreak): StaffBreak {
        val entity = toEntity(staffBreak)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): StaffBreak? =
        jpaRepository.findById(id).map { toDomain(it) }.orElse(null)

    override fun findByStaffId(staffId: UUID): List<StaffBreak> =
        jpaRepository.findByStaffId(staffId).map { toDomain(it) }

    override fun findByStaffIdAndDay(staffId: UUID, day: DayOfWeek): List<StaffBreak> =
        jpaRepository.findByStaffIdAndDayOfWeek(staffId, day).map { toDomain(it) }

    override fun delete(staffBreak: StaffBreak) {
        staffBreak.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: StaffBreakEntity) = StaffBreak(
        id = entity.id,
        staffId = entity.staffId,
        dayOfWeek = entity.dayOfWeek,
        startTime = entity.startTime,
        endTime = entity.endTime,
        breakName = entity.breakName,
        isActive = entity.isActive,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    private fun toEntity(domain: StaffBreak) = StaffBreakEntity(
        id = domain.id,
        staffId = domain.staffId,
        dayOfWeek = domain.dayOfWeek,
        startTime = domain.startTime,
        endTime = domain.endTime,
        breakName = domain.breakName,
        isActive = domain.isActive,
        createdAt = domain.createdAt ?: LocalDateTime.now(),
        updatedAt = domain.updatedAt ?: LocalDateTime.now()
    )
}

// ========== BLOCKED DATE REPOSITORY ==========

@Repository
class BlockedDateRepositoryImpl(
    private val jpaRepository: JpaBlockedDateRepository
) : BlockedDateRepository {

    override fun save(blockedDate: BlockedDate): BlockedDate {
        val entity = toEntity(blockedDate)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): BlockedDate? =
        jpaRepository.findById(id).map { toDomain(it) }.orElse(null)

    override fun findByDate(date: LocalDate): List<BlockedDate> =
        jpaRepository.findByBlockedDate(date).map { toDomain(it) }

    override fun findByStaffIdAndDate(staffId: UUID, date: LocalDate): List<BlockedDate> =
        jpaRepository.findByStaffIdAndBlockedDate(staffId, date).map { toDomain(it) }

    override fun findBusinessClosures(date: LocalDate): List<BlockedDate> =
        jpaRepository.findBusinessClosuresByDate(date).map { toDomain(it) }

    override fun isDateBlocked(staffId: UUID?, date: LocalDate): Boolean =
        jpaRepository.existsByStaffIdAndDate(staffId, date)

    override fun delete(blockedDate: BlockedDate) {
        blockedDate.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: BlockedDateEntity) = BlockedDate(
        id = entity.id,
        staffId = entity.staffId,
        blockedDate = entity.blockedDate,
        startTime = entity.startTime,
        endTime = entity.endTime,
        reason = entity.reason,
        createdBy = entity.createdBy,
        createdAt = entity.createdAt
    )

    private fun toEntity(domain: BlockedDate) = BlockedDateEntity(
        id = domain.id,
        staffId = domain.staffId,
        blockedDate = domain.blockedDate,
        startTime = domain.startTime,
        endTime = domain.endTime,
        reason = domain.reason,
        createdBy = domain.createdBy,
        createdAt = domain.createdAt ?: LocalDateTime.now()
    )
}

// ========== BOOKING ADDON REPOSITORY ==========

@Repository
class BookingAddonRepositoryImpl(
    private val jpaRepository: JpaBookingAddonRepository
) : BookingAddonRepository {

    override fun save(addon: BookingAddon): BookingAddon {
        val entity = toEntity(addon)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun saveAll(addons: List<BookingAddon>): List<BookingAddon> {
        val entities = addons.map { toEntity(it) }
        val saved = jpaRepository.saveAll(entities)
        return saved.map { toDomain(it) }
    }

    override fun findById(id: UUID): BookingAddon? =
        jpaRepository.findById(id).map { toDomain(it) }.orElse(null)

    override fun findByBookingId(bookingId: UUID): List<BookingAddon> =
        jpaRepository.findByBookingId(bookingId).map { toDomain(it) }

    override fun delete(addon: BookingAddon) {
        addon.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: BookingAddonEntity) = BookingAddon(
        id = entity.id,
        bookingId = entity.bookingId,
        addonId = entity.addonId,
        addonName = entity.addonName,
        price = entity.price,
        durationMinutes = entity.durationMinutes,
        createdAt = entity.createdAt
    )

    private fun toEntity(domain: BookingAddon) = BookingAddonEntity(
        id = domain.id,
        bookingId = domain.bookingId,
        addonId = domain.addonId,
        addonName = domain.addonName,
        price = domain.price,
        durationMinutes = domain.durationMinutes,
        createdAt = domain.createdAt ?: LocalDateTime.now()
    )
}

// ========== BOOKING STATUS HISTORY REPOSITORY ==========

@Repository
class BookingStatusHistoryRepositoryImpl(
    private val jpaRepository: JpaBookingStatusHistoryRepository
) : BookingStatusHistoryRepository {

    override fun save(history: BookingStatusHistory): BookingStatusHistory {
        val entity = toEntity(history)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findByBookingId(bookingId: UUID): List<BookingStatusHistory> =
        jpaRepository.findByBookingId(bookingId).map { toDomain(it) }

    private fun toDomain(entity: BookingStatusHistoryEntity) = BookingStatusHistory(
        id = entity.id,
        bookingId = entity.bookingId,
        fromStatus = entity.fromStatus,
        toStatus = entity.toStatus,
        changedBy = entity.changedBy,
        reason = entity.reason,
        changedAt = entity.changedAt
    )

    private fun toEntity(domain: BookingStatusHistory) = BookingStatusHistoryEntity(
        id = domain.id,
        bookingId = domain.bookingId,
        fromStatus = domain.fromStatus,
        toStatus = domain.toStatus,
        changedBy = domain.changedBy,
        reason = domain.reason,
        changedAt = domain.changedAt
    )
}

// ========== BOOKING REMINDER REPOSITORY ==========

@Repository
class BookingReminderRepositoryImpl(
    private val jpaRepository: JpaBookingReminderRepository
) : BookingReminderRepository {

    override fun save(reminder: BookingReminder): BookingReminder {
        val entity = toEntity(reminder)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): BookingReminder? =
        jpaRepository.findById(id).map { toDomain(it) }.orElse(null)

    override fun findByBookingId(bookingId: UUID): List<BookingReminder> =
        jpaRepository.findByBookingId(bookingId).map { toDomain(it) }

    override fun findPendingReminders(before: LocalDateTime): List<BookingReminder> =
        jpaRepository.findPendingBeforeTime(before).map { toDomain(it) }

    override fun delete(reminder: BookingReminder) {
        reminder.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: BookingReminderEntity) = BookingReminder(
        id = entity.id,
        bookingId = entity.bookingId,
        reminderType = entity.reminderType,
        scheduledFor = entity.scheduledFor,
        sentAt = entity.sentAt,
        status = entity.status,
        createdAt = entity.createdAt
    )

    private fun toEntity(domain: BookingReminder) = BookingReminderEntity(
        id = domain.id,
        bookingId = domain.bookingId,
        reminderType = domain.reminderType,
        scheduledFor = domain.scheduledFor,
        sentAt = domain.sentAt,
        status = domain.status,
        createdAt = domain.createdAt ?: LocalDateTime.now()
    )
}

// NOTE: Additional repository implementations (ServiceSubcategory, ServiceImage, ServiceAddon,
// ServiceStaff, StaffAvailability, StaffBreak, BlockedDate, BookingAddon, BookingStatusHistory,
// BookingReminder) follow the same pattern. They can be added as needed.