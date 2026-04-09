package com.emjay.backend.notifications.infrastructure.persistence.entity

import com.emjay.backend.notifications.domain.entity.*
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.*

// ========== EMAIL TEMPLATE ENTITY ==========

@Entity
@Table(name = "email_templates")
data class EmailTemplateEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(nullable = false, unique = true, length = 200)
    val name: String,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "template_type", nullable = false, columnDefinition = "email_template_type")
    val templateType: EmailTemplateType,

    @Column(nullable = false, length = 300)
    val subject: String,

    @Column(name = "html_content", nullable = false, columnDefinition = "TEXT")
    val htmlContent: String,

    @Column(name = "text_content", columnDefinition = "TEXT")
    val textContent: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    val variables: Map<String, String>? = null,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

// ========== SMS TEMPLATE ENTITY ==========

@Entity
@Table(name = "sms_templates")
data class SmsTemplateEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(nullable = false, unique = true, length = 200)
    val name: String,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "notification_type", nullable = false, columnDefinition = "notification_type")
    val notificationType: NotificationType,

    @Column(nullable = false, length = 500)
    val content: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    val variables: Map<String, String>? = null,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

// ========== NOTIFICATION QUEUE ENTITY ==========

@Entity
@Table(name = "notification_queue")
data class NotificationQueueEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "recipient_id")
    val recipientId: UUID? = null,

    @Column(name = "recipient_email", length = 200)
    val recipientEmail: String? = null,

    @Column(name = "recipient_phone", length = 20)
    val recipientPhone: String? = null,

    @Column(name = "recipient_name", length = 200)
    val recipientName: String? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "notification_type", nullable = false, columnDefinition = "notification_type")
    val notificationType: NotificationType,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "notification_channel")
    val channel: NotificationChannel,

    @Column(length = 300)
    val subject: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    val message: String,

    @Column(name = "html_content", columnDefinition = "TEXT")
    val htmlContent: String? = null,

    @Column(name = "template_id")
    val templateId: UUID? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "template_data", columnDefinition = "jsonb")
    val templateData: Map<String, Any>? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "notification_status")
    val status: NotificationStatus = NotificationStatus.PENDING,

    @Column(name = "scheduled_for")
    val scheduledFor: LocalDateTime? = null,

    @Column(name = "sent_at")
    val sentAt: LocalDateTime? = null,

    @Column(name = "failed_at")
    val failedAt: LocalDateTime? = null,

    @Column(name = "error_message", columnDefinition = "TEXT")
    val errorMessage: String? = null,

    @Column(name = "retry_count", nullable = false)
    val retryCount: Int = 0,

    @Column(name = "max_retries", nullable = false)
    val maxRetries: Int = 3,

    @Column(name = "related_entity_type", length = 50)
    val relatedEntityType: String? = null,

    @Column(name = "related_entity_id")
    val relatedEntityId: UUID? = null,

    @Column(name = "provider_message_id", length = 200)
    val providerMessageId: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}

// ========== NOTIFICATION HISTORY ENTITY ==========

@Entity
@Table(name = "notification_history")
data class NotificationHistoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "queue_id")
    val queueId: UUID? = null,

    @Column(name = "recipient_id")
    val recipientId: UUID? = null,

    @Column(name = "recipient_email", length = 200)
    val recipientEmail: String? = null,

    @Column(name = "recipient_phone", length = 20)
    val recipientPhone: String? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "notification_type", nullable = false, columnDefinition = "notification_type")
    val notificationType: NotificationType,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "notification_channel")
    val channel: NotificationChannel,

    @Column(length = 300)
    val subject: String? = null,

    @Column(columnDefinition = "TEXT")
    val message: String? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "notification_status")
    val status: NotificationStatus,

    @Column(name = "sent_at")
    val sentAt: LocalDateTime? = null,

    @Column(length = 50)
    val provider: String? = null,

    @Column(name = "provider_message_id", length = 200)
    val providerMessageId: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "provider_response", columnDefinition = "jsonb")
    val providerResponse: Map<String, Any>? = null,

    @Column(name = "related_entity_type", length = 50)
    val relatedEntityType: String? = null,

    @Column(name = "related_entity_id")
    val relatedEntityId: UUID? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}

// ========== NOTIFICATION PREFERENCES ENTITY ==========

@Entity
@Table(name = "notification_preferences")
data class NotificationPreferencesEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "customer_id", nullable = false, unique = true)
    val customerId: UUID,

    @Column(name = "email_order_updates", nullable = false)
    val emailOrderUpdates: Boolean = true,

    @Column(name = "email_booking_reminders", nullable = false)
    val emailBookingReminders: Boolean = true,

    @Column(name = "email_promotions", nullable = false)
    val emailPromotions: Boolean = true,

    @Column(name = "email_newsletter", nullable = false)
    val emailNewsletter: Boolean = true,

    @Column(name = "sms_order_updates", nullable = false)
    val smsOrderUpdates: Boolean = true,

    @Column(name = "sms_booking_reminders", nullable = false)
    val smsBookingReminders: Boolean = true,

    @Column(name = "sms_promotions", nullable = false)
    val smsPromotions: Boolean = false,

    @Column(name = "push_order_updates", nullable = false)
    val pushOrderUpdates: Boolean = true,

    @Column(name = "push_booking_reminders", nullable = false)
    val pushBookingReminders: Boolean = true,

    @Column(name = "push_promotions", nullable = false)
    val pushPromotions: Boolean = true,

    @Column(name = "opt_out_all", nullable = false)
    val optOutAll: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

// ========== SCHEDULED NOTIFICATION ENTITY ==========

@Entity
@Table(name = "scheduled_notifications")
data class ScheduledNotificationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "notification_type", nullable = false, columnDefinition = "notification_type")
    val notificationType: NotificationType,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "notification_channel")
    val channel: NotificationChannel,

    @Column(name = "trigger_type", nullable = false, length = 50)
    val triggerType: String,

    @Column(name = "trigger_offset_hours")
    val triggerOffsetHours: Int? = null,

    @Column(name = "template_id")
    val templateId: UUID? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}