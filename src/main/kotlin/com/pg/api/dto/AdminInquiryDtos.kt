package com.pg.api.dto

data class AdminInquiryEntryResponse(
    val id: String,
    val inquiryNo: String,
    val userId: String,
    val authorName: String,
    val authorUsername: String,
    val categoryCode: String,
    val title: String,
    val preview: String,
    val status: String,
    val hasAttachments: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val answeredAt: String? = null,
)

data class AdminInquiryFileResponse(
    val id: String,
    val inquiryId: String,
    val ownerType: String,
    val fileRole: String,
    val originalFileName: String,
    val fileUrl: String,
    val mimeType: String,
    val fileSizeBytes: Long,
    val createdAt: String,
)

data class AdminInquiryDetailResponse(
    val id: String,
    val inquiryNo: String,
    val userId: String,
    val authorName: String,
    val authorUsername: String,
    val categoryCode: String,
    val title: String,
    val contentText: String,
    val answerContentText: String? = null,
    val status: String,
    val hasAttachments: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val answeredAt: String? = null,
    val files: List<AdminInquiryFileResponse> = emptyList(),
)

data class AdminInquiryListResponse(
    val items: List<AdminInquiryEntryResponse>,
    val page: Int,
    val size: Int,
    val totalCount: Long,
    val totalPages: Int,
)

data class UpdateInquiryStatusRequest(
    val status: String,
)

data class UpdateInquiryAnswerRequest(
    val answerContentText: String,
    val status: String,
)

data class AdminInquiryDashboardSummaryResponse(
    val todayReceivedCount: Long,
    val unhandledCount: Long,
    val avgResponseMinutes: Int,
)
