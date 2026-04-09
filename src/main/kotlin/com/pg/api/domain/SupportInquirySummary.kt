package com.pg.api.domain

import java.time.LocalDateTime

data class SupportInquirySummary(
    val inquiryId: Long,
    val inquiryNo: String,
    val categoryCode: String,
    val title: String,
    val status: String,
    val createdAt: LocalDateTime,
)
