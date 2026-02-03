package com.emjay.backend.sms.domain.entity.staff


import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Staff Profile domain entity
 * Represents extended employee information beyond basic user account
 */
data class StaffProfile(
    val id: UUID? = null,
    val userId: UUID,
    val employeeId: String,

    // Personal Information
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate? = null,
    val gender: String? = null,
    val phoneNumber: String? = null,
    val personalEmail: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String = "Nigeria",
    val nationality: String? = null,
    val profileImageUrl: String? = null,


    // Employment Details
    val position: String,
    val department: String? = null,
    val employmentType: EmploymentType = EmploymentType.FULL_TIME,
    val hireDate: LocalDate,
    val contractEndDate: LocalDate? = null,

    // Compensation
    val salary: BigDecimal? = null,
    val bankName: String? = null,
    val accountNumber: String? = null,
    val accountName: String? = null,

    // Emergency Contact
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val emergencyContactRelationship: String? = null,

    // Status
    val status: StaffStatus = StaffStatus.ACTIVE,

    // Audit
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun fullName(): String = "$firstName $lastName"

    fun isActive(): Boolean = status == StaffStatus.ACTIVE

    fun isOnContract(): Boolean = employmentType == EmploymentType.CONTRACT

    fun yearsOfService(): Int {
        val now = LocalDate.now()
        return now.year - hireDate.year
    }

    fun hasEmergencyContact(): Boolean =
        !emergencyContactName.isNullOrBlank() && !emergencyContactPhone.isNullOrBlank()

    fun hasProfileImage(): Boolean = !profileImageUrl.isNullOrBlank()

}

/**
 * Employment type enum
 */
enum class EmploymentType {
    FULL_TIME,
    PART_TIME,
    CONTRACT,
    INTERN
}

/**
 * Staff status enum
 */
enum class StaffStatus {
    ACTIVE,
    ON_LEAVE,
    SUSPENDED,
    TERMINATED
}