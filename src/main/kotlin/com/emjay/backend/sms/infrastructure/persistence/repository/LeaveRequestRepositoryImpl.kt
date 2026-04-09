package com.emjay.backend.sms.infrastructure.persistence.repository

import com.emjay.backend.sms.domain.entity.leave.LeaveRequest
import com.emjay.backend.sms.domain.entity.leave.LeaveRequestStatus
import com.emjay.backend.sms.domain.repository.leave.LeaveRequestRepository
import com.emjay.backend.sms.infrastructure.persistence.entity.LeaveRequestEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Repository
class LeaveRequestRepositoryImpl(
    private val jpaRepository: JpaLeaveRequestRepository
) : LeaveRequestRepository {

    override fun save(leaveRequest: LeaveRequest): LeaveRequest {
        val entity = toEntity(leaveRequest)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): LeaveRequest? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findAll(pageable: Pageable): Page<LeaveRequest> {
        return jpaRepository.findAll(pageable).map { toDomain(it) }
    }

    override fun findByStaffProfileId(staffProfileId: UUID, pageable: Pageable): Page<LeaveRequest> {
        return jpaRepository.findByStaffProfileId(staffProfileId, pageable).map { toDomain(it) }
    }

    override fun findByStatus(status: LeaveRequestStatus, pageable: Pageable): Page<LeaveRequest> {
        return jpaRepository.findByStatus(status, pageable).map { toDomain(it) }
    }

    override fun findByStaffAndStatus(
        staffProfileId: UUID,
        status: LeaveRequestStatus,
        pageable: Pageable
    ): Page<LeaveRequest> {
        return jpaRepository.findByStaffAndStatus(staffProfileId, status, pageable).map { toDomain(it) }
    }

    override fun findPendingRequests(pageable: Pageable): Page<LeaveRequest> {
        return jpaRepository.findByStatus(LeaveRequestStatus.PENDING, pageable).map { toDomain(it) }
    }

    override fun findByDateRange(startDate: LocalDate, endDate: LocalDate, pageable: Pageable): Page<LeaveRequest> {
        return jpaRepository.findByDateRange(startDate, endDate, pageable).map { toDomain(it) }
    }

    override fun findByStaffAndDateRange(
        staffProfileId: UUID,
        startDate: LocalDate,
        endDate: LocalDate,
        pageable: Pageable
    ): Page<LeaveRequest> {
        return jpaRepository.findByStaffAndDateRange(staffProfileId, startDate, endDate, pageable)
            .map { toDomain(it) }
    }

    override fun findOverlappingRequests(
        staffProfileId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<LeaveRequest> {
        return jpaRepository.findOverlappingRequests(staffProfileId, startDate, endDate)
            .map { toDomain(it) }
    }

    override fun countPendingByStaff(staffProfileId: UUID): Long {
        return jpaRepository.countByStaffProfileIdAndStatus(staffProfileId, LeaveRequestStatus.PENDING)
    }

    override fun delete(leaveRequest: LeaveRequest) {
        leaveRequest.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: LeaveRequestEntity): LeaveRequest {
        return LeaveRequest(
            id = entity.id,
            staffProfileId = entity.staffProfileId,
            leaveBalanceId = entity.leaveBalanceId,
            leaveType = entity.leaveType,
            startDate = entity.startDate,
            endDate = entity.endDate,
            totalDays = entity.totalDays,
            reason = entity.reason,
            status = entity.status,
            requestedBy = entity.requestedBy,
            requestedAt = entity.requestedAt,
            reviewedBy = entity.reviewedBy,
            reviewedAt = entity.reviewedAt,
            rejectionReason = entity.rejectionReason,
            supportingDocumentUrl = entity.supportingDocumentUrl,
            staffNotes = entity.staffNotes,
            managerNotes = entity.managerNotes,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEntity(domain: LeaveRequest): LeaveRequestEntity {
        return LeaveRequestEntity(
            id = domain.id,
            staffProfileId = domain.staffProfileId,
            leaveBalanceId = domain.leaveBalanceId,
            leaveType = domain.leaveType,
            startDate = domain.startDate,
            endDate = domain.endDate,
            totalDays = domain.totalDays,
            reason = domain.reason,
            status = domain.status,
            requestedBy = domain.requestedBy,
            requestedAt = domain.requestedAt ?: LocalDateTime.now(),
            reviewedBy = domain.reviewedBy,
            reviewedAt = domain.reviewedAt,
            rejectionReason = domain.rejectionReason,
            supportingDocumentUrl = domain.supportingDocumentUrl,
            staffNotes = domain.staffNotes,
            managerNotes = domain.managerNotes,
            createdAt = domain.createdAt ?: LocalDateTime.now(),
            updatedAt = domain.updatedAt ?: LocalDateTime.now()
        )
    }
}