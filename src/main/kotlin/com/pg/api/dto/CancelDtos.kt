package com.pg.api.dto

import java.math.BigDecimal

data class GuideCancelRequest(
    val orderId: String,
    val cancelAmount: Long? = null,
    val cancelReason: String? = null,
)

data class GuideCancelResponse(
    val orderId: String,
    val paymentMethodId: String? = null,
    val status: String,
    val cancelAmount: Long? = null,
    val remainAmount: Long? = null,
    val canceledAt: String? = null,
    val paymentId: String? = null,
)

/**
 * 레거시 시뮬레이션 서비스 호환용 DTO
 */
data class CancelRequest(
    val paymentId: String,
    val cancelAmount: BigDecimal? = null,
    val cancelReason: String? = null,
)

data class CancelResponse(
    val status: String,
    val data: CancelData,
)

data class CancelData(
    val cancelId: String,
    val paymentId: String,
    val cancelledAmount: BigDecimal,
    val remainedAmount: BigDecimal,
    val status: String,
    val createdAt: String,
)
