package com.emjay.backend.sms.infrastructure.persistence.entity

import com.emjay.backend.sms.domain.entity.staff.EmploymentType
import com.emjay.backend.sms.domain.entity.staff.StaffStatus
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "staff_profiles")
data class StaffProfileEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "user_id", nullable = false, unique = true)
    val userId: UUID,

    @Column(name = "employee_id", nullable = false, unique = true, length = 50)
    val employeeId: String,

    // Personal Information
    @Column(name = "first_name", nullable = false, length = 100)
    val firstName: String,

    @Column(name = "last_name", nullable = false, length = 100)
    val lastName: String,

    @Column(name = "date_of_birth")
    val dateOfBirth: LocalDate? = null,

    @Column(length = 20)
    val gender: String? = null,

    @Column(name = "phone_number", length = 20)
    val phoneNumber: String? = null,

    @Column(name = "personal_email", length = 100)
    val personalEmail: String? = null,

    @Column(columnDefinition = "TEXT")
    val address: String? = null,

    @Column(length = 100)
    val city: String? = null,

    @Column(length = 100)
    val state: String? = null,

    @Column(length = 100)
    val country: String = "Nigeria",

    @Column(length = 100)
    val nationality: String? = null,


    @Column(name = "profile_image_url", length = 500)
    val profileImageUrl: String? = null,


    // Employment Details
    @Column(nullable = false, length = 100)
    val position: String,

    @Column(length = 100)
    val department: String? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "employment_type", nullable = false, columnDefinition = "employment_type")
    val employmentType: EmploymentType = EmploymentType.FULL_TIME,

    @Column(name = "hire_date", nullable = false)
    val hireDate: LocalDate,

    @Column(name = "contract_end_date")
    val contractEndDate: LocalDate? = null,

    // Compensation
    @Column(precision = 12, scale = 2)
    val salary: BigDecimal? = null,

    @Column(name = "bank_name", length = 100)
    val bankName: String? = null,

    @Column(name = "account_number", length = 20)
    val accountNumber: String? = null,

    @Column(name = "account_name", length = 100)
    val accountName: String? = null,

    // Emergency Contact
    @Column(name = "emergency_contact_name", length = 100)
    val emergencyContactName: String? = null,

    @Column(name = "emergency_contact_phone", length = 20)
    val emergencyContactPhone: String? = null,

    @Column(name = "emergency_contact_relationship", length = 50)
    val emergencyContactRelationship: String? = null,

    // Status
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "staff_status")
    val status: StaffStatus = StaffStatus.ACTIVE,

    // Audit
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}