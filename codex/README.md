# Codex

This directory contains shared guidance for working with Codex in this workspace.

## What Codex Is

Codex is a coding-focused AI agent used for:

- reading and understanding the local codebase
- editing files and implementing requested changes
- running local commands for inspection, build, and test work
- collaborating with other AI agents such as Gemini or Claude

Codex should be treated as a practical implementation agent, not just a chat assistant.

## How To Use Codex

Give Codex a concrete task with enough local context.

Recommended request style:

1. State the goal clearly.
2. Name the target project or directory.
3. Mention constraints.
4. Mention whether code changes, review, or investigation is needed.

Example:

```text
Update C:\test\one_api so the login endpoint returns the user role.
Keep existing response fields unchanged.
Run the relevant test or explain why it could not be run.
```

## Collaboration Expectations

- Prefer direct, task-oriented instructions.
- Ask for code changes when changes are desired.
- Ask for review when you want bugs, risks, regressions, or missing tests identified.
- Ask for investigation when the cause is unclear and evidence is needed first.

## Encoding And Shell Policy

For this workspace, all AI agents should assume:

- all text files must be UTF-8 without BOM unless a file clearly requires something else
- Git Bash is preferred over Windows PowerShell 5.1 for shell-oriented work
- avoid PowerShell 5.1 file append/write flows for Korean text
- do not rely on shell redirection for important Korean text output unless encoding is explicitly controlled

## Suggested Reading Order For Other AI Agents

1. Read this file first.
2. Read `C:\test\codex\COLLABORATION.md`.
3. Then inspect the target project before editing.

