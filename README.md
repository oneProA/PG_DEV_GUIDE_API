# AI 협업 개발 워크플로우 (API)

   이 프로젝트는 Gemini CLI(전략)와 Codex/Claude CLI(구현) 간의 협업 워크플로우를 담당하는 백엔드 가이드입니다.

   ## 0. 중요 공지 (Relay Flow)
   환경 문제로 인해 `scripts/relay.sh` 대신 **`scripts/relay.ps1` 사용을 강력히 권장**합니다. 자세한 내용은 `CODEX_RELAY_REPORT.md`를 참조하세요.

   ## 1. 사전 준비 사항
   * Gemini CLI와 Codex/Claude CLI가 설치 및 인증되어 있어야 합니다.
   * PowerShell 실행 권한이 설정되어 있는지 확인하세요.

   ## 2. 시작 방법 (SOP)
   1. `git pull` 후 Gemini CLI를 실행합니다.
   2. Gemini에게 미션 설계를 요청하면 `MISSION.md`가 업데이트됩니다.
   3. 아래 명령어를 통해 Codex가 작업을 수행하게 합니다.
      ```powershell
      powershell -ExecutionPolicy Bypass -File scripts/relay.ps1
      ```

   ## 3. 코딩 작업 실행
   Gemini가 설계한 내용을 바탕으로 Codex가 실제 소스 코드(Kotlin/Spring Boot)를 수정합니다.
   