package com.emjay.backend.sms.infrastructure.persistence.repository

import com.emjay.backend.sms.domain.entity.shift.ShiftSwapRequest
import com.emjay.backend.sms.domain.entity.shift.SwapRequestStatus
import com.emjay.backend.sms.domain.repository.shift.ShiftSwapRequestRepository
import com.emjay.backend.sms.infrastructure.persistence.entity.ShiftSwapRequestEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ShiftSwapRequestRepositoryImpl(
    private val jpaRepository: JpaShiftSwapRequestRepository
) : ShiftSwapRequestRepository {

    override fun save(swapRequest: ShiftSwapRequest): ShiftSwapRequest {
        val entity = toEntity(swapRequest)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): ShiftSwapRequest? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findAll(pageable: Pageable): Page<ShiftSwapRequest> {
        return jpaRepository.findAll(pageable).map { toDomain(it) }
    }

    override fun findByRequesterShiftId(requesterShiftId: UUID): ShiftSwapRequest? {
        return jpaRepository.findByRequesterShiftId(requesterShiftId)?.let { toDomain(it) }
    }

    override fun findByTargetShiftId(targetShiftId: UUID, pageable: Pageable): Page<ShiftSwapRequest> {
        return jpaRepository.findByTargetShiftId(targetShiftId, pageable).map { toDomain(it) }
    }

    override fun findByStatus(status: SwapRequestStatus, pageable: Pageable): Page<ShiftSwapRequest> {
        return jpaRepository.findByStatus(status, pageable).map { toDomain(it) }
    }

    override fun findPendingRequests(pageable: Pageable): Page<ShiftSwapRequest> {
        return jpaRepository.findByStatus(SwapRequestStatus.PENDING, pageable).map { toDomain(it) }
    }

    override fun delete(swapRequest: ShiftSwapRequest) {
        swapRequest.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: ShiftSwapRequestEntity): ShiftSwapRequest {
        return ShiftSwapRequest(
            id = entity.id,
            requesterShiftId = entity.requesterShiftId,
            targetShiftId = entity.targetShiftId,
            targetStaffId = entity.targetStaffId,
            reason = entity.reason,
            status = entity.status,
            approvedBy = entity.approvedBy,
            approvedAt = entity.approvedAt,
            rejectionReason = entity.rejectionReason,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEntity(domain: ShiftSwapRequest): ShiftSwapRequestEntity {
        return ShiftSwapRequestEntity(
            id = domain.id,
            requesterShiftId = domain.requesterShiftId,
            targetShiftId = domain.targetShiftId,
            targetStaffId = domain.targetStaffId,
            reason = domain.reason,
            status = domain.status,
            approvedBy = domain.approvedBy,
            approvedAt = domain.approvedAt,
            rejectionReason = domain.rejectionReason,
            createdAt = domain.createdAt ?: java.time.LocalDateTime.now(),
            updatedAt = domain.updatedAt ?: java.time.LocalDateTime.now()
        )
    }
}