package com.emjay.backend.messaging.presentation.websocket

import com.emjay.backend.common.infrastructure.security.jwt.JwtTokenProvider
import com.emjay.backend.messaging.application.dto.MessageResponse
import com.emjay.backend.messaging.application.dto.SendMessageRequest
import com.emjay.backend.messaging.application.service.ConversationService
import com.emjay.backend.messaging.domain.entity.ParticipantType
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class ConversationWebSocketController(
    private val conversationService: ConversationService,
    private val jwtTokenProvider: JwtTokenProvider
) {
    /**
     * Clients send to:  /app/conversations/{conversationId}/send
     * Broadcast goes to: /topic/conversations/{conversationId}
     */
    @MessageMapping("/conversations/{conversationId}/send")
    fun sendMessage(
        @DestinationVariable conversationId: UUID,
        request: SendMessageRequest,
        @Header("Authorization", required = false) token: String?
    ): MessageResponse {
        val jwt = token?.removePrefix("Bearer ") ?: throw IllegalArgumentException("Authorization required")
        val userId = jwtTokenProvider.getUserIdFromToken(jwt)
        return conversationService.sendMessage(conversationId, request, userId, ParticipantType.ADMIN)
    }
}
