package com.emjay.backend.infrastructure.config

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy
import org.hibernate.boot.model.naming.Identifier
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HibernateConfig {

    @Bean
    fun physicalNamingStrategy(): CamelCaseToUnderscoresNamingStrategy {
        return object : CamelCaseToUnderscoresNamingStrategy() {
            override fun toPhysicalColumnName(name: Identifier, context: JdbcEnvironment): Identifier {
                return super.toPhysicalColumnName(name, context)
            }
        }
    }
}