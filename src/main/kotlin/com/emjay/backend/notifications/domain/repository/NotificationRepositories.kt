package com.emjay.backend.notifications.domain.repository

import com.emjay.backend.notifications.domain.entity.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime
import java.util.*

// ========== EMAIL TEMPLATE REPOSITORY ==========

interface EmailTemplateRepository {
    fun save(template: EmailTemplate): EmailTemplate
    fun findById(id: UUID): EmailTemplate?
    fun findByType(type: EmailTemplateType): EmailTemplate?
    fun findAll(): List<EmailTemplate>
    fun findActive(): List<EmailTemplate>
    fun delete(template: EmailTemplate)
}

// ========== SMS TEMPLATE REPOSITORY ==========

interface SmsTemplateRepository {
    fun save(template: SmsTemplate): SmsTemplate
    fun findById(id: UUID): SmsTemplate?
    fun findByType(type: NotificationType): SmsTemplate?
    fun findAll(): List<SmsTemplate>
    fun findActive(): List<SmsTemplate>
    fun delete(template: SmsTemplate)
}

// ========== NOTIFICATION QUEUE REPOSITORY ==========

interface NotificationQueueRepository {
    fun save(notification: NotificationQueue): NotificationQueue
    fun findById(id: UUID): NotificationQueue?
    fun findPending(): List<NotificationQueue>
    fun findDueForSending(): List<NotificationQueue>
    fun findFailedRetryable(): List<NotificationQueue>
    fun findByRecipientId(recipientId: UUID, pageable: Pageable): Page<NotificationQueue>
    fun delete(notification: NotificationQueue)
}

// ========== NOTIFICATION HISTORY REPOSITORY ==========

interface NotificationHistoryRepository {
    fun save(history: NotificationHistory): NotificationHistory
    fun findById(id: UUID): NotificationHistory?
    fun findByRecipientId(recipientId: UUID, pageable: Pageable): Page<NotificationHistory>
    fun findByType(type: NotificationType, pageable: Pageable): Page<NotificationHistory>
    fun findSentAfter(sentAt: LocalDateTime): List<NotificationHistory>
    fun deleteOlderThan(date: LocalDateTime): Int
}

// ========== NOTIFICATION PREFERENCES REPOSITORY ==========

interface NotificationPreferencesRepository {
    fun save(preferences: NotificationPreferences): NotificationPreferences
    fun findById(id: UUID): NotificationPreferences?
    fun findByCustomerId(customerId: UUID): NotificationPreferences?
    fun delete(preferences: NotificationPreferences)
}

// ========== SCHEDULED NOTIFICATION REPOSITORY ==========

interface ScheduledNotificationRepository {
    fun save(scheduled: ScheduledNotification): ScheduledNotification
    fun findById(id: UUID): ScheduledNotification?
    fun findActive(): List<ScheduledNotification>
    fun delete(scheduled: ScheduledNotification)
}