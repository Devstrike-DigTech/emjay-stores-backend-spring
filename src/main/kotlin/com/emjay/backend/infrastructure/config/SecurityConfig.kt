package com.emjay.backend.infrastructure.config

import com.emjay.backend.common.infrastructure.security.filter.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {
    
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Public endpoints
                    .requestMatchers(
                        "/api/v1/auth/**",
                        "/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/actuator/health",
                        "/error"
                    ).permitAll()

                    // Public endpoints - No authentication required
                    .requestMatchers(
                        "/api/v1/customers/register",
                        "/api/v1/customers/login",
                        "/api/v1/customers/auth/google",
                        "/api/v1/customers/guest/session",
                        "/api/v1/customers/guest/session/**",
                        "/api/v1/payments/initiate",

                        "/api/v1/payments/webhook/**",
                        "/api/v1/payments/paystack/callback",      // ← Add explicit
                        "/api/v1/payments/flutterwave/callback",   // ← Add explicit
                        "/api/v1/payments/stripe/callback"         // ← Add explicit
                    ).permitAll()

                    // Swagger/OpenAPI endpoints
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                    ).permitAll()

                    // Health check endpoints
                    .requestMatchers("/actuator/**").permitAll()

                    // Public cart endpoints (for guest)
                    .requestMatchers(HttpMethod.GET, "/api/v1/cart").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/cart/guest").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/cart/items").permitAll()

                    // Guest checkout
                    .requestMatchers(HttpMethod.POST, "/api/v1/orders/checkout").permitAll()

                    // Public product browsing (from Phase 1)
                    .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()


                    // Admin only endpoints
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                    
                    // Manager and Admin endpoints
                    .requestMatchers(
                        HttpMethod.POST, "/api/v1/products/**"
                    ).hasAnyRole("ADMIN", "MANAGER")
                    
                    .requestMatchers(
                        HttpMethod.PUT, "/api/v1/products/**"
                    ).hasAnyRole("ADMIN", "MANAGER")
                    
                    .requestMatchers(
                        HttpMethod.DELETE, "/api/v1/products/**"
                    ).hasAnyRole("ADMIN", "MANAGER")

                    // All other endpoints require authentication
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        
        return http.build()
    }
    
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf(
            "http://localhost:3000",
            "http://localhost:4200",
            "http://localhost:8080"
        )
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
    
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
