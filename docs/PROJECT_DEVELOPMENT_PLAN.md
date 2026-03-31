# 프로젝트 이관 리뷰 및 향후 개발 계획서 (Project Development Plan)

본 문서는 기획안(`cjpg.txt`)의 프로젝트 이관 결과에 대한 기술적 리뷰와, 이를 실제 서비스로 고도화하기 위한 상세 개발 로직 및 업무 분담 계획을 담고 있습니다. (최종 업데이트: 2026-03-31)

---

## 1. 현재 구현 상태 리뷰 (Current State Review)

현재 `PG_DEV_GUIDE_VIEW`와 `PG_DEV_GUIDE_API`는 설계 단계를 넘어 **'Core Logic Implementation'** 단계에 진입했습니다.

### 1.1 프런트엔드 (PG_DEV_GUIDE_VIEW)
*   **완료 사항**:
    *   Tailwind CSS 기반의 전역 디자인 시스템 및 공통 레이아웃 구축 완료.
    *   주요 페이지(`Home`, `API Docs`, `Playground`, `Support`)의 라우팅 및 UI 구조화 완료.
    *   **UI 테스트**: Playwright를 이용한 UI 워크플로우 테스트 시나리오(`ui-payment-workflow.spec.ts`) 작성 완료.
*   **진행 중/미비 사항**:
    *   **Playground 실동작**: UI 시나리오는 준비되었으나, 실제 백엔드 API와의 런타임 연동 테스트 필요.
    *   **상태 관리**: Zustand 등을 활용한 전역 로그인 상태 및 유저 정보 관리 로직 고도화 필요.

### 1.2 백엔드 (PG_DEV_GUIDE_API)
*   **완료 사항**:
    *   **DB 설계**: `pgdev` 스키마 기반의 테이블(`payments`, `cancellations`, `users`, `inquiries`) 설계 완료 (`BACKEND_DESIGN.md`).
    *   **MyBatis 설정**: DTO 매핑 규칙 및 매퍼 인터페이스 설계 완료.
    *   **비즈니스 로직**: `PaymentService`의 핵심 로직(결제 취소, 잔액 검증 등) 구현 완료.
    *   **단위 테스트**: JUnit 5와 MockK를 이용한 서비스 레이어 단위 테스트 완료 (4개 테스트 모두 통과, `UNIT_TEST_REPORT.md`).
*   **진행 중/미비 사항**:
    *   **DB 연동**: 설계된 스키마에 따른 실제 MyBatis XML 매퍼 구현 및 DB 연동 테스트 필요.
    *   **인증/인가**: JWT 기반의 Bearer Token 발급 및 검증 인터셉터 구현 보완 필요.

---

## 2. 향후 개발 태스크 (Development Roadmap)

### Phase 1: 인프라 및 DB 연동 (Infrastructure & Persistence)
*   [x] **DB 스키마 설계**: `payments`, `cancellations`, `users`, `inquiries` 테이블 설계 완료.
*   [ ] **MyBatis XML 구현**: 설계된 매퍼 인터페이스에 대응하는 XML 쿼리 작성.
*   [ ] **DB 통합 테스트**: 실제 PostgreSQL 연동 후 CRUD 동작 확인.

### Phase 2: 핵심 API 및 보안 (Backend & Security)
*   [x] **비즈니스 로직 구현**: 결제 및 취소 핵심 서비스 로직 구현 및 단위 테스트 완료.
*   [ ] **인증 로직 고도화**: JWT 발급 및 Spring Security(또는 Interceptor) 연동.
*   [ ] **지원 센터 API**: 문의 등록 및 리스트 조회를 위한 실제 DB 연동 API 완성.

### Phase 3: 프런트엔드 연동 및 통합 (Frontend & Integration)
*   [ ] **로그인 버튼 수리**: `Navbar.tsx`에 `LoginModal` 연동 및 클릭 이벤트 추가.
*   [ ] **API 연동 (Axios/React Query)**: 하드코딩된 데이터를 백엔드 API 호출로 대체.
*   [ ] **Playground 기능 구현**: 결제 요청 버튼 클릭 시 실제 백엔드 연동 및 결과 렌더링.
*   [ ] **통합 테스트 수행**: 작성된 Playwright 시나리오를 바탕으로 실제 백엔드와의 연동 테스트 수행.
*   [ ] **에러 핸들링**: API 에러 발생 시 사용자에게 적절한 Toast/Modal 피드백 제공.

---

## 3. 업무 분담 가이드 (Role Assignment)

| 담당 파트 | 주요 작업 범위 | 관련 디렉터리 |
| :--- | :--- | :--- |
| **Backend Dev** | MyBatis XML 구현, DB 통합 테스트, 보안 설정 | `PG_DEV_GUIDE_API/src/main/kotlin` |
| **Frontend Dev** | API 연동, 동적 UI 구현, 상태 관리, 통합 테스트 | `PG_DEV_GUIDE_VIEW/src`, `tests` |
| **Architect/Lead** | 전체 데이터 흐름 관리, 기술 스택 확정, 코드 리뷰 | 프로젝트 루트 및 `GEMINI.md` |

---

## 4. 검토 및 피드백 요청

현재 설계와 단위 테스트는 완료되었으며, **'실제 DB 연동 및 통합 테스트'**가 가장 시급한 과제입니다. 이에 동의하시는지, 혹은 다른 우선순위가 있는지 확인 부탁드립니다.
