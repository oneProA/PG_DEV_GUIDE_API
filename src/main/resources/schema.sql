-- 테이블 초기화 (필요 시)
CREATE SCHEMA IF NOT EXISTS pgdev;

DROP TABLE IF EXISTS pgdev.payment_card_details CASCADE;
DROP TABLE IF EXISTS pgdev.cancellations CASCADE;
DROP TABLE IF EXISTS pgdev.payments CASCADE;
DROP TABLE IF EXISTS pgdev.users CASCADE;
DROP TABLE IF EXISTS pgdev.api_endpoints CASCADE;
DROP TABLE IF EXISTS pgdev.api_endpoint_fields CASCADE;
DROP TABLE IF EXISTS pgdev.api_endpoint_versions CASCADE;
DROP TABLE IF EXISTS pgdev.api_definitions CASCADE;

-- 결제 마스터 테이블
CREATE TABLE IF NOT EXISTS pgdev.payments (
    id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(50) UNIQUE NOT NULL,
    tid VARCHAR(50),
    mid VARCHAR(20) NOT NULL,
    order_id VARCHAR(50) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'READY',
    payment_method VARCHAR(20),
    goods_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    CONSTRAINT chk_payments_amount_nonnegative CHECK (amount >= 0)
);

-- 카드 결제 상세 테이블
CREATE TABLE IF NOT EXISTS pgdev.payment_card_details (
    payment_id BIGINT PRIMARY KEY REFERENCES pgdev.payments(id),
    issuer VARCHAR(50),
    card_number VARCHAR(20),
    installment_month INT DEFAULT 0,
    CONSTRAINT chk_payment_card_details_installment_month_nonnegative CHECK (installment_month >= 0)
);

-- 취소 내역 테이블
CREATE TABLE IF NOT EXISTS pgdev.cancellations (
    id BIGSERIAL PRIMARY KEY,
    cancel_id VARCHAR(50) UNIQUE NOT NULL,
    payment_id BIGINT NOT NULL REFERENCES pgdev.payments(id),
    cancel_amount DECIMAL(19, 2) NOT NULL,
    remained_amount DECIMAL(19, 2) NOT NULL,
    cancel_reason TEXT,
    cancelled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_cancellations_cancel_amount_positive CHECK (cancel_amount > 0),
    CONSTRAINT chk_cancellations_remained_amount_nonnegative CHECK (remained_amount >= 0)
);

CREATE INDEX IF NOT EXISTS idx_payments_tid ON pgdev.payments(tid);
CREATE INDEX IF NOT EXISTS idx_payments_order_id ON pgdev.payments(order_id);
CREATE INDEX IF NOT EXISTS idx_cancellations_payment_id ON pgdev.cancellations(payment_id);

-- 사용자 정보
CREATE TABLE IF NOT EXISTS pgdev.users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password TEXT NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_username ON pgdev.users(username);

-- API 엔드포인트 관리 테이블
CREATE TABLE IF NOT EXISTS pgdev.api_endpoints (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    endpoint VARCHAR(255) NOT NULL UNIQUE,
    version VARCHAR(20) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT '정상 운영',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_api_endpoints_endpoint ON pgdev.api_endpoints(endpoint);
CREATE INDEX IF NOT EXISTS idx_api_endpoints_status ON pgdev.api_endpoints(status);

-- 샘플 API 엔드포인트 데이터 (초기 목업)
INSERT INTO pgdev.api_endpoints (name, http_method, endpoint, version, status, description, created_at, updated_at)
VALUES 
('결제 요청', 'POST', '/v1/payments/request', 'v1.0.0', '정상 운영', '결제를 요청합니다.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('결제 상태 조회', 'GET', '/v1/payments/status/:paymentId', 'v1.0.0', '정상 운영', '결제 정보 및 취소 내역을 조회합니다.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('결제 취소', 'POST', '/v1/payments/cancel', 'v1.0.0', '정상 운영', '결제 전체 또는 부분 취소를 처리합니다.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- API 정의 마스터
CREATE TABLE IF NOT EXISTS pgdev.api_definitions (
    id BIGSERIAL PRIMARY KEY,
    api_code VARCHAR(100) NOT NULL UNIQUE,
    api_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- API 버전별 스펙
CREATE TABLE IF NOT EXISTS pgdev.api_endpoint_versions (
    id BIGSERIAL PRIMARY KEY,
    api_definition_id BIGINT NOT NULL REFERENCES pgdev.api_definitions(id),
    version VARCHAR(20) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT '정상 운영',
    description TEXT,
    is_current CHAR(1) NOT NULL DEFAULT 'Y',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_api_endpoint_versions_definition_version UNIQUE (api_definition_id, version),
    CONSTRAINT chk_api_endpoint_versions_is_current CHECK (is_current IN ('Y', 'N'))
);

CREATE INDEX IF NOT EXISTS idx_api_endpoint_versions_definition
    ON pgdev.api_endpoint_versions(api_definition_id);

-- API 요청/응답 필드 정의
CREATE TABLE IF NOT EXISTS pgdev.api_endpoint_fields (
    id BIGSERIAL PRIMARY KEY,
    api_version_id BIGINT NOT NULL REFERENCES pgdev.api_endpoint_versions(id) ON DELETE CASCADE,
    field_scope VARCHAR(20) NOT NULL,
    field_location VARCHAR(20) NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    field_type VARCHAR(50) NOT NULL,
    required_yn CHAR(1) NOT NULL DEFAULT 'N',
    field_order INT NOT NULL DEFAULT 1,
    description TEXT,
    sample_value VARCHAR(500),
    default_value VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_api_endpoint_fields_required CHECK (required_yn IN ('Y', 'N')),
    CONSTRAINT chk_api_endpoint_fields_scope CHECK (field_scope IN ('REQUEST', 'RESPONSE', 'HEADER')),
    CONSTRAINT chk_api_endpoint_fields_location CHECK (field_location IN ('BODY', 'QUERY', 'PATH', 'HEADER'))
);

CREATE INDEX IF NOT EXISTS idx_api_endpoint_fields_version
    ON pgdev.api_endpoint_fields(api_version_id);

CREATE INDEX IF NOT EXISTS idx_api_endpoint_fields_order
    ON pgdev.api_endpoint_fields(api_version_id, field_scope, field_order);

-- API 정의 시드
INSERT INTO pgdev.api_definitions (api_code, api_name, created_at, updated_at)
VALUES
('PAYMENT_READY', '결제 요청', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('PAYMENT_CANCEL', '결제 취소', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('PAYMENT_STATUS', '결제 상태 조회', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (api_code) DO NOTHING;

-- API 버전 시드
INSERT INTO pgdev.api_endpoint_versions (
    api_definition_id, version, endpoint, http_method, status, description, is_current, created_at, updated_at
)
SELECT id, 'v1.0.0', '/api/pay/ready', 'POST', '정상 운영', '결제 준비 요청 API입니다. 문서상 RESPONSE는 최종 결제 결과 기준으로 정의하며, kakaoPay 선택 시 최초 응답으로 next_redirect_pc_url이 반환됩니다.', 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_definitions
WHERE api_code = 'PAYMENT_READY'
ON CONFLICT (api_definition_id, version) DO NOTHING;

INSERT INTO pgdev.api_endpoint_versions (
    api_definition_id, version, endpoint, http_method, status, description, is_current, created_at, updated_at
)
SELECT id, 'v1.0.0', '/api/pay/cancel', 'POST', '정상 운영', '주문 번호 기준으로 결제 취소를 수행합니다.', 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_definitions
WHERE api_code = 'PAYMENT_CANCEL'
ON CONFLICT (api_definition_id, version) DO NOTHING;

INSERT INTO pgdev.api_endpoint_versions (
    api_definition_id, version, endpoint, http_method, status, description, is_current, created_at, updated_at
)
SELECT id, 'v1.0.0', '/api/pay/status/{orderId}', 'GET', '정상 운영', '주문 번호 기준으로 결제 상태를 조회합니다.', 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_definitions
WHERE api_code = 'PAYMENT_STATUS'
ON CONFLICT (api_definition_id, version) DO NOTHING;

-- 결제 요청 API 입력 필드
INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'QUERY', 'paymentMethodId', 'String',
       'Y', 1, '결제 수단 식별자(kakaoPay 또는 tossPay)', 'tossPay', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'QUERY', 'orderId', 'String',
       'N', 2, '가맹점 주문 번호. 없으면 서버에서 UUID 생성', 'ORD-20260406-0001', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'QUERY', 'userId', 'String',
       'Y', 3, '가맹점 사용자 ID', 'user01', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'QUERY', 'itemName', 'String',
       'Y', 4, '상품명', '테스트 상품', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'QUERY', 'amount', 'Integer',
       'Y', 5, '결제 금액', '10000', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'QUERY', 'approvalUrl', 'String',
       'N', 6, '결제 성공 후 이동할 URL', 'https://merchant.example/success', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'QUERY', 'cancelUrl', 'String',
       'N', 7, '결제 취소 후 이동할 URL', 'https://merchant.example/cancel', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'QUERY', 'failUrl', 'String',
       'N', 8, '결제 실패 후 이동할 URL', 'https://merchant.example/fail', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

-- 결제 요청 API 출력 필드
INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'orderId', 'String',
       'Y', 1, '최종 결제 결과 기준 주문 번호', 'ORD-20260406-0001', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'paymentMethodId', 'String',
       'Y', 2, '최종 결제 결과 기준 결제 수단 식별자', 'kakaoPay', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'paymentId', 'String',
       'Y', 3, '최종 결제 결과 기준 PG 거래 고유 키', 'T1234567890', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'status', 'String',
       'Y', 4, '최종 결제 결과 기준 상태값', 'APPROVED', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'amount', 'Integer',
       'Y', 5, '최종 결제 승인 금액', '10000', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'approvedAt', 'LocalDateTime',
       'Y', 6, '최종 결제 승인 시각', '2026-04-06T17:20:00', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'next_redirect_pc_url', 'String',
       'N', 7, '참고 필드입니다. kakaoPay 선택 시 최초 응답으로 외부 결제 페이지 URL이 반환됩니다. tossPay는 내부 checkout 브릿지 URL 흐름을 사용합니다.', 'https://online-pay.kakaopay.com/...', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

-- 결제 취소 API 입력 필드
INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'QUERY', 'orderId', 'String',
       'Y', 1, '취소할 주문 번호', 'ORD-20260406-0001', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_CANCEL' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'QUERY', 'cancelAmount', 'Integer',
       'Y', 2, '취소 금액', '1000', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_CANCEL' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'QUERY', 'cancelReason', 'String',
       'N', 3, '취소 사유', '고객 변심', '고객 변심', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_CANCEL' AND v.version = 'v1.0.0';

-- 결제 취소 API 출력 필드
INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'orderId', 'String',
       'Y', 1, '가맹점 주문 번호', 'ORD-20260406-0001', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_CANCEL' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'paymentMethodId', 'String',
       'Y', 2, '결제 수단 식별자', 'kakaoPay', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_CANCEL' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'status', 'String',
       'Y', 3, '취소 처리 상태', 'CANCELED', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_CANCEL' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'cancelAmount', 'Integer',
       'Y', 4, '이번 요청으로 취소된 금액', '1000', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_CANCEL' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'remainAmount', 'Integer',
       'Y', 5, '취소 후 남은 금액', '9000', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_CANCEL' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'canceledAt', 'String',
       'Y', 6, '취소 승인 일시', '2026-04-06T16:00:00', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_CANCEL' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'paymentId', 'String',
       'Y', 7, 'PG 거래 고유 키', 'T1234567890', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_CANCEL' AND v.version = 'v1.0.0';

-- 결제 상태 조회 API 입력 필드
INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'PATH', 'orderId', 'String',
       'Y', 1, '조회할 주문 번호', 'ORD-20260406-0001', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_STATUS' AND v.version = 'v1.0.0';

-- 결제 상태 조회 API 출력 필드
INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'orderId', 'String',
       'Y', 1, '주문 번호', 'ORD-20260406-0001', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_STATUS' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'userId', 'String',
       'Y', 2, '가맹점 사용자 ID', 'user01', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_STATUS' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'amount', 'Long',
       'Y', 3, '결제 금액', '10000', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_STATUS' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'status', 'String',
       'Y', 4, '결제 상태', 'READY', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_STATUS' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'paymentMethodId', 'String',
       'Y', 5, '결제 수단 식별자', 'tossPay', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_STATUS' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'paymentId', 'String',
       'Y', 6, 'PG 거래 고유 키', 'PAYMENT_KEY_12345', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_STATUS' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'createdAt', 'LocalDateTime',
       'Y', 7, '결제 생성 시각', '2026-04-06T15:30:00', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_STATUS' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'approvalAt', 'LocalDateTime',
       'Y', 8, '결제 승인 시각', '2026-04-06T15:35:00', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_STATUS' AND v.version = 'v1.0.0';
