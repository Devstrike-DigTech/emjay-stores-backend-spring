package com.emjay.backend.messaging.infrastructure.persistence.repository

import com.emjay.backend.messaging.domain.entity.ConversationCategory
import com.emjay.backend.messaging.infrastructure.persistence.entity.ConversationEntity
import com.emjay.backend.messaging.infrastructure.persistence.entity.ConversationMessageEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface JpaConversationRepository : JpaRepository<ConversationEntity, UUID> {
    override fun findAll(pageable: Pageable): Page<ConversationEntity>
    fun findByCategory(category: ConversationCategory): List<ConversationEntity>
    fun findByIsReadFalse(): List<ConversationEntity>

    @Modifying
    @Query("UPDATE ConversationEntity c SET c.isRead = true WHERE c.id = :id")
    fun markAsRead(@Param("id") id: UUID)

    @Modifying
    @Query("UPDATE ConversationEntity c SET c.lastMessageAt = :now WHERE c.id = :id")
    fun updateLastMessageAt(@Param("id") id: UUID, @Param("now") now: LocalDateTime = LocalDateTime.now())
}

@Repository
interface JpaConversationMessageRepository : JpaRepository<ConversationMessageEntity, UUID> {
    fun findByConversationId(conversationId: UUID, pageable: Pageable): Page<ConversationMessageEntity>

    @Query("SELECT m FROM ConversationMessageEntity m WHERE m.conversationId = :id ORDER BY m.sentAt DESC")
    fun findLatestByConversationId(@Param("id") conversationId: UUID, pageable: Pageable): Page<ConversationMessageEntity>
}
