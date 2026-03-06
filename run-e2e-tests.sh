#!/usr/bin/env bash
# run-e2e-tests.sh — Run instrumented E2E tests for Bizap
#
# Usage:
#   ./run-e2e-tests.sh
#
# Requirements:
#   - A connected Android device or running emulator (API 26+)
#   - ANDROID_HOME configured in your environment

set -euo pipefail

cd "$(dirname "$0")/Bizap"

echo "📦 Building E2E test APK..."
./gradlew :app:assembleAndroidTest

echo "🧪 Running E2E tests on connected device..."
./gradlew :app:connectedAndroidTest

echo "✅ E2E tests complete."
echo "📄 Results available at: app/build/reports/androidTests/connected/index.html"
