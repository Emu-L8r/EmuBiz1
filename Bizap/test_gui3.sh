#!/bin/bash
# GUI3 Phase 1 Device Testing Script
# Purpose: Automate APK installation and testing flow
# Date: April 13, 2026

echo "╔════════════════════════════════════════════════════════════════════╗"
echo "║       BIZAP GUI3 PHASE 1 - DEVICE TESTING AUTOMATION              ║"
echo "║                    Status: READY TO TEST                          ║"
echo "╚════════════════════════════════════════════════════════════════════╝"
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_DIR="C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
APK_PATH="$PROJECT_DIR\app\build\outputs\apk\debug\app-debug.apk"
PACKAGE_NAME="com.emul8r.bizap"
ACTIVITY="$PACKAGE_NAME.MainActivity"

# Step 1: Verify APK exists
echo -e "${BLUE}[1/7] Verifying APK artifact...${NC}"
if [ -f "$APK_PATH" ]; then
    APK_SIZE=$(ls -lh "$APK_PATH" | awk '{print $5}')
    echo -e "${GREEN}✅ APK found: $APK_SIZE${NC}"
else
    echo -e "${RED}❌ APK not found at $APK_PATH${NC}"
    echo "    Run: ./gradlew assembleDebug"
    exit 1
fi
echo ""

# Step 2: Check device connection
echo -e "${BLUE}[2/7] Checking device/emulator connection...${NC}"
DEVICES=$(adb devices | grep -v "^List" | grep -v "^$" | wc -l)
if [ $DEVICES -eq 0 ]; then
    echo -e "${RED}❌ No devices connected${NC}"
    echo "    Connect emulator or device and try again"
    echo "    For emulator: emulator -avd <device_name>"
    exit 1
else
    echo -e "${GREEN}✅ $DEVICES device(s) connected${NC}"
    adb devices
fi
echo ""

# Step 3: Clear app data for clean test
echo -e "${BLUE}[3/7] Clearing app data for clean first-launch...${NC}"
adb shell pm clear $PACKAGE_NAME 2>/dev/null
echo -e "${GREEN}✅ App data cleared${NC}"
echo ""

# Step 4: Uninstall old APK (optional)
echo -e "${BLUE}[4/7] Uninstalling old version...${NC}"
adb uninstall $PACKAGE_NAME 2>/dev/null
echo -e "${GREEN}✅ Old version uninstalled (if existed)${NC}"
echo ""

# Step 5: Install new APK
echo -e "${BLUE}[5/7] Installing APK ($APK_SIZE)...${NC}"
adb install "$APK_PATH"
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ APK installed successfully${NC}"
else
    echo -e "${RED}❌ Installation failed${NC}"
    exit 1
fi
echo ""

# Step 6: Launch app
echo -e "${BLUE}[6/7] Launching app...${NC}"
adb shell am start -n $ACTIVITY
echo -e "${GREEN}✅ App launched${NC}"
echo ""

# Step 7: Monitor logs
echo -e "${BLUE}[7/7] Monitoring logs (Ctrl+C to stop)...${NC}"
echo -e "${YELLOW}Watch for:${NC}"
echo "  ✅ 'BIZAP > MATRIX' in title (green text)"
echo "  ✅ Dashboard renders successfully"
echo "  ✅ No 'ERROR' or 'Exception' in logs"
echo "  ✅ No 'ANR' (Application Not Responding)"
echo ""
echo -e "${YELLOW}Filtering logs for 'bizap' and 'matrix' entries...${NC}"
adb logcat | grep -i "bizap\|matrix\|error\|exception"

echo ""
echo -e "${GREEN}✅ TESTING COMPLETE${NC}"
echo ""
echo "Next steps:"
echo "1. Verify LandingScreen shows 3 GUI options"
echo "2. Tap 'Enter The Matrix' button"
echo "3. Confirm MatrixGUIMainActivity launches"
echo "4. Check Dashboard styling (green/dark/monospace)"
echo "5. Test button navigation (NEW INVOICE, CUSTOMERS, etc.)"
echo "6. Test GUI switching (GUI1/GUI2 buttons)"
echo ""
echo "Documentation: See GUI3_QUICK_START.md"

