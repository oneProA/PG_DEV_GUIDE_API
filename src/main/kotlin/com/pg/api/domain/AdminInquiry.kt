package com.pg.api.domain

import java.time.LocalDateTime

data class AdminInquiry(
    val inquiryId: Long,
    val inquiryNo: String,
    val userId: Long,
    val authorName: String,
    val authorUsername: String,
    val categoryCode: String,
    val title: String,
    val contentText: String,
    val answerContentText: String? = null,
    val status: String,
    val hasAttachments: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val answeredAt: LocalDateTime? = null,
)

