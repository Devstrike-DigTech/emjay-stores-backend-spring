package com.emjay.backend.messaging.application.dto

import com.emjay.backend.messaging.domain.entity.ConversationCategory
import com.emjay.backend.messaging.domain.entity.MessageType
import com.emjay.backend.messaging.domain.entity.ParticipantType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.UUID

// ===== REQUESTS =====

data class CreateConversationRequest(
    val category: ConversationCategory = ConversationCategory.GENERAL,
    val subject: String? = null,
    @field:NotBlank(message = "Initial message content is required")
    val initialMessage: String
)

data class SendMessageRequest(
    @field:NotBlank(message = "Message content is required")
    val content: String,
    val messageType: MessageType = MessageType.TEXT,
    val referenceId: UUID? = null
)

// ===== RESPONSES =====

data class ConversationResponse(
    val id: String,
    val category: ConversationCategory,
    val subject: String?,
    val initiatorId: String,
    val initiatorType: ParticipantType,
    val isRead: Boolean,
    val lastMessageAt: LocalDateTime?,
    val createdAt: LocalDateTime?,
    val messages: List<MessageResponse> = emptyList()
)

data class ConversationSummaryResponse(
    val id: String,
    val category: ConversationCategory,
    val subject: String?,
    val initiatorId: String,
    val initiatorType: ParticipantType,
    val isRead: Boolean,
    val lastMessageAt: LocalDateTime?,
    val createdAt: LocalDateTime?
)

data class MessageResponse(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderType: ParticipantType,
    val content: String,
    val messageType: MessageType,
    val referenceId: String?,
    val sentAt: LocalDateTime?
)

// WebSocket payload (sent over STOMP)
data class WebSocketMessagePayload(
    val conversationId: String,
    val message: MessageResponse
)
