package com.pg.api.domain

import java.time.LocalDateTime

data class AdminInquiryResponseTimePair(
    val createdAt: LocalDateTime,
    val answeredAt: LocalDateTime,
)
