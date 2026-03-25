# AI Collaboration Guide

This file is intended for Gemini, Codex, Claude, and other AI agents working in `C:\test`.

## Primary Rules

- Preserve existing user changes.
- Do not revert unrelated work.
- Inspect before editing.
- Prefer minimal, targeted changes.
- Record assumptions when they affect behavior or risk.

## Encoding Rules

This workspace has had Korean text corruption caused by mixed encodings.

All agents must follow these rules:

- write text files as UTF-8 without BOM
- do not mix CP949, EUC-KR, ANSI, and UTF-8 in one file
- if a file already appears corrupted, stop and report it before appending new Korean text
- do not claim a file is safe based only on console output; verify bytes or verified UTF-8 reads when needed

## Shell Rules

- prefer Git Bash for shell execution when possible
- avoid Windows PowerShell 5.1 for Korean text file creation or append operations
- if PowerShell must be used, treat encoding as unsafe by default and verify results
- avoid `>` and `>>` redirection for important multilingual content unless encoding is explicitly controlled

## Editing Rules

- keep edits small and local to the task
- do not rewrite whole files unless necessary
- preserve line endings and formatting unless the task requires normalization
- if you normalize encoding, do it intentionally and note it clearly

## Handoff Format

When one AI agent hands work to another, include:

1. Goal
2. Files touched
3. Commands run
4. Tests run or not run
5. Open risks or assumptions

Example:

```text
Goal: Fix UTF-8 corruption in README footer.
Files touched: C:\test\project\README.md
Commands run: bash -lc "git diff -- README.md"
Tests run: none
Open risks: file may already contain mixed CP949 and UTF-8 content
```

## Recommended Behavior For Codex

- act as the implementation-focused agent
- verify local code before making assumptions
- prefer concrete fixes over broad rewrites
- report blockers with evidence

## Recommended Behavior For Gemini Or Other Review Agents

- challenge unsupported assumptions
- verify encoding-sensitive claims carefully
- distinguish between console display corruption and on-disk file corruption
- flag mixed encoding as a file integrity issue, not only a terminal issue

