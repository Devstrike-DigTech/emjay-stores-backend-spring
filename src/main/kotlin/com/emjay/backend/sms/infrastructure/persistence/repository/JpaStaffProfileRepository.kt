package com.emjay.backend.sms.infrastructure.persistence.repository

import com.emjay.backend.sms.domain.entity.staff.EmploymentType
import com.emjay.backend.sms.domain.entity.staff.StaffStatus
import com.emjay.backend.sms.infrastructure.persistence.entity.StaffProfileEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface JpaStaffProfileRepository : JpaRepository<StaffProfileEntity, UUID> {

    fun findByUserId(userId: UUID): StaffProfileEntity?

    fun findByEmployeeId(employeeId: String): StaffProfileEntity?

    fun findByDepartment(department: String, pageable: Pageable): Page<StaffProfileEntity>

    fun findByStatus(status: StaffStatus, pageable: Pageable): Page<StaffProfileEntity>

    fun findByEmploymentType(type: EmploymentType, pageable: Pageable): Page<StaffProfileEntity>

    fun existsByEmployeeId(employeeId: String): Boolean

    fun existsByUserId(userId: UUID): Boolean

    fun countByStatus(status: StaffStatus): Long

    fun countByDepartment(department: String): Long
}