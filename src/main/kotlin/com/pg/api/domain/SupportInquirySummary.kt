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

data class SupportInquiryDetail(
    val inquiryId: Long,
    val inquiryNo: String,
    val categoryCode: String,
    val title: String,
    val contentText: String,
    val answerContentText: String?,
    val status: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val answeredAt: LocalDateTime?,
)

data class SupportInquiryFileSummary(
    val fileId: Long,
    val inquiryId: Long,
    val ownerType: String,
    val fileRole: String,
    val originalFileName: String,
    val fileUrl: String,
    val mimeType: String,
    val fileSizeBytes: Long,
    val createdAt: LocalDateTime,
)
