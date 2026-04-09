package com.emjay.backend.sms.domain.repository.staff

import com.emjay.backend.sms.domain.entity.staff.EmploymentType
import com.emjay.backend.sms.domain.entity.staff.StaffProfile
import com.emjay.backend.sms.domain.entity.staff.StaffStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

/**
 * Repository interface for StaffProfile domain entity
 */
interface StaffProfileRepository {

    fun save(staffProfile: StaffProfile): StaffProfile

    fun findById(id: UUID): StaffProfile?

    fun findByUserId(userId: UUID): StaffProfile?

    fun findByEmployeeId(employeeId: String): StaffProfile?

    fun findAll(pageable: Pageable): Page<StaffProfile>

    fun findByDepartment(department: String, pageable: Pageable): Page<StaffProfile>

    fun findByStatus(status: StaffStatus, pageable: Pageable): Page<StaffProfile>

    fun findByEmploymentType(type: EmploymentType, pageable: Pageable): Page<StaffProfile>

    fun existsByEmployeeId(employeeId: String): Boolean

    fun existsByUserId(userId: UUID): Boolean

    fun countByStatus(status: StaffStatus): Long

    fun countByDepartment(department: String): Long

    fun delete(staffProfile: StaffProfile)
}