package com.emjay.backend.messaging.application.service

import com.emjay.backend.messaging.application.dto.*
import com.emjay.backend.messaging.domain.entity.Conversation
import com.emjay.backend.messaging.domain.entity.ConversationCategory
import com.emjay.backend.messaging.domain.entity.ConversationMessage
import com.emjay.backend.messaging.domain.entity.ParticipantType
import com.emjay.backend.messaging.domain.repository.ConversationMessageRepository
import com.emjay.backend.messaging.domain.repository.ConversationRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ConversationService(
    private val conversationRepository: ConversationRepository,
    private val messageRepository: ConversationMessageRepository,
    private val messagingTemplate: SimpMessagingTemplate
) {

    @Transactional
    fun createConversation(
        request: CreateConversationRequest,
        senderId: UUID,
        senderType: ParticipantType
    ): ConversationResponse {
        val conversation = Conversation(
            category = request.category,
            subject = request.subject,
            initiatorId = senderId,
            initiatorType = senderType
        )
        val saved = conversationRepository.save(conversation)

        // Send initial message
        val message = ConversationMessage(
            conversationId = saved.id!!,
            senderId = senderId,
            senderType = senderType,
            content = request.initialMessage
        )
        val savedMessage = messageRepository.save(message)
        conversationRepository.updateLastMessageAt(saved.id)

        return toConversationResponse(saved, listOf(savedMessage))
    }

    fun getAllConversations(page: Int = 0, size: Int = 20): List<ConversationSummaryResponse> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastMessageAt"))
        return conversationRepository.findAll(pageable).content.map { toSummaryResponse(it) }
    }

    fun getConversationsByCategory(category: ConversationCategory): List<ConversationSummaryResponse> =
        conversationRepository.findByCategory(category).map { toSummaryResponse(it) }

    fun getUnreadConversations(): List<ConversationSummaryResponse> =
        conversationRepository.findUnread().map { toSummaryResponse(it) }

    fun getConversationById(id: UUID): ConversationResponse {
        val conversation = conversationRepository.findById(id)
            ?: throw NoSuchElementException("Conversation not found: $id")
        val messages = messageRepository.findLatestByConversationId(id)
        return toConversationResponse(conversation, messages)
    }

    fun getMessages(conversationId: UUID, page: Int = 0, size: Int = 50): List<MessageResponse> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"))
        return messageRepository.findByConversationId(conversationId, pageable).content.map { toMessageResponse(it) }
    }

    @Transactional
    fun sendMessage(
        conversationId: UUID,
        request: SendMessageRequest,
        senderId: UUID,
        senderType: ParticipantType
    ): MessageResponse {
        conversationRepository.findById(conversationId)
            ?: throw NoSuchElementException("Conversation not found: $conversationId")

        val message = ConversationMessage(
            conversationId = conversationId,
            senderId = senderId,
            senderType = senderType,
            content = request.content,
            messageType = request.messageType,
            referenceId = request.referenceId
        )
        val saved = messageRepository.save(message)
        conversationRepository.updateLastMessageAt(conversationId)

        // Broadcast via WebSocket
        val messageResponse = toMessageResponse(saved)
        val payload = WebSocketMessagePayload(
            conversationId = conversationId.toString(),
            message = messageResponse
        )
        messagingTemplate.convertAndSend("/topic/conversations/$conversationId", payload)

        return messageResponse
    }

    @Transactional
    fun markAsRead(conversationId: UUID) {
        conversationRepository.findById(conversationId)
            ?: throw NoSuchElementException("Conversation not found: $conversationId")
        conversationRepository.markAsRead(conversationId)
    }

    private fun toConversationResponse(c: Conversation, messages: List<ConversationMessage>) =
        ConversationResponse(
            id = c.id.toString(),
            category = c.category,
            subject = c.subject,
            initiatorId = c.initiatorId.toString(),
            initiatorType = c.initiatorType,
            isRead = c.isRead,
            lastMessageAt = c.lastMessageAt,
            createdAt = c.createdAt,
            messages = messages.map { toMessageResponse(it) }
        )

    private fun toSummaryResponse(c: Conversation) = ConversationSummaryResponse(
        id = c.id.toString(),
        category = c.category,
        subject = c.subject,
        initiatorId = c.initiatorId.toString(),
        initiatorType = c.initiatorType,
        isRead = c.isRead,
        lastMessageAt = c.lastMessageAt,
        createdAt = c.createdAt
    )

    private fun toMessageResponse(m: ConversationMessage) = MessageResponse(
        id = m.id.toString(),
        conversationId = m.conversationId.toString(),
        senderId = m.senderId.toString(),
        senderType = m.senderType,
        content = m.content,
        messageType = m.messageType,
        referenceId = m.referenceId?.toString(),
        sentAt = m.sentAt
    )
}
