package com.emjay.backend.sms.application.service

import com.emjay.backend.common.domain.repository.user.UserRepository
import com.emjay.backend.domain.exception.ResourceAlreadyExistsException
import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.infrastructure.storage.FileStorageService
import com.emjay.backend.sms.application.dto.staff.*
import com.emjay.backend.sms.domain.entity.staff.EmploymentType
import com.emjay.backend.sms.domain.entity.staff.StaffProfile
import com.emjay.backend.sms.domain.entity.staff.StaffStatus
import com.emjay.backend.sms.domain.repository.staff.StaffProfileRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class StaffProfileService(
    private val staffProfileRepository: StaffProfileRepository,
    private val userRepository: UserRepository,
    private val fileStorageService: FileStorageService

) {

    @Transactional
    fun createStaffProfile(request: CreateStaffProfileRequest): StaffProfileResponse {
        // Validate user exists
        userRepository.findById(request.userId)
            ?: throw ResourceNotFoundException("User not found")

        // Check if employee ID already exists
        if (staffProfileRepository.existsByEmployeeId(request.employeeId)) {
            throw ResourceAlreadyExistsException("Employee ID '${request.employeeId}' already exists")
        }

        // Check if user already has a staff profile
        if (staffProfileRepository.existsByUserId(request.userId)) {
            throw ResourceAlreadyExistsException("Staff profile already exists for this user")
        }

        // Create staff profile
        val staffProfile = StaffProfile(
            userId = request.userId,
            employeeId = request.employeeId,
            firstName = request.firstName,
            lastName = request.lastName,
            dateOfBirth = request.dateOfBirth,
            gender = request.gender,
            phoneNumber = request.phoneNumber,
            personalEmail = request.personalEmail,
            address = request.address,
            city = request.city,
            state = request.state,
            country = request.country,
            nationality = request.nationality,
            position = request.position,
            department = request.department,
            employmentType = request.employmentType,
            hireDate = request.hireDate,
            contractEndDate = request.contractEndDate,
            salary = request.salary,
            bankName = request.bankName,
            accountNumber = request.accountNumber,
            accountName = request.accountName,
            emergencyContactName = request.emergencyContactName,
            emergencyContactPhone = request.emergencyContactPhone,
            emergencyContactRelationship = request.emergencyContactRelationship
        )

        val saved = staffProfileRepository.save(staffProfile)
        return toStaffProfileResponse(saved)
    }

    fun getStaffProfileById(id: UUID): StaffProfileResponse {
        val staffProfile = staffProfileRepository.findById(id)
            ?: throw ResourceNotFoundException("Staff profile not found")
        return toStaffProfileResponse(staffProfile)
    }

    fun getStaffProfileByUserId(userId: UUID): StaffProfileResponse {
        val staffProfile = staffProfileRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Staff profile not found for this user")
        return toStaffProfileResponse(staffProfile)
    }

    fun getStaffProfileByEmployeeId(employeeId: String): StaffProfileResponse {
        val staffProfile = staffProfileRepository.findByEmployeeId(employeeId)
            ?: throw ResourceNotFoundException("Staff profile not found with employee ID: $employeeId")
        return toStaffProfileResponse(staffProfile)
    }

    fun getAllStaffProfiles(page: Int = 0, size: Int = 20): StaffProfileListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "firstName", "lastName"))
        val profilesPage = staffProfileRepository.findAll(pageable)

        val responses = profilesPage.content.map { toStaffProfileResponse(it) }

        return StaffProfileListResponse(
            content = responses,
            totalElements = profilesPage.totalElements,
            totalPages = profilesPage.totalPages,
            currentPage = profilesPage.number,
            pageSize = profilesPage.size
        )
    }

    fun getStaffByDepartment(department: String, page: Int = 0, size: Int = 20): StaffProfileListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "firstName", "lastName"))
        val profilesPage = staffProfileRepository.findByDepartment(department, pageable)

        val responses = profilesPage.content.map { toStaffProfileResponse(it) }

        return StaffProfileListResponse(
            content = responses,
            totalElements = profilesPage.totalElements,
            totalPages = profilesPage.totalPages,
            currentPage = profilesPage.number,
            pageSize = profilesPage.size
        )
    }

    fun getStaffByStatus(status: StaffStatus, page: Int = 0, size: Int = 20): StaffProfileListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "firstName", "lastName"))
        val profilesPage = staffProfileRepository.findByStatus(status, pageable)

        val responses = profilesPage.content.map { toStaffProfileResponse(it) }

        return StaffProfileListResponse(
            content = responses,
            totalElements = profilesPage.totalElements,
            totalPages = profilesPage.totalPages,
            currentPage = profilesPage.number,
            pageSize = profilesPage.size
        )
    }

    fun getStaffByEmploymentType(type: EmploymentType, page: Int = 0, size: Int = 20): StaffProfileListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "firstName", "lastName"))
        val profilesPage = staffProfileRepository.findByEmploymentType(type, pageable)

        val responses = profilesPage.content.map { toStaffProfileResponse(it) }

        return StaffProfileListResponse(
            content = responses,
            totalElements = profilesPage.totalElements,
            totalPages = profilesPage.totalPages,
            currentPage = profilesPage.number,
            pageSize = profilesPage.size
        )
    }

    @Transactional
    fun updateStaffProfile(id: UUID, request: UpdateStaffProfileRequest): StaffProfileResponse {
        val existing = staffProfileRepository.findById(id)
            ?: throw ResourceNotFoundException("Staff profile not found")

        val updated = existing.copy(
            firstName = request.firstName ?: existing.firstName,
            lastName = request.lastName ?: existing.lastName,
            dateOfBirth = request.dateOfBirth ?: existing.dateOfBirth,
            gender = request.gender ?: existing.gender,
            phoneNumber = request.phoneNumber ?: existing.phoneNumber,
            personalEmail = request.personalEmail ?: existing.personalEmail,
            address = request.address ?: existing.address,
            city = request.city ?: existing.city,
            state = request.state ?: existing.state,
            country = request.country ?: existing.country,
            nationality = request.nationality ?: existing.nationality,
            position = request.position ?: existing.position,
            department = request.department ?: existing.department,
            employmentType = request.employmentType ?: existing.employmentType,
            contractEndDate = request.contractEndDate ?: existing.contractEndDate,
            salary = request.salary ?: existing.salary,
            bankName = request.bankName ?: existing.bankName,
            accountNumber = request.accountNumber ?: existing.accountNumber,
            accountName = request.accountName ?: existing.accountName,
            emergencyContactName = request.emergencyContactName ?: existing.emergencyContactName,
            emergencyContactPhone = request.emergencyContactPhone ?: existing.emergencyContactPhone,
            emergencyContactRelationship = request.emergencyContactRelationship ?: existing.emergencyContactRelationship,
            status = request.status ?: existing.status
        )

        val saved = staffProfileRepository.save(updated)
        return toStaffProfileResponse(saved)
    }

    @Transactional
    fun deleteStaffProfile(id: UUID) {
        val staffProfile = staffProfileRepository.findById(id)
            ?: throw ResourceNotFoundException("Staff profile not found")
        staffProfileRepository.delete(staffProfile)
    }


    @Transactional
    fun uploadProfileImage(id: UUID, file: MultipartFile): StaffProfileResponse {
        val staffProfile = staffProfileRepository.findById(id)
            ?: throw ResourceNotFoundException("Staff profile not found")

        // Delete old image if exists
        staffProfile.profileImageUrl?.let {
            fileStorageService.deleteFile(it)
        }

        // Upload new image to staff-profiles directory
        val uploadResult = fileStorageService.storeFile(file, "staff-profiles")

        // Update profile with new image URL
        val updated = staffProfile.copy(profileImageUrl = uploadResult.filePath)
        val saved = staffProfileRepository.save(updated)

        return toStaffProfileResponse(saved)
    }

    @Transactional
    fun deleteProfileImage(id: UUID): StaffProfileResponse {
        val staffProfile = staffProfileRepository.findById(id)
            ?: throw ResourceNotFoundException("Staff profile not found")

        // Delete image file if exists
        staffProfile.profileImageUrl?.let {
            fileStorageService.deleteFile(it)
        }

        // Update profile to remove image URL
        val updated = staffProfile.copy(profileImageUrl = null)
        val saved = staffProfileRepository.save(updated)

        return toStaffProfileResponse(saved)
    }


    fun getStaffStatistics(): StaffStatisticsResponse {
        return StaffStatisticsResponse(
            totalStaff = staffProfileRepository.countByStatus(StaffStatus.ACTIVE) +
                    staffProfileRepository.countByStatus(StaffStatus.ON_LEAVE) +
                    staffProfileRepository.countByStatus(StaffStatus.SUSPENDED) +
                    staffProfileRepository.countByStatus(StaffStatus.TERMINATED),
            activeStaff = staffProfileRepository.countByStatus(StaffStatus.ACTIVE),
            onLeave = staffProfileRepository.countByStatus(StaffStatus.ON_LEAVE),
            suspended = staffProfileRepository.countByStatus(StaffStatus.SUSPENDED),
            terminated = staffProfileRepository.countByStatus(StaffStatus.TERMINATED),
            fullTimeStaff = countByEmploymentType(EmploymentType.FULL_TIME),
            partTimeStaff = countByEmploymentType(EmploymentType.PART_TIME),
            contractStaff = countByEmploymentType(EmploymentType.CONTRACT),
            internStaff = countByEmploymentType(EmploymentType.INTERN)
        )
    }

    private fun countByEmploymentType(type: EmploymentType): Long {
        return staffProfileRepository.findByEmploymentType(type, PageRequest.of(0, 1)).totalElements
    }

    private fun toStaffProfileResponse(staffProfile: StaffProfile): StaffProfileResponse {
        return StaffProfileResponse(
            id = staffProfile.id.toString(),
            userId = staffProfile.userId.toString(),
            employeeId = staffProfile.employeeId,
            firstName = staffProfile.firstName,
            lastName = staffProfile.lastName,
            fullName = staffProfile.fullName(),
            dateOfBirth = staffProfile.dateOfBirth,
            gender = staffProfile.gender,
            phoneNumber = staffProfile.phoneNumber,
            personalEmail = staffProfile.personalEmail,
            address = staffProfile.address,
            city = staffProfile.city,
            state = staffProfile.state,
            country = staffProfile.country,
            nationality = staffProfile.nationality,
            profileImageUrl = staffProfile.profileImageUrl,
            position = staffProfile.position,
            department = staffProfile.department,
            employmentType = staffProfile.employmentType,
            hireDate = staffProfile.hireDate,
            contractEndDate = staffProfile.contractEndDate,
            yearsOfService = staffProfile.yearsOfService(),
            salary = staffProfile.salary,
            bankName = staffProfile.bankName,
            accountNumber = staffProfile.accountNumber,
            accountName = staffProfile.accountName,
            emergencyContactName = staffProfile.emergencyContactName,
            emergencyContactPhone = staffProfile.emergencyContactPhone,
            emergencyContactRelationship = staffProfile.emergencyContactRelationship,
            hasEmergencyContact = staffProfile.hasEmergencyContact(),
            status = staffProfile.status,
            createdAt = staffProfile.createdAt!!,
            updatedAt = staffProfile.updatedAt!!
        )
    }
}