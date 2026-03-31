package com.pg.api.dto

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val data: LoginResponseData,
    val message: String? = null
)

data class LoginResponseData(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresAt: String = "2099-12-31T23:59:59",
    val username: String,
    val email: String,
    val role: String
)
