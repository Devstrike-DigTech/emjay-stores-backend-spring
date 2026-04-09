package com.emjay.backend.sms.application.service

import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.sms.application.dto.shift.*
import com.emjay.backend.sms.domain.entity.shift.ShiftStatus
import com.emjay.backend.sms.domain.entity.shift.StaffShift
import com.emjay.backend.sms.domain.repository.shift.ShiftTemplateRepository
import com.emjay.backend.sms.domain.repository.shift.StaffShiftRepository
import com.emjay.backend.sms.domain.repository.staff.StaffProfileRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*

@Service
class StaffShiftService(
    private val staffShiftRepository: StaffShiftRepository,
    private val staffProfileRepository: StaffProfileRepository,
    private val shiftTemplateRepository: ShiftTemplateRepository
) {

    @Transactional
    fun createStaffShift(request: CreateStaffShiftRequest): StaffShiftResponse {
        // Validate staff profile exists
        staffProfileRepository.findById(request.staffProfileId)
            ?: throw ResourceNotFoundException("Staff profile not found")

        // Validate shift template if provided
        val template = request.shiftTemplateId?.let {
            shiftTemplateRepository.findById(it)
                ?: throw ResourceNotFoundException("Shift template not found")
        }

        // Get current user ID for assignedBy
        val currentUserId = getCurrentUserId()

        val staffShift = StaffShift(
            staffProfileId = request.staffProfileId,
            shiftTemplateId = request.shiftTemplateId,
            shiftDate = request.shiftDate,
            startTime = request.startTime,
            endTime = request.endTime,
            breakDurationMinutes = request.breakDurationMinutes,
            notes = request.notes,
            assignedBy = currentUserId
        )

        val saved = staffShiftRepository.save(staffShift)
        return toStaffShiftResponse(saved)
    }

    @Transactional
    fun createBulkStaffShifts(request: BulkCreateStaffShiftsRequest): StaffShiftListResponse {
        // Validate all staff profiles exist
        request.staffProfileIds.forEach { staffId ->
            staffProfileRepository.findById(staffId)
                ?: throw ResourceNotFoundException("Staff profile not found: $staffId")
        }

        // Validate shift template if provided
        request.shiftTemplateId?.let {
            shiftTemplateRepository.findById(it)
                ?: throw ResourceNotFoundException("Shift template not found")
        }

        val currentUserId = getCurrentUserId()

        // Create shifts for each staff member on each date
        val shifts = mutableListOf<StaffShift>()
        request.staffProfileIds.forEach { staffId ->
            request.shiftDates.forEach { date ->
                val shift = StaffShift(
                    staffProfileId = staffId,
                    shiftTemplateId = request.shiftTemplateId,
                    shiftDate = date,
                    startTime = request.startTime,
                    endTime = request.endTime,
                    breakDurationMinutes = request.breakDurationMinutes,
                    notes = request.notes,
                    assignedBy = currentUserId
                )
                shifts.add(shift)
            }
        }

        val saved = staffShiftRepository.saveAll(shifts)
        val responses = saved.map { toStaffShiftResponse(it) }

        return StaffShiftListResponse(
            content = responses,
            totalElements = responses.size.toLong(),
            totalPages = 1,
            currentPage = 0,
            pageSize = responses.size
        )
    }

    fun getStaffShiftById(id: UUID): StaffShiftResponse {
        val shift = staffShiftRepository.findById(id)
            ?: throw ResourceNotFoundException("Staff shift not found")
        return toStaffShiftResponse(shift)
    }

    fun getAllStaffShifts(page: Int = 0, size: Int = 20): StaffShiftListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "shiftDate"))
        val shiftsPage = staffShiftRepository.findAll(pageable)

        val responses = shiftsPage.content.map { toStaffShiftResponse(it) }

        return StaffShiftListResponse(
            content = responses,
            totalElements = shiftsPage.totalElements,
            totalPages = shiftsPage.totalPages,
            currentPage = shiftsPage.number,
            pageSize = shiftsPage.size
        )
    }

    fun getStaffShiftsByStaff(staffProfileId: UUID, page: Int = 0, size: Int = 20): StaffShiftListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "shiftDate"))
        val shiftsPage = staffShiftRepository.findByStaffProfileId(staffProfileId, pageable)

        val responses = shiftsPage.content.map { toStaffShiftResponse(it) }

        return StaffShiftListResponse(
            content = responses,
            totalElements = shiftsPage.totalElements,
            totalPages = shiftsPage.totalPages,
            currentPage = shiftsPage.number,
            pageSize = shiftsPage.size
        )
    }

    fun getStaffShiftsByDate(date: LocalDate, page: Int = 0, size: Int = 20): StaffShiftListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "startTime"))
        val shiftsPage = staffShiftRepository.findByShiftDate(date, pageable)

        val responses = shiftsPage.content.map { toStaffShiftResponse(it) }

        return StaffShiftListResponse(
            content = responses,
            totalElements = shiftsPage.totalElements,
            totalPages = shiftsPage.totalPages,
            currentPage = shiftsPage.number,
            pageSize = shiftsPage.size
        )
    }

    fun getStaffShiftsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
        page: Int = 0,
        size: Int = 20
    ): StaffShiftListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "shiftDate", "startTime"))
        val shiftsPage = staffShiftRepository.findByDateRange(startDate, endDate, pageable)

        val responses = shiftsPage.content.map { toStaffShiftResponse(it) }

        return StaffShiftListResponse(
            content = responses,
            totalElements = shiftsPage.totalElements,
            totalPages = shiftsPage.totalPages,
            currentPage = shiftsPage.number,
            pageSize = shiftsPage.size
        )
    }

    fun getUpcomingShifts(staffProfileId: UUID, page: Int = 0, size: Int = 20): StaffShiftListResponse {
        val today = LocalDate.now()
        val pageable = PageRequest.of(page, size)
        val shiftsPage = staffShiftRepository.findUpcomingShifts(staffProfileId, today, pageable)

        val responses = shiftsPage.content.map { toStaffShiftResponse(it) }

        return StaffShiftListResponse(
            content = responses,
            totalElements = shiftsPage.totalElements,
            totalPages = shiftsPage.totalPages,
            currentPage = shiftsPage.number,
            pageSize = shiftsPage.size
        )
    }

    fun getWeeklyRoster(request: WeeklyRosterRequest): WeeklyRosterResponse {
        val weekStart = request.startDate
        val weekEnd = weekStart.plusDays(6)

        val pageable = PageRequest.of(0, 1000, Sort.by(Sort.Direction.ASC, "shiftDate", "startTime"))
        val shiftsPage = staffShiftRepository.findByDateRange(weekStart, weekEnd, pageable)

        // Filter by staff if specified
        val shifts = if (request.staffProfileIds != null) {
            shiftsPage.content.filter { it.staffProfileId in request.staffProfileIds }
        } else {
            shiftsPage.content
        }

        val shiftResponses = shifts.map { toStaffShiftResponse(it) }

        // Calculate staff summaries
        val staffSummaries = shifts.groupBy { it.staffProfileId }
            .map { (staffId, staffShifts) ->
                val staffProfile = staffProfileRepository.findById(staffId)
                StaffWeeklySummary(
                    staffProfileId = staffId.toString(),
                    staffName = staffProfile?.fullName() ?: "Unknown",
                    totalShifts = staffShifts.size,
                    totalHours = staffShifts.sumOf { it.workDurationHours() },
                    shiftsByDay = staffShifts.groupBy { it.shiftDate.dayOfWeek.name }
                        .mapValues { it.value.size }
                )
            }

        return WeeklyRosterResponse(
            weekStartDate = weekStart,
            weekEndDate = weekEnd,
            shifts = shiftResponses,
            staffSummaries = staffSummaries
        )
    }

    @Transactional
    fun updateStaffShift(id: UUID, request: UpdateStaffShiftRequest): StaffShiftResponse {
        val existing = staffShiftRepository.findById(id)
            ?: throw ResourceNotFoundException("Staff shift not found")

        val updated = existing.copy(
            shiftDate = request.shiftDate ?: existing.shiftDate,
            startTime = request.startTime ?: existing.startTime,
            endTime = request.endTime ?: existing.endTime,
            breakDurationMinutes = request.breakDurationMinutes ?: existing.breakDurationMinutes,
            status = request.status ?: existing.status,
            notes = request.notes ?: existing.notes
        )

        val saved = staffShiftRepository.save(updated)
        return toStaffShiftResponse(saved)
    }

    @Transactional
    fun cancelStaffShift(id: UUID): StaffShiftResponse {
        val existing = staffShiftRepository.findById(id)
            ?: throw ResourceNotFoundException("Staff shift not found")

        if (!existing.canBeCancelled()) {
            throw IllegalStateException("Shift cannot be cancelled in current status: ${existing.status}")
        }

        val cancelled = existing.copy(status = ShiftStatus.CANCELLED)
        val saved = staffShiftRepository.save(cancelled)
        return toStaffShiftResponse(saved)
    }

    @Transactional
    fun deleteStaffShift(id: UUID) {
        val shift = staffShiftRepository.findById(id)
            ?: throw ResourceNotFoundException("Staff shift not found")
        staffShiftRepository.delete(shift)
    }

    private fun toStaffShiftResponse(shift: StaffShift): StaffShiftResponse {
        val staffProfile = staffProfileRepository.findById(shift.staffProfileId)
        val template = shift.shiftTemplateId?.let { shiftTemplateRepository.findById(it) }

        return StaffShiftResponse(
            id = shift.id.toString(),
            staffProfileId = shift.staffProfileId.toString(),
            staffName = staffProfile?.fullName(),
            shiftTemplateId = shift.shiftTemplateId?.toString(),
            shiftTemplateName = template?.name,
            shiftDate = shift.shiftDate,
            startTime = shift.startTime,
            endTime = shift.endTime,
            breakDurationMinutes = shift.breakDurationMinutes,
            totalDurationMinutes = shift.totalDurationMinutes(),
            workDurationMinutes = shift.workDurationMinutes(),
            workDurationHours = shift.workDurationHours(),
            status = shift.status,
            notes = shift.notes,
            assignedBy = shift.assignedBy?.toString(),
            isOvernight = shift.isOvernight(),
            createdAt = shift.createdAt!!,
            updatedAt = shift.updatedAt!!
        )
    }

    private fun getCurrentUserId(): UUID? {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication
            UUID.fromString(authentication?.name)
        } catch (e: Exception) {
            null
        }
    }
}