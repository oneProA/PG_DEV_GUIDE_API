#!/bin/bash

export LC_ALL=en_US.UTF-8
export LANG=en_US.UTF-8

MISSION_FILE="MISSION.md"
REPORT_FILE="MISSION_REPORT.md"
CONVERSATION_LOG="conversation_log.md"
CODEX_CLI_COMMAND="${CODEX_CLI_COMMAND:-}"

SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

MISSION_PATH="${PROJECT_ROOT}/${MISSION_FILE}"
REPORT_PATH="${PROJECT_ROOT}/${REPORT_FILE}"
CONVERSATION_LOG_PATH="${PROJECT_ROOT}/${CONVERSATION_LOG}"
CODEX_RUNTIME_DIR="${PROJECT_ROOT}/.codex-runtime"
CODEX_HOME_DIR="${CODEX_RUNTIME_DIR}/home"
CODEX_TMP_DIR="${CODEX_RUNTIME_DIR}/tmp"

log_message() {
  local message="$1"
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] $message" | tee -a "$CONVERSATION_LOG_PATH"
}

resolve_codex_command() {
  local candidate=""

  if [ -n "$CODEX_CLI_COMMAND" ] && command -v "$CODEX_CLI_COMMAND" >/dev/null 2>&1; then
    command -v "$CODEX_CLI_COMMAND"
    return 0
  fi

  if command -v codex.exe >/dev/null 2>&1; then
    command -v codex.exe
    return 0
  fi

  if command -v codex >/dev/null 2>&1; then
    candidate="$(command -v codex)"
    case "$candidate" in
      *.cmd|*.exe)
        echo "$candidate"
        return 0
        ;;
    esac
  fi

  if [ -n "$USERPROFILE" ]; then
    candidate="${USERPROFILE}/.vscode/extensions/openai.chatgpt-26.323.20928-win32-x64/bin/windows-x86_64/codex.exe"
    if [ -x "$candidate" ]; then
      echo "$candidate"
      return 0
    fi
  fi

  return 1
}

execute_codex_task() {
  local resolved_codex=""
  log_message "Executing Codex task. Mission file: ${MISSION_PATH}"

  if ! resolved_codex="$(resolve_codex_command)"; then
    log_message "Error: Unable to resolve a Codex CLI executable. Set CODEX_CLI_COMMAND if needed."
    return 1
  fi

  log_message "Resolved Codex CLI: ${resolved_codex}"

  mkdir -p "${CODEX_HOME_DIR}" "${CODEX_TMP_DIR}"

  CODEX_HOME="${CODEX_HOME_DIR}" TMPDIR="${CODEX_TMP_DIR}" TMP="${CODEX_TMP_DIR}" TEMP="${CODEX_TMP_DIR}" \
    "${resolved_codex}" exec --sandbox workspace-write --skip-git-repo-check < "${MISSION_PATH}" > "$REPORT_PATH" 2>&1

  local exit_code=$?
  if [ $exit_code -eq 0 ]; then
    log_message "Codex task completed successfully. Report: ${REPORT_PATH}"
    return 0
  else
    log_message "Codex task failed with exit code ${exit_code}. Report: ${REPORT_PATH}"
    return 1
  fi
}

log_message "--- AI relay script started ---"

if [ ! -f "$MISSION_PATH" ]; then
  log_message "Error: Mission file '${MISSION_FILE}' was not found in '${PROJECT_ROOT}'."
  exit 1
fi

MISSION_CONTENT=$(cat "$MISSION_PATH")

if [ -z "$MISSION_CONTENT" ]; then
  log_message "Mission file '${MISSION_FILE}' is empty. Nothing to do."
  exit 0
fi

if execute_codex_task "$MISSION_CONTENT"; then
  echo -e "
--- Task Complete (Codex) ---
$(cat "$REPORT_PATH")" > "$MISSION_PATH"
  log_message "MISSION.md was updated after a successful run."
else
  echo -e "
--- Task Failed (Codex) ---
$(cat "$REPORT_PATH")" > "$MISSION_PATH"
  log_message "MISSION.md was updated after a failed run."
fi

log_message "--- AI relay script finished ---"

exit 0
