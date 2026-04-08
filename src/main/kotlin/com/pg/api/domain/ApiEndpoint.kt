package com.pg.api.domain

import java.time.LocalDateTime

/**
 * API 엔드포인트 메타데이터
 */
data class ApiEndpoint(
    val id: Long? = null,
    val name: String,
    val httpMethod: String,
    val endpoint: String,
    val version: String,
    val displayOrder: Int = 999,
    var status: String = "정상 운영",
    val description: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
