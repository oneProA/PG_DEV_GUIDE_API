package com.pg.api.service

import com.pg.api.domain.User
import com.pg.api.config.JwtProperties
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey
import org.springframework.stereotype.Component

@Component
class JwtTokenProvider(private val properties: JwtProperties) {

    private val key: SecretKey = Keys.hmacShaKeyFor(properties.secret.toByteArray(StandardCharsets.UTF_8))

    fun generateToken(user: User): TokenDetails {
        val now = Instant.now()
        val expiresAt = now.plusSeconds(properties.expirationSeconds)

        val token = Jwts.builder()
            .setSubject(user.username)
            .setIssuer(properties.issuer)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiresAt))
            .claim("role", user.role)
            .claim("userId", user.userId)
            .signWith(key)
            .compact()

        return TokenDetails(token, expiresAt)
    }

    fun validate(token: String) {
        Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
    }
}

data class TokenDetails(
    val token: String,
    val expiresAt: Instant
)
