SET search_path TO pgdev;

-- 결제 1건 생성
INSERT INTO payments (
    payment_id,
    tid,
    mid,
    order_id,
    amount,
    status,
    payment_method,
    goods_name,
    created_at,
    approved_at
) VALUES (
    'CJ_ORD_00000001',
    'T202603270001',
    'MID_1001',
    'ORDER_20260327_0001',
    45000.00,
    'PAID',
    'CARD',
    '프리미엄 구독권',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '5 minutes'
);

INSERT INTO payment_card_details (
    payment_id,
    issuer,
    card_number,
    installment_month
)
SELECT
    id,
    'CJ_CARD',
    '4518-****-****-1234',
    0
FROM payments
WHERE payment_id = 'CJ_ORD_00000001';

INSERT INTO cancellations (
    cancel_id,
    payment_id,
    cancel_amount,
    remained_amount,
    cancel_reason,
    cancelled_at
)
SELECT
    'CXL_20260327_0001',
    id,
    10000.00,
    35000.00,
    '고객 요청 부분 취소',
    CURRENT_TIMESTAMP - INTERVAL '12 hours'
FROM payments
WHERE payment_id = 'CJ_ORD_00000001';

UPDATE payments
SET status = 'PARTIAL_CANCELLED'
WHERE payment_id = 'CJ_ORD_00000001';

-- 결제 1건 추가 생성
INSERT INTO payments (
    payment_id,
    tid,
    mid,
    order_id,
    amount,
    status,
    payment_method,
    goods_name,
    created_at,
    approved_at
) VALUES (
    'CJ_ORD_00000002',
    'T202603270002',
    'MID_1002',
    'ORDER_20260327_0002',
    99000.00,
    'PAID',
    'CARD',
    '정기 결제 상품',
    CURRENT_TIMESTAMP - INTERVAL '6 hours',
    CURRENT_TIMESTAMP - INTERVAL '6 hours' + INTERVAL '3 minutes'
);

INSERT INTO payment_card_details (
    payment_id,
    issuer,
    card_number,
    installment_month
)
SELECT
    id,
    'CJ_CARD',
    '5399-****-****-5678',
    3
FROM payments
WHERE payment_id = 'CJ_ORD_00000002';

INSERT INTO users (username, password, email, role)
VALUES (
    'demo.user',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoOa7vVByDo.bFQJz78zE0yB/3Q1D6spKa',
    'demo.user@cjone.com',
    'ADMIN'
);
