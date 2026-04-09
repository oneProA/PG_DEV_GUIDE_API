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
