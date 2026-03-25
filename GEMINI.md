# 프로젝트별 AI 지침 (PG_DEV_GUIDE_API)

이 파일은 해당 프로젝트 디렉터리 내에서 Gemini CLI가 작업을 수행할 때 필요한 특정 지침, 컨텍스트 및 규칙을 포함합니다.

## 프로젝트 컨텍스트
*   이 프로젝트는 **Backend (API)** 개발 가이드 및 구현을 담당합니다.
*   주요 기술 스택: [추가 예정]

## AI 워크플로우 규칙
*   모든 로직 구현은 **Codex (Claude)**에게 위임합니다.
*   Gemini는 전체적인 설계와 `MISSION.md` 작성을 담당합니다.
*   **실행 방식**: `scripts/relay.sh` 대신 **`scripts/relay.ps1` 사용을 강력히 권장**합니다. (Git Bash 불안정성 및 인코딩 이슈 방지)

## 🤖 Codex 협업 가이드라인 (중요)
Codex에게 `MISSION.md`를 전달할 때, 반드시 다음 사항을 명시해야 합니다.
1.  **"생각만 하지 말고, 반드시 도구(`replace`, `write_file` 등)를 사용하여 파일을 직접 수정하라."**
2.  **"답변만 하지 말고, 실제 소스 코드에 결과를 반영하라."**
3.  **"작업이 완료되면 `MISSION_REPORT.md`에 수행한 작업의 요약을 남겨라."**
4.  **"한글 깨짐 방지를 위해 모든 파일 읽기/쓰기 시 반드시 UTF-8 (BOM 없음) 인코딩을 사용하라."**
5.  **"런타임 오류 방지를 위해 프로젝트 로컬의 `.codex-runtime` 경로를 사용하도록 강제하라."**

## 알려진 환경 차단 요소 (Blockers)
*   **권한 오류**: 기본 홈 디렉토리 하위의 런타임 데이터 쓰기 시 `readonly database` 에러 발생 가능. 반드시 로컬 `.codex-runtime` 사용 필요.
*   **네트워크 오류**: `os error 10013` 또는 특정 URL 접속 실패 발생 시 환경 정책 문제일 가능성이 높음.
*   **Git Bash 불안정**: `couldn't create signal pipe` 에러 발생 시 PowerShell(`relay.ps1`)로 전환.

## 현재 작업 지시
*   [현재 진행 중인 백엔드 작업 내역을 여기에 기록합니다.]

## Relay Note
Read `CODEX_RELAY_REPORT.md` before using `scripts/relay.ps1`.
