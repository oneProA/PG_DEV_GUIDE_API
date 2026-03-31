package com.pg.api.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "security.jwt")
data class JwtProperties(
    var secret: String = "CJPG_SUPER_SECRET_TOKEN_KEY_2026_1234567890",
    var issuer: String = "pg-dev-guide",
    var expirationSeconds: Long = 3600
)
