package com.pg.api.service

import com.pg.api.config.JwtProperties
import com.pg.api.domain.User
import com.pg.api.dto.LoginRequest
import com.pg.api.repository.UserMapper
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class AuthServiceTest {
    private val userMapper = mockk<UserMapper>()
    private val passwordEncoder = BCryptPasswordEncoder()
    private val jwtProperties = JwtProperties(
        secret = "CJPG_TEST_SECRET_TOKEN_2026_1234567890",
        issuer = "pg-dev-guide-test",
        expirationSeconds = 3600
    )
    private val jwtTokenProvider = JwtTokenProvider(jwtProperties)
    private val authService = AuthService(userMapper, passwordEncoder, jwtTokenProvider)

    @Test
    fun `login returns token when credentials are valid`() {
        val user = User(
            userId = 1L,
            username = "demo.user",
            password = passwordEncoder.encode("password"),
            email = "demo.user@cjone.com",
            role = "ADMIN"
        )

        every { userMapper.findByUsername("demo.user") } returns user

        val response = authService.login(LoginRequest("demo.user", "password"))

        assertEquals("demo.user", response.username)
        assertEquals("Bearer", response.tokenType)
        assertTrue(response.accessToken.split('.').size == 3)
    }

    @Test
    fun `login fails when user is missing`() {
        every { userMapper.findByUsername("missing") } returns null

        val exception = assertThrows<IllegalArgumentException> {
            authService.login(LoginRequest("missing", "password"))
        }

        assertEquals("Invalid username or password", exception.message)
    }

    @Test
    fun `login fails when password does not match`() {
        val user = User(
            userId = 2L,
            username = "demo.user",
            password = passwordEncoder.encode("correct"),
            email = "demo.user@cjone.com",
            role = "ADMIN"
        )

        every { userMapper.findByUsername("demo.user") } returns user

        val exception = assertThrows<IllegalArgumentException> {
            authService.login(LoginRequest("demo.user", "wrong"))
        }

        assertEquals("Invalid username or password", exception.message)
    }
}
