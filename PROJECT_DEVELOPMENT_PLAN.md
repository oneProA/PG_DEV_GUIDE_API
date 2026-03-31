# 프로젝트 이관 리뷰 및 향후 개발 계획서 (Project Development Plan)

본 문서는 기획안(`cjpg.txt`)의 프로젝트 이관 결과에 대한 기술적 리뷰와, 이를 실제 서비스로 고도화하기 위한 상세 개발 로직 및 업무 분담 계획을 담고 있습니다.

---

## 1. 현재 구현 상태 리뷰 (Current State Review)

현재 `PG_DEV_GUIDE_VIEW`와 `PG_DEV_GUIDE_API`는 기획안의 디자인과 구조를 프로젝트 틀에 이관한 **'Mock-up'** 단계입니다.

### 1.1 프런트엔드 (PG_DEV_GUIDE_VIEW)
*   **완료 사항**:
    *   Tailwind CSS 기반의 전역 디자인 시스템(Color, Font) 설정 완료.
    *   공통 레이아웃(`Navbar`, `Footer`, `Sidebar`) 컴포넌트화 완료.
    *   주요 페이지(`Home`, `API Docs`, `Playground`, `Support`)의 UI 구조 및 라우팅 설정 완료.
    *   ✅ **사용자 인증**: Zustand 기반 로그인/로그아웃 기능 구현 완료 (`useAuth.ts`, `LoginDialog.tsx`).
    *   ✅ **역할 기반 UI**: ADMIN 역할 사용자 자동 리다이렉트 및 조건부 네비게이션 구현 완료.
    *   ✅ **관리자 포털**: Admin Dashboard, 문의관리, 회원관리, API관리 4개 페이지 레퍼런스 디자인 기반 구축 완료.
*   **미비 사항 (Hardcoded)**:
    *   **데이터 연동**: API 문서 및 지원 센터의 데이터가 하드코딩된 정적 텍스트임.
    *   **기능 로직**: `Playground`의 API 호출 기능이 실제 백엔드와 연동되지 않음 (UI만 존재).

### 1.2 백엔드 (PG_DEV_GUIDE_API)
*   **완료 사항**:
    *   기획안의 API 규격에 따른 DTO(Data Transfer Object) 정의 완료.
    *   핵심 엔드포인트(`Payment`, `Cancel`, `Status`) 컨트롤러 생성 완료.
    *   ✅ **인증 API**: `/auth/login` 엔드포인트 구현 완료 (`AuthController.kt`, `AuthService.kt`).
    *   ✅ **DB 연동**: `pgdev.users` 테이블 조회를 통한 사용자 검증 및 역할(role) 기반 로그인 응답 구현.
    *   ✅ **사용자 정보**: 로그인 응답에 `username`, `email`, `role` 포함하도록 DTO 확장 완료.
*   **미비 사항 (Hardcoded)**:
    *   **비즈니스 로직**: 실제 결제 처리 로직 없이 랜덤 ID와 성공 메시지만 반환함.
    *   **인증/인가**: 비밀번호 검증 및 JWT 토큰 기반 인가 로직이 미구현 (현재 username 존재 여부만 확인).

---

## 2. 향후 개발 태스크 (Development Roadmap)

개발자들이 각자 분담하여 처리할 수 있도록 태스크를 세분화하였습니다.

### Phase 1: 인프라 및 환경 구축 (Infrastructure)
* [x] **DB 스키마 설계**: PostgreSQL 기반의 `pgdev.users` 테이블 연동 완료.
* [x] **MyBatis 설정 및 Mapper 작성**: `UserMapper.xml`을 통한 사용자 조회 SQL 구현 완료.
*   [x] **CORS 설정**: 프런트엔드와 백엔드 간의 원활한 통신을 위한 보안 설정 완료.

### Phase 2: 핵심 API 기능 구현 (Backend Focus)
*   [ ] **결제 로직 연동**: 가짜 응답 대신 실제 로직(DB 저장 및 검증)을 수행하도록 컨트롤러 및 서비스 고도화.
*   [ ] **지원 센터 CRUD**: 문의 등록 및 리스트 조회를 위한 실제 데이터 처리 API 구현.
*   [x] **인증 API**: `/auth/login` 엔드포인트 구현 완료.
*   [ ] **API 보안**: JWT/Bearer Token 기반의 인증 시스템 및 비밀번호 검증 구축.

### Phase 3: 프런트엔드 기능 고도화 (Frontend Focus)
*   [ ] **API 연동 (Axios/React Query)**: 하드코딩된 데이터를 백엔드 API 호출로 대체.
*   [ ] **Playground 실동작**: 사용자가 입력한 데이터로 실제 백엔드를 호출하고 결과를 출력하도록 구현.
*   [x] **상태 관리**: Zustand 기반 전역 로그인 상태 및 유저 정보 관리 구현 완료.

### Phase 4: 안정성 및 품질 관리 (QA & Docs)
*   [ ] **예외 처리**: API 에러 발생 시 사용자에게 적절한 피드백(Toast, Modal) 제공.
*   [ ] **테스트**: 주요 비즈니스 로직에 대한 단위 테스트 및 통합 테스트 수행.

### Phase 5: 관리자 포털 구축 (Admin Portal) ✅ 완료
*   [x] **Admin 대시보드**: KPI 카드, SVG 막대/도넛 차트, 미처리 문의 리스트 구현.
*   [x] **문의관리**: 문의 리스트 + 상세보기/답변 작성 2컬럼 레이아웃 구현.
*   [x] **회원관리**: 회원 테이블 + 상세 정보 수정 사이드 패널(프로필, Role, 활동 로그) 구현.
*   [x] **API관리**: KPI 카드, API 목록 테이블, 상세 등록/수정 폼(파라미터 테이블 포함) 구현.
*   [x] **Sidebar 리디자인**: Admin Console 로고, 메뉴 구조, 프로필 영역(로그인 사용자 정보 동적 표시).
*   [x] **Navbar 조건부 렌더링**: Admin 모드에서 API/Support 숨김, 로고 클릭 비활성화, 검색바 Admin 전용.
*   [x] **역할 기반 라우팅**: ADMIN 역할 로그인 시 자동으로 Admin 대시보드로 리다이렉트.

---

## 3. 업무 분담 가이드 (Role Assignment)

| 담당 파트 | 주요 작업 범위 | 관련 디렉터리 |
| :--- | :--- | :--- |
| **Backend Dev** | DB 설계, API 비즈니스 로직 구현, 보안 설정 | `PG_DEV_GUIDE_API/src/main/kotlin` |
| **Frontend Dev** | API 연동, 동적 UI 구현, 상태 관리, 폼 검증 | `PG_DEV_GUIDE_VIEW/src/pages`, `api` |
| **Architect/Lead** | 전체 데이터 흐름 관리, 기술 스택 확정, 코드 리뷰 | 프로젝트 루트 및 `GEMINI.md` |

---

## 4. 검토 및 피드백 요청

본 계획서는 현재의 Mock 구조를 실제 서비스로 전환하기 위한 최소한의 가이드라인입니다. 개발팀은 이 문서를 바탕으로 세부 구현 계획을 수립하시기 바랍니다.
