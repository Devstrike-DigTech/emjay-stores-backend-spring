package com.emjay.backend.sms.domain.repository.shift

import com.emjay.backend.sms.domain.entity.shift.ShiftSwapRequest
import com.emjay.backend.sms.domain.entity.shift.SwapRequestStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

/**
 * Repository interface for ShiftSwapRequest domain entity
 */
interface ShiftSwapRequestRepository {

    fun save(swapRequest: ShiftSwapRequest): ShiftSwapRequest

    fun findById(id: UUID): ShiftSwapRequest?

    fun findAll(pageable: Pageable): Page<ShiftSwapRequest>

    fun findByRequesterShiftId(requesterShiftId: UUID): ShiftSwapRequest?

    fun findByTargetShiftId(targetShiftId: UUID, pageable: Pageable): Page<ShiftSwapRequest>

    fun findByStatus(status: SwapRequestStatus, pageable: Pageable): Page<ShiftSwapRequest>

    fun findPendingRequests(pageable: Pageable): Page<ShiftSwapRequest>

    fun delete(swapRequest: ShiftSwapRequest)
}