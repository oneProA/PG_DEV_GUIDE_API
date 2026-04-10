package com.pg.api.dto

data class CreateSupportInquiryResponse(
    val inquiryId: String,
    val inquiryNo: String,
    val uploadedFileCount: Int,
)

data class SupportInquirySummaryResponse(
    val inquiryId: String,
    val inquiryNo: String,
    val categoryCode: String,
    val title: String,
    val status: String,
    val createdAt: String,
)

data class SupportInquiryDetailResponse(
    val inquiryId: String,
    val inquiryNo: String,
    val categoryCode: String,
    val title: String,
    val contentText: String,
    val answerContentText: String?,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val answeredAt: String?,
    val files: List<SupportInquiryFileResponse>,
)

data class SupportInquiryFileResponse(
    val fileId: String,
    val inquiryId: String,
    val ownerType: String,
    val fileRole: String,
    val originalFileName: String,
    val fileUrl: String,
    val mimeType: String,
    val fileSizeBytes: Long,
    val createdAt: String,
)
