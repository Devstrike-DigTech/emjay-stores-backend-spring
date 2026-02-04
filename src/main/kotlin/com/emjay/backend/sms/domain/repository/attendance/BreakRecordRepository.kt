package com.emjay.backend.sms.domain.repository.attendance

import com.emjay.backend.sms.domain.entity.attendance.BreakRecord
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

/**
 * Repository interface for BreakRecord domain entity
 */
interface BreakRecordRepository {

    fun save(breakRecord: BreakRecord): BreakRecord

    fun findById(id: UUID): BreakRecord?

    fun findByAttendanceRecordId(attendanceRecordId: UUID, pageable: Pageable): Page<BreakRecord>

    fun findActiveBreak(attendanceRecordId: UUID): BreakRecord?

    fun findAll(pageable: Pageable): Page<BreakRecord>

    fun delete(breakRecord: BreakRecord)
}