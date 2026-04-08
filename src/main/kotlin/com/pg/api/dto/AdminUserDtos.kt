package com.pg.api.dto

data class AdminUserEntryResponse(
    val id: String,
    val username: String,
    val name: String,
    val email: String,
    val status: String,
    val role: String,
    val phone: String? = null,
    val profileImageUrl: String? = null,
    val lastLoginAt: String? = null,
)

data class AdminUserListResponse(
    val items: List<AdminUserEntryResponse>,
    val page: Int,
    val size: Int,
    val totalCount: Long,
    val totalPages: Int,
)

data class AdminUserActivityLogResponse(
    val id: String,
    val activityType: String,
    val activityTitle: String,
    val activityDetail: String? = null,
    val actorUsername: String? = null,
    val createdAt: String,
)

data class AdminUserDetailResponse(
    val id: String,
    val username: String,
    val name: String,
    val email: String,
    val status: String,
    val role: String,
    val phone: String? = null,
    val profileImageUrl: String? = null,
    val lastLoginAt: String? = null,
    val activityLogs: List<AdminUserActivityLogResponse> = emptyList(),
)
