package com.emjay.backend.sms.infrastructure.persistence.repository

import com.emjay.backend.sms.domain.entity.attendance.BreakRecord
import com.emjay.backend.sms.domain.repository.attendance.BreakRecordRepository
import com.emjay.backend.sms.infrastructure.persistence.entity.BreakRecordEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class BreakRecordRepositoryImpl(
    private val jpaRepository: JpaBreakRecordRepository
) : BreakRecordRepository {

    override fun save(breakRecord: BreakRecord): BreakRecord {
        val entity = toEntity(breakRecord)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): BreakRecord? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findByAttendanceRecordId(attendanceRecordId: UUID, pageable: Pageable): Page<BreakRecord> {
        return jpaRepository.findByAttendanceRecordId(attendanceRecordId, pageable).map { toDomain(it) }
    }

    override fun findActiveBreak(attendanceRecordId: UUID): BreakRecord? {
        return jpaRepository.findActiveBreak(attendanceRecordId)?.let { toDomain(it) }
    }

    override fun findAll(pageable: Pageable): Page<BreakRecord> {
        return jpaRepository.findAll(pageable).map { toDomain(it) }
    }

    override fun delete(breakRecord: BreakRecord) {
        breakRecord.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: BreakRecordEntity): BreakRecord {
        return BreakRecord(
            id = entity.id,
            attendanceRecordId = entity.attendanceRecordId,
            breakStartTime = entity.breakStartTime,
            breakEndTime = entity.breakEndTime,
            breakDurationMinutes = entity.breakDurationMinutes,
            breakType = entity.breakType,
            notes = entity.notes,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEntity(domain: BreakRecord): BreakRecordEntity {
        return BreakRecordEntity(
            id = domain.id,
            attendanceRecordId = domain.attendanceRecordId,
            breakStartTime = domain.breakStartTime,
            breakEndTime = domain.breakEndTime,
            breakDurationMinutes = domain.breakDurationMinutes,
            breakType = domain.breakType,
            notes = domain.notes,
            createdAt = domain.createdAt ?: LocalDateTime.now(),
            updatedAt = domain.updatedAt ?: LocalDateTime.now()
        )
    }
}