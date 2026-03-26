package com.pg.api.dto

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
