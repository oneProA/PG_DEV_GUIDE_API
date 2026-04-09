package com.pg.api.domain

import java.time.LocalDateTime

data class SupportInquiryCreateCommand(
    var inquiryId: Long? = null,
    val inquiryNo: String,
    val userId: Long,
    val categoryCode: String,
    val title: String,
    val contentText: String,
    val status: String,
    val priority: String,
    val hasAttachments: String,
    val viewCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
