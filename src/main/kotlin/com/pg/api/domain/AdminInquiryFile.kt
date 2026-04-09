package com.pg.api.domain

import java.time.LocalDateTime

data class AdminInquiryFile(
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

