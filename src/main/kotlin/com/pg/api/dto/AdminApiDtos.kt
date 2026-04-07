package com.pg.api.dto

data class AdminApiEntryResponse(
    val id: String,
    val name: String,
    val endpoint: String,
    val method: String,
    val version: String,
    val status: String,
    val lastModified: String,
    val description: String? = null
)

data class AdminApiFieldResponse(
    val id: String,
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

data class AdminApiDetailResponse(
    val id: String,
    val name: String,
    val endpoint: String,
    val method: String,
    val version: String,
    val status: String,
    val lastModified: String,
    val description: String? = null,
    val fields: List<AdminApiFieldResponse> = emptyList()
)

data class AdminApiFieldRequest(
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

data class CreateAdminApiRequest(
    val name: String,
    val method: String,
    val endpoint: String,
    val version: String,
    val status: String = "정상 운영",
    val description: String? = null,
    val fields: List<AdminApiFieldRequest> = emptyList()
)

data class UpdateAdminApiRequest(
    val name: String,
    val method: String,
    val endpoint: String,
    val version: String,
    val status: String? = null,
    val description: String? = null,
    val fields: List<AdminApiFieldRequest> = emptyList()
)
