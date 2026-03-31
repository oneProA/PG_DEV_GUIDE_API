package com.pg.api.service

import com.pg.api.dto.LoginRequest
import com.pg.api.dto.LoginResponse
import com.pg.api.dto.LoginResponseData
import com.pg.api.repository.UserMapper
import org.springframework.stereotype.Service

@Service
class AuthService(private val userMapper: UserMapper) {

    fun login(request: LoginRequest): LoginResponse {
        println("Login attempt for user: ${request.username}")
        
        // username 존재 여부만 확인 (사용자 요청사항)
        val user = userMapper.findByUsername(request.username)
            ?: throw IllegalArgumentException("로그인 실패")
        
        println("User found: ${user.username}, Role: ${user.role}")

        return LoginResponse(
            data = LoginResponseData(
                accessToken = "mock-jwt-token-${user.username}-${System.currentTimeMillis()}",
                username = user.username,
                email = user.email,
                role = user.role
            ),
            message = "로그인 성공"
        )
    }
}
