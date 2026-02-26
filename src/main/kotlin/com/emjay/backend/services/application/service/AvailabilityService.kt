package com.emjay.backend.services.application.service

import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.services.application.dto.*
import com.emjay.backend.services.domain.entity.*
import com.emjay.backend.services.domain.repository.*
import org.springframework.stereotype.Service
import java.time.DayOfWeek as JavaDayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

@Service
class AvailabilityService(
    private val serviceRepository: ServiceRepository,
    private val serviceStaffRepository: ServiceStaffRepository,
    private val staffAvailabilityRepository: StaffAvailabilityRepository,
    private val staffBreakRepository: StaffBreakRepository,
    private val blockedDateRepository: BlockedDateRepository,
    private val bookingRepository: BookingRepository
) {

    /**
     * Calculate available time slots for a service on a given date
     * This is the HEART of the booking system!
     */
    fun getAvailableSlots(request: GetAvailableSlotsRequest): AvailableSlotsResponse {
        // Step 1: Get service details
        val service = serviceRepository.findById(request.serviceId)
            ?: throw ResourceNotFoundException("Service not found")

        val totalDuration = service.durationMinutes + service.bufferTimeMinutes

        // Step 2: Get staff list (specific or all assigned)
        val staffList = if (request.staffId != null) {
            listOf(request.staffId)
        } else {
            serviceStaffRepository.findByServiceId(request.serviceId)
                .map { it.staffId }
        }

        if (staffList.isEmpty()) {
            return AvailableSlotsResponse(
                date = request.date,
                serviceId = request.serviceId.toString(),
                serviceName = service.name,
                durationMinutes = totalDuration,
                slots = emptyList()
            )
        }

        // Step 3: Convert date to day of week
        val dayOfWeek = convertToDayOfWeek(request.date.dayOfWeek)

        // Step 4: Calculate slots for each staff member
        val allSlots = mutableListOf<TimeSlot>()

        for (staffId in staffList) {
            // Check if date is blocked for this staff
            if (blockedDateRepository.isDateBlocked(staffId, request.date)) {
                continue
            }

            // Get staff working hours for this day
            val availabilityList = staffAvailabilityRepository
                .findByStaffIdAndDay(staffId, dayOfWeek)
                .filter { it.isActive }

            if (availabilityList.isEmpty()) continue

            for (availability in availabilityList) {
                // Get breaks for this staff on this day
                val breaks = staffBreakRepository
                    .findByStaffIdAndDay(staffId, dayOfWeek)
                    .filter { it.isActive }

                // Get existing bookings for this staff on this date
                val existingBookings = bookingRepository
                    .findByStaffAndDate(staffId, request.date)
                    .filter { it.status !in listOf(BookingStatus.CANCELLED, BookingStatus.NO_SHOW) }

                // Calculate free time slots
                val freeSlots = calculateFreeSlots(
                    workStart = availability.startTime,
                    workEnd = availability.endTime,
                    slotDuration = totalDuration,
                    breaks = breaks,
                    bookings = existingBookings
                )

                // Add slots with staff info
                val isPrimary = serviceStaffRepository
                    .findByServiceAndStaff(request.serviceId, staffId)
                    ?.isPrimary ?: false

                allSlots.addAll(freeSlots.map { slot ->
                    TimeSlot(
                        startTime = slot.start,
                        endTime = slot.end,
                        availableStaff = listOf(
                            StaffSlotInfo(
                                staffId = staffId.toString(),
                                staffName = "Staff Member", // TODO: Fetch from user service
                                isPrimary = isPrimary
                            )
                        )
                    )
                })
            }
        }

        // Step 5: Group slots by time (multiple staff may be available at same time)
        val groupedSlots = groupSlotsByTime(allSlots)

        return AvailableSlotsResponse(
            date = request.date,
            serviceId = request.serviceId.toString(),
            serviceName = service.name,
            durationMinutes = totalDuration,
            slots = groupedSlots
        )
    }

    /**
     * Calculate free time slots within working hours
     * Avoids breaks and existing bookings
     */
    private fun calculateFreeSlots(
        workStart: LocalTime,
        workEnd: LocalTime,
        slotDuration: Int,
        breaks: List<StaffBreak>,
        bookings: List<Booking>
    ): List<TimeSlotRange> {
        val slots = mutableListOf<TimeSlotRange>()
        var current = workStart

        // Move through the day in 15-minute increments
        while (current.plusMinutes(slotDuration.toLong()) <= workEnd) {
            val slotEnd = current.plusMinutes(slotDuration.toLong())

            // Check if slot conflicts with any break
            val conflictsWithBreak = breaks.any { staffBreak ->
                timesOverlap(
                    start1 = current,
                    end1 = slotEnd,
                    start2 = staffBreak.startTime,
                    end2 = staffBreak.endTime
                )
            }

            // Check if slot conflicts with any booking
            val conflictsWithBooking = bookings.any { booking ->
                timesOverlap(
                    start1 = current,
                    end1 = slotEnd,
                    start2 = booking.startTime,
                    end2 = booking.endTime
                )
            }

            // If no conflicts, this slot is available
            if (!conflictsWithBreak && !conflictsWithBooking) {
                slots.add(TimeSlotRange(current, slotEnd))
            }

            // Move to next 15-minute slot
            current = current.plusMinutes(15)
        }

        return slots
    }

    /**
     * Check if two time ranges overlap
     */
    private fun timesOverlap(
        start1: LocalTime,
        end1: LocalTime,
        start2: LocalTime,
        end2: LocalTime
    ): Boolean {
        return (start1 < end2 && end1 > start2)
    }

    /**
     * Group slots by start/end time and combine staff availability
     */
    private fun groupSlotsByTime(slots: List<TimeSlot>): List<TimeSlot> {
        return slots
            .groupBy { it.startTime to it.endTime }
            .map { (time, slotGroup) ->
                TimeSlot(
                    startTime = time.first,
                    endTime = time.second,
                    availableStaff = slotGroup
                        .flatMap { it.availableStaff }
                        .distinctBy { it.staffId }
                )
            }
            .sortedBy { it.startTime }
    }

    /**
     * Convert Java DayOfWeek to our domain DayOfWeek enum
     */
    private fun convertToDayOfWeek(javaDayOfWeek: JavaDayOfWeek): DayOfWeek {
        return when (javaDayOfWeek) {
            JavaDayOfWeek.MONDAY -> DayOfWeek.MONDAY
            JavaDayOfWeek.TUESDAY -> DayOfWeek.TUESDAY
            JavaDayOfWeek.WEDNESDAY -> DayOfWeek.WEDNESDAY
            JavaDayOfWeek.THURSDAY -> DayOfWeek.THURSDAY
            JavaDayOfWeek.FRIDAY -> DayOfWeek.FRIDAY
            JavaDayOfWeek.SATURDAY -> DayOfWeek.SATURDAY
            JavaDayOfWeek.SUNDAY -> DayOfWeek.SUNDAY
        }
    }

    /**
     * Check if a specific time slot is available for a staff member
     */
    fun isSlotAvailable(
        staffId: UUID,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        excludeBookingId: UUID? = null
    ): Boolean {
        // Check blocked dates
        if (blockedDateRepository.isDateBlocked(staffId, date)) {
            return false
        }

        // Check for booking conflicts
        return !bookingRepository.existsConflict(
            staffId = staffId,
            date = date,
            startTime = startTime,
            endTime = endTime,
            excludeBookingId = excludeBookingId
        )
    }
}

/**
 * Internal data class for time slot calculations
 */
private data class TimeSlotRange(
    val start: LocalTime,
    val end: LocalTime
)