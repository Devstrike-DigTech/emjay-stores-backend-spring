package com.emjay.backend.sms.infrastructure.persistence.repository


import com.emjay.backend.sms.domain.entity.shift.ShiftTemplate
import com.emjay.backend.sms.domain.entity.shift.ShiftType
import com.emjay.backend.sms.domain.repository.shift.ShiftTemplateRepository
import com.emjay.backend.sms.infrastructure.persistence.entity.ShiftTemplateEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ShiftTemplateRepositoryImpl(
    private val jpaRepository: JpaShiftTemplateRepository
) : ShiftTemplateRepository {

    override fun save(shiftTemplate: ShiftTemplate): ShiftTemplate {
        val entity = toEntity(shiftTemplate)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): ShiftTemplate? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findAll(pageable: Pageable): Page<ShiftTemplate> {
        return jpaRepository.findAll(pageable).map { toDomain(it) }
    }

    override fun findByShiftType(type: ShiftType, pageable: Pageable): Page<ShiftTemplate> {
        return jpaRepository.findByShiftType(type, pageable).map { toDomain(it) }
    }

    override fun findActiveTemplates(pageable: Pageable): Page<ShiftTemplate> {
        return jpaRepository.findByIsActive(true, pageable).map { toDomain(it) }
    }

    override fun existsByName(name: String): Boolean {
        return jpaRepository.existsByName(name)
    }

    override fun delete(shiftTemplate: ShiftTemplate) {
        shiftTemplate.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: ShiftTemplateEntity): ShiftTemplate {
        return ShiftTemplate(
            id = entity.id,
            name = entity.name,
            shiftType = entity.shiftType,
            startTime = entity.startTime,
            endTime = entity.endTime,
            description = entity.description,
            colorCode = entity.colorCode,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEntity(domain: ShiftTemplate): ShiftTemplateEntity {
        return ShiftTemplateEntity(
            id = domain.id,
            name = domain.name,
            shiftType = domain.shiftType,
            startTime = domain.startTime,
            endTime = domain.endTime,
            description = domain.description,
            colorCode = domain.colorCode,
            isActive = domain.isActive,
            createdAt = domain.createdAt ?: java.time.LocalDateTime.now(),
            updatedAt = domain.updatedAt ?: java.time.LocalDateTime.now()
        )
    }
}