package com.emjay.backend.sms.infrastructure.persistence.repository

import com.emjay.backend.sms.domain.entity.shift.StaffShift
import com.emjay.backend.sms.domain.entity.shift.ShiftStatus
import com.emjay.backend.sms.domain.repository.shift.StaffShiftRepository
import com.emjay.backend.sms.infrastructure.persistence.entity.StaffShiftEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
class StaffShiftRepositoryImpl(
    private val jpaRepository: JpaStaffShiftRepository
) : StaffShiftRepository {

    override fun save(staffShift: StaffShift): StaffShift {
        val entity = toEntity(staffShift)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun saveAll(staffShifts: List<StaffShift>): List<StaffShift> {
        val entities = staffShifts.map { toEntity(it) }
        val saved = jpaRepository.saveAll(entities)
        return saved.map { toDomain(it) }
    }

    override fun findById(id: UUID): StaffShift? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findAll(pageable: Pageable): Page<StaffShift> {
        return jpaRepository.findAll(pageable).map { toDomain(it) }
    }

    override fun findByStaffProfileId(staffProfileId: UUID, pageable: Pageable): Page<StaffShift> {
        return jpaRepository.findByStaffProfileId(staffProfileId, pageable).map { toDomain(it) }
    }

    override fun findByShiftDate(date: LocalDate, pageable: Pageable): Page<StaffShift> {
        return jpaRepository.findByShiftDate(date, pageable).map { toDomain(it) }
    }

    override fun findByDateRange(startDate: LocalDate, endDate: LocalDate, pageable: Pageable): Page<StaffShift> {
        return jpaRepository.findByDateRange(startDate, endDate, pageable).map { toDomain(it) }
    }

    override fun findByStaffAndDateRange(
        staffProfileId: UUID,
        startDate: LocalDate,
        endDate: LocalDate,
        pageable: Pageable
    ): Page<StaffShift> {
        return jpaRepository.findByStaffAndDateRange(staffProfileId, startDate, endDate, pageable)
            .map { toDomain(it) }
    }

    override fun findByStatus(status: ShiftStatus, pageable: Pageable): Page<StaffShift> {
        return jpaRepository.findByStatus(status, pageable).map { toDomain(it) }
    }

    override fun findUpcomingShifts(staffProfileId: UUID, fromDate: LocalDate, pageable: Pageable): Page<StaffShift> {
        return jpaRepository.findUpcomingShifts(staffProfileId, fromDate, pageable).map { toDomain(it) }
    }

    override fun countByStaffAndDate(staffProfileId: UUID, date: LocalDate): Long {
        return jpaRepository.countByStaffProfileIdAndShiftDate(staffProfileId, date)
    }

    override fun countByDateAndStatus(date: LocalDate, status: ShiftStatus): Long {
        return jpaRepository.countByShiftDateAndStatus(date, status)
    }

    override fun delete(staffShift: StaffShift) {
        staffShift.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: StaffShiftEntity): StaffShift {
        return StaffShift(
            id = entity.id,
            staffProfileId = entity.staffProfileId,
            shiftTemplateId = entity.shiftTemplateId,
            shiftDate = entity.shiftDate,
            startTime = entity.startTime,
            endTime = entity.endTime,
            breakDurationMinutes = entity.breakDurationMinutes,
            status = entity.status,
            notes = entity.notes,
            assignedBy = entity.assignedBy,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEntity(domain: StaffShift): StaffShiftEntity {
        return StaffShiftEntity(
            id = domain.id,
            staffProfileId = domain.staffProfileId,
            shiftTemplateId = domain.shiftTemplateId,
            shiftDate = domain.shiftDate,
            startTime = domain.startTime,
            endTime = domain.endTime,
            breakDurationMinutes = domain.breakDurationMinutes,
            status = domain.status,
            notes = domain.notes,
            assignedBy = domain.assignedBy,
            createdAt = domain.createdAt ?: java.time.LocalDateTime.now(),
            updatedAt = domain.updatedAt ?: java.time.LocalDateTime.now()
        )
    }
}