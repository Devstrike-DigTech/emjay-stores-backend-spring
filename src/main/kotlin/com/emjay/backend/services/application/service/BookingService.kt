package com.emjay.backend.services.application.service

import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.notifications.application.service.NotificationService
import com.emjay.backend.services.application.dto.*
import com.emjay.backend.services.domain.entity.*
import com.emjay.backend.services.domain.repository.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Service
class BookingService(
    private val bookingRepository: BookingRepository,
    private val serviceRepository: ServiceRepository,
    private val serviceAddonRepository: ServiceAddonRepository,
    private val bookingAddonRepository: BookingAddonRepository,
    private val bookingStatusHistoryRepository: BookingStatusHistoryRepository,
    private val availabilityService: AvailabilityService,
    private val notificationService: NotificationService
) {

    /**
     * Create a new booking
     */
    @Transactional
    fun createBooking(customerId: UUID, request: CreateBookingRequest): BookingResponse {
        // Step 1: Get service details
        val service = serviceRepository.findById(request.serviceId)
            ?: throw ResourceNotFoundException("Service not found")

        if (!service.isActive()) {
            throw IllegalStateException("Service is not available")
        }

        val totalDuration = service.durationMinutes + service.bufferTimeMinutes
        val endTime = request.startTime.plusMinutes(totalDuration.toLong())

        // Step 2: Check if slot is available
        val isAvailable = availabilityService.isSlotAvailable(
            staffId = request.staffId,
            date = request.bookingDate,
            startTime = request.startTime,
            endTime = endTime
        )

        if (!isAvailable) {
            throw IllegalArgumentException("Selected time slot is not available")
        }

        // Step 3: Get and validate add-ons
        val addons = if (request.addonIds.isNotEmpty()) {
            val allAddons = serviceAddonRepository.findActiveByServiceId(request.serviceId)
            allAddons.filter { it.id in request.addonIds }
        } else {
            emptyList()
        }

        val addonsPrice = addons.sumOf { it.price }
        val addonsDuration = addons.sumOf { it.durationMinutes }

        // Adjust end time if add-ons present
        val finalEndTime = endTime.plusMinutes(addonsDuration.toLong())
        val finalDuration = totalDuration + addonsDuration

        // Step 4: Calculate total amount
        val totalAmount = service.currentPrice() + addonsPrice

        // Step 5: Generate booking number
        val bookingNumber = bookingRepository.generateBookingNumber()

        // Step 6: Create booking
        val booking = Booking(
            bookingNumber = bookingNumber,
            customerId = customerId,
            serviceId = request.serviceId,
            staffId = request.staffId,
            bookingDate = request.bookingDate,
            startTime = request.startTime,
            endTime = finalEndTime,
            durationMinutes = finalDuration,
            status = BookingStatus.PENDING,
            servicePrice = service.currentPrice(),
            addonsPrice = addonsPrice,
            totalAmount = totalAmount,
            customerNotes = request.customerNotes,
            bookedAt = LocalDateTime.now()
        )

        val savedBooking = bookingRepository.save(booking)

        // Step 7: Save booking add-ons
        if (addons.isNotEmpty()) {
            val bookingAddons = addons.map { addon ->
                BookingAddon(
                    bookingId = savedBooking.id!!,
                    addonId = addon.id!!,
                    addonName = addon.name,
                    price = addon.price,
                    durationMinutes = addon.durationMinutes
                )
            }
            bookingAddonRepository.saveAll(bookingAddons)
        }

        // Step 8: Record status history
        recordStatusChange(savedBooking.id!!, null, BookingStatus.PENDING, customerId)

        // Step 9: TODO - Initiate payment using existing payment gateway

        // Step 9: Send booking confirmation notification
        sendBookingConfirmationNotification(savedBooking, service, customerId)

        // Step 10: Schedule 24hr reminder
        scheduleBookingReminder(savedBooking, service, customerId)


        return toBookingResponse(savedBooking, addons)
    }

    /**
     * Get booking details
     */
    fun getBooking(id: UUID): BookingResponse {
        val booking = bookingRepository.findById(id)
            ?: throw ResourceNotFoundException("Booking not found")

        val addons = bookingAddonRepository.findByBookingId(id)
        return toBookingResponse(booking, addons.map { toAddon(it) })
    }

    /**
     * Get customer's bookings
     */
    fun getCustomerBookings(customerId: UUID, page: Int = 0, size: Int = 20): BookingListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "bookingDate"))
        val bookings = bookingRepository.findByCustomerId(customerId, pageable)

        return BookingListResponse(
            content = bookings.content.map { toBookingSummary(it) },
            totalElements = bookings.totalElements,
            totalPages = bookings.totalPages,
            currentPage = page,
            pageSize = size
        )
    }

    /**
     * Get upcoming bookings for customer
     */
    fun getUpcomingBookings(customerId: UUID): List<BookingSummaryResponse> {
        return bookingRepository.findUpcomingByCustomer(customerId)
            .map { toBookingSummary(it) }
    }

    /**
     * Confirm booking (admin/staff action)
     */
    @Transactional
    fun confirmBooking(id: UUID, userId: UUID): BookingResponse {
        val booking = bookingRepository.findById(id)
            ?: throw ResourceNotFoundException("Booking not found")

        if (!booking.isPending()) {
            throw IllegalStateException("Only pending bookings can be confirmed")
        }

        val confirmed = booking.copy(
            status = BookingStatus.CONFIRMED,
            confirmedAt = LocalDateTime.now()
        )

        val saved = bookingRepository.save(confirmed)
        recordStatusChange(id, BookingStatus.PENDING, BookingStatus.CONFIRMED, userId)

        return toBookingResponse(saved, emptyList())
    }

    /**
     * Reschedule booking
     */
    @Transactional
    fun rescheduleBooking(
        id: UUID,
        customerId: UUID,
        request: RescheduleBookingRequest
    ): BookingResponse {
        val booking = bookingRepository.findById(id)
            ?: throw ResourceNotFoundException("Booking not found")

        if (booking.customerId != customerId) {
            throw IllegalArgumentException("Not authorized to reschedule this booking")
        }

        if (!booking.canBeRescheduled()) {
            throw IllegalStateException("Booking cannot be rescheduled in current status")
        }

        // Calculate new end time
        val newEndTime = request.newStartTime.plusMinutes(booking.durationMinutes.toLong())

        // Check if new slot is available
        val isAvailable = availabilityService.isSlotAvailable(
            staffId = booking.staffId,
            date = request.newDate,
            startTime = request.newStartTime,
            endTime = newEndTime,
            excludeBookingId = booking.id
        )

        if (!isAvailable) {
            throw IllegalArgumentException("Selected time slot is not available")
        }

        val rescheduled = booking.copy(
            bookingDate = request.newDate,
            startTime = request.newStartTime,
            endTime = newEndTime,
            status = BookingStatus.RESCHEDULED
        )

        val saved = bookingRepository.save(rescheduled)
        recordStatusChange(
            id,
            booking.status,
            BookingStatus.RESCHEDULED,
            customerId,
            request.reason
        )

        return toBookingResponse(saved, emptyList())
    }

    /**
     * Cancel booking
     */
    @Transactional
    fun cancelBooking(id: UUID, customerId: UUID, request: CancelBookingRequest): BookingResponse {
        val booking = bookingRepository.findById(id)
            ?: throw ResourceNotFoundException("Booking not found")

        if (booking.customerId != customerId) {
            throw IllegalArgumentException("Not authorized to cancel this booking")
        }

        if (!booking.canBeCancelled()) {
            throw IllegalStateException("Booking cannot be cancelled in current status")
        }

        val cancelled = booking.copy(
            status = BookingStatus.CANCELLED,
            cancellationReason = request.reason,
            cancelledAt = LocalDateTime.now()
        )

        val saved = bookingRepository.save(cancelled)
        recordStatusChange(
            id,
            booking.status,
            BookingStatus.CANCELLED,
            customerId,
            request.reason
        )

        // TODO - Process refund if paid

        return toBookingResponse(saved, emptyList())
    }

    /**
     * Start booking (staff marks service as started)
     */
    @Transactional
    fun startBooking(id: UUID, staffId: UUID): BookingResponse {
        val booking = bookingRepository.findById(id)
            ?: throw ResourceNotFoundException("Booking not found")

        if (booking.staffId != staffId) {
            throw IllegalArgumentException("Not authorized to start this booking")
        }

        if (!booking.isConfirmed()) {
            throw IllegalStateException("Only confirmed bookings can be started")
        }

        val started = booking.copy(
            status = BookingStatus.IN_PROGRESS,
            startedAt = LocalDateTime.now()
        )

        val saved = bookingRepository.save(started)
        recordStatusChange(id, BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS, staffId)

        return toBookingResponse(saved, emptyList())
    }

    /**
     * Complete booking
     */
    @Transactional
    fun completeBooking(id: UUID, staffId: UUID, staffNotes: String?): BookingResponse {
        val booking = bookingRepository.findById(id)
            ?: throw ResourceNotFoundException("Booking not found")

        if (booking.staffId != staffId) {
            throw IllegalArgumentException("Not authorized to complete this booking")
        }

        if (booking.status != BookingStatus.IN_PROGRESS) {
            throw IllegalStateException("Only in-progress bookings can be completed")
        }

        val completed = booking.copy(
            status = BookingStatus.COMPLETED,
            completedAt = LocalDateTime.now(),
            staffNotes = staffNotes
        )

        val saved = bookingRepository.save(completed)
        recordStatusChange(id, BookingStatus.IN_PROGRESS, BookingStatus.COMPLETED, staffId)

        return toBookingResponse(saved, emptyList())
    }

    /**
     * Record booking status change in history
     */
    private fun recordStatusChange(
        bookingId: UUID,
        fromStatus: BookingStatus?,
        toStatus: BookingStatus,
        changedBy: UUID,
        reason: String? = null
    ) {
        val history = BookingStatusHistory(
            bookingId = bookingId,
            fromStatus = fromStatus,
            toStatus = toStatus,
            changedBy = changedBy,
            reason = reason,
            changedAt = LocalDateTime.now()
        )
        bookingStatusHistoryRepository.save(history)
    }

    /**
     * Convert booking to response DTO
     */
    private fun toBookingResponse(booking: Booking, addons: List<ServiceAddon>): BookingResponse {
        val service = serviceRepository.findById(booking.serviceId)

        return BookingResponse(
            id = booking.id.toString(),
            bookingNumber = booking.bookingNumber,
            customerId = booking.customerId.toString(),
            customerName = "Customer", // TODO: Fetch
            service = ServiceSummaryResponse(
                id = booking.serviceId.toString(),
                name = service?.name ?: "Service",
                slug = service?.slug ?: "",
                categoryName = "Category",
                shortDescription = service?.shortDescription,
                currentPrice = booking.servicePrice,
                hasDiscount = false,
                durationMinutes = booking.durationMinutes,
                primaryImage = null,
                isFeatured = false,
                status = ServiceStatus.ACTIVE
            ),
            staff = StaffInfo(
                id = booking.staffId.toString(),
                name = "Staff Member",
                imageUrl = null
            ),
            bookingDate = booking.bookingDate,
            startTime = booking.startTime,
            endTime = booking.endTime,
            durationMinutes = booking.durationMinutes,
            status = booking.status,
            servicePrice = booking.servicePrice,
            addonsPrice = booking.addonsPrice,
            totalAmount = booking.totalAmount,
            addons = addons.map { toBookingAddonResponse(it) },
            paymentStatus = booking.paymentStatus,
            isPaid = booking.isPaid(),
            canCancel = booking.canBeCancelled(),
            canReschedule = booking.canBeRescheduled(),
            customerNotes = booking.customerNotes,
            staffNotes = booking.staffNotes,
            bookedAt = booking.bookedAt,
            confirmedAt = booking.confirmedAt,
            completedAt = booking.completedAt
        )
    }

    private fun toBookingSummary(booking: Booking): BookingSummaryResponse {
        return BookingSummaryResponse(
            id = booking.id.toString(),
            bookingNumber = booking.bookingNumber,
            serviceName = "Service", // TODO: Fetch
            staffName = "Staff", // TODO: Fetch
            bookingDate = booking.bookingDate,
            startTime = booking.startTime,
            status = booking.status,
            totalAmount = booking.totalAmount,
            isPaid = booking.isPaid()
        )
    }

    private fun toBookingAddonResponse(addon: ServiceAddon): BookingAddonResponse {
        return BookingAddonResponse(
            id = addon.id.toString(),
            name = addon.name,
            price = addon.price,
            durationMinutes = addon.durationMinutes
        )
    }

    private fun toAddon(bookingAddon: BookingAddon): ServiceAddon {
        return ServiceAddon(
            id = bookingAddon.addonId,
            serviceId = UUID.randomUUID(), // Not needed for response
            name = bookingAddon.addonName,
            price = bookingAddon.price,
            durationMinutes = bookingAddon.durationMinutes
        )
    }


    // ========== NOTIFICATION HELPER METHODS ==========

    private fun sendBookingConfirmationNotification(booking: Booking, service: com.emjay.backend.services.domain.entity.Service, customerId: UUID) {
        try {
            // TODO: Fetch actual customer details
            val customerEmail = "customer@example.com"  // Replace with actual customer fetch
            val customerPhone = "+234801234567"  // Replace with actual customer fetch
            val customerName = "Customer"  // Replace with actual customer fetch

            // Send Email
            notificationService.queueNotification(
                com.emjay.backend.notifications.application.dto.QueueNotificationRequest(
                    recipientId = customerId,
                    recipientEmail = customerEmail,
                    recipientName = customerName,
                    notificationType = com.emjay.backend.notifications.domain.entity.NotificationType.BOOKING_CONFIRMATION,
                    channel = com.emjay.backend.notifications.domain.entity.NotificationChannel.EMAIL,
                    subject = "Booking Confirmed - ${booking.bookingNumber}",
                    htmlContent = buildBookingConfirmationEmail(booking, service),
                    relatedEntityType = "BOOKING",
                    relatedEntityId = booking.id
                )
            )

            // Send SMS
            notificationService.queueNotification(
                com.emjay.backend.notifications.application.dto.QueueNotificationRequest(
                    recipientId = customerId,
                    recipientPhone = customerPhone,
                    recipientName = customerName,
                    notificationType = com.emjay.backend.notifications.domain.entity.NotificationType.BOOKING_CONFIRMATION,
                    channel = com.emjay.backend.notifications.domain.entity.NotificationChannel.SMS,
                    message = "Booking confirmed! ${service.name} on ${booking.bookingDate} at ${booking.startTime}. Booking #${booking.bookingNumber}. -Emjay",
                    relatedEntityType = "BOOKING",
                    relatedEntityId = booking.id
                )
            )
        } catch (e: Exception) {
            println("Failed to send booking confirmation: ${e.message}")
        }
    }

    private fun scheduleBookingReminder(booking: Booking, service: com.emjay.backend.services.domain.entity.Service, customerId: UUID) {
        try {
            val reminderTime = booking.bookingDate.atTime(booking.startTime).minusHours(24)

            // Only schedule if booking is more than 24 hours away
            if (reminderTime.isAfter(LocalDateTime.now())) {
                // TODO: Fetch actual customer details
                val customerEmail = "customer@example.com"
                val customerPhone = "+234801234567"
                val customerName = "Customer"

                // Schedule Email Reminder
                notificationService.queueNotification(
                    com.emjay.backend.notifications.application.dto.QueueNotificationRequest(
                        recipientId = customerId,
                        recipientEmail = customerEmail,
                        recipientName = customerName,
                        notificationType = com.emjay.backend.notifications.domain.entity.NotificationType.BOOKING_REMINDER,
                        channel = com.emjay.backend.notifications.domain.entity.NotificationChannel.EMAIL,
                        scheduledFor = reminderTime,
                        subject = "Reminder: Appointment Tomorrow",
                        htmlContent = buildBookingReminderEmail(booking, service),
                        relatedEntityType = "BOOKING",
                        relatedEntityId = booking.id
                    )
                )

                // Schedule SMS Reminder
                notificationService.queueNotification(
                    com.emjay.backend.notifications.application.dto.QueueNotificationRequest(
                        recipientId = customerId,
                        recipientPhone = customerPhone,
                        recipientName = customerName,
                        notificationType = com.emjay.backend.notifications.domain.entity.NotificationType.BOOKING_REMINDER,
                        channel = com.emjay.backend.notifications.domain.entity.NotificationChannel.SMS,
                        scheduledFor = reminderTime,
                        message = "Reminder: You have an appointment for ${service.name} tomorrow at ${booking.startTime}. See you soon! -Emjay",
                        relatedEntityType = "BOOKING",
                        relatedEntityId = booking.id
                    )
                )
            }
        } catch (e: Exception) {
            println("Failed to schedule booking reminder: ${e.message}")
        }
    }

    private fun buildBookingConfirmationEmail(booking: Booking, service: com.emjay.backend.services.domain.entity.Service): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; margin: 0; padding: 0; }
                    .header { background: #4F46E5; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .booking-details { background: #F3F4F6; padding: 15px; margin: 20px 0; border-radius: 5px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>✅ Booking Confirmed!</h1>
                </div>
                <div class="content">
                    <p>Your appointment has been confirmed.</p>
                    
                    <div class="booking-details">
                        <h3>Appointment Details</h3>
                        <p><strong>Booking Number:</strong> ${booking.bookingNumber}</p>
                        <p><strong>Service:</strong> ${service.name}</p>
                        <p><strong>Date:</strong> ${booking.bookingDate}</p>
                        <p><strong>Time:</strong> ${booking.startTime}</p>
                        <p><strong>Duration:</strong> ${service.durationMinutes} minutes</p>
                        <p><strong>Total Amount:</strong> ₦${booking.totalAmount}</p>
                    </div>
                    
                    <p>We'll send you a reminder 24 hours before your appointment.</p>
                    <p>See you soon!</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildBookingReminderEmail(booking: Booking, service: com.emjay.backend.services.domain.entity.Service): String {
        return """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>🔔 Appointment Reminder</h2>
                <p>This is a reminder about your upcoming appointment:</p>
                <ul>
                    <li><strong>Service:</strong> ${service.name}</li>
                    <li><strong>Date & Time:</strong> ${booking.bookingDate} at ${booking.startTime}</li>
                    <li><strong>Duration:</strong> ${service.durationMinutes} minutes</li>
                </ul>
                <p>We look forward to seeing you!</p>
            </body>
            </html>
        """.trimIndent()
    }
}