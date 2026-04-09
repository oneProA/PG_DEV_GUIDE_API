-- Add 20 answered support inquiries for PAYMENT_ERROR on 2026-04-04.
-- - Fixed date: 2026-04-04
-- - Status: ANSWERED
-- - Category: PAYMENT_ERROR
-- - Safe to re-run (ON CONFLICT DO NOTHING on inquiry_no)

WITH target_users AS (
    SELECT user_id
    FROM pgdev.users
    WHERE role = 'USER'
    ORDER BY user_id
    LIMIT 3
),
base AS (
    SELECT
        gs AS seq,
        (ARRAY(SELECT user_id FROM target_users))[((gs - 1) % 3) + 1] AS user_id,
        (
            DATE '2026-04-04'
            + make_interval(hours => 9 + ((gs - 1) % 9), mins => (gs * 11) % 60)
        )::timestamp AS created_at
    FROM generate_series(1, 20) AS gs
)
INSERT INTO pgdev.support_inquiries (
    inquiry_no,
    user_id,
    category_code,
    title,
    content_text,
    answer_content_text,
    status,
    priority,
    has_attachments,
    view_count,
    created_at,
    updated_at,
    answered_at
)
SELECT
    FORMAT('INQ-APR04-ANS-%s', LPAD(seq::text, 4, '0')) AS inquiry_no,
    user_id,
    'PAYMENT_ERROR' AS category_code,
    FORMAT('[테스트] 2026-04-04 결제/승인 오류 답변완료 더미 %s', seq) AS title,
    FORMAT(
        '2026-04-04 결제/승인 오류 문의 더미 %s번입니다.%s상태는 ANSWERED 입니다.',
        seq,
        E'\n'
    ) AS content_text,
    FORMAT('더미 답변 %s번: 결제/승인 오류 처리 완료되었습니다.', seq) AS answer_content_text,
    'ANSWERED' AS status,
    'LOW' AS priority,
    CASE
        WHEN seq % 4 = 0 THEN 'Y'
        ELSE 'N'
    END AS has_attachments,
    (seq * 3) % 21 AS view_count,
    created_at,
    created_at + INTERVAL '30 minutes' AS updated_at,
    created_at + INTERVAL '1 hour' AS answered_at
FROM base
WHERE user_id IS NOT NULL
ON CONFLICT (inquiry_no) DO NOTHING;
