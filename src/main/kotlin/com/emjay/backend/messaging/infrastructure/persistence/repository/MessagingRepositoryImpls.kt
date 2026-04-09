package com.emjay.backend.messaging.infrastructure.persistence.repository

import com.emjay.backend.messaging.domain.entity.Conversation
import com.emjay.backend.messaging.domain.entity.ConversationCategory
import com.emjay.backend.messaging.domain.entity.ConversationMessage
import com.emjay.backend.messaging.domain.entity.MessageType
import com.emjay.backend.messaging.domain.entity.ParticipantType
import com.emjay.backend.messaging.domain.repository.ConversationMessageRepository
import com.emjay.backend.messaging.domain.repository.ConversationRepository
import com.emjay.backend.messaging.infrastructure.persistence.entity.ConversationEntity
import com.emjay.backend.messaging.infrastructure.persistence.entity.ConversationMessageEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class ConversationRepositoryImpl(
    private val jpaRepository: JpaConversationRepository
) : ConversationRepository {

    override fun save(conversation: Conversation): Conversation =
        toDomain(jpaRepository.save(toEntity(conversation)))

    override fun findById(id: UUID): Conversation? =
        jpaRepository.findById(id).map { toDomain(it) }.orElse(null)

    override fun findAll(pageable: Pageable): Page<Conversation> =
        jpaRepository.findAll(pageable).map { toDomain(it) }

    override fun findByCategory(category: ConversationCategory): List<Conversation> =
        jpaRepository.findByCategory(category).map { toDomain(it) }

    override fun findUnread(): List<Conversation> =
        jpaRepository.findByIsReadFalse().map { toDomain(it) }

    override fun markAsRead(id: UUID) = jpaRepository.markAsRead(id)

    override fun updateLastMessageAt(id: UUID) = jpaRepository.updateLastMessageAt(id)

    private fun toDomain(e: ConversationEntity) = Conversation(
        id = e.id,
        category = e.category,
        subject = e.subject,
        initiatorId = e.initiatorId,
        initiatorType = e.initiatorType,
        isRead = e.isRead,
        lastMessageAt = e.lastMessageAt,
        createdAt = e.createdAt
    )

    private fun toEntity(d: Conversation) = ConversationEntity(
        id = d.id,
        category = d.category,
        subject = d.subject,
        initiatorId = d.initiatorId,
        initiatorType = d.initiatorType,
        isRead = d.isRead,
        lastMessageAt = d.lastMessageAt,
        createdAt = d.createdAt ?: LocalDateTime.now()
    )
}

@Repository
class ConversationMessageRepositoryImpl(
    private val jpaRepository: JpaConversationMessageRepository
) : ConversationMessageRepository {

    override fun save(message: ConversationMessage): ConversationMessage =
        toDomain(jpaRepository.save(toEntity(message)))

    override fun findByConversationId(conversationId: UUID, pageable: Pageable): Page<ConversationMessage> =
        jpaRepository.findByConversationId(conversationId, pageable).map { toDomain(it) }

    override fun findLatestByConversationId(conversationId: UUID, limit: Int): List<ConversationMessage> {
        val pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "sentAt"))
        return jpaRepository.findLatestByConversationId(conversationId, pageable).content.map { toDomain(it) }
    }

    private fun toDomain(e: ConversationMessageEntity) = ConversationMessage(
        id = e.id,
        conversationId = e.conversationId,
        senderId = e.senderId,
        senderType = e.senderType,
        content = e.content,
        messageType = e.messageType,
        referenceId = e.referenceId,
        sentAt = e.sentAt
    )

    private fun toEntity(d: ConversationMessage) = ConversationMessageEntity(
        id = d.id,
        conversationId = d.conversationId,
        senderId = d.senderId,
        senderType = d.senderType,
        content = d.content,
        messageType = d.messageType,
        referenceId = d.referenceId,
        sentAt = d.sentAt ?: LocalDateTime.now()
    )
}
