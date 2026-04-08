package com.pg.api.domain

import java.time.LocalDateTime

data class UserActivityLog(
    val id: Long,
    val userId: Long,
    val activityType: String,
    val activityTitle: String,
    val activityDetail: String? = null,
    val actorUsername: String? = null,
    val createdAt: LocalDateTime,
)
