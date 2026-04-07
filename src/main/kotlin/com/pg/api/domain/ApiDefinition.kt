package com.pg.api.domain

import java.time.LocalDateTime

data class ApiDefinition(
    var id: Long? = null,
    val apiCode: String,
    val apiName: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
