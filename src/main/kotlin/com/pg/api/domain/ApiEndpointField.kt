package com.pg.api.domain

/**
 * API 엔드포인트 요청/응답 필드 메타데이터
 */
data class ApiEndpointField(
    val id: Long,
    val fieldScope: String,
    val fieldLocation: String,
    val fieldName: String,
    val fieldType: String,
    val requiredYn: String,
    val fieldOrder: Int,
    val description: String? = null,
    val sampleValue: String? = null,
    val defaultValue: String? = null
)
