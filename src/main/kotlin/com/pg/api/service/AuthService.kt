package com.pg.api.service

import com.pg.api.domain.User
import com.pg.api.dto.LoginRequest
import com.pg.api.dto.LoginResponse
import com.pg.api.repository.UserMapper
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userMapper: UserMapper,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    fun login(request: LoginRequest): LoginResponse {
        val user = userMapper.findByUsername(request.username)
            ?: throw IllegalArgumentException("Invalid username or password")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("Invalid username or password")
        }

        val tokenDetails = jwtTokenProvider.generateToken(user)
        return LoginResponse(
            accessToken = tokenDetails.token,
            tokenType = "Bearer",
            expiresAt = tokenDetails.expiresAt.toString(),
            username = user.username,
            role = user.role
        )
    }

    fun seedDefaultUser(username: String, password: String, email: String, role: String = "ADMIN") {
        if (userMapper.findByUsername(username) != null) {
            return
        }

        val encodedPassword = passwordEncoder.encode(password)
        val user = User(username = username, password = encodedPassword, email = email, role = role)
        userMapper.insertUser(user)
    }
}
