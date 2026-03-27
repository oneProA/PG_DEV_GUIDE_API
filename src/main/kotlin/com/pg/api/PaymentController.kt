package com.pg.api

import com.pg.api.dto.*
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@RestController
class PaymentController {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    @PostMapping("/v2/payments/request")
    fun requestPayment(@RequestBody request: PaymentRequest): PaymentResponse {
        return PaymentResponse(
            status = "success",
            data = PaymentData(
                paymentKey = "pk_test_" + UUID.randomUUID().toString().replace("-", ""),
                orderId = request.orderId,
                amount = request.amount,
                requestedAt = LocalDateTime.now().minusSeconds(5).format(formatter),
                approvedAt = LocalDateTime.now().format(formatter)
            )
        )
    }

    @PostMapping("/v1/payments/cancel")
    fun cancelPayment(@RequestBody request: CancelRequest): CancelResponse {
        return CancelResponse(
            status = "SUCCESS",
            data = CancelData(
                cancelId = "CXL_" + (1000000..9999999).random(),
                paymentId = request.paymentId,
                cancelledAmount = request.cancelAmount ?: 0.toBigDecimal(),
                remainedAmount = 12500.toBigDecimal(), // Mock value
                status = "PARTIAL_CANCELLED",
                createdAt = LocalDateTime.now().format(formatter) + "Z"
            )
        )
    }

    @GetMapping("/v1/payments/status/{tid}")
    fun getStatus(@PathVariable tid: String, @RequestParam mid: String): StatusResponse {
        return StatusResponse(
            status = "SUCCESS",
            tid = tid,
            amount = 45000,
            paid_at = "2024-10-27T14:30:05",
            method = "CARD",
            card_info = CardInfo(
                issuer = "CJ_CARD",
                number = "4518-****-****-1234",
                quota = 0
            ),
            receipt_url = "https://pg.cj.net/r/" + UUID.randomUUID().toString()
        )
    }
}
