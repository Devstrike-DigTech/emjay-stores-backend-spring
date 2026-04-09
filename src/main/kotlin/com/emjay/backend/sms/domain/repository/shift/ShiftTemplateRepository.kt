package com.emjay.backend.sms.domain.repository.shift

import com.emjay.backend.sms.domain.entity.shift.ShiftTemplate
import com.emjay.backend.sms.domain.entity.shift.ShiftType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

/**
 * Repository interface for ShiftTemplate domain entity
 */
interface ShiftTemplateRepository {

    fun save(shiftTemplate: ShiftTemplate): ShiftTemplate

    fun findById(id: UUID): ShiftTemplate?

    fun findAll(pageable: Pageable): Page<ShiftTemplate>

    fun findByShiftType(type: ShiftType, pageable: Pageable): Page<ShiftTemplate>

    fun findActiveTemplates(pageable: Pageable): Page<ShiftTemplate>

    fun existsByName(name: String): Boolean

    fun delete(shiftTemplate: ShiftTemplate)
}