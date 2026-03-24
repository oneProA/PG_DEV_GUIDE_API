#!/bin/bash

# UTF-8 ?�코???�정
export LC_ALL=en_US.UTF-8
export LANG=en_US.UTF-8

# --- ?�정 가?�한 변??---
MISSION_FILE="MISSION.md"
REPORT_FILE="MISSION_REPORT.md"
CONVERSATION_LOG="conversation_log.md"
CODEX_CLI_COMMAND="codex" # ?�제 Codex CLI 명령??(?�용???�인 ?�료)

# --- ?�렉?�리 경로 ?�정 ---
# ?�재 ?�크립트 ?�일???�는 ?�렉?�리 (?? C:	est\PG_DEV_GUIDE_API\scripts)
SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"
# ?�로?�트 루트 ?�렉?�리 (scripts ?�렉?�리???�위 ?�렉?�리, ?? C:	est\PG_DEV_GUIDE_API)
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# ?�일 경로 ?�정 (?�로?�트 루트 기�?)
MISSION_PATH="${PROJECT_ROOT}/${MISSION_FILE}"
REPORT_PATH="${PROJECT_ROOT}/${REPORT_FILE}"
CONVERSATION_LOG_PATH="${PROJECT_ROOT}/${CONVERSATION_LOG}"

# --- ?�수 ?�의 ---

# 로그 메시지 출력 ?�수
log_message() {
  local message="$1"
  # conversation_log.md ?�일???�로?�트 루트???�다�?가?�합?�다.
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] $message" | tee -a "$CONVERSATION_LOG_PATH"
}

# Codex CLI ?�행 ?�수
execute_codex_task() {
  log_message "Codex CLI�??�용?�여 ?�업???�행?�니?? Mission File: ${MISSION_PATH}"

  # ?�자(Argument)�??�기지 ?�고, ?��? ?�력 리다?�렉??<)???�용?�여 ?�일 ?�용??직접 ?�달?�니??
  ${CODEX_CLI_COMMAND} exec --sandbox workspace-write --skip-git-repo-check < "${MISSION_PATH}" > "$REPORT_PATH" 2>&1

  local exit_code=$?
  if [ $exit_code -eq 0 ]; then
    log_message "Codex ?�업???�공?�으�??�료?�었?�니?? 결과: ${REPORT_PATH}"
    return 0
  else
    log_message "Codex ?�업 �??�류가 발생?�습?�다. Exit code: ${exit_code}. Report: ${REPORT_PATH}"
    return 1
  fi
}

# --- 메인 ?�크립트 로직 ---

log_message "--- AI ?�업 릴레???�크립트 ?�작 ---"

# MISSION.md ?�일 존재 ?��? ?�인 (?�로?�트 루트 기�?)
if [ ! -f "$MISSION_PATH" ]; then
  log_message "?�류: Mission file '${MISSION_FILE}'??�? 찾을 ???�습?�다. ?�로?�트 루트 '${PROJECT_ROOT}'???�당 ?�일???�는지 ?�인?�주?�요."
  exit 1
fi

# MISSION.md ?�일 ?�용 ?�기
MISSION_CONTENT=$(cat "$MISSION_PATH")

# MISSION.md ?�일 ?�용??비어?�는지 ?�인
if [ -z "$MISSION_CONTENT" ]; then
  log_message "Mission file '${MISSION_FILE}'??가) 비어 ?�습?�다. ?�업???�용???�습?�다."
  exit 0
fi

# Codex ?�업 ?�행
if execute_codex_task "$MISSION_CONTENT"; then
  # ?�업 ?�공 ??처리: MISSION.md ?�일???�공 메시지?� 보고???�용?�로 ?�데?�트
  echo -e "
--- ?�업 ?�료 (Codex) ---
$(cat "$REPORT_PATH")" > "$MISSION_PATH"
  log_message "MISSION.md ?�일???�데?�트?�었?�니??"
else
  # ?�업 ?�패 ??처리: MISSION.md ?�일???�패 메시지?� 보고???�용?�로 ?�데?�트
  echo -e "
--- ?�업 ?�패 (Codex) ---
$(cat "$REPORT_PATH")" > "$MISSION_PATH"
  log_message "MISSION.md ?�일???�업 ?�패 ?�태�??�데?�트?�었?�니??"
fi

log_message "--- AI ?�업 릴레???�크립트 종료 ---"

exit 0
