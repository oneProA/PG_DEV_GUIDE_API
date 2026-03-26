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
*   **미비 사항 (Hardcoded)**:
    *   **데이터 연동**: API 문서 및 지원 센터의 데이터가 하드코딩된 정적 텍스트임.
    *   **기능 로직**: `Playground`의 API 호출 기능이 실제 백엔드와 연동되지 않음 (UI만 존재).
    *   **사용자 인증**: 로그인/로그아웃 버튼의 기능이 구현되지 않음.

### 1.2 백엔드 (PG_DEV_GUIDE_API)
*   **완료 사항**:
    *   기획안의 API 규격에 따른 DTO(Data Transfer Object) 정의 완료.
    *   핵심 엔드포인트(`Payment`, `Cancel`, `Status`) 컨트롤러 생성 완료.
*   **미비 사항 (Hardcoded)**:
    *   **비즈니스 로직**: 실제 결제 처리 로직 없이 랜덤 ID와 성공 메시지만 반환함.
    *   **데이터베이스**: DB 연동이 전혀 되어 있지 않아 요청 데이터가 저장되지 않음.
    *   **인증/인가**: API 보안을 위한 Bearer Token 검증 로직이 생략됨.

---

## 2. 향후 개발 태스크 (Development Roadmap)

개발자들이 각자 분담하여 처리할 수 있도록 태스크를 세분화하였습니다.

### Phase 1: 인프라 및 환경 구축 (Infrastructure)
* [ ] **DB 스키마 설계**: PostgreSQL 기반의 결제 내역(Payments), 취소 내역(Cancels), 문의 게시판(Inquiries) 테이블 설계 및 생성.
* [ ] **MyBatis 설정 및 Mapper 작성**: 백엔드 프로젝트와 DB 연동 및 SQL Mapper(XML 또는 Annotation) 구현.
*   [ ] **CORS 설정**: 프런트엔드와 백엔드 간의 원활한 통신을 위한 보안 설정.

### Phase 2: 핵심 API 기능 구현 (Backend Focus)
*   [ ] **결제 로직 연동**: 가짜 응답 대신 실제 로직(DB 저장 및 검증)을 수행하도록 컨트롤러 및 서비스 고도화.
*   [ ] **지원 센터 CRUD**: 문의 등록 및 리스트 조회를 위한 실제 데이터 처리 API 구현.
*   [ ] **API 보안**: API Key 발급 로직 및 JWT/Bearer Token 기반의 인증 시스템 구축.

### Phase 3: 프런트엔드 기능 고도화 (Frontend Focus)
*   [ ] **API 연동 (Axios/React Query)**: 하드코딩된 데이터를 백엔드 API 호출로 대체.
*   [ ] **Playground 실동작**: 사용자가 입력한 데이터로 실제 백엔드를 호출하고 결과를 출력하도록 구현.
*   [ ] **상태 관리**: Zustand 등을 활용한 전역 로그인 상태 및 유저 정보 관리.

### Phase 4: 안정성 및 품질 관리 (QA & Docs)
*   [ ] **예외 처리**: API 에러 발생 시 사용자에게 적절한 피드백(Toast, Modal) 제공.
*   [ ] **테스트**: 주요 비즈니스 로직에 대한 단위 테스트 및 통합 테스트 수행.

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
