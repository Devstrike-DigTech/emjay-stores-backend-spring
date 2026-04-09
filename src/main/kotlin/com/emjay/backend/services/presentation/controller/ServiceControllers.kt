package com.emjay.backend.services.presentation.controller

import com.emjay.backend.common.infrastructure.security.jwt.JwtTokenProvider
import com.emjay.backend.services.application.dto.*
import com.emjay.backend.services.application.service.*
import com.emjay.backend.services.domain.entity.BookingStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

// ========== SERVICE CATEGORY CONTROLLER ==========

@RestController
@RequestMapping("/api/v1/services/categories")
@Tag(name = "Service Categories", description = "Manage beauty service categories")
class ServiceCategoryController(
    private val serviceManagementService: ServiceManagementService
) {

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create service category", security = [SecurityRequirement(name = "bearerAuth")])
    fun createCategory(@Valid @RequestBody request: CreateServiceCategoryRequest): ResponseEntity<ServiceCategoryResponse> {
        val response = serviceManagementService.createCategory(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(summary = "Get all active categories (public)")
    fun getCategories(): ResponseEntity<List<ServiceCategoryResponse>> {
        val categories = serviceManagementService.getCategories()
        return ResponseEntity.ok(categories)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update category", security = [SecurityRequirement(name = "bearerAuth")])
    fun updateCategory(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateServiceCategoryRequest
    ): ResponseEntity<ServiceCategoryResponse> {
        val response = serviceManagementService.updateCategory(id, request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{categoryId}/subcategories")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create subcategory", security = [SecurityRequirement(name = "bearerAuth")])
    fun createSubcategory(
        @PathVariable categoryId: UUID,
        @Valid @RequestBody request: CreateServiceSubcategoryRequest
    ): ResponseEntity<ServiceSubcategoryResponse> {
        val response = serviceManagementService.createSubcategory(categoryId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{categoryId}/subcategories")
    @Operation(summary = "Get subcategories (public)")
    fun getSubcategories(@PathVariable categoryId: UUID): ResponseEntity<List<ServiceSubcategoryResponse>> {
        val subcategories = serviceManagementService.getSubcategories(categoryId)
        return ResponseEntity.ok(subcategories)
    }
}

// ========== SERVICE CONTROLLER ==========

@RestController
@RequestMapping("/api/v1/services")
@Tag(name = "Services", description = "Manage beauty services")
class ServiceController(
    private val serviceManagementService: ServiceManagementService,
    private val availabilityService: AvailabilityService
) {

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create service", security = [SecurityRequirement(name = "bearerAuth")])
    fun createService(@Valid @RequestBody request: CreateServiceRequest): ResponseEntity<ServiceResponse> {
        val response = serviceManagementService.createService(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(summary = "Get all services (public catalog)")
    fun getServices(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ServiceListResponse> {
        val response = serviceManagementService.getServices(page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service details (public)")
    fun getService(@PathVariable id: UUID): ResponseEntity<ServiceResponse> {
        val response = serviceManagementService.getService(id)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update service", security = [SecurityRequirement(name = "bearerAuth")])
    fun updateService(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateServiceRequest
    ): ResponseEntity<ServiceResponse> {
        val response = serviceManagementService.updateService(id, request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{serviceId}/images")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Add service image", security = [SecurityRequirement(name = "bearerAuth")])
    fun addServiceImage(
        @PathVariable serviceId: UUID,
        @Valid @RequestBody request: AddServiceImageRequest
    ): ResponseEntity<ServiceImageResponse> {
        val response = serviceManagementService.addServiceImage(serviceId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/{serviceId}/addons")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Add service add-on", security = [SecurityRequirement(name = "bearerAuth")])
    fun addServiceAddon(
        @PathVariable serviceId: UUID,
        @Valid @RequestBody request: CreateServiceAddonRequest
    ): ResponseEntity<ServiceAddonResponse> {
        val response = serviceManagementService.addServiceAddon(serviceId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/{serviceId}/staff")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Assign staff to service", security = [SecurityRequirement(name = "bearerAuth")])
    fun assignStaff(
        @PathVariable serviceId: UUID,
        @Valid @RequestBody request: AssignStaffToServiceRequest
    ): ResponseEntity<StaffAssignmentResponse> {
        val response = serviceManagementService.assignStaffToService(serviceId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{serviceId}/available-slots")
    @Operation(summary = "Get available booking slots (public)")
    fun getAvailableSlots(
        @PathVariable serviceId: UUID,
        @Valid request: GetAvailableSlotsRequest
    ): ResponseEntity<AvailableSlotsResponse> {
        val response = availabilityService.getAvailableSlots(request)
        return ResponseEntity.ok(response)
    }
}

// ========== BOOKING CONTROLLER ==========

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Manage service bookings")
class BookingController(
    private val bookingService: BookingService,
    private val jwtUtil: JwtTokenProvider
) {

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create booking")
    fun createBooking(
        @Valid @RequestBody request: CreateBookingRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<BookingResponse> {
        val customerId = extractCustomerIdFromToken(token)
        val response = bookingService.createBooking(customerId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/my-bookings")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my bookings")
    fun getMyBookings(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<BookingListResponse> {
        val customerId = extractCustomerIdFromToken(token)
        val response = bookingService.getCustomerBookings(customerId, page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/upcoming")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get upcoming bookings")
    fun getUpcomingBookings(@RequestHeader("Authorization") token: String): ResponseEntity<List<BookingSummaryResponse>> {
        val customerId = extractCustomerIdFromToken(token)
        val bookings = bookingService.getUpcomingBookings(customerId)
        return ResponseEntity.ok(bookings)
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get booking details")
    fun getBooking(@PathVariable id: UUID): ResponseEntity<BookingResponse> {
        val response = bookingService.getBooking(id)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Confirm booking (staff/admin)")
    fun confirmBooking(
        @PathVariable id: UUID,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<BookingResponse> {
        val userId = extractUserIdFromToken(token)
        val response = bookingService.confirmBooking(id, userId)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{id}/reschedule")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Reschedule booking")
    fun rescheduleBooking(
        @PathVariable id: UUID,
        @Valid @RequestBody request: RescheduleBookingRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<BookingResponse> {
        val customerId = extractCustomerIdFromToken(token)
        val response = bookingService.rescheduleBooking(id, customerId, request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{id}/cancel")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cancel booking")
    fun cancelBooking(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CancelBookingRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<BookingResponse> {
        val customerId = extractCustomerIdFromToken(token)
        val response = bookingService.cancelBooking(id, customerId, request)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Start booking (staff)")
    fun startBooking(
        @PathVariable id: UUID,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<BookingResponse> {
        val staffId = extractUserIdFromToken(token)
        val response = bookingService.startBooking(id, staffId)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Complete booking (staff)")
    fun completeBooking(
        @PathVariable id: UUID,
        @RequestParam(required = false) staffNotes: String?,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<BookingResponse> {
        val staffId = extractUserIdFromToken(token)
        val response = bookingService.completeBooking(id, staffId, staffNotes)
        return ResponseEntity.ok(response)
    }

    private fun extractCustomerIdFromToken(token: String): UUID {
        val jwt = token.removePrefix("Bearer ")
        val userId = jwtUtil.getUserIdFromToken(jwt)
        return userId
    }

    private fun extractUserIdFromToken(token: String): UUID {
        val jwt = token.removePrefix("Bearer ")
        val userId = jwtUtil.getUserIdFromToken(jwt)
        return userId
    }
}

// ========== STAFF SCHEDULE CONTROLLER ==========

@RestController
@RequestMapping("/api/v1/staff")
@Tag(name = "Staff Schedule", description = "Manage staff availability and schedules")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
@SecurityRequirement(name = "bearerAuth")
class StaffScheduleController(
    private val staffScheduleService: StaffScheduleService,
    private val jwtUtil: JwtTokenProvider
) {

    @PostMapping("/availability")
    @Operation(summary = "Set staff availability")
    fun setAvailability(
        @Valid @RequestBody request: SetStaffAvailabilityRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<StaffAvailabilityResponse> {
        val staffId = extractStaffIdFromToken(token)
        val response = staffScheduleService.setStaffAvailability(staffId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/availability")
    @Operation(summary = "Get staff availability")
    fun getAvailability(@RequestHeader("Authorization") token: String): ResponseEntity<List<StaffAvailabilityResponse>> {
        val staffId = extractStaffIdFromToken(token)
        val response = staffScheduleService.getStaffAvailability(staffId)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/breaks")
    @Operation(summary = "Add staff break")
    fun addBreak(
        @Valid @RequestBody request: AddStaffBreakRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<StaffBreakResponse> {
        val staffId = extractStaffIdFromToken(token)
        val response = staffScheduleService.addStaffBreak(staffId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/blocked-dates")
    @Operation(summary = "Block a date")
    fun blockDate(
        @Valid @RequestBody request: BlockDateRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<Map<String, String>> {
        val staffId = extractStaffIdFromToken(token)
        val message = staffScheduleService.blockDate(request, staffId)
        return ResponseEntity.ok(mapOf("message" to message))
    }

    @GetMapping("/schedule")
    @Operation(summary = "Get staff schedule")
    fun getSchedule(
        @RequestParam date: String,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<StaffScheduleResponse> {
        val staffId = extractStaffIdFromToken(token)
        val localDate = java.time.LocalDate.parse(date)
        val response = staffScheduleService.getStaffSchedule(staffId, localDate)
        return ResponseEntity.ok(response)
    }

    private fun extractStaffIdFromToken(token: String): UUID {
        val jwt = token.removePrefix("Bearer ")
        val userId = jwtUtil.getUserIdFromToken(jwt)
        return userId
    }
}

// ========== ADMIN BOOKING CONTROLLER ==========

@RestController
@RequestMapping("/api/v1/bookings/admin")
@Tag(name = "Admin Bookings", description = "Admin endpoints for managing all bookings")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
@SecurityRequirement(name = "bearerAuth")
class AdminBookingController(
    private val bookingService: BookingService
) {

    @GetMapping
    @Operation(summary = "Get all bookings (admin view, paginated)")
    fun getAllBookings(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) status: BookingStatus?
    ): ResponseEntity<BookingListResponse> {
        val response = bookingService.getAllBookings(page, size, status)
        return ResponseEntity.ok(response)
    }
}