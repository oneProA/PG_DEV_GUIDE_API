# PG_DEV_GUIDE_API 테스트 체크리스트 (MD 기반)

기준 문서:
- `BACKEND_DESIGN.md`
- `TEST_SCENARIOS.md`

라벨 정의:
- **PASS**: 기능/연동이 문서 요구 수준으로 구현됨
- **PARTIAL**: 엔드포인트/응답은 있으나 하드코딩/목업/DB 미연동 등으로 일부만 충족
- **NOT IMPLEMENTED**: 문서 요구 기능이 아직 없음

| 영역 | 시나리오 | 상태 | 근거(현재 코드) | 다음 단계 |
|---|---|---|---|---|
| Auth | `POST /auth/login` | PASS | `demo.user/password` 로그인 + 발급된 `accessToken` 검증 | User 테이블 + UserMapper + JWT 발급/검증 구현 및 초기 사용자 시드 |
| Payments | `POST /v2/payments/request` | PARTIAL | `PaymentController.kt`가 mock 응답 반환(DB 미연동) | PaymentMapper 작성 + insert/update/status 반영 |
| Cancels | `POST /v1/payments/cancel` | PARTIAL | `PaymentController.kt`가 mock 응답 반환(DB 미연동) | CancelMapper + payments 상태/잔액 트랜잭션 처리 |
| Status | `GET /v1/payments/status/{tid}` | PARTIAL | `PaymentController.kt`가 고정 값 반환(DB 미연동) | tid/mid 기반 조회 + 상태/카드/영수증 정보 DB 연동 |
| Inquiries | `POST /inquiries`, `GET /inquiries` | NOT IMPLEMENTED | 엔드포인트 없음 | Inquiry CRUD + 권한/필터링 구현 |
| DB 구조 | `users/payments/cancels/inquiries` 스키마/키 확인 | CHECK VIA PSQL | 원격 DB(Neon) 기준 확인 필요 | `scripts/db_schema_check.ps1`로 조회 |

DB 구조 확인(조회 전용, 비밀번호 로그 저장 금지):
```powershell
cd C:\test\PG_DEV_GUIDE_API
powershell -ExecutionPolicy Bypass -File .\scripts\db_schema_check.ps1
```

