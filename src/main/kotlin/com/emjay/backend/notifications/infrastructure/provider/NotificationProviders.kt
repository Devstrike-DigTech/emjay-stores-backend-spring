package com.emjay.backend.notifications.infrastructure.provider

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

// ========== PROVIDER INTERFACES ==========

interface EmailProvider {
    fun send(to: String, subject: String, htmlContent: String, textContent: String? = null): SendResult
}

interface SmsProvider {
    fun send(to: String, message: String): SendResult
}

data class SendResult(
    val success: Boolean,
    val messageId: String?,
    val error: String? = null
)

// ========== MOCK EMAIL PROVIDER (for testing) ==========

@Service
@ConditionalOnProperty(
    prefix = "notification.email",
    name = ["provider"],
    havingValue = "mock",
    matchIfMissing = true
)
class MockEmailProvider : EmailProvider {
    private val logger = LoggerFactory.getLogger(MockEmailProvider::class.java)

    override fun send(to: String, subject: String, htmlContent: String, textContent: String?): SendResult {
        logger.info("📧 [MOCK EMAIL] Sending email to: $to")
        logger.info("📧 [MOCK EMAIL] Subject: $subject")
        logger.info("📧 [MOCK EMAIL] Content: ${htmlContent.take(100)}...")

        // Simulate successful send
        val messageId = "mock-email-${System.currentTimeMillis()}"
        return SendResult(
            success = true,
            messageId = messageId
        )
    }
}

// ========== MOCK SMS PROVIDER (for testing) ==========

@Service
@ConditionalOnProperty(
    prefix = "notification.sms",
    name = ["provider"],
    havingValue = "mock",
    matchIfMissing = true
)
class MockSmsProvider : SmsProvider {
    private val logger = LoggerFactory.getLogger(MockSmsProvider::class.java)

    override fun send(to: String, message: String): SendResult {
        logger.info("📱 [MOCK SMS] Sending SMS to: $to")
        logger.info("📱 [MOCK SMS] Message: $message")

        // Simulate successful send
        val messageId = "mock-sms-${System.currentTimeMillis()}"
        return SendResult(
            success = true,
            messageId = messageId
        )
    }
}

// ========== SENDGRID EMAIL PROVIDER (production-ready) ==========

/*
@Service
@ConditionalOnProperty("notification.email.provider", havingValue = "sendgrid")
class SendGridEmailProvider(
    @Value("\${notification.email.sendgrid.api-key}") private val apiKey: String,
    @Value("\${notification.email.from}") private val fromEmail: String,
    @Value("\${notification.email.from-name}") private val fromName: String
) : EmailProvider {

    override fun send(to: String, subject: String, htmlContent: String, textContent: String?): SendResult {
        try {
            val from = Email(fromEmail, fromName)
            val toEmail = Email(to)
            val content = Content("text/html", htmlContent)
            val mail = Mail(from, subject, toEmail, content)

            if (textContent != null) {
                mail.addContent(Content("text/plain", textContent))
            }

            val sg = SendGrid(apiKey)
            val request = Request()
            request.method = Method.POST
            request.endpoint = "mail/send"
            request.body = mail.build()

            val response = sg.api(request)

            return if (response.statusCode in 200..299) {
                SendResult(true, response.headers["X-Message-Id"]?.firstOrNull())
            } else {
                SendResult(false, null, "SendGrid error: ${response.statusCode}")
            }
        } catch (e: Exception) {
            return SendResult(false, null, "SendGrid exception: ${e.message}")
        }
    }
}
*/

// ========== TWILIO SMS PROVIDER (production-ready) ==========

/*
@Service
@ConditionalOnProperty("notification.sms.provider", havingValue = "twilio")
class TwilioSmsProvider(
    @Value("\${notification.sms.twilio.account-sid}") private val accountSid: String,
    @Value("\${notification.sms.twilio.auth-token}") private val authToken: String,
    @Value("\${notification.sms.from}") private val fromNumber: String
) : SmsProvider {

    override fun send(to: String, message: String): SendResult {
        try {
            Twilio.init(accountSid, authToken)

            val smsMessage = Message.creator(
                PhoneNumber(to),
                PhoneNumber(fromNumber),
                message
            ).create()

            return SendResult(true, smsMessage.sid)
        } catch (e: Exception) {
            return SendResult(false, null, "Twilio exception: ${e.message}")
        }
    }
}
*/

// ========== TERMII SMS PROVIDER (Nigeria - production-ready) ==========

/*
@Service
@ConditionalOnProperty("notification.sms.provider", havingValue = "termii")
class TermiiSmsProvider(
    @Value("\${notification.sms.termii.api-key}") private val apiKey: String,
    @Value("\${notification.sms.termii.sender-id}") private val senderId: String,
    private val restTemplate: RestTemplate
) : SmsProvider {

    private val termiiUrl = "https://api.ng.termii.com/api/sms/send"

    override fun send(to: String, message: String): SendResult {
        try {
            val request = mapOf(
                "api_key" to apiKey,
                "to" to to,
                "from" to senderId,
                "sms" to message,
                "type" to "plain",
                "channel" to "generic"
            )

            val response = restTemplate.postForEntity(
                termiiUrl,
                request,
                Map::class.java
            )

            val body = response.body as? Map<*, *>
            val messageId = body?.get("message_id")?.toString()

            return if (response.statusCode.is2xxSuccessful && messageId != null) {
                SendResult(true, messageId)
            } else {
                SendResult(false, null, "Termii error: ${body?.get("message")}")
            }
        } catch (e: Exception) {
            return SendResult(false, null, "Termii exception: ${e.message}")
        }
    }
}
*/