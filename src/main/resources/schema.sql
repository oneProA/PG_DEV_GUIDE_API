-- ?뚯씠釉?珥덇린??(?꾩슂 ??
CREATE SCHEMA IF NOT EXISTS pgdev;

DROP TABLE IF EXISTS pgdev.payment_card_details CASCADE;
DROP TABLE IF EXISTS pgdev.cancellations CASCADE;
DROP TABLE IF EXISTS pgdev.payments CASCADE;
DROP TABLE IF EXISTS pgdev.users CASCADE;
DROP TABLE IF EXISTS pgdev.api_endpoints CASCADE;
DROP TABLE IF EXISTS pgdev.api_endpoint_fields CASCADE;
DROP TABLE IF EXISTS pgdev.api_endpoint_versions CASCADE;
DROP TABLE IF EXISTS pgdev.api_definitions CASCADE;

-- 寃곗젣 留덉뒪???뚯씠釉?
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

-- 移대뱶 寃곗젣 ?곸꽭 ?뚯씠釉?
CREATE TABLE IF NOT EXISTS pgdev.payment_card_details (
    payment_id BIGINT PRIMARY KEY REFERENCES pgdev.payments(id),
    issuer VARCHAR(50),
    card_number VARCHAR(20),
    installment_month INT DEFAULT 0,
    CONSTRAINT chk_payment_card_details_installment_month_nonnegative CHECK (installment_month >= 0)
);

-- 痍⑥냼 ?댁뿭 ?뚯씠釉?
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

-- ?ъ슜???뺣낫
CREATE TABLE IF NOT EXISTS pgdev.users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password TEXT NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    name VARCHAR(100),
    phone VARCHAR(30),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    profile_image_url TEXT,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_username ON pgdev.users(username);

CREATE TABLE IF NOT EXISTS pgdev.user_activity_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES pgdev.users(user_id) ON DELETE CASCADE,
    activity_type VARCHAR(50) NOT NULL,
    activity_title VARCHAR(200) NOT NULL,
    activity_detail TEXT,
    actor_username VARCHAR(50),
    ip_address VARCHAR(50),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_activity_logs_user_id ON pgdev.user_activity_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_user_activity_logs_created_at ON pgdev.user_activity_logs(created_at DESC);

-- API ?붾뱶?ъ씤??愿由??뚯씠釉?
CREATE TABLE IF NOT EXISTS pgdev.api_endpoints (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    endpoint VARCHAR(255) NOT NULL UNIQUE,
    version VARCHAR(20) NOT NULL,
    display_order INT NOT NULL DEFAULT 999,
    status VARCHAR(50) NOT NULL DEFAULT '?뺤긽 ?댁쁺',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_api_endpoints_endpoint ON pgdev.api_endpoints(endpoint);
CREATE INDEX IF NOT EXISTS idx_api_endpoints_status ON pgdev.api_endpoints(status);

-- ?섑뵆 API ?붾뱶?ъ씤???곗씠??(珥덇린 紐⑹뾽)
INSERT INTO pgdev.api_endpoints (name, http_method, endpoint, version, display_order, status, description, created_at, updated_at)
VALUES 
('寃곗젣 ?붿껌', 'POST', '/v1/payments/request', 'v1.0.0', 1, '?뺤긽 ?댁쁺', '寃곗젣瑜??붿껌?⑸땲??', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('寃곗젣 ?곹깭 議고쉶', 'GET', '/v1/payments/status/:paymentId', 'v1.0.0', 3, '?뺤긽 ?댁쁺', '寃곗젣 ?뺣낫 諛?痍⑥냼 ?댁뿭??議고쉶?⑸땲??', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('寃곗젣 痍⑥냼', 'POST', '/v1/payments/cancel', 'v1.0.0', 2, '?뺤긽 ?댁쁺', '寃곗젣 ?꾩껜 ?먮뒗 遺遺?痍⑥냼瑜?泥섎━?⑸땲??', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- API ?뺤쓽 留덉뒪??CREATE TABLE IF NOT EXISTS pgdev.api_definitions (
    id BIGSERIAL PRIMARY KEY,
    api_code VARCHAR(100) NOT NULL UNIQUE,
    api_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- API 踰꾩쟾蹂??ㅽ럺
CREATE TABLE IF NOT EXISTS pgdev.api_endpoint_versions (
    id BIGSERIAL PRIMARY KEY,
    api_definition_id BIGINT NOT NULL REFERENCES pgdev.api_definitions(id),
    version VARCHAR(20) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT '?뺤긽 ?댁쁺',
    description TEXT,
    is_current CHAR(1) NOT NULL DEFAULT 'Y',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_api_endpoint_versions_definition_version UNIQUE (api_definition_id, version),
    CONSTRAINT chk_api_endpoint_versions_is_current CHECK (is_current IN ('Y', 'N'))
);

CREATE INDEX IF NOT EXISTS idx_api_endpoint_versions_definition
    ON pgdev.api_endpoint_versions(api_definition_id);

-- API ?붿껌/?묐떟 ?꾨뱶 ?뺤쓽
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

-- API ?뺤쓽 ?쒕뱶
INSERT INTO pgdev.api_definitions (api_code, api_name, created_at, updated_at)
VALUES
('PAYMENT_READY', '寃곗젣 ?붿껌', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('PAYMENT_CANCEL', '寃곗젣 痍⑥냼', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('PAYMENT_STATUS', '寃곗젣 ?곹깭 議고쉶', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (api_code) DO NOTHING;

-- API 踰꾩쟾 ?쒕뱶
INSERT INTO pgdev.api_endpoint_versions (
    api_definition_id, version, endpoint, http_method, display_order, status, description, is_current, created_at, updated_at
)
SELECT id, 'v1.0.0', '/api/pay/ready', 'POST', 1, '?뺤긽 ?댁쁺', '寃곗젣 以鍮??붿껌 API?낅땲?? 臾몄꽌??RESPONSE??理쒖쥌 寃곗젣 寃곌낵 湲곗??쇰줈 ?뺤쓽?섎ŉ, kakaoPay ?좏깮 ??理쒖큹 ?묐떟?쇰줈 next_redirect_pc_url??諛섑솚?⑸땲??', 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_definitions
WHERE api_code = 'PAYMENT_READY'
ON CONFLICT (api_definition_id, version) DO NOTHING;

INSERT INTO pgdev.api_endpoint_versions (
    api_definition_id, version, endpoint, http_method, display_order, status, description, is_current, created_at, updated_at
)
SELECT id, 'v1.0.0', '/api/pay/cancel', 'POST', 2, '?뺤긽 ?댁쁺', '二쇰Ц 踰덊샇 湲곗??쇰줈 寃곗젣 痍⑥냼瑜??섑뻾?⑸땲??', 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_definitions
WHERE api_code = 'PAYMENT_CANCEL'
ON CONFLICT (api_definition_id, version) DO NOTHING;

INSERT INTO pgdev.api_endpoint_versions (
    api_definition_id, version, endpoint, http_method, display_order, status, description, is_current, created_at, updated_at
)
SELECT id, 'v1.0.0', '/api/pay/status/{orderId}', 'GET', 3, '?뺤긽 ?댁쁺', '二쇰Ц 踰덊샇 湲곗??쇰줈 寃곗젣 ?곹깭瑜?議고쉶?⑸땲??', 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_definitions
WHERE api_code = 'PAYMENT_STATUS'
ON CONFLICT (api_definition_id, version) DO NOTHING;

-- 寃곗젣 ?붿껌 API ?낅젰 ?꾨뱶
INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'QUERY', 'paymentMethodId', 'String',
       'Y', 1, '寃곗젣 ?섎떒 ?앸퀎??kakaoPay ?먮뒗 tossPay)', 'tossPay', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'QUERY', 'orderId', 'String',
       'N', 2, '媛留뱀젏 二쇰Ц 踰덊샇. ?놁쑝硫??쒕쾭?먯꽌 UUID ?앹꽦', 'ORD-20260406-0001', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'QUERY', 'userId', 'String',
       'Y', 3, '媛留뱀젏 ?ъ슜??ID', 'user01', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'QUERY', 'itemName', 'String',
       'Y', 4, '?곹뭹紐?, '?뚯뒪???곹뭹', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'QUERY', 'amount', 'Integer',
       'Y', 5, '寃곗젣 湲덉븸', '10000', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'QUERY', 'approvalUrl', 'String',
       'N', 6, '寃곗젣 ?깃났 ???대룞??URL', 'https://merchant.example/success', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'QUERY', 'cancelUrl', 'String',
       'N', 7, '寃곗젣 痍⑥냼 ???대룞??URL', 'https://merchant.example/cancel', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'QUERY', 'failUrl', 'String',
       'N', 8, '寃곗젣 ?ㅽ뙣 ???대룞??URL', 'https://merchant.example/fail', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

-- 寃곗젣 ?붿껌 API 異쒕젰 ?꾨뱶
INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'orderId', 'String',
       'Y', 1, '理쒖쥌 寃곗젣 寃곌낵 湲곗? 二쇰Ц 踰덊샇', 'ORD-20260406-0001', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'paymentMethodId', 'String',
       'Y', 2, '理쒖쥌 寃곗젣 寃곌낵 湲곗? 寃곗젣 ?섎떒 ?앸퀎??, 'kakaoPay', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'paymentId', 'String',
       'Y', 3, '理쒖쥌 寃곗젣 寃곌낵 湲곗? PG 嫄곕옒 怨좎쑀 ??, 'T1234567890', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'status', 'String',
       'Y', 4, '理쒖쥌 寃곗젣 寃곌낵 湲곗? ?곹깭媛?, 'APPROVED', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'amount', 'Integer',
       'Y', 5, '理쒖쥌 寃곗젣 ?뱀씤 湲덉븸', '10000', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'approvedAt', 'LocalDateTime',
       'Y', 6, '理쒖쥌 寃곗젣 ?뱀씤 ?쒓컖', '2026-04-06T17:20:00', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'next_redirect_pc_url', 'String',
       'N', 7, '李멸퀬 ?꾨뱶?낅땲?? kakaoPay ?좏깮 ??理쒖큹 ?묐떟?쇰줈 ?몃? 寃곗젣 ?섏씠吏 URL??諛섑솚?⑸땲?? tossPay???대? checkout 釉뚮┸吏 URL ?먮쫫???ъ슜?⑸땲??', 'https://online-pay.kakaopay.com/...', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_READY' AND v.version = 'v1.0.0';

-- 寃곗젣 痍⑥냼 API ?낅젰 ?꾨뱶
INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'QUERY', 'orderId', 'String',
       'Y', 1, '痍⑥냼??二쇰Ц 踰덊샇', 'ORD-20260406-0001', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_CANCEL' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'QUERY', 'cancelAmount', 'Integer',
       'Y', 2, '痍⑥냼 湲덉븸', '1000', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_CANCEL' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'QUERY', 'cancelReason', 'String',
       'N', 3, '痍⑥냼 ?ъ쑀', '怨좉컼 蹂??, '怨좉컼 蹂??, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_CANCEL' AND v.version = 'v1.0.0';

-- 寃곗젣 痍⑥냼 API 異쒕젰 ?꾨뱶
INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'orderId', 'String',
       'Y', 1, '媛留뱀젏 二쇰Ц 踰덊샇', 'ORD-20260406-0001', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_CANCEL' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'paymentMethodId', 'String',
       'Y', 2, '寃곗젣 ?섎떒 ?앸퀎??, 'kakaoPay', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_CANCEL' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'status', 'String',
       'Y', 3, '痍⑥냼 泥섎━ ?곹깭', 'CANCELED', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_CANCEL' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'cancelAmount', 'Integer',
       'Y', 4, '?대쾲 ?붿껌?쇰줈 痍⑥냼??湲덉븸', '1000', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_CANCEL' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'remainAmount', 'Integer',
       'Y', 5, '痍⑥냼 ???⑥? 湲덉븸', '9000', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_CANCEL' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'canceledAt', 'String',
       'Y', 6, '痍⑥냼 ?뱀씤 ?쇱떆', '2026-04-06T16:00:00', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_CANCEL' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'paymentId', 'String',
       'Y', 7, 'PG 嫄곕옒 怨좎쑀 ??, 'T1234567890', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_CANCEL' AND v.version = 'v1.0.0';

-- 寃곗젣 ?곹깭 議고쉶 API ?낅젰 ?꾨뱶
INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'REQUEST', 'PATH', 'orderId', 'String',
       'Y', 1, '議고쉶??二쇰Ц 踰덊샇', 'ORD-20260406-0001', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_STATUS' AND v.version = 'v1.0.0';

-- 寃곗젣 ?곹깭 議고쉶 API 異쒕젰 ?꾨뱶
INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'orderId', 'String',
       'Y', 1, '二쇰Ц 踰덊샇', 'ORD-20260406-0001', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_STATUS' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'userId', 'String',
       'Y', 2, '媛留뱀젏 ?ъ슜??ID', 'user01', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_STATUS' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'amount', 'Long',
       'Y', 3, '寃곗젣 湲덉븸', '10000', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_STATUS' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'status', 'String',
       'Y', 4, '寃곗젣 ?곹깭', 'READY', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_STATUS' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'paymentMethodId', 'String',
       'Y', 5, '寃곗젣 ?섎떒 ?앸퀎??, 'tossPay', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_STATUS' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'paymentId', 'String',
       'Y', 6, 'PG 嫄곕옒 怨좎쑀 ??, 'PAYMENT_KEY_12345', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_STATUS' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'createdAt', 'LocalDateTime',
       'Y', 7, '寃곗젣 ?앹꽦 ?쒓컖', '2026-04-06T15:30:00', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_STATUS' AND v.version = 'v1.0.0';

INSERT INTO pgdev.api_endpoint_fields (
    api_version_id, field_scope, field_location, field_name, field_type,
    required_yn, field_order, description, sample_value, default_value, created_at, updated_at
)
SELECT v.id, 'RESPONSE', 'BODY', 'approvalAt', 'LocalDateTime',
       'Y', 8, '寃곗젣 ?뱀씤 ?쒓컖', '2026-04-06T15:35:00', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM pgdev.api_endpoint_versions v
JOIN pgdev.api_definitions d ON d.id = v.api_definition_id
WHERE d.api_code = 'PAYMENT_STATUS' AND v.version = 'v1.0.0';

