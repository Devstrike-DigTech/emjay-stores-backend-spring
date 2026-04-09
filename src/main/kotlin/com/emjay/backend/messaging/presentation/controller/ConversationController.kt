package com.emjay.backend.messaging.presentation.controller

import com.emjay.backend.common.infrastructure.security.jwt.JwtTokenProvider
import com.emjay.backend.messaging.application.dto.*
import com.emjay.backend.messaging.application.service.ConversationService
import com.emjay.backend.messaging.domain.entity.ConversationCategory
import com.emjay.backend.messaging.domain.entity.ParticipantType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/conversations")
@Tag(name = "Conversations", description = "Manage real-time conversations and messages")
@SecurityRequirement(name = "bearerAuth")
class ConversationController(
    private val conversationService: ConversationService,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get all conversations")
    fun getAllConversations(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) category: ConversationCategory?
    ): ResponseEntity<List<ConversationSummaryResponse>> {
        val result = if (category != null)
            conversationService.getConversationsByCategory(category)
        else
            conversationService.getAllConversations(page, size)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get all unread conversations")
    fun getUnreadConversations(): ResponseEntity<List<ConversationSummaryResponse>> =
        ResponseEntity.ok(conversationService.getUnreadConversations())

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get conversation with messages")
    fun getConversation(@PathVariable id: UUID): ResponseEntity<ConversationResponse> =
        ResponseEntity.ok(conversationService.getConversationById(id))

    @GetMapping("/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get paginated message history")
    fun getMessages(
        @PathVariable id: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<List<MessageResponse>> =
        ResponseEntity.ok(conversationService.getMessages(id, page, size))

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Start a new conversation")
    fun createConversation(
        @Valid @RequestBody request: CreateConversationRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<ConversationResponse> {
        val userId = jwtTokenProvider.getUserIdFromToken(token.removePrefix("Bearer "))
        val response = conversationService.createConversation(request, userId, ParticipantType.ADMIN)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Send a message (REST fallback for non-WebSocket clients)")
    fun sendMessage(
        @PathVariable id: UUID,
        @Valid @RequestBody request: SendMessageRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<MessageResponse> {
        val userId = jwtTokenProvider.getUserIdFromToken(token.removePrefix("Bearer "))
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(conversationService.sendMessage(id, request, userId, ParticipantType.ADMIN))
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Mark conversation as read")
    fun markAsRead(@PathVariable id: UUID): ResponseEntity<Map<String, String>> {
        conversationService.markAsRead(id)
        return ResponseEntity.ok(mapOf("message" to "Conversation marked as read"))
    }
}
