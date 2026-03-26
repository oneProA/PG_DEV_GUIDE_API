package com.pg.api.dto

data class CancelRequest(
    val payment_id: String,
    val cancel_amount: Long,
    val cancel_reason: String? = null
)

data class CancelResponse(
    val status: String,
    val data: CancelData
)

data class CancelData(
    val cancel_id: String,
    val payment_id: String,
    val cancelled_amount: Long,
    val remained_amount: Long,
    val status: String,
    val created_at: String
)
