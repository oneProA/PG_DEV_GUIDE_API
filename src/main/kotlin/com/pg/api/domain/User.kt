package com.pg.api.domain

data class User(
    val username: String,
    val password: String,
    val email: String,
    val role: String
)
