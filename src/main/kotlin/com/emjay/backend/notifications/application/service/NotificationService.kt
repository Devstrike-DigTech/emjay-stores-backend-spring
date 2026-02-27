package com.emjay.backend.notifications.application.service

import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.notifications.application.dto.*
import com.emjay.backend.notifications.domain.entity.*
import com.emjay.backend.notifications.domain.repository.*
import com.emjay.backend.notifications.infrastructure.provider.EmailProvider
import com.emjay.backend.notifications.infrastructure.provider.SmsProvider
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class NotificationService(
    private val queueRepository: NotificationQueueRepository,
    private val historyRepository: NotificationHistoryRepository,
    private val emailTemplateRepository: EmailTemplateRepository,
    private val smsTemplateRepository: SmsTemplateRepository,
    private val preferencesRepository: NotificationPreferencesRepository,
    private val emailProvider: EmailProvider,
    private val smsProvider: SmsProvider
) {
    private val logger = LoggerFactory.getLogger(NotificationService::class.java)

    // ========== QUEUE NOTIFICATION ==========

    @Transactional
    fun queueNotification(request: QueueNotificationRequest): NotificationQueueResponse {
        // Check preferences if customer ID provided
        if (request.recipientId != null) {
            val prefs = preferencesRepository.findByCustomerId(request.recipientId)
            if (prefs != null) {
                val canSend = when (request.channel) {
                    NotificationChannel.EMAIL -> prefs.canSendEmail(request.notificationType)
                    NotificationChannel.SMS -> prefs.canSendSms(request.notificationType)
                    else -> true
                }

                if (!canSend) {
                    logger.info("Notification blocked by user preferences: ${request.notificationType}")
                    throw IllegalStateException("User has opted out of this notification type")
                }
            }
        }

        val notification = NotificationQueue(
            recipientId = request.recipientId,
            recipientEmail = request.recipientEmail,
            recipientPhone = request.recipientPhone,
            recipientName = request.recipientName,
            notificationType = request.notificationType,
            channel = request.channel,
            subject = request.subject,
            message = request.message ?: "",
            htmlContent = request.htmlContent,
            templateId = request.templateId,
            templateData = request.templateData,
            scheduledFor = request.scheduledFor,
            relatedEntityType = request.relatedEntityType,
            relatedEntityId = request.relatedEntityId
        )

        val saved = queueRepository.save(notification)
        logger.info("Queued notification: ${saved.id} - ${saved.notificationType} via ${saved.channel}")

        return NotificationQueueResponse(
            id = saved.id.toString(),
            recipientEmail = saved.recipientEmail,
            recipientPhone = saved.recipientPhone,
            notificationType = saved.notificationType,
            channel = saved.channel,
            status = saved.status,
            scheduledFor = saved.scheduledFor,
            createdAt = saved.createdAt!!
        )
    }

    // ========== SEND NOTIFICATION ==========

    @Transactional
    fun sendNotification(queueId: UUID): NotificationHistoryResponse {
        val notification = queueRepository.findById(queueId)
            ?: throw ResourceNotFoundException("Notification not found")

        val result = when (notification.channel) {
            NotificationChannel.EMAIL -> sendEmail(notification)
            NotificationChannel.SMS -> sendSms(notification)
            else -> throw IllegalArgumentException("Unsupported channel: ${notification.channel}")
        }

        // Update queue status
        val updated = if (result.success) {
            notification.markAsSent(result.messageId!!)
        } else {
            notification.markAsFailed(result.error ?: "Unknown error")
        }
        queueRepository.save(updated)

        // Save to history
        val history = NotificationHistory(
            queueId = notification.id,
            recipientId = notification.recipientId,
            recipientEmail = notification.recipientEmail,
            recipientPhone = notification.recipientPhone,
            notificationType = notification.notificationType,
            channel = notification.channel,
            subject = notification.subject,
            message = notification.message,
            status = updated.status,
            sentAt = updated.sentAt,
            provider = getProviderName(notification.channel),
            providerMessageId = result.messageId,
            providerResponse = mapOf("success" to result.success, "error" to result.error!!),
            relatedEntityType = notification.relatedEntityType,
            relatedEntityId = notification.relatedEntityId
        )
        val savedHistory = historyRepository.save(history)

        return toHistoryResponse(savedHistory)
    }

    private fun sendEmail(notification: NotificationQueue): com.emjay.backend.notifications.infrastructure.provider.SendResult {
        val email = notification.recipientEmail
            ?: return com.emjay.backend.notifications.infrastructure.provider.SendResult(false, null, "No email address")

        val subject = notification.subject ?: notification.notificationType.name
        val htmlContent = notification.htmlContent ?: notification.message

        return emailProvider.send(email, subject, htmlContent)
    }

    private fun sendSms(notification: NotificationQueue): com.emjay.backend.notifications.infrastructure.provider.SendResult {
        val phone = notification.recipientPhone
            ?: return com.emjay.backend.notifications.infrastructure.provider.SendResult(false, null, "No phone number")

        return smsProvider.send(phone, notification.message)
    }

    // ========== PROCESS QUEUE (Scheduled) ==========

    @Scheduled(fixedDelay = 30000) // Every 30 seconds
    @Transactional
    fun processQueue() {
        val notifications = queueRepository.findDueForSending()
        logger.info("Processing ${notifications.size} pending notifications")

        notifications.forEach { notification ->
            try {
                sendNotification(notification.id!!)
            } catch (e: Exception) {
                logger.error("Failed to send notification ${notification.id}: ${e.message}", e)
            }
        }
    }

    // ========== RETRY FAILED (Scheduled) ==========

    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    @Transactional
    fun retryFailed() {
        val failures = queueRepository.findFailedRetryable()
        logger.info("Retrying ${failures.size} failed notifications")

        failures.forEach { notification ->
            try {
                val retrying = notification.markAsRetrying()
                queueRepository.save(retrying)
                sendNotification(notification.id!!)
            } catch (e: Exception) {
                logger.error("Retry failed for notification ${notification.id}: ${e.message}", e)
            }
        }
    }

    // ========== CLEANUP OLD HISTORY (Scheduled) ==========

    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    @Transactional
    fun cleanupOldHistory() {
        val cutoffDate = LocalDateTime.now().minusDays(30)
        val deleted = historyRepository.deleteOlderThan(cutoffDate)
        logger.info("Cleaned up $deleted old notification history records")
    }

    // ========== HELPER METHODS ==========

    fun getHistory(page: Int = 0, size: Int = 20): Page<NotificationHistoryResponse> {
        val pageable = PageRequest.of(page, size)
        return historyRepository.findSentAfter(LocalDateTime.now().minusDays(30))
            .let { Page.empty() } // TODO: Fix pagination
    }

    private fun getProviderName(channel: NotificationChannel): String {
        return when (channel) {
            NotificationChannel.EMAIL -> "Mock Email Provider"
            NotificationChannel.SMS -> "Mock SMS Provider"
            else -> "Unknown"
        }
    }

    private fun toHistoryResponse(history: NotificationHistory) = NotificationHistoryResponse(
        id = history.id.toString(),
        recipientEmail = history.recipientEmail,
        recipientPhone = history.recipientPhone,
        notificationType = history.notificationType,
        channel = history.channel,
        subject = history.subject,
        status = history.status,
        sentAt = history.sentAt,
        provider = history.provider,
        createdAt = history.createdAt!!
    )
}

// ========== HELPER SERVICES ==========

@Service
class NotificationTemplateService(
    private val emailTemplateRepository: EmailTemplateRepository,
    private val smsTemplateRepository: SmsTemplateRepository
) {

    @Transactional
    fun createEmailTemplate(request: CreateEmailTemplateRequest): EmailTemplateResponse {
        val template = EmailTemplate(
            name = request.name,
            templateType = request.templateType,
            subject = request.subject,
            htmlContent = request.htmlContent,
            textContent = request.textContent,
            variables = request.variables,
            description = request.description
        )

        val saved = emailTemplateRepository.save(template)
        return EmailTemplateResponse(
            id = saved.id.toString(),
            name = saved.name,
            templateType = saved.templateType,
            subject = saved.subject,
            variables = saved.variables,
            description = saved.description,
            isActive = saved.isActive
        )
    }

    fun getEmailTemplates(): List<EmailTemplateResponse> {
        return emailTemplateRepository.findActive().map { template ->
            EmailTemplateResponse(
                id = template.id.toString(),
                name = template.name,
                templateType = template.templateType,
                subject = template.subject,
                variables = template.variables,
                description = template.description,
                isActive = template.isActive
            )
        }
    }

    @Transactional
    fun createSmsTemplate(request: CreateSmsTemplateRequest): SmsTemplateResponse {
        val template = SmsTemplate(
            name = request.name,
            notificationType = request.notificationType,
            content = request.content,
            variables = request.variables,
            description = request.description
        )

        val saved = smsTemplateRepository.save(template)
        return SmsTemplateResponse(
            id = saved.id.toString(),
            name = saved.name,
            notificationType = saved.notificationType,
            content = saved.content,
            characterCount = saved.getCharacterCount(),
            variables = saved.variables,
            description = saved.description,
            isActive = saved.isActive
        )
    }
}

@Service
class NotificationPreferencesService(
    private val preferencesRepository: NotificationPreferencesRepository
) {

    fun getPreferences(customerId: UUID): NotificationPreferencesResponse {
        val prefs = preferencesRepository.findByCustomerId(customerId)
            ?: createDefaultPreferences(customerId)

        return NotificationPreferencesResponse(
            customerId = prefs.customerId.toString(),
            emailOrderUpdates = prefs.emailOrderUpdates,
            emailBookingReminders = prefs.emailBookingReminders,
            emailPromotions = prefs.emailPromotions,
            emailNewsletter = prefs.emailNewsletter,
            smsOrderUpdates = prefs.smsOrderUpdates,
            smsBookingReminders = prefs.smsBookingReminders,
            smsPromotions = prefs.smsPromotions,
            pushOrderUpdates = prefs.pushOrderUpdates,
            pushBookingReminders = prefs.pushBookingReminders,
            pushPromotions = prefs.pushPromotions,
            optOutAll = prefs.optOutAll
        )
    }

    @Transactional
    fun updatePreferences(customerId: UUID, request: UpdateNotificationPreferencesRequest): NotificationPreferencesResponse {
        val existing = preferencesRepository.findByCustomerId(customerId)
            ?: createDefaultPreferences(customerId)

        val updated = existing.copy(
            emailOrderUpdates = request.emailOrderUpdates ?: existing.emailOrderUpdates,
            emailBookingReminders = request.emailBookingReminders ?: existing.emailBookingReminders,
            emailPromotions = request.emailPromotions ?: existing.emailPromotions,
            emailNewsletter = request.emailNewsletter ?: existing.emailNewsletter,
            smsOrderUpdates = request.smsOrderUpdates ?: existing.smsOrderUpdates,
            smsBookingReminders = request.smsBookingReminders ?: existing.smsBookingReminders,
            smsPromotions = request.smsPromotions ?: existing.smsPromotions,
            pushOrderUpdates = request.pushOrderUpdates ?: existing.pushOrderUpdates,
            pushBookingReminders = request.pushBookingReminders ?: existing.pushBookingReminders,
            pushPromotions = request.pushPromotions ?: existing.pushPromotions,
            optOutAll = request.optOutAll ?: existing.optOutAll
        )

        val saved = preferencesRepository.save(updated)
        return getPreferences(customerId)
    }

    private fun createDefaultPreferences(customerId: UUID): NotificationPreferences {
        val prefs = NotificationPreferences(customerId = customerId)
        return preferencesRepository.save(prefs)
    }
}