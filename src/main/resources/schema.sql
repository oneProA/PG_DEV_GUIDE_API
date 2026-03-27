-- 결제 마스터 테이블
CREATE TABLE IF NOT EXISTS payments (
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
    approved_at TIMESTAMP
);

-- 카드 결제 상세 테이블
CREATE TABLE IF NOT EXISTS payment_card_details (
    payment_id BIGINT PRIMARY KEY REFERENCES payments(id),
    issuer VARCHAR(50),
    card_number VARCHAR(20),
    installment_month INT DEFAULT 0
);

-- 취소 내역 테이블
CREATE TABLE IF NOT EXISTS cancellations (
    id BIGSERIAL PRIMARY KEY,
    cancel_id VARCHAR(50) UNIQUE NOT NULL,
    payment_id BIGINT NOT NULL REFERENCES payments(id),
    cancel_amount DECIMAL(19, 2) NOT NULL,
    remained_amount DECIMAL(19, 2) NOT NULL,
    cancel_reason TEXT,
    cancelled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
