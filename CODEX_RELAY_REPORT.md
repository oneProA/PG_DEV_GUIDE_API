# Codex Relay Report

Date: 2026-03-25
Project: `PG_DEV_GUIDE_API`

## Summary

This report captures the Codex relay debugging work so Gemini can continue from the current state without re-investigating the same issue.

## What Was Observed

The original relay flow was:

- `scripts/relay.sh`
- `codex exec --sandbox workspace-write --skip-git-repo-check`

However, earlier `MISSION_REPORT.md` output showed:

- `approval: never`
- `sandbox: read-only`

That made it look like Codex was ignoring the relay script's `workspace-write` option.

## What Was Verified

1. The current Codex session in this workspace is not read-only.
2. `C:\Users\lanzbrok\AppData\Roaming\npm\codex.ps1` is only a thin wrapper and does not force `read-only`.
3. `C:\Users\lanzbrok\.codex\config.toml` does not explain the old `read-only` result.
4. Running Codex directly with the VS Code extension binary showed:
   - `sandbox: workspace-write`
   - so the relay option itself is valid
5. The direct run also showed two different blockers:
   - Codex state DB warnings under `C:\Users\lanzbrok\.codex`
   - network/socket failures when connecting to Codex endpoints

## Confirmed Error Signals

Direct Codex execution produced evidence for these problems:

- `attempt to write a readonly database`
- websocket/connectivity failures such as:
  - `os error 10013`
  - `error sending request for url (https://chatgpt.com/backend-api/codex/responses)`

Git Bash execution was also unreliable in this environment:

- `bash.exe: couldn't create signal pipe, Win32 error 5`

## Changes Applied

### 1. Hardened Bash Relay

Updated:

- `scripts/relay.sh`

Changes:

- resolve a concrete Codex executable instead of relying on bare `codex`
- log the resolved executable path
- set project-local runtime paths:
  - `.codex-runtime/home`
  - `.codex-runtime/tmp`
- force Codex to use those runtime directories

Purpose:

- avoid ambiguity about which Codex binary is launched
- reduce failures caused by writing under `C:\Users\lanzbrok\.codex`

### 2. Added PowerShell Relay

Added:

- `scripts/relay.ps1`

Purpose:

- provide a non-Bash execution path
- avoid Git Bash startup failures
- avoid text corruption from default PowerShell file encoding behavior

Key implementation details:

- all file reads/writes use explicit UTF-8 without BOM
- mission/report/log files are handled through .NET file APIs
- Codex input is piped through a redirected process, not via lossy shell text handling

## Current Best Execution Path

Prefer PowerShell relay over Git Bash in the current environment.

Recommended command:

```powershell
powershell -ExecutionPolicy Bypass -File C:\test\PG_DEV_GUIDE_API\scripts\relay.ps1
```

## Current Status

The original diagnosis "Codex is always read-only" is no longer the best explanation.

Current understanding:

- `workspace-write` can be applied correctly
- Git Bash is unstable in this machine/session
- project-local runtime directories should avoid the readonly DB issue
- the main remaining blocker is network/security policy around Codex endpoint access

## What Gemini Should Do Next

1. Use `scripts/relay.ps1` first, not `scripts/relay.sh`.
2. Check `conversation_log.md` for:
   - `Resolved Codex CLI: ...`
   - execution start/end timestamps
3. Check `MISSION_REPORT.md` for:
   - the sandbox line
   - any network or socket error messages
4. If execution still fails, treat the remaining issue as environment or policy related, not a simple relay script bug.

## Files Changed By Codex During This Investigation

- `C:\test\PG_DEV_GUIDE_API\scripts\relay.sh`
- `C:\test\PG_DEV_GUIDE_API\scripts\relay.ps1`
- `C:\test\PG_DEV_GUIDE_VIEW\scripts\relay.sh`
- `C:\test\PG_DEV_GUIDE_VIEW\scripts\relay.ps1`

## Important Note

The existing `README.md`, `GEMINI.md`, `conversation_log.md`, and `MISSION_REPORT.md` in this workspace already contain some encoding damage from earlier runs. This report was written as a clean handoff artifact so Gemini can rely on it instead of reverse-engineering broken logs.
