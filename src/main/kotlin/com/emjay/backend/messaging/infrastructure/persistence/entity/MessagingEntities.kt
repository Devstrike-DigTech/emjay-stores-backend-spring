package com.emjay.backend.messaging.infrastructure.persistence.entity

import com.emjay.backend.messaging.domain.entity.ConversationCategory
import com.emjay.backend.messaging.domain.entity.MessageType
import com.emjay.backend.messaging.domain.entity.ParticipantType
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "conversations")
data class ConversationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "conversation_category")
    val category: ConversationCategory = ConversationCategory.GENERAL,

    @Column(length = 300)
    val subject: String? = null,

    @Column(name = "initiator_id", nullable = false)
    val initiatorId: UUID,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "initiator_type", nullable = false, columnDefinition = "participant_type")
    val initiatorType: ParticipantType,

    @Column(name = "is_read", nullable = false)
    val isRead: Boolean = false,

    @Column(name = "last_message_at")
    val lastMessageAt: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() { createdAt = LocalDateTime.now() }
}

@Entity
@Table(name = "conversation_messages")
data class ConversationMessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "conversation_id", nullable = false)
    val conversationId: UUID,

    @Column(name = "sender_id", nullable = false)
    val senderId: UUID,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "sender_type", nullable = false, columnDefinition = "participant_type")
    val senderType: ParticipantType,

    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "message_type", nullable = false, columnDefinition = "message_type")
    val messageType: MessageType = MessageType.TEXT,

    @Column(name = "reference_id")
    val referenceId: UUID? = null,

    @Column(name = "sent_at", nullable = false, updatable = false)
    var sentAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() { sentAt = LocalDateTime.now() }
}
