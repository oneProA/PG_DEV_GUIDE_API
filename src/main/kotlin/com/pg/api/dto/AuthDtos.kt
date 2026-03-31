package com.pg.api.dto

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresAt: String,
    val username: String,
    val role: String
)
