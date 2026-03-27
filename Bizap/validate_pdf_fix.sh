#!/bin/bash

# PDF Export Fix Validation Script
# Captures logcat and filters for FileUriProvider and PDF generation events

echo "🔍 PDF Export Fix Validation Script"
echo "===================================="
echo ""
echo "Prerequisites:"
echo "  ✓ App must be running on emulator/device"
echo "  ✓ Logcat must be accessible"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check for patterns in logcat
check_for_pattern() {
    local pattern=$1
    local description=$2
    local lookback_lines=200

    echo "📝 Checking for: $description"

    if adb logcat -d | tail -n $lookback_lines | grep -q "$pattern"; then
        echo -e "${GREEN}✓ Found:${NC} $pattern"
        echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
        adb logcat -d | tail -n $lookback_lines | grep "$pattern" | tail -n 3
        echo ""
    else
        echo -e "${YELLOW}✗ Not found:${NC} $pattern"
        echo ""
    fi
}

# Clear logcat first
echo "📋 Clearing logcat..."
adb logcat -c
sleep 1

echo ""
echo "🎬 Now perform these steps in your app:"
echo "   1. Open an invoice"
echo "   2. Tap 'Export as PDF'"
echo "   3. Tap 'Share Invoice'"
echo "   4. Select an app or cancel"
echo ""
read -p "Press ENTER when done with PDF export test..."

echo ""
echo "🔎 Analyzing logcat for PDF export events..."
echo ""

# Check for PDF generation success
check_for_pattern "PDF generated successfully" "PDF Generation Success"

# Check for FileUriProvider validation
check_for_pattern "FileUriProvider: Successfully converted" "FileUriProvider Success"

# Check for any errors
check_for_pattern "PDF sharing failed" "PDF Sharing Error"

# Check for validation failures
check_for_pattern "File does not exist" "File Validation Error"

echo ""
echo "📊 Summary of Possible States:"
echo ""
echo "✅ SUCCESS (All good):"
echo "   ✓ PDF generated successfully message found"
echo "   ✓ FileUriProvider converted URI successfully"
echo "   ✓ NO error messages"
echo ""
echo "⚠️  PARTIAL (File generated but sharing failed):"
echo "   ✓ PDF generated successfully message found"
echo "   ✗ FileUriProvider error (check file_paths.xml)"
echo ""
echo "❌ FAILURE (PDF generation failed):"
echo "   ✗ PDF generated message NOT found"
echo "   ✗ Error message found"
echo ""

# Show Firebase Crashlytics reminder
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "🔥 Also check Firebase Crashlytics:"
echo "   1. Open Firebase Console"
echo "   2. Go to Crashlytics"
echo "   3. Filter by com.emul8r.bizap"
echo "   4. Look for PDF-related crashes"
echo "   5. Expected: ZERO crashes"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

