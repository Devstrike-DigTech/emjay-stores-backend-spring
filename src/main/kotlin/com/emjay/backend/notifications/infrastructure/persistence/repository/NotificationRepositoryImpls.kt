package com.emjay.backend.notifications.infrastructure.persistence.repository

import com.emjay.backend.notifications.domain.entity.*
import com.emjay.backend.notifications.domain.repository.*
import com.emjay.backend.notifications.infrastructure.persistence.entity.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

// ========== JPA REPOSITORIES ==========

@Repository
interface JpaEmailTemplateRepository : JpaRepository<EmailTemplateEntity, UUID> {
    fun findByTemplateType(type: EmailTemplateType): EmailTemplateEntity?
    fun findByIsActiveTrue(): List<EmailTemplateEntity>
}

@Repository
interface JpaSmsTemplateRepository : JpaRepository<SmsTemplateEntity, UUID> {
    fun findByNotificationType(type: NotificationType): SmsTemplateEntity?
    fun findByIsActiveTrue(): List<SmsTemplateEntity>
}

@Repository
interface JpaNotificationQueueRepository : JpaRepository<NotificationQueueEntity, UUID> {
    fun findByStatus(status: NotificationStatus): List<NotificationQueueEntity>

    @Query("""
        SELECT n FROM NotificationQueueEntity n 
        WHERE n.status = 'PENDING' 
        AND (n.scheduledFor IS NULL OR n.scheduledFor <= :now)
    """)
    fun findDueForSending(@Param("now") now: LocalDateTime): List<NotificationQueueEntity>

    @Query("""
        SELECT n FROM NotificationQueueEntity n 
        WHERE n.status = 'FAILED' 
        AND n.retryCount < n.maxRetries
    """)
    fun findFailedRetryable(): List<NotificationQueueEntity>

    fun findByRecipientId(recipientId: UUID, pageable: Pageable): Page<NotificationQueueEntity>
}

@Repository
interface JpaNotificationHistoryRepository : JpaRepository<NotificationHistoryEntity, UUID> {
    fun findByRecipientId(recipientId: UUID, pageable: Pageable): Page<NotificationHistoryEntity>
    fun findByNotificationType(type: NotificationType, pageable: Pageable): Page<NotificationHistoryEntity>
    fun findBySentAtAfter(sentAt: LocalDateTime): List<NotificationHistoryEntity>

    @Modifying
    @Query("DELETE FROM NotificationHistoryEntity n WHERE n.createdAt < :date")
    fun deleteOlderThan(@Param("date") date: LocalDateTime): Int
}

@Repository
interface JpaNotificationPreferencesRepository : JpaRepository<NotificationPreferencesEntity, UUID> {
    fun findByCustomerId(customerId: UUID): NotificationPreferencesEntity?
}

@Repository
interface JpaScheduledNotificationRepository : JpaRepository<ScheduledNotificationEntity, UUID> {
    fun findByIsActiveTrue(): List<ScheduledNotificationEntity>
}

// ========== REPOSITORY IMPLEMENTATIONS ==========

@Repository
class EmailTemplateRepositoryImpl(
    private val jpaRepository: JpaEmailTemplateRepository
) : EmailTemplateRepository {
    override fun save(template: EmailTemplate) = toDomain(jpaRepository.save(toEntity(template)))
    override fun findById(id: UUID) = jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    override fun findByType(type: EmailTemplateType) = jpaRepository.findByTemplateType(type)?.let { toDomain(it) }
    override fun findAll() = jpaRepository.findAll().map { toDomain(it) }
    override fun findActive() = jpaRepository.findByIsActiveTrue().map { toDomain(it) }
    override fun delete(template: EmailTemplate) { template.id?.let { jpaRepository.deleteById(it) } }

    private fun toDomain(entity: EmailTemplateEntity) = EmailTemplate(
        id = entity.id, name = entity.name, templateType = entity.templateType,
        subject = entity.subject, htmlContent = entity.htmlContent, textContent = entity.textContent,
        variables = entity.variables, description = entity.description, isActive = entity.isActive,
        createdAt = entity.createdAt, updatedAt = entity.updatedAt
    )

    private fun toEntity(domain: EmailTemplate) = EmailTemplateEntity(
        id = domain.id, name = domain.name, templateType = domain.templateType,
        subject = domain.subject, htmlContent = domain.htmlContent, textContent = domain.textContent,
        variables = domain.variables, description = domain.description, isActive = domain.isActive,
        createdAt = domain.createdAt ?: LocalDateTime.now(), updatedAt = domain.updatedAt ?: LocalDateTime.now()
    )
}

@Repository
class SmsTemplateRepositoryImpl(
    private val jpaRepository: JpaSmsTemplateRepository
) : SmsTemplateRepository {
    override fun save(template: SmsTemplate) = toDomain(jpaRepository.save(toEntity(template)))
    override fun findById(id: UUID) = jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    override fun findByType(type: NotificationType) = jpaRepository.findByNotificationType(type)?.let { toDomain(it) }
    override fun findAll() = jpaRepository.findAll().map { toDomain(it) }
    override fun findActive() = jpaRepository.findByIsActiveTrue().map { toDomain(it) }
    override fun delete(template: SmsTemplate) { template.id?.let { jpaRepository.deleteById(it) } }

    private fun toDomain(entity: SmsTemplateEntity) = SmsTemplate(
        id = entity.id, name = entity.name, notificationType = entity.notificationType,
        content = entity.content, variables = entity.variables, description = entity.description,
        isActive = entity.isActive, createdAt = entity.createdAt, updatedAt = entity.updatedAt
    )

    private fun toEntity(domain: SmsTemplate) = SmsTemplateEntity(
        id = domain.id, name = domain.name, notificationType = domain.notificationType,
        content = domain.content, variables = domain.variables, description = domain.description,
        isActive = domain.isActive, createdAt = domain.createdAt ?: LocalDateTime.now(),
        updatedAt = domain.updatedAt ?: LocalDateTime.now()
    )
}

@Repository
class NotificationQueueRepositoryImpl(
    private val jpaRepository: JpaNotificationQueueRepository
) : NotificationQueueRepository {
    override fun save(notification: NotificationQueue) = toDomain(jpaRepository.save(toEntity(notification)))
    override fun findById(id: UUID) = jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    override fun findPending() = jpaRepository.findByStatus(NotificationStatus.PENDING).map { toDomain(it) }
    override fun findDueForSending() = jpaRepository.findDueForSending(LocalDateTime.now()).map { toDomain(it) }
    override fun findFailedRetryable() = jpaRepository.findFailedRetryable().map { toDomain(it) }
    override fun findByRecipientId(recipientId: UUID, pageable: Pageable) =
        jpaRepository.findByRecipientId(recipientId, pageable).map { toDomain(it) }
    override fun delete(notification: NotificationQueue) { notification.id?.let { jpaRepository.deleteById(it) } }

    private fun toDomain(entity: NotificationQueueEntity) = NotificationQueue(
        id = entity.id, recipientId = entity.recipientId, recipientEmail = entity.recipientEmail,
        recipientPhone = entity.recipientPhone, recipientName = entity.recipientName,
        notificationType = entity.notificationType, channel = entity.channel, subject = entity.subject,
        message = entity.message, htmlContent = entity.htmlContent, templateId = entity.templateId,
        templateData = entity.templateData, status = entity.status, scheduledFor = entity.scheduledFor,
        sentAt = entity.sentAt, failedAt = entity.failedAt, errorMessage = entity.errorMessage,
        retryCount = entity.retryCount, maxRetries = entity.maxRetries,
        relatedEntityType = entity.relatedEntityType, relatedEntityId = entity.relatedEntityId,
        providerMessageId = entity.providerMessageId, createdAt = entity.createdAt
    )

    private fun toEntity(domain: NotificationQueue) = NotificationQueueEntity(
        id = domain.id, recipientId = domain.recipientId, recipientEmail = domain.recipientEmail,
        recipientPhone = domain.recipientPhone, recipientName = domain.recipientName,
        notificationType = domain.notificationType, channel = domain.channel, subject = domain.subject,
        message = domain.message, htmlContent = domain.htmlContent, templateId = domain.templateId,
        templateData = domain.templateData, status = domain.status, scheduledFor = domain.scheduledFor,
        sentAt = domain.sentAt, failedAt = domain.failedAt, errorMessage = domain.errorMessage,
        retryCount = domain.retryCount, maxRetries = domain.maxRetries,
        relatedEntityType = domain.relatedEntityType, relatedEntityId = domain.relatedEntityId,
        providerMessageId = domain.providerMessageId, createdAt = domain.createdAt ?: LocalDateTime.now()
    )
}

@Repository
class NotificationHistoryRepositoryImpl(
    private val jpaRepository: JpaNotificationHistoryRepository
) : NotificationHistoryRepository {
    override fun save(history: NotificationHistory) = toDomain(jpaRepository.save(toEntity(history)))
    override fun findById(id: UUID) = jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    override fun findByRecipientId(recipientId: UUID, pageable: Pageable) =
        jpaRepository.findByRecipientId(recipientId, pageable).map { toDomain(it) }
    override fun findByType(type: NotificationType, pageable: Pageable) =
        jpaRepository.findByNotificationType(type, pageable).map { toDomain(it) }
    override fun findSentAfter(sentAt: LocalDateTime) =
        jpaRepository.findBySentAtAfter(sentAt).map { toDomain(it) }

    @Transactional
    override fun deleteOlderThan(date: LocalDateTime) = jpaRepository.deleteOlderThan(date)

    private fun toDomain(entity: NotificationHistoryEntity) = NotificationHistory(
        id = entity.id, queueId = entity.queueId, recipientId = entity.recipientId,
        recipientEmail = entity.recipientEmail, recipientPhone = entity.recipientPhone,
        notificationType = entity.notificationType, channel = entity.channel, subject = entity.subject,
        message = entity.message, status = entity.status, sentAt = entity.sentAt, provider = entity.provider,
        providerMessageId = entity.providerMessageId, providerResponse = entity.providerResponse,
        relatedEntityType = entity.relatedEntityType, relatedEntityId = entity.relatedEntityId,
        createdAt = entity.createdAt
    )

    private fun toEntity(domain: NotificationHistory) = NotificationHistoryEntity(
        id = domain.id, queueId = domain.queueId, recipientId = domain.recipientId,
        recipientEmail = domain.recipientEmail, recipientPhone = domain.recipientPhone,
        notificationType = domain.notificationType, channel = domain.channel, subject = domain.subject,
        message = domain.message, status = domain.status, sentAt = domain.sentAt, provider = domain.provider,
        providerMessageId = domain.providerMessageId, providerResponse = domain.providerResponse,
        relatedEntityType = domain.relatedEntityType, relatedEntityId = domain.relatedEntityId,
        createdAt = domain.createdAt ?: LocalDateTime.now()
    )
}

@Repository
class NotificationPreferencesRepositoryImpl(
    private val jpaRepository: JpaNotificationPreferencesRepository
) : NotificationPreferencesRepository {
    override fun save(preferences: NotificationPreferences) = toDomain(jpaRepository.save(toEntity(preferences)))
    override fun findById(id: UUID) = jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    override fun findByCustomerId(customerId: UUID) =
        jpaRepository.findByCustomerId(customerId)?.let { toDomain(it) }
    override fun delete(preferences: NotificationPreferences) {
        preferences.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: NotificationPreferencesEntity) = NotificationPreferences(
        id = entity.id, customerId = entity.customerId,
        emailOrderUpdates = entity.emailOrderUpdates, emailBookingReminders = entity.emailBookingReminders,
        emailPromotions = entity.emailPromotions, emailNewsletter = entity.emailNewsletter,
        smsOrderUpdates = entity.smsOrderUpdates, smsBookingReminders = entity.smsBookingReminders,
        smsPromotions = entity.smsPromotions, pushOrderUpdates = entity.pushOrderUpdates,
        pushBookingReminders = entity.pushBookingReminders, pushPromotions = entity.pushPromotions,
        optOutAll = entity.optOutAll, createdAt = entity.createdAt, updatedAt = entity.updatedAt
    )

    private fun toEntity(domain: NotificationPreferences) = NotificationPreferencesEntity(
        id = domain.id, customerId = domain.customerId,
        emailOrderUpdates = domain.emailOrderUpdates, emailBookingReminders = domain.emailBookingReminders,
        emailPromotions = domain.emailPromotions, emailNewsletter = domain.emailNewsletter,
        smsOrderUpdates = domain.smsOrderUpdates, smsBookingReminders = domain.smsBookingReminders,
        smsPromotions = domain.smsPromotions, pushOrderUpdates = domain.pushOrderUpdates,
        pushBookingReminders = domain.pushBookingReminders, pushPromotions = domain.pushPromotions,
        optOutAll = domain.optOutAll, createdAt = domain.createdAt ?: LocalDateTime.now(),
        updatedAt = domain.updatedAt ?: LocalDateTime.now()
    )
}

@Repository
class ScheduledNotificationRepositoryImpl(
    private val jpaRepository: JpaScheduledNotificationRepository
) : ScheduledNotificationRepository {
    override fun save(scheduled: ScheduledNotification) = toDomain(jpaRepository.save(toEntity(scheduled)))
    override fun findById(id: UUID) = jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    override fun findActive() = jpaRepository.findByIsActiveTrue().map { toDomain(it) }
    override fun delete(scheduled: ScheduledNotification) { scheduled.id?.let { jpaRepository.deleteById(it) } }

    private fun toDomain(entity: ScheduledNotificationEntity) = ScheduledNotification(
        id = entity.id, notificationType = entity.notificationType, channel = entity.channel,
        triggerType = entity.triggerType, triggerOffsetHours = entity.triggerOffsetHours,
        templateId = entity.templateId, isActive = entity.isActive,
        createdAt = entity.createdAt, updatedAt = entity.updatedAt
    )

    private fun toEntity(domain: ScheduledNotification) = ScheduledNotificationEntity(
        id = domain.id, notificationType = domain.notificationType, channel = domain.channel,
        triggerType = domain.triggerType, triggerOffsetHours = domain.triggerOffsetHours,
        templateId = domain.templateId, isActive = domain.isActive,
        createdAt = domain.createdAt ?: LocalDateTime.now(), updatedAt = domain.updatedAt ?: LocalDateTime.now()
    )
}