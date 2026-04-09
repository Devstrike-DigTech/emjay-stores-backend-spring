package com.emjay.backend.messaging.domain.repository

import com.emjay.backend.messaging.domain.entity.Conversation
import com.emjay.backend.messaging.domain.entity.ConversationCategory
import com.emjay.backend.messaging.domain.entity.ConversationMessage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface ConversationRepository {
    fun save(conversation: Conversation): Conversation
    fun findById(id: UUID): Conversation?
    fun findAll(pageable: Pageable): Page<Conversation>
    fun findByCategory(category: ConversationCategory): List<Conversation>
    fun findUnread(): List<Conversation>
    fun markAsRead(id: UUID)
    fun updateLastMessageAt(id: UUID)
}

interface ConversationMessageRepository {
    fun save(message: ConversationMessage): ConversationMessage
    fun findByConversationId(conversationId: UUID, pageable: Pageable): Page<ConversationMessage>
    fun findLatestByConversationId(conversationId: UUID, limit: Int = 50): List<ConversationMessage>
}
