package com.pg.api.dto

import java.math.BigDecimal

data class StatusResponse(
    val status: String,
    val tid: String,
    val amount: Long,
    val paid_at: String,
    val method: String,
    val card_info: CardInfo? = null,
    val receipt_url: String? = null
)

data class CardInfo(
    val issuer: String,
    val number: String,
    val quota: Int
)

/**
 * DB 연동형 결제 상태 조회 응답 DTO
 */
data class PaymentStatusResponse(
    val paymentId: String,
    val tid: String?,
    val status: String,
    val amount: BigDecimal,
    val remainedAmount: BigDecimal,
    val goodsName: String?,
    val method: String?,
    val cardInfo: Map<String, Any?>?
)
