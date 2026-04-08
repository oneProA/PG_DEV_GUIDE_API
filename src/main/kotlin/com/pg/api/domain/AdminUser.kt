package com.pg.api.domain

import java.time.LocalDateTime

data class AdminUser(
    val id: Long,
    val username: String,
    val name: String,
    val email: String,
    val status: String,
    val role: String,
    val phone: String? = null,
    val profileImageUrl: String? = null,
    val lastLoginAt: LocalDateTime? = null,
)
