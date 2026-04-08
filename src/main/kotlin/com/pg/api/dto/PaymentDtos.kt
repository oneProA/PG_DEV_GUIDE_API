package com.pg.api.dto

/**
 * 가이드 사이트에서 사용하는 표준 결제 요청 DTO
 */
data class GuidePaymentRequest(
    val paymentMethodId: String? = null,
    val orderId: String? = null,
    val userId: String,
    val itemName: String,
    val amount: Long,
    val approvalUrl: String? = null,
    val cancelUrl: String? = null,
    val failUrl: String? = null,
)

/**
 * 공통 API 응답 포맷
 */
data class ApiResponse<T>(
    val status: String,
    val data: T? = null,
    val message: String? = null,
)

/**
 * 가이드 사이트 표준 결제 요청 응답 DTO
 */
data class GuidePaymentRequestResponse(
    val orderId: String,
    val paymentMethodId: String,
    val paymentId: String? = null,
    val status: String,
    val amount: Long,
    val approvedAt: String? = null,
    val nextRedirectPcUrl: String? = null,
)

/**
 * 가이드 사이트 표준 결제 상태 응답 DTO
 */
data class GuidePaymentStatusResponse(
    val orderId: String,
    val userId: String? = null,
    val amount: Long,
    val status: String,
    val paymentMethodId: String? = null,
    val paymentId: String? = null,
    val createdAt: String? = null,
    val approvalAt: String? = null,
)
