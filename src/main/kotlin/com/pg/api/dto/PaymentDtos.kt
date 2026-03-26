package com.pg.api.dto

data class PaymentRequest(
    val mid: String,
    val orderId: String,
    val amount: Long,
    val goodsName: String? = null
)

data class PaymentResponse(
    val status: String,
    val data: PaymentData
)

data class PaymentData(
    val paymentKey: String,
    val orderId: String,
    val amount: Long,
    val requestedAt: String,
    val approvedAt: String
)
