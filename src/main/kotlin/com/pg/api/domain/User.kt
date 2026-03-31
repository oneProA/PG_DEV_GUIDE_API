package com.pg.api.domain

import java.time.LocalDateTime

data class User(
    val userId: Long? = null,
    val username: String,
    val password: String,
    val email: String,
    val role: String = "USER",
    val createdAt: LocalDateTime? = null
)
