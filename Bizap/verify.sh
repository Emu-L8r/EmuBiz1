#!/usr/bin/env bash
# =============================================================================
# verify.sh — Bizap Build & Test Verification Script
# =============================================================================
# Usage:
#   ./verify.sh           — Full verification (build + tests + lint)
#   ./verify.sh build     — Debug APK build only
#   ./verify.sh test      — Unit tests only
#   ./verify.sh lint      — Lint checks only
#   ./verify.sh clean     — Clean + full verification
# =============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# ── Colours ──────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Colour

# ── Helpers ───────────────────────────────────────────────────────────────────
log_step()   { echo -e "\n${BLUE}▶ $*${NC}"; }
log_ok()     { echo -e "${GREEN}✅ $*${NC}"; }
log_warn()   { echo -e "${YELLOW}⚠️  $*${NC}"; }
log_error()  { echo -e "${RED}❌ $*${NC}"; }

PASS=0
FAIL=0
SKIP=0

record_result() {
  local name="$1" status="$2"
  if [[ "$status" == "PASS" ]]; then
    log_ok "$name"
    ((PASS++)) || true
  elif [[ "$status" == "SKIP" ]]; then
    log_warn "$name (skipped)"
    ((SKIP++)) || true
  else
    log_error "$name"
    ((FAIL++)) || true
  fi
}

# ── Gradle wrapper ─────────────────────────────────────────────────────────────
GRADLEW="./gradlew"
if [[ ! -f "$GRADLEW" ]]; then
  log_error "gradlew not found in $(pwd). Run from the Bizap/ directory."
  exit 1
fi
chmod +x "$GRADLEW"

# ── Mode ──────────────────────────────────────────────────────────────────────
MODE="${1:-all}"

run_build() {
  log_step "Building debug APK…"
  if "$GRADLEW" :app:assembleDebug --no-daemon -q 2>&1; then
    record_result "Debug APK build" PASS
    APK_PATH=$(find app/build/outputs/apk/debug -name "*.apk" 2>/dev/null | head -1)
    if [[ -n "$APK_PATH" ]]; then
      SIZE=$(du -h "$APK_PATH" | awk '{print $1}')
      echo "    APK: $APK_PATH ($SIZE)"
    fi
  else
    record_result "Debug APK build" FAIL
  fi
}

run_tests() {
  log_step "Running unit tests…"
  if "$GRADLEW" :app:testDebugUnitTest --no-daemon -q 2>&1; then
    record_result "Unit tests" PASS
    REPORT="app/build/reports/tests/testDebugUnitTest/index.html"
    [[ -f "$REPORT" ]] && echo "    Report: $REPORT"
  else
    record_result "Unit tests" FAIL
    REPORT="app/build/reports/tests/testDebugUnitTest/index.html"
    [[ -f "$REPORT" ]] && echo "    Report: $REPORT"
  fi
}

run_lint() {
  log_step "Running lint checks…"
  if "$GRADLEW" :app:lintDebug --no-daemon -q 2>&1; then
    record_result "Lint" PASS
  else
    REPORT="app/build/reports/lint-results-debug.html"
    [[ -f "$REPORT" ]] && log_warn "Lint issues found — see $REPORT"
    record_result "Lint" FAIL
  fi
}

run_clean() {
  log_step "Cleaning build artifacts…"
  "$GRADLEW" clean --no-daemon -q 2>&1
  log_ok "Clean complete"
}

# ── Environment check ─────────────────────────────────────────────────────────
log_step "Environment checks"
java -version 2>&1 | head -1 && record_result "Java" PASS || record_result "Java" FAIL
[[ -n "${ANDROID_HOME:-}" ]] && record_result "ANDROID_HOME set" PASS || record_result "ANDROID_HOME set" SKIP

# ── Main ──────────────────────────────────────────────────────────────────────
case "$MODE" in
  build) run_build ;;
  test)  run_tests ;;
  lint)  run_lint  ;;
  clean) run_clean; run_build; run_tests; run_lint ;;
  all)   run_build; run_tests; run_lint ;;
  *)
    echo "Unknown mode: $MODE"
    echo "Usage: $0 [build|test|lint|clean|all]"
    exit 1
    ;;
esac

# ── Summary ───────────────────────────────────────────────────────────────────
echo ""
echo "═══════════════════════════════════════"
echo "  VERIFICATION SUMMARY"
echo "═══════════════════════════════════════"
echo -e "  ${GREEN}Passed: $PASS${NC}  ${RED}Failed: $FAIL${NC}  ${YELLOW}Skipped: $SKIP${NC}"
echo "═══════════════════════════════════════"

if [[ $FAIL -gt 0 ]]; then
  log_error "Verification FAILED ($FAIL check(s) failed)"
  exit 1
else
  log_ok "All checks passed"
  exit 0
fi
