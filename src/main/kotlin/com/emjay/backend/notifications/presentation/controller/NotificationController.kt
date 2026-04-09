package com.emjay.backend.notifications.presentation.controller

import com.emjay.backend.common.infrastructure.security.jwt.JwtTokenProvider
import com.emjay.backend.notifications.application.dto.*
import com.emjay.backend.notifications.application.service.NotificationPreferencesService
import com.emjay.backend.notifications.application.service.NotificationService
import com.emjay.backend.notifications.application.service.NotificationTemplateService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Notification and communication management")
class NotificationController(
    private val notificationService: NotificationService,
    private val templateService: NotificationTemplateService,
    private val preferencesService: NotificationPreferencesService,
    private val jwtUtil: JwtTokenProvider
) {

    // ========== NOTIFICATIONS ==========

    @PostMapping("/queue")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Queue notification for sending")
    fun queueNotification(
        @Valid @RequestBody request: QueueNotificationRequest
    ): ResponseEntity<NotificationQueueResponse> {
        val response = notificationService.queueNotification(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/send/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Send notification immediately")
    fun sendNotification(@PathVariable id: UUID): ResponseEntity<NotificationHistoryResponse> {
        val response = notificationService.sendNotification(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get notification history")
    fun getHistory(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<NotificationHistoryResponse>> {
        val history = notificationService.getHistory(page, size)
        return ResponseEntity.ok(history)
    }

    // ========== TEMPLATES ==========

    @PostMapping("/templates/email")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create email template")
    fun createEmailTemplate(
        @Valid @RequestBody request: CreateEmailTemplateRequest
    ): ResponseEntity<EmailTemplateResponse> {
        val response = templateService.createEmailTemplate(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/templates/email")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get email templates")
    fun getEmailTemplates(): ResponseEntity<List<EmailTemplateResponse>> {
        val templates = templateService.getEmailTemplates()
        return ResponseEntity.ok(templates)
    }

    @PostMapping("/templates/sms")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create SMS template")
    fun createSmsTemplate(
        @Valid @RequestBody request: CreateSmsTemplateRequest
    ): ResponseEntity<SmsTemplateResponse> {
        val response = templateService.createSmsTemplate(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    // ========== PREFERENCES ==========

    @GetMapping("/preferences")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get notification preferences")
    fun getPreferences(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<NotificationPreferencesResponse> {
        val customerId = extractUserIdFromToken(token)
        val prefs = preferencesService.getPreferences(customerId)
        return ResponseEntity.ok(prefs)
    }

    @PutMapping("/preferences")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update notification preferences")
    fun updatePreferences(
        @Valid @RequestBody request: UpdateNotificationPreferencesRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<NotificationPreferencesResponse> {
        val customerId = extractUserIdFromToken(token)
        val prefs = preferencesService.updatePreferences(customerId, request)
        return ResponseEntity.ok(prefs)
    }

    // ========== HELPER ==========

    private fun extractUserIdFromToken(token: String): UUID {
        val jwt = token.removePrefix("Bearer ")
        val userId = jwtUtil.getUserIdFromToken(jwt)
        return UUID.fromString(userId.toString())
    }
}