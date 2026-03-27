package com.pg.api.dto

/**
 * 결제 요청 DTO
 */
data class PaymentRequest(
    val mid: String,
    val orderId: String,
    val amount: Long,
    val goodsName: String? = null
)

/**
 * 공통 API 응답 포맷
 */
data class ApiResponse<T>(
    val status: String,
    val data: T? = null,
    val message: String? = null
)

/**
 * 결제 승인 응답 DTO
 */
data class PaymentResponse(
    val status: String,
    val data: PaymentData
)

/**
 * 결제 승인 상세 데이터
 */
data class PaymentData(
    val paymentKey: String,
    val orderId: String,
    val amount: Long,
    val requestedAt: String,
    val approvedAt: String
)
