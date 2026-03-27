package com.pg.api.controller

import com.pg.api.domain.Payment
import com.pg.api.dto.ApiResponse
import com.pg.api.dto.CancelRequest
import com.pg.api.service.PaymentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/v1/payments")
class PaymentController(private val paymentService: PaymentService) {

    /**
     * 결제 요청 시뮬레이션 (POST /v1/payments/request)
     */
    @PostMapping("/request")
    fun requestPayment(@RequestBody payment: Payment): ResponseEntity<ApiResponse<Payment>> {
        // 결제 고유 ID 생성 (현실에서는 PG사에서 발급하거나 내부 규칙에 따름)
        val newPayment = payment.copy(
            paymentId = "CJ_ORD_" + UUID.randomUUID().toString().substring(0, 8).uppercase()
        )
        val saved = paymentService.requestPayment(newPayment)
        return ResponseEntity.ok(ApiResponse("SUCCESS", saved))
    }

    /**
     * 결제 상태 조회 (GET /v1/payments/status/{paymentId})
     */
    @GetMapping("/status/{paymentId}")
    fun getStatus(@PathVariable paymentId: String): ResponseEntity<ApiResponse<Any>> {
        val statusResponse = paymentService.getPaymentStatus(paymentId)
            ?: return ResponseEntity.status(404).body(ApiResponse("ERROR", null, "결제 정보를 찾을 수 없습니다."))
        
        return ResponseEntity.ok(ApiResponse("SUCCESS", statusResponse))
    }

    /**
     * 결제 취소 (POST /v1/payments/cancel)
     */
    @PostMapping("/cancel")
    fun cancelPayment(@RequestBody request: CancelRequest): ResponseEntity<ApiResponse<Any>> {
        return try {
            val cancellation = paymentService.cancelPayment(request)
            ResponseEntity.ok(ApiResponse("SUCCESS", cancellation))
        } catch (e: Exception) {
            ResponseEntity.status(400).body(ApiResponse("ERROR", null, e.message))
        }
    }
}
