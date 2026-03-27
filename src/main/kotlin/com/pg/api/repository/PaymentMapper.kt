package com.pg.api.repository

import com.pg.api.domain.CardDetail
import com.pg.api.domain.Cancellation
import com.pg.api.domain.Payment
import org.apache.ibatis.annotations.Mapper

@Mapper
interface PaymentMapper {
    /**
     * 결제 마스터 정보 저장
     */
    fun insertPayment(payment: Payment): Int

    /**
     * 카드 결제 상세 정보 저장
     */
    fun insertCardDetail(cardDetail: CardDetail): Int

    /**
     * 취소 내역 저장
     */
    fun insertCancellation(cancellation: Cancellation): Int

    /**
     * 결제 고유 ID(payment_id)로 결제 정보 조회 (상세 및 취소 내역 포함)
     */
    fun findByPaymentId(paymentId: String): Payment?

    /**
     * 거래 고유 번호(tid)로 결제 정보 조회
     */
    fun findByTid(tid: String): Payment?

    /**
     * 결제 상태 업데이트
     */
    fun updateStatus(paymentId: String, status: String): Int
}
