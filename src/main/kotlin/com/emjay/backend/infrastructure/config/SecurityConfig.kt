package com.emjay.backend.infrastructure.config

import com.emjay.backend.common.infrastructure.security.filter.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl
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

    // ──────────────────────────────────────────────────────────────────
    // Role Hierarchy  (method-level only — Spring Security 6.2 does NOT
    // apply RoleHierarchy to authorizeHttpRequests automatically).
    //
    // ADMIN  > MANAGER > STAFF
    //
    // Effect: an ADMIN token satisfies every @PreAuthorize role check.
    // URL-level rules use hasAnyRole("ADMIN","MANAGER") explicitly.
    // ──────────────────────────────────────────────────────────────────

    @Bean
    fun roleHierarchy(): RoleHierarchy =
        RoleHierarchyImpl().apply {
            setHierarchy("ROLE_ADMIN > ROLE_MANAGER\nROLE_MANAGER > ROLE_STAFF")
        }

    /**
     * Wire role hierarchy into @PreAuthorize / @PostAuthorize expressions.
     * Without this bean, method-level security ignores the hierarchy.
     */
    @Bean
    fun methodSecurityExpressionHandler(roleHierarchy: RoleHierarchy): MethodSecurityExpressionHandler =
        DefaultMethodSecurityExpressionHandler().apply {
            setRoleHierarchy(roleHierarchy)
        }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // ── Public: no token required ──────────────────────────────
                    .requestMatchers(
                        "/api/v1/auth/**",
                        "/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/actuator/health",
                        "/error",
                        "/ws/**"
                    ).permitAll()

                    .requestMatchers(
                        "/api/v1/customers/register",
                        "/api/v1/customers/login",
                        "/api/v1/customers/auth/google",
                        "/api/v1/customers/guest/session",
                        "/api/v1/customers/guest/session/**",
                        "/api/v1/payments/initiate",
                        "/api/v1/payments/webhook/**",
                        "/api/v1/payments/paystack/callback",
                        "/api/v1/payments/flutterwave/callback",
                        "/api/v1/payments/stripe/callback"
                    ).permitAll()

                    .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                    ).permitAll()

                    .requestMatchers("/actuator/**").permitAll()

                    // Public cart (guest)
                    .requestMatchers(HttpMethod.GET,  "/api/v1/cart").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/cart/guest").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/cart/items").permitAll()

                    // Guest checkout
                    .requestMatchers(HttpMethod.POST, "/api/v1/orders/checkout").permitAll()

                    // Public browsing — products & categories
                    .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()

                    // Public reviews
                    .requestMatchers(HttpMethod.GET,  "/api/v1/products/*/reviews").permitAll()
                    .requestMatchers(HttpMethod.GET,  "/api/v1/products/*/reviews/summary").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/products/*/reviews").permitAll()

                    // Public bundles & promotions
                    .requestMatchers(HttpMethod.GET,  "/api/v1/bundles").permitAll()
                    .requestMatchers(HttpMethod.GET,  "/api/v1/bundles/active").permitAll()
                    .requestMatchers(HttpMethod.GET,  "/api/v1/bundles/**").permitAll()
                    .requestMatchers(HttpMethod.GET,  "/api/v1/promotions/active").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/promotions/validate-code").permitAll()

                    // Public ads
                    .requestMatchers(HttpMethod.GET, "/api/v1/ads").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/ads/active").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/ads/**").permitAll()

                    // Public store info
                    .requestMatchers(HttpMethod.GET, "/api/v1/settings/store").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/settings/contact").permitAll()

                    // Public blog / CMS
                    .requestMatchers(HttpMethod.GET,  "/api/v1/blog/posts").permitAll()
                    .requestMatchers(HttpMethod.GET,  "/api/v1/blog/posts/**").permitAll()
                    .requestMatchers(HttpMethod.GET,  "/api/v1/blog/categories").permitAll()
                    .requestMatchers(HttpMethod.GET,  "/api/v1/blog/tags").permitAll()
                    .requestMatchers(HttpMethod.GET,  "/api/v1/blog/search").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/blog/posts/*/comments").permitAll()
                    .requestMatchers(HttpMethod.GET,  "/api/v1/blog/posts/*/comments").permitAll()

                    // ── ADMIN + MANAGER access ──────────────────────────────────
                    // Role hierarchy applies to @PreAuthorize only (Spring Security 6.2).
                    // URL-level rules must list both roles explicitly.

                    .requestMatchers(HttpMethod.GET,    "/api/v1/analytics/**").hasAnyRole("ADMIN", "MANAGER")

                    .requestMatchers(HttpMethod.POST,   "/api/v1/products/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.PUT,    "/api/v1/products/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.PATCH,  "/api/v1/products/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasAnyRole("ADMIN", "MANAGER")

                    .requestMatchers(HttpMethod.GET,    "/api/v1/suppliers/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.POST,   "/api/v1/suppliers/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.PUT,    "/api/v1/suppliers/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.PATCH,  "/api/v1/suppliers/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/suppliers/**").hasAnyRole("ADMIN", "MANAGER")

                    .requestMatchers(HttpMethod.POST,   "/api/v1/categories/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.PUT,    "/api/v1/categories/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.PATCH,  "/api/v1/categories/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasAnyRole("ADMIN", "MANAGER")

                    .requestMatchers(HttpMethod.POST,   "/api/v1/ads/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.PUT,    "/api/v1/ads/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.PATCH,  "/api/v1/ads/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/ads/**").hasAnyRole("ADMIN", "MANAGER")

                    .requestMatchers(HttpMethod.POST,   "/api/v1/bundles/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.PUT,    "/api/v1/bundles/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.PATCH,  "/api/v1/bundles/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/bundles/**").hasAnyRole("ADMIN", "MANAGER")

                    .requestMatchers(HttpMethod.POST,   "/api/v1/promotions/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.PUT,    "/api/v1/promotions/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.PATCH,  "/api/v1/promotions/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/promotions/**").hasAnyRole("ADMIN", "MANAGER")

                    .requestMatchers(HttpMethod.POST,   "/api/v1/stock-adjustments/**").hasAnyRole("ADMIN", "MANAGER")

                    // ADMIN-only endpoints (MANAGER cannot change store settings or users)
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT,    "/api/v1/settings/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PATCH,  "/api/v1/settings/**").hasRole("ADMIN")

                    // Everything else: authenticated user of any role
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        // CORS_ORIGINS env var: comma-separated list of allowed origins.
        // Falls back to local dev origins when not set.
        val envOrigins = System.getenv("CORS_ORIGINS")
        configuration.allowedOrigins = if (!envOrigins.isNullOrBlank()) {
            envOrigins.split(",").map { it.trim() }
        } else {
            listOf(
                "http://localhost:3000",
                "http://localhost:4200",
                "http://localhost:8080"
            )
        }
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
