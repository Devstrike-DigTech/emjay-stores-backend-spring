package com.emjay.backend.sms.infrastructure.persistence.repository

import com.emjay.backend.sms.domain.entity.leave.LeavePolicy
import com.emjay.backend.sms.domain.entity.leave.LeaveType
import com.emjay.backend.sms.domain.repository.leave.LeavePolicyRepository
import com.emjay.backend.sms.infrastructure.persistence.entity.LeavePolicyEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class LeavePolicyRepositoryImpl(
    private val jpaRepository: JpaLeavePolicyRepository
) : LeavePolicyRepository {

    override fun save(leavePolicy: LeavePolicy): LeavePolicy {
        val entity = toEntity(leavePolicy)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): LeavePolicy? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findByLeaveType(leaveType: LeaveType): LeavePolicy? {
        return jpaRepository.findByLeaveType(leaveType)?.let { toDomain(it) }
    }

    override fun findAll(pageable: Pageable): Page<LeavePolicy> {
        return jpaRepository.findAll(pageable).map { toDomain(it) }
    }

    override fun findActivePolicy(pageable: Pageable): Page<LeavePolicy> {
        return jpaRepository.findByIsActive(true, pageable).map { toDomain(it) }
    }

    override fun delete(leavePolicy: LeavePolicy) {
        leavePolicy.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: LeavePolicyEntity): LeavePolicy {
        return LeavePolicy(
            id = entity.id,
            leaveType = entity.leaveType,
            policyName = entity.policyName,
            description = entity.description,
            daysPerYear = entity.daysPerYear,
            maxConsecutiveDays = entity.maxConsecutiveDays,
            minDaysNotice = entity.minDaysNotice,
            allowCarryover = entity.allowCarryover,
            maxCarryoverDays = entity.maxCarryoverDays,
            carryoverExpiryMonths = entity.carryoverExpiryMonths,
            requiresDocumentation = entity.requiresDocumentation,
            requiresManagerApproval = entity.requiresManagerApproval,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEntity(domain: LeavePolicy): LeavePolicyEntity {
        return LeavePolicyEntity(
            id = domain.id,
            leaveType = domain.leaveType,
            policyName = domain.policyName,
            description = domain.description,
            daysPerYear = domain.daysPerYear,
            maxConsecutiveDays = domain.maxConsecutiveDays,
            minDaysNotice = domain.minDaysNotice,
            allowCarryover = domain.allowCarryover,
            maxCarryoverDays = domain.maxCarryoverDays,
            carryoverExpiryMonths = domain.carryoverExpiryMonths,
            requiresDocumentation = domain.requiresDocumentation,
            requiresManagerApproval = domain.requiresManagerApproval,
            isActive = domain.isActive,
            createdAt = domain.createdAt ?: LocalDateTime.now(),
            updatedAt = domain.updatedAt ?: LocalDateTime.now()
        )
    }
}