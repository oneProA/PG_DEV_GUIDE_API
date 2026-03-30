package com.pg.api.service

import com.pg.api.domain.Cancellation
import com.pg.api.domain.Payment
import com.pg.api.dto.CancelRequest
import com.pg.api.repository.PaymentMapper
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDateTime

class PaymentServiceTest {

    private val paymentMapper = mockk<PaymentMapper>()
    private val paymentService = PaymentService(paymentMapper)

    @Test
    fun `전액 취소 성공 테스트`() {
        // Given
        val paymentId = "PAY_001"
        val payment = Payment(
            id = 1L,
            paymentId = paymentId,
            mid = "MID_001",
            orderId = "ORD_001",
            amount = BigDecimal("10000"),
            status = "APPROVED"
        )
        
        val request = CancelRequest(paymentId = paymentId, cancelAmount = null, cancelReason = "고객 변심")

        every { paymentMapper.findByPaymentId(paymentId) } returns payment
        every { paymentMapper.insertCancellation(any()) } returns 1
        every { paymentMapper.updateStatus(paymentId, "CANCELLED") } returns 1

        // When
        val result = paymentService.cancelPayment(request)

        // Then
        assertEquals(BigDecimal("10000"), result.cancelAmount)
        assertEquals(BigDecimal.ZERO, result.remainedAmount)
        verify { paymentMapper.updateStatus(paymentId, "CANCELLED") }
        verify { paymentMapper.insertCancellation(match { it.cancelAmount == BigDecimal("10000") }) }
    }

    @Test
    fun `부분 취소 성공 테스트`() {
        // Given
        val paymentId = "PAY_002"
        val payment = Payment(
            id = 2L,
            paymentId = paymentId,
            mid = "MID_001",
            orderId = "ORD_002",
            amount = BigDecimal("10000"),
            status = "APPROVED"
        )
        
        val request = CancelRequest(paymentId = paymentId, cancelAmount = BigDecimal("3000"), cancelReason = "부분 반품")

        every { paymentMapper.findByPaymentId(paymentId) } returns payment
        every { paymentMapper.insertCancellation(any()) } returns 1
        every { paymentMapper.updateStatus(paymentId, "PARTIAL_CANCELLED") } returns 1

        // When
        val result = paymentService.cancelPayment(request)

        // Then
        assertEquals(BigDecimal("3000"), result.cancelAmount)
        assertEquals(BigDecimal("7000"), result.remainedAmount)
        verify { paymentMapper.updateStatus(paymentId, "PARTIAL_CANCELLED") }
    }

    @Test
    fun `잔액 초과 취소 시 예외 발생`() {
        // Given
        val paymentId = "PAY_003"
        val payment = Payment(
            id = 3L,
            paymentId = paymentId,
            mid = "MID_001",
            orderId = "ORD_003",
            amount = BigDecimal("10000"),
            status = "APPROVED"
        )
        
        val request = CancelRequest(paymentId = paymentId, cancelAmount = BigDecimal("12000"))

        every { paymentMapper.findByPaymentId(paymentId) } returns payment

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            paymentService.cancelPayment(request)
        }
        assertEquals("취소 요청 금액이 남은 잔액을 초과합니다.", exception.message)
    }

    @Test
    fun `이미 전액 취소된 경우 예외 발생`() {
        // Given
        val paymentId = "PAY_004"
        val payment = Payment(
            id = 4L,
            paymentId = paymentId,
            mid = "MID_001",
            orderId = "ORD_004",
            amount = BigDecimal("10000"),
            status = "CANCELLED"
        )
        
        val request = CancelRequest(paymentId = paymentId, cancelAmount = BigDecimal("1000"))

        every { paymentMapper.findByPaymentId(paymentId) } returns payment

        // When & Then
        val exception = assertThrows<IllegalStateException> {
            paymentService.cancelPayment(request)
        }
        assertEquals("이미 전액 취소된 결제입니다.", exception.message)
    }
}
