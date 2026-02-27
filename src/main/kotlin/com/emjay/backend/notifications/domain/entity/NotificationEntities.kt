package com.emjay.backend.notifications.domain.entity

import java.time.LocalDateTime
import java.util.*

// ========== ENUMS ==========

enum class NotificationType {
    ORDER_CONFIRMATION,
    ORDER_SHIPPED,
    ORDER_DELIVERED,
    PAYMENT_RECEIVED,
    PAYMENT_FAILED,
    BOOKING_CONFIRMATION,
    BOOKING_REMINDER,
    BOOKING_CANCELLED,
    BOOKING_RESCHEDULED,
    BOOKING_COMPLETED,
    PROMO_CODE,
    ACCOUNT_CREATED,
    PASSWORD_RESET,
    CUSTOM
}

enum class NotificationChannel {
    EMAIL,
    SMS,
    PUSH,
    IN_APP
}

enum class NotificationStatus {
    PENDING,
    SENT,
    FAILED,
    RETRYING
}

enum class EmailTemplateType {
    ORDER_CONFIRMATION,
    BOOKING_CONFIRMATION,
    BOOKING_REMINDER,
    PAYMENT_RECEIPT,
    WELCOME_EMAIL,
    PASSWORD_RESET,
    PROMO_NOTIFICATION
}

// ========== EMAIL TEMPLATE ==========

data class EmailTemplate(
    val id: UUID? = null,
    val name: String,
    val templateType: EmailTemplateType,
    val subject: String,
    val htmlContent: String,
    val textContent: String? = null,
    val variables: Map<String, String>? = null,
    val description: String? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun renderHtml(data: Map<String, Any>): String {
        var rendered = htmlContent
        data.forEach { (key, value) ->
            rendered = rendered.replace("{{$key}}", value.toString())
        }
        return rendered
    }

    fun renderSubject(data: Map<String, Any>): String {
        var rendered = subject
        data.forEach { (key, value) ->
            rendered = rendered.replace("{{$key}}", value.toString())
        }
        return rendered
    }
}

// ========== SMS TEMPLATE ==========

data class SmsTemplate(
    val id: UUID? = null,
    val name: String,
    val notificationType: NotificationType,
    val content: String,
    val variables: Map<String, String>? = null,
    val description: String? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun render(data: Map<String, Any>): String {
        var rendered = content
        data.forEach { (key, value) ->
            rendered = rendered.replace("{{$key}}", value.toString())
        }
        return rendered
    }

    fun getCharacterCount(): Int = content.length

    fun isWithinSmsLimit(): Boolean = getCharacterCount() <= 160
}

// ========== NOTIFICATION QUEUE ==========

data class NotificationQueue(
    val id: UUID? = null,
    val recipientId: UUID? = null,
    val recipientEmail: String? = null,
    val recipientPhone: String? = null,
    val recipientName: String? = null,
    val notificationType: NotificationType,
    val channel: NotificationChannel,
    val subject: String? = null,
    val message: String,
    val htmlContent: String? = null,
    val templateId: UUID? = null,
    val templateData: Map<String, Any>? = null,
    val status: NotificationStatus = NotificationStatus.PENDING,
    val scheduledFor: LocalDateTime? = null,
    val sentAt: LocalDateTime? = null,
    val failedAt: LocalDateTime? = null,
    val errorMessage: String? = null,
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val relatedEntityType: String? = null,
    val relatedEntityId: UUID? = null,
    val providerMessageId: String? = null,
    val createdAt: LocalDateTime? = null
) {
    fun isPending(): Boolean = status == NotificationStatus.PENDING

    fun isSent(): Boolean = status == NotificationStatus.SENT

    fun hasFailed(): Boolean = status == NotificationStatus.FAILED

    fun canRetry(): Boolean = status == NotificationStatus.FAILED && retryCount < maxRetries

    fun shouldSendNow(): Boolean {
        return isPending() && (scheduledFor == null || LocalDateTime.now().isAfter(scheduledFor))
    }

    fun markAsSent(messageId: String): NotificationQueue {
        return copy(
            status = NotificationStatus.SENT,
            sentAt = LocalDateTime.now(),
            providerMessageId = messageId
        )
    }

    fun markAsFailed(error: String): NotificationQueue {
        return copy(
            status = NotificationStatus.FAILED,
            failedAt = LocalDateTime.now(),
            errorMessage = error,
            retryCount = retryCount + 1
        )
    }

    fun markAsRetrying(): NotificationQueue {
        return copy(status = NotificationStatus.RETRYING)
    }
}

// ========== NOTIFICATION HISTORY ==========

data class NotificationHistory(
    val id: UUID? = null,
    val queueId: UUID? = null,
    val recipientId: UUID? = null,
    val recipientEmail: String? = null,
    val recipientPhone: String? = null,
    val notificationType: NotificationType,
    val channel: NotificationChannel,
    val subject: String? = null,
    val message: String? = null,
    val status: NotificationStatus,
    val sentAt: LocalDateTime? = null,
    val provider: String? = null,
    val providerMessageId: String? = null,
    val providerResponse: Map<String, Any>? = null,
    val relatedEntityType: String? = null,
    val relatedEntityId: UUID? = null,
    val createdAt: LocalDateTime? = null
)

// ========== NOTIFICATION PREFERENCES ==========

data class NotificationPreferences(
    val id: UUID? = null,
    val customerId: UUID,

    // Email preferences
    val emailOrderUpdates: Boolean = true,
    val emailBookingReminders: Boolean = true,
    val emailPromotions: Boolean = true,
    val emailNewsletter: Boolean = true,

    // SMS preferences
    val smsOrderUpdates: Boolean = true,
    val smsBookingReminders: Boolean = true,
    val smsPromotions: Boolean = false,

    // Push preferences
    val pushOrderUpdates: Boolean = true,
    val pushBookingReminders: Boolean = true,
    val pushPromotions: Boolean = true,

    // Global opt-out
    val optOutAll: Boolean = false,

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun canSendEmail(type: NotificationType): Boolean {
        if (optOutAll) return false

        return when (type) {
            NotificationType.ORDER_CONFIRMATION,
            NotificationType.ORDER_SHIPPED,
            NotificationType.ORDER_DELIVERED,
            NotificationType.PAYMENT_RECEIVED -> emailOrderUpdates

            NotificationType.BOOKING_CONFIRMATION,
            NotificationType.BOOKING_REMINDER,
            NotificationType.BOOKING_CANCELLED,
            NotificationType.BOOKING_RESCHEDULED,
            NotificationType.BOOKING_COMPLETED -> emailBookingReminders

            NotificationType.PROMO_CODE -> emailPromotions

            else -> true
        }
    }

    fun canSendSms(type: NotificationType): Boolean {
        if (optOutAll) return false

        return when (type) {
            NotificationType.ORDER_CONFIRMATION,
            NotificationType.ORDER_SHIPPED,
            NotificationType.ORDER_DELIVERED -> smsOrderUpdates

            NotificationType.BOOKING_REMINDER,
            NotificationType.BOOKING_CONFIRMATION -> smsBookingReminders

            NotificationType.PROMO_CODE -> smsPromotions

            else -> false
        }
    }
}

// ========== SCHEDULED NOTIFICATION ==========

data class ScheduledNotification(
    val id: UUID? = null,
    val notificationType: NotificationType,
    val channel: NotificationChannel,
    val triggerType: String,
    val triggerOffsetHours: Int? = null,
    val templateId: UUID? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)