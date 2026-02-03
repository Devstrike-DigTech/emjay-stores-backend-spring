package com.emjay.backend.sms.presentation.controller.staff

import com.emjay.backend.ims.presentation.controller.product.ImageUploadRequest
import com.emjay.backend.sms.application.dto.staff.*
import com.emjay.backend.sms.application.service.StaffProfileService
import com.emjay.backend.sms.domain.entity.staff.EmploymentType
import com.emjay.backend.sms.domain.entity.staff.StaffStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/api/v1/staff/profiles")
@Tag(name = "Staff Profiles", description = "Endpoints for managing staff profiles")
@SecurityRequirement(name = "bearerAuth")
class StaffProfileController(
    private val staffProfileService: StaffProfileService
) {

    @PostMapping
    @Operation(summary = "Create staff profile (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun createStaffProfile(
        @Valid @RequestBody request: CreateStaffProfileRequest
    ): ResponseEntity<StaffProfileResponse> {
        val response = staffProfileService.createStaffProfile(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(summary = "Get all staff profiles (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getAllStaffProfiles(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<StaffProfileListResponse> {
        val response = staffProfileService.getAllStaffProfiles(page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get staff profile by ID (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getStaffProfileById(@PathVariable id: UUID): ResponseEntity<StaffProfileResponse> {
        val response = staffProfileService.getStaffProfileById(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get staff profile by user ID")
    fun getStaffProfileByUserId(@PathVariable userId: UUID): ResponseEntity<StaffProfileResponse> {
        val response = staffProfileService.getStaffProfileByUserId(userId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get staff profile by employee ID (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getStaffProfileByEmployeeId(@PathVariable employeeId: String): ResponseEntity<StaffProfileResponse> {
        val response = staffProfileService.getStaffProfileByEmployeeId(employeeId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/department/{department}")
    @Operation(summary = "Get staff by department (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getStaffByDepartment(
        @PathVariable department: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<StaffProfileListResponse> {
        val response = staffProfileService.getStaffByDepartment(department, page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get staff by status (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getStaffByStatus(
        @PathVariable status: StaffStatus,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<StaffProfileListResponse> {
        val response = staffProfileService.getStaffByStatus(status, page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/employment-type/{type}")
    @Operation(summary = "Get staff by employment type (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getStaffByEmploymentType(
        @PathVariable type: EmploymentType,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<StaffProfileListResponse> {
        val response = staffProfileService.getStaffByEmploymentType(type, page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get staff statistics (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getStaffStatistics(): ResponseEntity<StaffStatisticsResponse> {
        val response = staffProfileService.getStaffStatistics()
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update staff profile (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun updateStaffProfile(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateStaffProfileRequest
    ): ResponseEntity<StaffProfileResponse> {
        val response = staffProfileService.updateStaffProfile(id, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete staff profile (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteStaffProfile(@PathVariable id: UUID): ResponseEntity<Void> {
        staffProfileService.deleteStaffProfile(id)
        return ResponseEntity.noContent().build()
    }


    @PostMapping("/{id}/profile-image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Upload profile image (Admin/Manager)",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = [Content(
                mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                schema = Schema(implementation = ImageUploadRequest::class)
            )]
        )
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun uploadProfileImage(
        @PathVariable id: UUID,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<StaffProfileResponse> {
        val response = staffProfileService.uploadProfileImage(id, file)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}/profile-image")
    @Operation(summary = "Delete profile image (Admin/Manager)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun deleteProfileImage(@PathVariable id: UUID): ResponseEntity<StaffProfileResponse> {
        val response = staffProfileService.deleteProfileImage(id)
        return ResponseEntity.ok(response)
    }
}