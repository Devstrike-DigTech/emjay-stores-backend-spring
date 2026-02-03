package com.emjay.backend.sms.application.service

import com.emjay.backend.domain.exception.ResourceAlreadyExistsException
import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.sms.application.dto.shift.*
import com.emjay.backend.sms.domain.entity.shift.ShiftTemplate
import com.emjay.backend.sms.domain.entity.shift.ShiftType
import com.emjay.backend.sms.domain.repository.shift.ShiftTemplateRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ShiftTemplateService(
    private val shiftTemplateRepository: ShiftTemplateRepository
) {

    @Transactional
    fun createShiftTemplate(request: CreateShiftTemplateRequest): ShiftTemplateResponse {
        // Check if template with same name exists
        if (shiftTemplateRepository.existsByName(request.name)) {
            throw ResourceAlreadyExistsException("Shift template with name '${request.name}' already exists")
        }

        // Validate times
        if (request.endTime <= request.startTime && request.endTime != java.time.LocalTime.MIDNIGHT) {
            // Allow overnight shifts (end time before start time)
            // But ensure it's intentional
        }

        val shiftTemplate = ShiftTemplate(
            name = request.name,
            shiftType = request.shiftType,
            startTime = request.startTime,
            endTime = request.endTime,
            description = request.description,
            colorCode = request.colorCode
        )

        val saved = shiftTemplateRepository.save(shiftTemplate)
        return toShiftTemplateResponse(saved)
    }

    fun getShiftTemplateById(id: UUID): ShiftTemplateResponse {
        val template = shiftTemplateRepository.findById(id)
            ?: throw ResourceNotFoundException("Shift template not found")
        return toShiftTemplateResponse(template)
    }

    fun getAllShiftTemplates(page: Int = 0, size: Int = 20): ShiftTemplateListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"))
        val templatesPage = shiftTemplateRepository.findAll(pageable)

        val responses = templatesPage.content.map { toShiftTemplateResponse(it) }

        return ShiftTemplateListResponse(
            content = responses,
            totalElements = templatesPage.totalElements,
            totalPages = templatesPage.totalPages,
            currentPage = templatesPage.number,
            pageSize = templatesPage.size
        )
    }

    fun getActiveShiftTemplates(page: Int = 0, size: Int = 20): ShiftTemplateListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"))
        val templatesPage = shiftTemplateRepository.findActiveTemplates(pageable)

        val responses = templatesPage.content.map { toShiftTemplateResponse(it) }

        return ShiftTemplateListResponse(
            content = responses,
            totalElements = templatesPage.totalElements,
            totalPages = templatesPage.totalPages,
            currentPage = templatesPage.number,
            pageSize = templatesPage.size
        )
    }

    fun getShiftTemplatesByType(type: ShiftType, page: Int = 0, size: Int = 20): ShiftTemplateListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "startTime"))
        val templatesPage = shiftTemplateRepository.findByShiftType(type, pageable)

        val responses = templatesPage.content.map { toShiftTemplateResponse(it) }

        return ShiftTemplateListResponse(
            content = responses,
            totalElements = templatesPage.totalElements,
            totalPages = templatesPage.totalPages,
            currentPage = templatesPage.number,
            pageSize = templatesPage.size
        )
    }

    @Transactional
    fun updateShiftTemplate(id: UUID, request: UpdateShiftTemplateRequest): ShiftTemplateResponse {
        val existing = shiftTemplateRepository.findById(id)
            ?: throw ResourceNotFoundException("Shift template not found")

        // Check name uniqueness if name is being changed
        if (request.name != null && request.name != existing.name) {
            if (shiftTemplateRepository.existsByName(request.name)) {
                throw ResourceAlreadyExistsException("Shift template with name '${request.name}' already exists")
            }
        }

        val updated = existing.copy(
            name = request.name ?: existing.name,
            shiftType = request.shiftType ?: existing.shiftType,
            startTime = request.startTime ?: existing.startTime,
            endTime = request.endTime ?: existing.endTime,
            description = request.description ?: existing.description,
            colorCode = request.colorCode ?: existing.colorCode,
            isActive = request.isActive ?: existing.isActive
        )

        val saved = shiftTemplateRepository.save(updated)
        return toShiftTemplateResponse(saved)
    }

    @Transactional
    fun deleteShiftTemplate(id: UUID) {
        val template = shiftTemplateRepository.findById(id)
            ?: throw ResourceNotFoundException("Shift template not found")
        shiftTemplateRepository.delete(template)
    }

    @Transactional
    fun deactivateShiftTemplate(id: UUID): ShiftTemplateResponse {
        val existing = shiftTemplateRepository.findById(id)
            ?: throw ResourceNotFoundException("Shift template not found")

        val deactivated = existing.copy(isActive = false)
        val saved = shiftTemplateRepository.save(deactivated)
        return toShiftTemplateResponse(saved)
    }

    @Transactional
    fun activateShiftTemplate(id: UUID): ShiftTemplateResponse {
        val existing = shiftTemplateRepository.findById(id)
            ?: throw ResourceNotFoundException("Shift template not found")

        val activated = existing.copy(isActive = true)
        val saved = shiftTemplateRepository.save(activated)
        return toShiftTemplateResponse(saved)
    }

    private fun toShiftTemplateResponse(template: ShiftTemplate): ShiftTemplateResponse {
        return ShiftTemplateResponse(
            id = template.id.toString(),
            name = template.name,
            shiftType = template.shiftType,
            startTime = template.startTime,
            endTime = template.endTime,
            description = template.description,
            colorCode = template.colorCode,
            isActive = template.isActive,
            durationHours = template.durationInHours(),
            isOvernight = template.isOvernight(),
            createdAt = template.createdAt!!,
            updatedAt = template.updatedAt!!
        )
    }
}