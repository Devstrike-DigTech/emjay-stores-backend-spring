package com.emjay.backend.infrastructure.security.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "application.jwt")
data class JwtProperties(
    var secret: String = "",
    var accessTokenExpiration: Long = 900000, // 15 minutes
    var refreshTokenExpiration: Long = 604800000 // 7 days
)
