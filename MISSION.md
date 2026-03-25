# MISSION: PG_DEV_GUIDE_API 한글 인코딩 복구 및 파일 정규화

**Goal:** 인코딩 오류로 깨진 README.md 및 MISSION_REPORT.md 파일을 정상적인 한글로 복구하고, 모든 문서를 UTF-8(BOM 없음)로 통일합니다.

## 프로젝트 컨텍스트
- 현재 `PG_DEV_GUIDE_API` 내의 `README.md`, `MISSION_REPORT.md` 등이 잘못된 인코딩으로 저장되어 한글이 깨져 있습니다.

## 작업 지시사항 (Codex 전용)
1. **README.md 복구**:
   - 기존의 깨진 내용을 삭제하고, 아래의 정상적인 내용을 **UTF-8**로 다시 작성하세요.
   ```markdown
   # AI 협업 개발 워크플로우 (API)

   이 프로젝트는 Gemini CLI(전략)와 Codex/Claude CLI(구현) 간의 협업 워크플로우를 담당하는 백엔드 가이드입니다.

   ## 1. 사전 준비 사항
   * Gemini CLI와 Codex/Claude CLI가 설치 및 인증되어 있어야 합니다.
   * `scripts/relay.sh` 파일에 실행 권한이 있는지 확인하세요.

   ## 2. 시작 방법 (SOP)
   1. `git pull` 후 Gemini CLI를 실행합니다.
   2. Gemini에게 미션 설계를 요청하면 `MISSION.md`가 업데이트됩니다.
   3. `relay.sh`를 실행하여 Codex가 작업을 수행하게 합니다.

   ## 3. 코딩 작업 실행
   Gemini가 설계한 내용을 바탕으로 Codex가 실제 소스 코드(Kotlin/Spring Boot)를 수정합니다.
   ```

2. **MISSION_REPORT.md 초기화**:
   - 깨진 내용을 삭제하고, 현재 인코딩 복구 작업을 수행했음을 기록하세요.

3. **인코딩 강제**:
   - 모든 파일 작업 시 반드시 `Set-Content -Encoding UTF8` 등을 사용하여 한글이 깨지지 않도록 하세요.

**주의**: "생각만 하지 말고, 반드시 도구(`replace`, `write_file` 등)를 사용하여 파일을 직접 수정하라. 작업이 완료되면 MISSION_REPORT.md에 요약을 남겨라."

## 체크리스트
- [ ] README.md 한글 복구 및 UTF-8 저장
- [ ] MISSION_REPORT.md 한글 복구 및 UTF-8 저장
- [ ] 프로젝트 내 모든 `.md` 파일의 인코딩 점검
