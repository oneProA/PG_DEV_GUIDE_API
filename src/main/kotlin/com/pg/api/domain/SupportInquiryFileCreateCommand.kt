package com.pg.api.domain

import java.time.LocalDateTime

data class SupportInquiryFileCreateCommand(
    var fileId: Long? = null,
    val inquiryId: Long,
    val ownerType: String,
    val fileRole: String,
    val originalFileName: String,
    val storedFileName: String,
    val fileUrl: String,
    val mimeType: String,
    val fileSizeBytes: Long,
    val inlineKey: String?,
    val sortOrder: Int,
    val uploadedByUserId: Long,
    val createdAt: LocalDateTime,
)
