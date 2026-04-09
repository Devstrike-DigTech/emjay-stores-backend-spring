package com.emjay.backend.sms.application.dto.staff

import com.emjay.backend.sms.domain.entity.staff.EmploymentType
import com.emjay.backend.sms.domain.entity.staff.StaffStatus
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

// Request DTOs
data class CreateStaffProfileRequest(
    @field:NotNull(message = "User ID is required")
    val userId: UUID,

    @field:NotBlank(message = "Employee ID is required")
    val employeeId: String,

    @field:NotBlank(message = "First name is required")
    val firstName: String,

    @field:NotBlank(message = "Last name is required")
    val lastName: String,

    @field:Past(message = "Date of birth must be in the past")
    val dateOfBirth: LocalDate? = null,

    val gender: String? = null,
    val phoneNumber: String? = null,

    @field:Email(message = "Invalid email format")
    val personalEmail: String? = null,

    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String = "Nigeria",
    val nationality: String? = null,

    @field:NotBlank(message = "Position is required")
    val position: String,

    val department: String? = null,

    @field:NotNull(message = "Employment type is required")
    val employmentType: EmploymentType,

    @field:NotNull(message = "Hire date is required")
    val hireDate: LocalDate,

    val contractEndDate: LocalDate? = null,
    val salary: BigDecimal? = null,
    val bankName: String? = null,
    val accountNumber: String? = null,
    val accountName: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val emergencyContactRelationship: String? = null
)

data class UpdateStaffProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val dateOfBirth: LocalDate? = null,
    val gender: String? = null,
    val phoneNumber: String? = null,

    @field:Email(message = "Invalid email format")
    val personalEmail: String? = null,

    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val nationality: String? = null,
    val position: String? = null,
    val department: String? = null,
    val employmentType: EmploymentType? = null,
    val contractEndDate: LocalDate? = null,
    val salary: BigDecimal? = null,
    val bankName: String? = null,
    val accountNumber: String? = null,
    val accountName: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val emergencyContactRelationship: String? = null,
    val status: StaffStatus? = null
)

// Response DTOs
data class StaffProfileResponse(
    val id: String,
    val userId: String,
    val employeeId: String,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val dateOfBirth: LocalDate?,
    val gender: String?,
    val phoneNumber: String?,
    val personalEmail: String?,
    val address: String?,
    val city: String?,
    val state: String?,
    val country: String,
    val nationality: String?,
    val profileImageUrl: String?,
    val position: String,
    val department: String?,
    val employmentType: EmploymentType,
    val hireDate: LocalDate,
    val contractEndDate: LocalDate?,
    val yearsOfService: Int,
    val salary: BigDecimal?,
    val bankName: String?,
    val accountNumber: String?,
    val accountName: String?,
    val emergencyContactName: String?,
    val emergencyContactPhone: String?,
    val emergencyContactRelationship: String?,
    val hasEmergencyContact: Boolean,
    val status: StaffStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class StaffProfileListResponse(
    val content: List<StaffProfileResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)

data class StaffStatisticsResponse(
    val totalStaff: Long,
    val activeStaff: Long,
    val onLeave: Long,
    val suspended: Long,
    val terminated: Long,
    val fullTimeStaff: Long,
    val partTimeStaff: Long,
    val contractStaff: Long,
    val internStaff: Long
)