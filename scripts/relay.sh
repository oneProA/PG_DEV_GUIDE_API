#!/bin/bash

# UTF-8 인코딩 설정
export LC_ALL=en_US.UTF-8
export LANG=en_US.UTF-8

# --- 설정 가능한 변수 ---
MISSION_FILE="MISSION.md"
REPORT_FILE="MISSION_REPORT.md"
CONVERSATION_LOG="conversation_log.md"
CODEX_CLI_COMMAND="codex" # 실제 Codex CLI 명령어 (사용자 확인 완료)

# --- 디렉터리 경로 설정 ---
# 현재 스크립트 파일이 있는 디렉터리 (예: C:	est\PG_DEV_GUIDE_API\scripts)
SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"
# 프로젝트 루트 디렉터리 (scripts 디렉터리의 상위 디렉터리, 예: C:	est\PG_DEV_GUIDE_API)
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# 파일 경로 설정 (프로젝트 루트 기준)
MISSION_PATH="${PROJECT_ROOT}/${MISSION_FILE}"
REPORT_PATH="${PROJECT_ROOT}/${REPORT_FILE}"
CONVERSATION_LOG_PATH="${PROJECT_ROOT}/${CONVERSATION_LOG}"

# --- 함수 정의 ---

# 로그 메시지 출력 함수
log_message() {
  local message="$1"
  # conversation_log.md 파일이 프로젝트 루트에 있다고 가정합니다.
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] $message" | tee -a "$CONVERSATION_LOG_PATH"
}

# Codex CLI 실행 함수
execute_codex_task() {
  log_message "Codex CLI를 사용하여 작업을 실행합니다. Mission File: ${MISSION_PATH}"

  # 인자(Argument)로 넘기지 않고, 표준 입력 리다이렉션(<)을 사용하여 파일 내용을 직접 전달합니다.
  ${CODEX_CLI_COMMAND} exec --skip-git-repo-check < "${MISSION_PATH}" > "$REPORT_PATH" 2>&1

  local exit_code=$?
  if [ $exit_code -eq 0 ]; then
    log_message "Codex 작업이 성공적으로 완료되었습니다. 결과: ${REPORT_PATH}"
    return 0
  else
    log_message "Codex 작업 중 오류가 발생했습니다. Exit code: ${exit_code}. Report: ${REPORT_PATH}"
    return 1
  fi
}

# --- 메인 스크립트 로직 ---

log_message "--- AI 협업 릴레이 스크립트 시작 ---"

# MISSION.md 파일 존재 여부 확인 (프로젝트 루트 기준)
if [ ! -f "$MISSION_PATH" ]; then
  log_message "오류: Mission file '${MISSION_FILE}'을(를) 찾을 수 없습니다. 프로젝트 루트 '${PROJECT_ROOT}'에 해당 파일이 있는지 확인해주세요."
  exit 1
fi

# MISSION.md 파일 내용 읽기
MISSION_CONTENT=$(cat "$MISSION_PATH")

# MISSION.md 파일 내용이 비어있는지 확인
if [ -z "$MISSION_CONTENT" ]; then
  log_message "Mission file '${MISSION_FILE}'이(가) 비어 있습니다. 작업할 내용이 없습니다."
  exit 0
fi

# Codex 작업 실행
if execute_codex_task "$MISSION_CONTENT"; then
  # 작업 성공 시 처리: MISSION.md 파일을 성공 메시지와 보고서 내용으로 업데이트
  echo -e "
--- 작업 완료 (Codex) ---
$(cat "$REPORT_PATH")" > "$MISSION_PATH"
  log_message "MISSION.md 파일이 업데이트되었습니다."
else
  # 작업 실패 시 처리: MISSION.md 파일을 실패 메시지와 보고서 내용으로 업데이트
  echo -e "
--- 작업 실패 (Codex) ---
$(cat "$REPORT_PATH")" > "$MISSION_PATH"
  log_message "MISSION.md 파일이 작업 실패 상태로 업데이트되었습니다."
fi

log_message "--- AI 협업 릴레이 스크립트 종료 ---"

exit 0
