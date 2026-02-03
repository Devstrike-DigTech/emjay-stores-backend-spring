package com.emjay.backend.sms.presentation.controller.shift

import com.emjay.backend.sms.application.dto.shift.*
import com.emjay.backend.sms.application.service.ShiftTemplateService
import com.emjay.backend.sms.domain.entity.shift.ShiftType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/shifts/templates")
@Tag(name = "Shift Templates", description = "Endpoints for managing shift templates")
@SecurityRequirement(name = "bearerAuth")
class ShiftTemplateController(
    private val shiftTemplateService: ShiftTemplateService
) {

    @PostMapping
    @Operation(summary = "Create shift template (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun createShiftTemplate(
        @Valid @RequestBody request: CreateShiftTemplateRequest
    ): ResponseEntity<ShiftTemplateResponse> {
        val response = shiftTemplateService.createShiftTemplate(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(summary = "Get all shift templates")
    fun getAllShiftTemplates(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ShiftTemplateListResponse> {
        val response = shiftTemplateService.getAllShiftTemplates(page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/active")
    @Operation(summary = "Get active shift templates")
    fun getActiveShiftTemplates(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ShiftTemplateListResponse> {
        val response = shiftTemplateService.getActiveShiftTemplates(page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get shift template by ID")
    fun getShiftTemplateById(@PathVariable id: UUID): ResponseEntity<ShiftTemplateResponse> {
        val response = shiftTemplateService.getShiftTemplateById(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get shift templates by type")
    fun getShiftTemplatesByType(
        @PathVariable type: ShiftType,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ShiftTemplateListResponse> {
        val response = shiftTemplateService.getShiftTemplatesByType(type, page, size)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update shift template (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun updateShiftTemplate(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateShiftTemplateRequest
    ): ResponseEntity<ShiftTemplateResponse> {
        val response = shiftTemplateService.updateShiftTemplate(id, request)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate shift template (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun deactivateShiftTemplate(@PathVariable id: UUID): ResponseEntity<ShiftTemplateResponse> {
        val response = shiftTemplateService.deactivateShiftTemplate(id)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate shift template (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun activateShiftTemplate(@PathVariable id: UUID): ResponseEntity<ShiftTemplateResponse> {
        val response = shiftTemplateService.activateShiftTemplate(id)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete shift template (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteShiftTemplate(@PathVariable id: UUID): ResponseEntity<Void> {
        shiftTemplateService.deleteShiftTemplate(id)
        return ResponseEntity.noContent().build()
    }
}