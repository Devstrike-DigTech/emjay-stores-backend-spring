package com.emjay.backend.notifications.application.dto

import com.emjay.backend.notifications.domain.entity.*
import jakarta.validation.constraints.*
import java.time.LocalDateTime
import java.util.*

// ========== REQUEST DTOs ==========

data class QueueNotificationRequest(
    @field:NotNull val notificationType: NotificationType,
    @field:NotNull val channel: NotificationChannel,

    val recipientId: UUID? = null,
    val recipientEmail: String? = null,
    val recipientPhone: String? = null,
    val recipientName: String? = null,

    val subject: String? = null,
    val message: String? = null,
    val htmlContent: String? = null,

    val templateId: UUID? = null,
    val templateData: Map<String, Any>? = null,

    val scheduledFor: LocalDateTime? = null,
    val relatedEntityType: String? = null,
    val relatedEntityId: UUID? = null
)

data class CreateEmailTemplateRequest(
    @field:NotBlank val name: String,
    @field:NotNull val templateType: EmailTemplateType,
    @field:NotBlank val subject: String,
    @field:NotBlank val htmlContent: String,
    val textContent: String? = null,
    val variables: Map<String, String>? = null,
    val description: String? = null
)

data class CreateSmsTemplateRequest(
    @field:NotBlank val name: String,
    @field:NotNull val notificationType: NotificationType,
    @field:NotBlank @field:Size(max = 500) val content: String,
    val variables: Map<String, String>? = null,
    val description: String? = null
)

data class UpdateNotificationPreferencesRequest(
    val emailOrderUpdates: Boolean? = null,
    val emailBookingReminders: Boolean? = null,
    val emailPromotions: Boolean? = null,
    val emailNewsletter: Boolean? = null,
    val smsOrderUpdates: Boolean? = null,
    val smsBookingReminders: Boolean? = null,
    val smsPromotions: Boolean? = null,
    val pushOrderUpdates: Boolean? = null,
    val pushBookingReminders: Boolean? = null,
    val pushPromotions: Boolean? = null,
    val optOutAll: Boolean? = null
)

data class SendOrderConfirmationRequest(
    @field:NotNull val orderId: UUID,
    @field:NotNull val customerId: UUID,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String?,
    val orderNumber: String,
    val totalAmount: String,
    val shippingAddress: String
)

data class SendBookingReminderRequest(
    @field:NotNull val bookingId: UUID,
    @field:NotNull val customerId: UUID,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String?,
    val serviceName: String,
    val bookingDate: String,
    val bookingTime: String,
    val staffName: String,
    val location: String
)

// ========== RESPONSE DTOs ==========

data class NotificationQueueResponse(
    val id: String,
    val recipientEmail: String?,
    val recipientPhone: String?,
    val notificationType: NotificationType,
    val channel: NotificationChannel,
    val status: NotificationStatus,
    val scheduledFor: LocalDateTime?,
    val createdAt: LocalDateTime
)

data class NotificationHistoryResponse(
    val id: String,
    val recipientEmail: String?,
    val recipientPhone: String?,
    val notificationType: NotificationType,
    val channel: NotificationChannel,
    val subject: String?,
    val status: NotificationStatus,
    val sentAt: LocalDateTime?,
    val provider: String?,
    val createdAt: LocalDateTime
)

data class EmailTemplateResponse(
    val id: String,
    val name: String,
    val templateType: EmailTemplateType,
    val subject: String,
    val variables: Map<String, String>?,
    val description: String?,
    val isActive: Boolean
)

data class SmsTemplateResponse(
    val id: String,
    val name: String,
    val notificationType: NotificationType,
    val content: String,
    val characterCount: Int,
    val variables: Map<String, String>?,
    val description: String?,
    val isActive: Boolean
)

data class NotificationPreferencesResponse(
    val customerId: String,
    val emailOrderUpdates: Boolean,
    val emailBookingReminders: Boolean,
    val emailPromotions: Boolean,
    val emailNewsletter: Boolean,
    val smsOrderUpdates: Boolean,
    val smsBookingReminders: Boolean,
    val smsPromotions: Boolean,
    val pushOrderUpdates: Boolean,
    val pushBookingReminders: Boolean,
    val pushPromotions: Boolean,
    val optOutAll: Boolean
)

data class SendNotificationResponse(
    val success: Boolean,
    val messageId: String?,
    val message: String
)