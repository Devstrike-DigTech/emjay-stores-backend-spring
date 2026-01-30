package com.emjay.backend.infrastructure.security.jwt

import com.emjay.backend.domain.entity.user.UserRole
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties
) {
    
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    
    fun generateAccessToken(userId: UUID, email: String, role: UserRole): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtProperties.accessTokenExpiration)
        
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role.name)
            .claim("type", "ACCESS")
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }
    
    fun generateRefreshToken(userId: UUID): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtProperties.refreshTokenExpiration)
        
        return Jwts.builder()
            .subject(userId.toString())
            .claim("type", "REFRESH")
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }
    
    fun getUserIdFromToken(token: String): UUID {
        val claims = getAllClaimsFromToken(token)
        return UUID.fromString(claims.subject)
    }
    
    fun getEmailFromToken(token: String): String {
        val claims = getAllClaimsFromToken(token)
        return claims["email"] as String
    }
    
    fun getRoleFromToken(token: String): UserRole {
        val claims = getAllClaimsFromToken(token)
        return UserRole.valueOf(claims["role"] as String)
    }
    
    fun validateToken(token: String): Boolean {
        return try {
            val claims = getAllClaimsFromToken(token)
            !isTokenExpired(claims)
        } catch (e: Exception) {
            false
        }
    }
    
    fun isAccessToken(token: String): Boolean {
        return try {
            val claims = getAllClaimsFromToken(token)
            claims["type"] == "ACCESS"
        } catch (e: Exception) {
            false
        }
    }
    
    fun isRefreshToken(token: String): Boolean {
        return try {
            val claims = getAllClaimsFromToken(token)
            claims["type"] == "REFRESH"
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getAllClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
    
    private fun isTokenExpired(claims: Claims): Boolean {
        val expiration = claims.expiration
        return expiration.before(Date())
    }
}
