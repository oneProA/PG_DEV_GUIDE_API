# 상세 기능 구현 설계서 (Implementation Detail Design)

본 문서는 `PG_DEV_GUIDE` 프로젝트의 미구현 기능(로그인, 버튼 로직) 및 DB 연동(MyBatis)을 위한 기술 설계안입니다. **(소스 코드 수정 전 설계 단계)**

---

## 1. 데이터베이스 스키마 확장 (PostgreSQL)

현재 `schema.sql`에 정의된 결제 관련 테이블 외에, 서비스 운영에 필요한 사용자 및 문의 테이블을 추가 정의합니다.

### 1.1 사용자 테이블 (`users`)
- 목적: 관리자/사용자 로그인 인증 및 권한 관리.
- 구조:
    - `id`: SERIAL (PK)
    - `username`: VARCHAR(50) (Unique, ID)
    - `password`: VARCHAR(255) (암호화된 비밀번호)
    - `role`: VARCHAR(20) (ADMIN, USER)
    - `created_at`: TIMESTAMP

### 1.2 문의 게시판 테이블 (`inquiries`)
- 목적: 고객 지원 센터 기능 지원.
- 구조:
    - `id`: SERIAL (PK)
    - `user_id`: INT (FK -> users.id)
    - `title`, `content`, `category`: VARCHAR/TEXT
    - `status`: VARCHAR (OPEN, CLOSED)

---

## 2. 백엔드 로직 설계 (PG_DEV_GUIDE_API)

### 2.1 MyBatis XML 매퍼 구현 방향
- **PaymentMapper.xml**: `insertPayment`, `findByPaymentId` 등 기존 설계된 쿼리의 실제 동작 검증.
- **UserMapper.xml (신규)**:
    - `findByUsername`: 입력된 ID로 유저 정보를 조회하여 비밀번호 검증 로직 지원.
- **InquiryMapper.xml (신규)**:
    - `insertInquiry`: 문의 내용 저장.
    - `findAllByUserId`: 특정 사용자의 문의 내역 조회.

### 2.2 인증(Auth) API 설계
- `POST /api/auth/login`: 
    - 로직: `UserMapper`를 통해 유저 조회 -> 비밀번호 일치 확인 -> JWT 토큰 생성 및 반환.
- `POST /api/auth/logout`: 클라이언트 측 토큰 삭제 유도.

---

## 3. 프런트엔드 UI 로직 수리 설계 (PG_DEV_GUIDE_VIEW)

### 3.1 로그인 버튼 (Navbar.tsx)
- **문제점**: 현재 버튼이 단순 정적 요소로 구현되어 클릭 시 반응 없음.
- **수리 설계**:
    1.  `LoginModal.tsx` 컴포넌트 설계: ID/PW 입력 필드 및 로그인 요청 함수 포함.
    2.  `Navbar.tsx` 연동: "로그인" 버튼 클릭 시 `isLoginModalOpen` 상태를 `true`로 변경하여 모달 표시.
    3.  로그인 성공 시: 서버로부터 받은 토큰을 `localStorage`에 저장하고, 버튼을 "로그아웃" 또는 "사용자명"으로 변경.

### 3.2 Playground 결제 요청 버튼
- **수리 설계**:
    1.  사용자가 입력한 JSON 데이터를 `POST /api/payments`로 전송하는 `handlePaymentRequest` 함수 설계.
    2.  API 응답값을 하단의 `Response` 영역(JSON 뷰어)에 동적으로 렌더링하도록 `useState` 연동.

---

## 4. 향후 작업 로드맵

1.  **[문서]** 위 설계안에 따른 API 명세서(`API_SPEC.md`) 작성.
2.  **[DB]** `schema.sql`에 신규 테이블 정의 추가 (문서상).
3.  **[코드]** (사용자 승인 시) 위 설계에 따른 실제 구현 착수.

본 설계 내용이 의도하신 방향과 일치하는지 확인 부탁드립니다.
