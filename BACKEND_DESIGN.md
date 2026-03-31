# PG_DEV_GUIDE_API 상세 설계서 (MyBatis & PostgreSQL)

본 문서는 `pgdev` 스키마의 실제 DB 구조와 Kotlin DTO를 바탕으로 한 최종 데이터 레이어 설계안입니다.

---

## 1. 데이터베이스 스키마 명세 (pgdev Schema)

### 1.1 결제 내역 테이블 (`payments`)
- `id` (BIGINT, PK, SERIAL): 내부 고유 ID (FK 참조용)
- `payment_id` (VARCHAR(50), UNIQUE): 비즈니스 고유 키 (예: `pk_test_...`)
- `tid` (VARCHAR(50), INDEX): 실제 PG 거래 고유 번호
- `mid`, `order_id`, `amount`, `status`
- `payment_method`, `goods_name`, `created_at`, `approved_at`

### 1.2 결제 카드 상세 정보 (`payment_card_details`)
- `id` (PK, SERIAL), `payment_id` (FK -> `payments.id`)
- `issuer`, `card_number`, `installment_plan_months` (D과 매핑 필요)

### 1.3 결제 취소 테이블 (`cancellations`)
- `id` (PK, SERIAL), `cancel_id` (UNIQUE)
- `payment_id` (FK -> `payments.id`)
- `cancel_amount`, `remained_amount`, `cancel_reason`, `cancelled_at`

### 1.4 사용자 테이블 (`users`)
- `user_id` (PK, SERIAL), `username` (UNIQUE), `password`, `email`, `role`, `created_at`

### 1.5 문의 게시판 테이블 (`inquiries`)
- `id` (PK, SERIAL), `user_id` (FK -> `users.user_id`)
- `category`, `title`, `content`, `status`, `answer`, `created_at`

---

## 2. MyBatis 매퍼 및 DTO 매핑 가이드

### 2.1 주요 매핑 규칙
1.  **CamelCase**: `application.yml`에 `map-underscore-to-camel-case: true`가 적용되어 있습니다.
    - 예: `order_id` (DB) -> `orderId` (DTO) 자동 매핑.
2.  **ID 참조**: 
    - `CancelRequest` 등의 DTO에서 넘어오는 `payment_id`는 DB의 `payments.payment_id` (String)입니다.
    - `cancellations` 테이블에 INSERT 시에는 `payments.id` (BigInt)를 조회하여 FK로 사용해야 합니다.

### 2.2 Mapper 설계
- **UserMapper**: `findByUsername`, `insertUser`
- **PaymentMapper**: 
    - `insertPayment`: 결제 요청 저장
    - `updateApprovedStatus`: 승인 성공 시 `tid`, `status`, `approved_at` 업데이트
- **InquiryMapper**: `insertInquiry`, `findByUserId` (Join with `users` if needed)

---

## 3. Codex 구현 지침
- `pgdev` 스키마를 항상 명시하거나 설정에서 기본 스키마로 사용하십시오.
- `INSERT` 시 PostgreSQL의 `RETURNING` 문법을 활용할 수 있습니다.
- 모든 시간 데이터는 `TIMESTAMP` 타입을 기준으로 처리하십시오.
### Auth Flow
- `AuthService` uses `users.username` with `PasswordEncoder.matches`, then `JwtTokenProvider` issues HS256 tokens (subject/role/userId) based on `security.jwt` props.
- `AuthController.POST /auth/login` returns `LoginResponse(accessToken, tokenType, expiresAt, username, role)` inside the shared `ApiResponse` schema.
- `DataInitializer` seeds `demo.user`/`password` to make login flows reproducible.
