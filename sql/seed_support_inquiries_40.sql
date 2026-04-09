-- Add 40 support inquiries for paging/date-range testing.
-- - Date range: recent 10 days
-- - Status mix: RECEIVED / IN_PROGRESS / ANSWERED
-- - Category mix: PAYMENT_ERROR / API_INTEGRATION / ACCOUNT_PERMISSION / ETC
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
        (ARRAY['PAYMENT_ERROR', 'API_INTEGRATION', 'ACCOUNT_PERMISSION', 'ETC'])[((gs - 1) % 4) + 1] AS category_code,
        (ARRAY['RECEIVED', 'IN_PROGRESS', 'ANSWERED'])[((gs - 1) % 3) + 1] AS status,
        (
            CURRENT_DATE
            - (((gs - 1) % 10)::text || ' days')::interval
            + make_interval(hours => 9 + ((gs * 3) % 10), mins => (gs * 7) % 60)
        )::timestamp AS created_at
    FROM generate_series(1, 40) AS gs
),
inserted AS (
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
        FORMAT('INQ-BULK-%s', LPAD(seq::text, 4, '0')) AS inquiry_no,
        user_id,
        category_code,
        FORMAT('[테스트] 문의 더미 데이터 %s', seq) AS title,
        FORMAT(
            '페이징/기간 조회 테스트용 더미 문의 %s번입니다.%s현재 상태는 %s 입니다.',
            seq,
            E'\n',
            status
        ) AS content_text,
        CASE
            WHEN status = 'ANSWERED' THEN FORMAT('더미 답변 %s번: 처리 완료되었습니다.', seq)
            ELSE NULL
        END AS answer_content_text,
        status,
        CASE
            WHEN status = 'RECEIVED' THEN 'NORMAL'
            WHEN status = 'IN_PROGRESS' THEN 'HIGH'
            ELSE 'LOW'
        END AS priority,
        CASE
            WHEN seq % 5 = 0 THEN 'Y'
            ELSE 'N'
        END AS has_attachments,
        (seq * 2) % 17 AS view_count,
        created_at,
        created_at + INTERVAL '1 hour' AS updated_at,
        CASE
            WHEN status = 'ANSWERED' THEN created_at + INTERVAL '2 hours'
            ELSE NULL
        END AS answered_at
    FROM base
    WHERE user_id IS NOT NULL
    ON CONFLICT (inquiry_no) DO NOTHING
    RETURNING inquiry_id, user_id, inquiry_no, created_at
)
INSERT INTO pgdev.support_inquiry_files (
    inquiry_id,
    owner_type,
    file_role,
    original_file_name,
    stored_file_name,
    file_url,
    mime_type,
    file_size_bytes,
    inline_key,
    sort_order,
    uploaded_by_user_id,
    created_at
)
SELECT
    i.inquiry_id,
    'INQUIRY' AS owner_type,
    'ATTACHMENT' AS file_role,
    FORMAT('dummy_%s.png', i.inquiry_no) AS original_file_name,
    FORMAT('dummy_%s.png', i.inquiry_no) AS stored_file_name,
    FORMAT('https://example.com/dummy/%s.png', i.inquiry_no) AS file_url,
    'image/png' AS mime_type,
    102400 + (ROW_NUMBER() OVER (ORDER BY i.inquiry_id) * 100) AS file_size_bytes,
    NULL AS inline_key,
    1 AS sort_order,
    i.user_id AS uploaded_by_user_id,
    i.created_at + INTERVAL '10 minutes' AS created_at
FROM inserted i
WHERE i.inquiry_no LIKE '%0'
ON CONFLICT DO NOTHING;
