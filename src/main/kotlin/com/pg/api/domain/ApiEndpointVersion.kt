package com.pg.api.domain

import java.time.LocalDateTime

data class ApiEndpointVersion(
    var id: Long? = null,
    val apiDefinitionId: Long,
    val version: String,
    val endpoint: String,
    val httpMethod: String,
    val displayOrder: Int = 999,
    val status: String,
    val description: String? = null,
    val isCurrent: String = "Y",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
