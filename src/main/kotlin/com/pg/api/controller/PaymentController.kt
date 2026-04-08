package com.pg.api.controller

import com.pg.api.dto.ApiResponse
import com.pg.api.dto.GuideCancelRequest
import com.pg.api.dto.GuideCancelResponse
import com.pg.api.dto.GuidePaymentRequest
import com.pg.api.dto.GuidePaymentRequestResponse
import com.pg.api.dto.GuidePaymentStatusResponse
import com.pg.api.service.PgApiBridgeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/payments")
class PaymentController(
    private val pgApiBridgeService: PgApiBridgeService,
) {

    @PostMapping("/request")
    fun requestPayment(@RequestBody request: GuidePaymentRequest): ResponseEntity<ApiResponse<GuidePaymentRequestResponse>> {
        val response = pgApiBridgeService.requestPayment(request)
        return ResponseEntity.ok(ApiResponse("SUCCESS", response))
    }

    @GetMapping("/status/{orderId}")
    fun getStatus(@PathVariable orderId: String): ResponseEntity<ApiResponse<GuidePaymentStatusResponse>> {
        val response = pgApiBridgeService.getPaymentStatus(orderId)
        return ResponseEntity.ok(ApiResponse("SUCCESS", response))
    }

    @PostMapping("/cancel")
    fun cancelPayment(@RequestBody request: GuideCancelRequest): ResponseEntity<ApiResponse<GuideCancelResponse>> {
        val response = pgApiBridgeService.cancelPayment(request)
        return ResponseEntity.ok(ApiResponse("SUCCESS", response))
    }
}
