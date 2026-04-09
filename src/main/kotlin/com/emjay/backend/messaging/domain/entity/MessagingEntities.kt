package com.emjay.backend.messaging.domain.entity

import java.time.LocalDateTime
import java.util.UUID

data class Conversation(
    val id: UUID? = null,
    val category: ConversationCategory = ConversationCategory.GENERAL,
    val subject: String? = null,
    val initiatorId: UUID,
    val initiatorType: ParticipantType,
    val isRead: Boolean = false,
    val lastMessageAt: LocalDateTime? = null,
    val createdAt: LocalDateTime? = null
)

data class ConversationMessage(
    val id: UUID? = null,
    val conversationId: UUID,
    val senderId: UUID,
    val senderType: ParticipantType,
    val content: String,
    val messageType: MessageType = MessageType.TEXT,
    val referenceId: UUID? = null,
    val sentAt: LocalDateTime? = null
)

enum class ConversationCategory {
    PRODUCT_REQUEST, STAFF_MESSAGE, CONTACT_US, INVENTORY, GENERAL
}

enum class ParticipantType { CUSTOMER, STAFF, ADMIN }

enum class MessageType { TEXT, IMAGE, PRODUCT_LINK, ORDER_LINK }
