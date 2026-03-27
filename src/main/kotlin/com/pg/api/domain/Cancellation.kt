package com.pg.api.domain

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 취소 내역 정보
 */
data class Cancellation(
    val id: Long? = null,
    val cancelId: String,
    val paymentId: Long,
    val cancelAmount: BigDecimal,
    val remainedAmount: BigDecimal,
    val cancelReason: String? = null,
    val cancelledAt: LocalDateTime = LocalDateTime.now()
)
