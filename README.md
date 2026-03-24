# AI 협업 개발 워크플로우

이 프로젝트는 Gemini CLI(전략)와 Codex/Claude CLI(구현) 간의 협업 워크플로우를 활용합니다.

## 1. 사전 준비 사항
*   Gemini CLI와 Codex/Claude CLI가 설치 및 인증되어 있어야 합니다.
*   `scripts/relay.sh` (또는 이에 상응하는 스크립트) 파일에 실행 권한이 있는지 확인하세요.

## 2. 시작 방법 (표준 운영 절차 - SOP)
1.  `git pull` 후 Gemini CLI를 실행합니다.
2.  Gemini에게 **"GEMINI.md를 읽고 현재 MISSION.md의 상태를 확인해줘"** 라고 요청합니다.
3.  Gemini가 작업을 설계하면 `MISSION.md`가 생성/업데이트됩니다.

## 3. 코딩 작업 실행 (Gemini -> Codex)

Gemini CLI가 `MISSION.md` 파일을 생성/업데이트하면, 다음 단계를 통해 Codex CLI로 작업을 전달하고 결과를 확인합니다.

### 3.1. 작업 요청: MISSION.md 작성
1.  수행하고자 하는 작업을 명확하게 정의하여 프로젝트 루트 디렉터리 (`C:	est\PG_DEV_GUIDE_API`)에 `MISSION.md` 파일을 생성하거나 수정합니다.
2.  `MISSION.md` 파일에는 **Goal, Scope, Instructions, Constraints** 등의 명확한 지시사항을 포함해야 합니다. (상세 형식은 `GEMINI.md` 또는 관련 문서를 참고하세요.)

### 3.2. 작업 실행: relay.sh 스크립트
1.  Git Bash 또는 다른 Bash 환경을 엽니다.
2.  해당 프로젝트의 루트 디렉터리로 이동합니다 (`cd /c/test/PG_DEV_GUIDE_API`).
3.  스크립트에 실행 권한이 있는지 확인합니다. (권한이 없다면 `chmod +x scripts/relay.sh` 실행)
4.  다음 명령어로 스크립트를 실행합니다:
    ```bash
    ./scripts/relay.sh
    ```
5.  스크립트가 실행되면 `MISSION.md`의 내용을 바탕으로 Codex CLI가 작업을 수행하고, 결과는 `MISSION_REPORT.md`와 `MISSION.md` 파일에 기록됩니다. `conversation_log.md`에도 실행 기록이 남습니다.

### 3.3. 결과 검증
*   Gemini CLI가 `MISSION.md` 또는 `MISSION_REPORT.md`를 검토하여 작업 완료 여부 및 결과의 타당성을 확인합니다.
