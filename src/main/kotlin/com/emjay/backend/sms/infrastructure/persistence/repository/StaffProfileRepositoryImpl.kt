package com.emjay.backend.sms.infrastructure.persistence.repository

import com.emjay.backend.sms.domain.entity.staff.EmploymentType
import com.emjay.backend.sms.domain.entity.staff.StaffProfile
import com.emjay.backend.sms.domain.entity.staff.StaffStatus
import com.emjay.backend.sms.domain.repository.staff.StaffProfileRepository
import com.emjay.backend.sms.infrastructure.persistence.entity.StaffProfileEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class StaffProfileRepositoryImpl(
    private val jpaRepository: JpaStaffProfileRepository
) : StaffProfileRepository {

    override fun save(staffProfile: StaffProfile): StaffProfile {
        val entity = toEntity(staffProfile)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): StaffProfile? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findByUserId(userId: UUID): StaffProfile? {
        return jpaRepository.findByUserId(userId)?.let { toDomain(it) }
    }

    override fun findByEmployeeId(employeeId: String): StaffProfile? {
        return jpaRepository.findByEmployeeId(employeeId)?.let { toDomain(it) }
    }

    override fun findAll(pageable: Pageable): Page<StaffProfile> {
        return jpaRepository.findAll(pageable).map { toDomain(it) }
    }

    override fun findByDepartment(department: String, pageable: Pageable): Page<StaffProfile> {
        return jpaRepository.findByDepartment(department, pageable).map { toDomain(it) }
    }

    override fun findByStatus(status: StaffStatus, pageable: Pageable): Page<StaffProfile> {
        return jpaRepository.findByStatus(status, pageable).map { toDomain(it) }
    }

    override fun findByEmploymentType(type: EmploymentType, pageable: Pageable): Page<StaffProfile> {
        return jpaRepository.findByEmploymentType(type, pageable).map { toDomain(it) }
    }

    override fun existsByEmployeeId(employeeId: String): Boolean {
        return jpaRepository.existsByEmployeeId(employeeId)
    }

    override fun existsByUserId(userId: UUID): Boolean {
        return jpaRepository.existsByUserId(userId)
    }

    override fun countByStatus(status: StaffStatus): Long {
        return jpaRepository.countByStatus(status)
    }

    override fun countByDepartment(department: String): Long {
        return jpaRepository.countByDepartment(department)
    }

    override fun delete(staffProfile: StaffProfile) {
        staffProfile.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: StaffProfileEntity): StaffProfile {
        return StaffProfile(
            id = entity.id,
            userId = entity.userId,
            employeeId = entity.employeeId,
            firstName = entity.firstName,
            lastName = entity.lastName,
            dateOfBirth = entity.dateOfBirth,
            gender = entity.gender,
            phoneNumber = entity.phoneNumber,
            personalEmail = entity.personalEmail,
            address = entity.address,
            city = entity.city,
            state = entity.state,
            country = entity.country,
            nationality = entity.nationality,
            profileImageUrl = entity.profileImageUrl,
            position = entity.position,
            department = entity.department,
            employmentType = entity.employmentType,
            hireDate = entity.hireDate,
            contractEndDate = entity.contractEndDate,
            salary = entity.salary,
            bankName = entity.bankName,
            accountNumber = entity.accountNumber,
            accountName = entity.accountName,
            emergencyContactName = entity.emergencyContactName,
            emergencyContactPhone = entity.emergencyContactPhone,
            emergencyContactRelationship = entity.emergencyContactRelationship,
            status = entity.status,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEntity(domain: StaffProfile): StaffProfileEntity {
        return StaffProfileEntity(
            id = domain.id,
            userId = domain.userId,
            employeeId = domain.employeeId,
            firstName = domain.firstName,
            lastName = domain.lastName,
            dateOfBirth = domain.dateOfBirth,
            gender = domain.gender,
            phoneNumber = domain.phoneNumber,
            personalEmail = domain.personalEmail,
            address = domain.address,
            city = domain.city,
            state = domain.state,
            country = domain.country,
            nationality = domain.nationality,
            profileImageUrl = domain.profileImageUrl,
            position = domain.position,
            department = domain.department,
            employmentType = domain.employmentType,
            hireDate = domain.hireDate,
            contractEndDate = domain.contractEndDate,
            salary = domain.salary,
            bankName = domain.bankName,
            accountNumber = domain.accountNumber,
            accountName = domain.accountName,
            emergencyContactName = domain.emergencyContactName,
            emergencyContactPhone = domain.emergencyContactPhone,
            emergencyContactRelationship = domain.emergencyContactRelationship,
            status = domain.status,
            createdAt = domain.createdAt ?: java.time.LocalDateTime.now(),
            updatedAt = domain.updatedAt ?: java.time.LocalDateTime.now()
        )
    }
}