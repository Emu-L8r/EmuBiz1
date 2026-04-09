#!/bin/bash
# Health Score Remediation - Build & Test Script

set -e

echo "════════════════════════════════════════════════════════════════════════════"
echo "🎯 Bizap Health Score Remediation - Build & Test"
echo "════════════════════════════════════════════════════════════════════════════"
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Step 1: Clean build
echo "${YELLOW}[1/5] Cleaning build artifacts...${NC}"
./gradlew clean

# Step 2: Run unit tests
echo ""
echo "${YELLOW}[2/5] Running unit tests...${NC}"
./gradlew test

# Step 3: Run lint checks
echo ""
echo "${YELLOW}[3/5] Running lint checks...${NC}"
./gradlew lint

# Step 4: Generate test coverage report
echo ""
echo "${YELLOW}[4/5] Generating test coverage report...${NC}"
./gradlew jacocoTestReport

# Step 5: Build APK
echo ""
echo "${YELLOW}[5/5] Building debug APK...${NC}"
./gradlew build

echo ""
echo "${GREEN}════════════════════════════════════════════════════════════════════════════${NC}"
echo "${GREEN}✅ Build successful!${NC}"
echo "${GREEN}════════════════════════════════════════════════════════════════════════════${NC}"
echo ""
echo "📊 Test Coverage Report:"
echo "   Location: build/reports/jacoco/jacocoTestReport/html/index.html"
echo ""
echo "🔍 Next Steps:"
echo "   1. Review test coverage report"
echo "   2. Run connected instrumentation tests: ./gradlew connectedAndroidTest"
echo "   3. Deploy APK to device/emulator for manual testing"
echo "   4. Verify performance improvements:"
echo "      - PIN operations: <5ms (was 20-50ms)"
echo "      - Database queries: <100ms p99 (was 250ms)"
echo "      - Startup latency: Reduced by ~45ms"
echo ""

