# PG_DEV_GUIDE_API 통합 테스트 시나리오 정의서

## 1. 개요
본 문서는 `CJPG.txt` 기획안과 `PaymentController.kt`의 엔드포인트를 바탕으로, MyBatis 기반의 실제 DB(`pgdev` 스키마) 연동을 검증하기 위한 통합 테스트 시나리오를 정의합니다.

---

## 2. API별 상세 테스트 시나리오

### 2.1 사용자 인증 및 접근 제어
*   **엔드포인트**: `POST /auth/login` (Codex 구현 예정)
*   **시나리오**:
    1.  **[정상]** `users` 테이블에 존재하는 계정으로 로그인 성공 시 `access_token` 발급 확인.
    2.  **[예외]** 잘못된 자격 증명으로 로그인 시 `401 Unauthorized` 반환.
    3.  **[보안]** 유효한 `Bearer Token` 없이 `/v2/payments/request` 등 보안 API 호출 시 `401` 차단 확인.

### 2.2 결제 요청 및 DB 기록 (`POST /v2/payments/request`)
*   **시나리오**:
    1.  **[정상]** 결제 요청 시 `SUCCESS` 응답을 받고, `payments` 테이블에 상태가 `READY`로 저장되는지 확인.
    2.  **[데이터]** 응답의 `paymentKey`가 DB의 `tid` 컬럼과 일치하는지 확인.
    3.  **[예외]** `amount`가 0 이하인 경우 처리 로직 검증.

### 2.3 결제 취소 및 내역 관리 (`POST /v1/payments/cancel`)
*   **시나리오**:
    1.  **[정상]** 부분 취소 요청 시 `cancels` 테이블에 내역이 저장되고, `payments` 테이블의 `status`가 `PARTIAL_CANCELLED`로 변경되는지 확인.
    2.  **[정합성]** 취소 후 `remained_amount`(잔액)가 `original_amount - sum(cancel_amount)`와 일치하는지 계산 검증.
    3.  **[예외]** 존재하지 않는 `payment_id`(TID)로 취소 시도 시 에러 확인.

### 2.4 실시간 결제 상태 조회 (`GET /v1/payments/status/{tid}`)
*   **시나리오**:
    1.  **[정상]** `tid`로 조회 시 DB의 최신 `status`, `amount`, `card_info`가 응답 필드에 정확히 매핑되는지 확인.
    2.  **[연동]** 취소 완료된 건을 조회했을 때 `status`가 `CANCELLED`로 나오는지 확인.

### 2.5 지원 센터 문의 등록 및 조회
*   **엔드포인트**: `POST /inquiries`, `GET /inquiries` (Codex 구현 예정)
*   **시나리오**:
    1.  **[정상]** 사용자가 문의 등록 시 `inquiries` 테이블에 `user_id`와 함께 저장되는지 확인.
    2.  **[정상]** 리스트 조회 시 본인이 작성한 내역만 필터링되어 나오는지 확인.
    3.  **[상태]** 관리자가 DB에서 `answer`를 업데이트했을 때 사용자 조회 API에서 `status`가 `COMPLETED`로 반영되는지 확인.

---

## 3. 통합 테스트 체크리스트 (DB 연동 확인)
- [ ] 모든 SQL 쿼리가 `pgdev` 스키마를 명시하거나 기본 스키마로 사용하고 있는가?
- [ ] `map-underscore-to-camel-case` 설정이 `PaymentData` 등의 DTO 필드에 올바르게 적용되는가?
- [ ] 트랜잭션: 취소 API 실패 시 `payments` 테이블의 상태가 롤백되는가?
