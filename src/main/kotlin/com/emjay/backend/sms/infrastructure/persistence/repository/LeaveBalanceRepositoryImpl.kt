package com.emjay.backend.sms.infrastructure.persistence.repository

import com.emjay.backend.sms.domain.entity.leave.LeaveBalance
import com.emjay.backend.sms.domain.entity.leave.LeaveType
import com.emjay.backend.sms.domain.repository.leave.LeaveBalanceRepository
import com.emjay.backend.sms.infrastructure.persistence.entity.LeaveBalanceEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class LeaveBalanceRepositoryImpl(
    private val jpaRepository: JpaLeaveBalanceRepository
) : LeaveBalanceRepository {

    override fun save(leaveBalance: LeaveBalance): LeaveBalance {
        val entity = toEntity(leaveBalance)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): LeaveBalance? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findByStaffProfileId(staffProfileId: UUID, pageable: Pageable): Page<LeaveBalance> {
        return jpaRepository.findByStaffProfileId(staffProfileId, pageable).map { toDomain(it) }
    }

    override fun findByStaffAndYear(staffProfileId: UUID, year: Int, pageable: Pageable): Page<LeaveBalance> {
        return jpaRepository.findByStaffAndYear(staffProfileId, year, pageable).map { toDomain(it) }
    }

    override fun findByStaffAndLeaveTypeAndYear(
        staffProfileId: UUID,
        leaveType: LeaveType,
        year: Int
    ): LeaveBalance? {
        return jpaRepository.findByStaffProfileIdAndLeaveTypeAndYear(staffProfileId, leaveType, year)
            ?.let { toDomain(it) }
    }

    override fun findByYear(year: Int, pageable: Pageable): Page<LeaveBalance> {
        return jpaRepository.findByYear(year, pageable).map { toDomain(it) }
    }

    override fun findAll(pageable: Pageable): Page<LeaveBalance> {
        return jpaRepository.findAll(pageable).map { toDomain(it) }
    }

    override fun delete(leaveBalance: LeaveBalance) {
        leaveBalance.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: LeaveBalanceEntity): LeaveBalance {
        return LeaveBalance(
            id = entity.id,
            staffProfileId = entity.staffProfileId,
            leaveType = entity.leaveType,
            year = entity.year,
            totalDays = entity.totalDays,
            usedDays = entity.usedDays,
            pendingDays = entity.pendingDays,
            carriedOverDays = entity.carriedOverDays,
            allowNegative = entity.allowNegative,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEntity(domain: LeaveBalance): LeaveBalanceEntity {
        return LeaveBalanceEntity(
            id = domain.id,
            staffProfileId = domain.staffProfileId,
            leaveType = domain.leaveType,
            year = domain.year,
            totalDays = domain.totalDays,
            usedDays = domain.usedDays,
            pendingDays = domain.pendingDays,
            carriedOverDays = domain.carriedOverDays,
            allowNegative = domain.allowNegative,
            createdAt = domain.createdAt ?: LocalDateTime.now(),
            updatedAt = domain.updatedAt ?: LocalDateTime.now()
        )
    }
}