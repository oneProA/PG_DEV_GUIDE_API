package com.pg.api.domain

/**
 * 카드 결제 상세 정보
 */
data class CardDetail(
    val paymentId: Long,
    val issuer: String? = null,
    val cardNumber: String? = null,
    val installmentMonth: Int = 0
)
