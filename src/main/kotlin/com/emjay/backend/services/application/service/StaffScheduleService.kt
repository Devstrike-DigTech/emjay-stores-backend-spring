package com.emjay.backend.services.application.service

import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.services.application.dto.*
import com.emjay.backend.services.domain.entity.*
import com.emjay.backend.services.domain.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Service
class StaffScheduleService(
    private val staffAvailabilityRepository: StaffAvailabilityRepository,
    private val staffBreakRepository: StaffBreakRepository,
    private val blockedDateRepository: BlockedDateRepository,
    private val bookingRepository: BookingRepository
) {

    /**
     * Set staff availability for a specific day
     */
    @Transactional
    fun setStaffAvailability(
        staffId: UUID,
        request: SetStaffAvailabilityRequest
    ): StaffAvailabilityResponse {
        // Validate times
        if (request.startTime >= request.endTime) {
            throw IllegalArgumentException("Start time must be before end time")
        }

        val availability = StaffAvailability(
            staffId = staffId,
            dayOfWeek = request.dayOfWeek,
            startTime = request.startTime,
            endTime = request.endTime
        )

        val saved = staffAvailabilityRepository.save(availability)
        return toAvailabilityResponse(saved)
    }

    /**
     * Get staff availability
     */
    fun getStaffAvailability(staffId: UUID): List<StaffAvailabilityResponse> {
        return staffAvailabilityRepository.findActiveByStaffId(staffId)
            .sortedBy { it.dayOfWeek }
            .map { toAvailabilityResponse(it) }
    }

    /**
     * Add staff break
     */
    @Transactional
    fun addStaffBreak(staffId: UUID, request: AddStaffBreakRequest): StaffBreakResponse {
        if (request.startTime >= request.endTime) {
            throw IllegalArgumentException("Start time must be before end time")
        }

        val staffBreak = StaffBreak(
            staffId = staffId,
            dayOfWeek = request.dayOfWeek,
            startTime = request.startTime,
            endTime = request.endTime,
            breakName = request.breakName
        )

        val saved = staffBreakRepository.save(staffBreak)
        return toBreakResponse(saved)
    }

    /**
     * Block a date for staff or entire business
     */
    @Transactional
    fun blockDate(request: BlockDateRequest, createdBy: UUID): String {
        val blockedDate = BlockedDate(
            staffId = request.staffId,
            blockedDate = request.blockedDate,
            startTime = request.startTime,
            endTime = request.endTime,
            reason = request.reason,
            createdBy = createdBy
        )

        val saved = blockedDateRepository.save(blockedDate)

        return if (saved.isBusinessClosure()) {
            "Business closed for entire day: ${request.blockedDate}"
        } else {
            "Staff unavailable on ${request.blockedDate}"
        }
    }

    /**
     * Get staff schedule for a specific date
     */
    fun getStaffSchedule(staffId: UUID, date: LocalDate): StaffScheduleResponse {
        val dayOfWeek = convertToDayOfWeek(date.dayOfWeek)

        // Get availability for the day
        val availability = staffAvailabilityRepository
            .findByStaffIdAndDay(staffId, dayOfWeek)
            .firstOrNull { it.isActive }

        // Get breaks
        val breaks = staffBreakRepository
            .findByStaffIdAndDay(staffId, dayOfWeek)
            .filter { it.isActive }
            .map { toBreakResponse(it) }

        // Get bookings
        val bookings = bookingRepository
            .findByStaffAndDate(staffId, date)
            .filter { it.status !in listOf(BookingStatus.CANCELLED, BookingStatus.NO_SHOW) }
            .map { toBookingSummary(it) }

        // Get blocked slots
        val blockedSlots = blockedDateRepository
            .findByStaffIdAndDate(staffId, date)
            .map { toBlockedSlotResponse(it) }

        return StaffScheduleResponse(
            staffId = staffId.toString(),
            staffName = "Staff Member", // TODO: Fetch from user service
            date = date,
            availability = availability?.let { toAvailabilityResponse(it) },
            breaks = breaks,
            bookings = bookings,
            blockedSlots = blockedSlots
        )
    }

    /**
     * Get schedule for multiple days
     */
    fun getStaffWeekSchedule(staffId: UUID, startDate: LocalDate): List<StaffScheduleResponse> {
        return (0..6).map { dayOffset ->
            val date = startDate.plusDays(dayOffset.toLong())
            getStaffSchedule(staffId, date)
        }
    }

    /**
     * Deactivate availability
     */
    @Transactional
    fun deactivateAvailability(availabilityId: UUID): String {
        val availability = staffAvailabilityRepository.findById(availabilityId)
            ?: throw ResourceNotFoundException("Availability not found")

        val updated = availability.copy(isActive = false)
        staffAvailabilityRepository.save(updated)

        return "Availability deactivated successfully"
    }

    /**
     * Delete break
     */
    @Transactional
    fun deleteBreak(breakId: UUID): String {
        val staffBreak = staffBreakRepository.findById(breakId)
            ?: throw ResourceNotFoundException("Break not found")

        staffBreakRepository.delete(staffBreak)
        return "Break deleted successfully"
    }

    // ========== HELPER METHODS ==========

    private fun toAvailabilityResponse(availability: StaffAvailability) = StaffAvailabilityResponse(
        id = availability.id.toString(),
        dayOfWeek = availability.dayOfWeek,
        startTime = availability.startTime,
        endTime = availability.endTime,
        isActive = availability.isActive
    )

    private fun toBreakResponse(staffBreak: StaffBreak) = StaffBreakResponse(
        id = staffBreak.id.toString(),
        startTime = staffBreak.startTime,
        endTime = staffBreak.endTime,
        breakName = staffBreak.breakName
    )

    private fun toBlockedSlotResponse(blocked: BlockedDate) = BlockedSlotResponse(
        startTime = blocked.startTime,
        endTime = blocked.endTime,
        reason = blocked.reason,
        isAllDay = blocked.isAllDay()
    )

    private fun toBookingSummary(booking: Booking) = BookingSummaryResponse(
        id = booking.id.toString(),
        bookingNumber = booking.bookingNumber,
        serviceName = "Service", // TODO: Fetch
        staffName = "Staff",
        bookingDate = booking.bookingDate,
        startTime = booking.startTime,
        status = booking.status,
        totalAmount = booking.totalAmount,
        isPaid = booking.isPaid()
    )

    private fun convertToDayOfWeek(javaDayOfWeek: java.time.DayOfWeek): DayOfWeek {
        return when (javaDayOfWeek) {
            java.time.DayOfWeek.MONDAY -> DayOfWeek.MONDAY
            java.time.DayOfWeek.TUESDAY -> DayOfWeek.TUESDAY
            java.time.DayOfWeek.WEDNESDAY -> DayOfWeek.WEDNESDAY
            java.time.DayOfWeek.THURSDAY -> DayOfWeek.THURSDAY
            java.time.DayOfWeek.FRIDAY -> DayOfWeek.FRIDAY
            java.time.DayOfWeek.SATURDAY -> DayOfWeek.SATURDAY
            java.time.DayOfWeek.SUNDAY -> DayOfWeek.SUNDAY
        }
    }
}