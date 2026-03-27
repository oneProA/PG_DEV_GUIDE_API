package com.pg.api.domain

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 결제 마스터 정보
 */
data class Payment(
    val id: Long? = null,
    val paymentId: String,
    val tid: String? = null,
    val mid: String,
    val orderId: String,
    val amount: BigDecimal,
    var status: String = "READY",
    val paymentMethod: String? = null,
    val goodsName: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var approvedAt: LocalDateTime? = null,
    var cardDetail: CardDetail? = null,
    val cancellations: MutableList<Cancellation> = mutableListOf()
)
