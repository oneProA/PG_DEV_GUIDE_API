# MISSION: 결제 시스템 DB 스키마 및 엔티티 구현

## 1. 목표
`CJPG.txt` 분석 내용을 바탕으로 결제(Payment) 및 취소(Cancellation) 시스템을 위한 DB 스키마를 생성하고, 이를 매핑할 Kotlin 엔티티 클래스를 구현합니다.

## 2. 세부 작업 내역
### A. 데이터베이스 스키마 생성 (`src/main/resources/schema.sql`)
- **`payments` 테이블**: 결제 마스터 정보 저장
    - `id` (BIGSERIAL PK)
    - `payment_id` (VARCHAR(50) UNIQUE): 외부 노출용 결제 고유 ID
    - `tid` (VARCHAR(50)): PG 거래 번호
    - `mid` (VARCHAR(20)): 상점 아이디
    - `order_id` (VARCHAR(50)): 상점 주문 번호
    - `amount` (DECIMAL(19, 2)): 결제 금액
    - `status` (VARCHAR(20)): 상태 (READY, PAID, FAILED, CANCELLED, PARTIAL_CANCELLED)
    - `payment_method` (VARCHAR(20)): 결제 수단 (CARD, TRANS, VBANK)
    - `goods_name` (VARCHAR(255)): 상품명
    - `created_at` (TIMESTAMP), `approved_at` (TIMESTAMP)
- **`payment_card_details` 테이블**: 카드 결제 상세 정보 (1:1 관계)
    - `payment_id` (BIGINT PK/FK)
    - `issuer` (VARCHAR(50)): 카드사
    - `card_number` (VARCHAR(20)): 마스킹된 카드 번호
    - `installment_month` (INT): 할부 개월 수
- **`cancellations` 테이블**: 취소 내역 저장 (1:N 관계)
    - `id` (BIGSERIAL PK)
    - `cancel_id` (VARCHAR(50) UNIQUE): 취소 고유 ID
    - `payment_id` (BIGINT FK): 원 거래 ID
    - `cancel_amount` (DECIMAL(19, 2)): 취소 요청 금액
    - `remained_amount` (DECIMAL(19, 2)): 취소 후 잔액
    - `cancel_reason` (TEXT): 취소 사유
    - `cancelled_at` (TIMESTAMP)

### B. Kotlin 엔티티 구현 (`src/main/kotlin/com/pg/api/domain/`)
- `Payment`, `CardDetail`, `Cancellation` 데이터 클래스 작성
- MyBatis `type-aliases-package`(`com.pg.api`)에 맞춰 패키지 구성

## 3. 기술 제약 사항
- **인코딩**: 모든 파일은 UTF-8 (BOM 없음)로 작성할 것.
- **도구 사용**: `write_file` 등을 사용하여 실제 소스 코드에 결과를 반영할 것.
- **검증**: 작업 완료 후 `MISSION_REPORT.md`를 작성할 것.
- **런타임**: `.codex-runtime` 경로를 사용할 것.
