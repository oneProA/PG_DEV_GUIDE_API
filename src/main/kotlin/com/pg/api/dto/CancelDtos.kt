package com.pg.api.dto

import java.math.BigDecimal

data class CancelRequest(
    val paymentId: String,
    val cancelAmount: BigDecimal? = null,
    val cancelReason: String? = null
)

data class CancelResponse(
    val status: String,
    val data: CancelData
)

data class CancelData(
    val cancelId: String,
    val paymentId: String,
    val cancelledAmount: BigDecimal,
    val remainedAmount: BigDecimal,
    val status: String,
    val createdAt: String
)
