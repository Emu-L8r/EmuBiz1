#!/bin/bash

echo "🚀 BIZAP APK INSTALLATION & TESTING - OPTION B"
echo "=============================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
PACKAGE_NAME="com.emul8r.bizap"
MAIN_ACTIVITY="com.emul8r.bizap.MainActivity"

# Step 1: Check if device is connected
echo -e "${YELLOW}1️⃣  Checking device connection...${NC}"
if ! adb devices | grep -q "device$"; then
    echo -e "${RED}❌ No device connected!${NC}"
    echo "Connect a device or start an emulator, then try again."
    exit 1
fi
echo -e "${GREEN}✅ Device connected${NC}"
echo ""

# Step 2: Uninstall old APK
echo -e "${YELLOW}2️⃣  Uninstalling old APK...${NC}"
adb uninstall "$PACKAGE_NAME" > /dev/null 2>&1
echo -e "${GREEN}✅ Old APK removed (or was not installed)${NC}"
echo ""

# Step 3: Check if APK file exists
echo -e "${YELLOW}3️⃣  Verifying APK file...${NC}"
if [ ! -f "$APK_PATH" ]; then
    echo -e "${RED}❌ APK not found at: $APK_PATH${NC}"
    exit 1
fi
APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
echo -e "${GREEN}✅ APK found (Size: $APK_SIZE)${NC}"
echo ""

# Step 4: Install fresh APK
echo -e "${YELLOW}4️⃣  Installing fresh APK...${NC}"
if adb install -r "$APK_PATH" | grep -q "Success"; then
    echo -e "${GREEN}✅ APK installed successfully${NC}"
else
    echo -e "${RED}❌ Installation failed!${NC}"
    adb install -r "$APK_PATH"
    exit 1
fi
echo ""

# Step 5: Launch app
echo -e "${YELLOW}5️⃣  Launching app...${NC}"
adb shell am start -n "$PACKAGE_NAME/.MainActivity"
echo -e "${GREEN}✅ App launch command sent${NC}"
echo ""

# Step 6: Wait a moment and check logcat
echo -e "${YELLOW}6️⃣  Monitoring logs for 20 seconds...${NC}"
echo "Look for:"
echo "  ✅ No 'FATAL EXCEPTION' or 'ClassNotFoundException'"
echo "  ✅ 'BizapApplication: 🚀' message"
echo "  ✅ 'MainActivity: onCreate()' message"
echo ""

# Capture and display logs
adb logcat -c  # Clear logcat
sleep 2  # Wait for app to start
timeout 20 adb logcat 2>/dev/null | while IFS= read -r line; do
    if echo "$line" | grep -qE "FATAL|ClassNotFoundException|BizapApplication|MainActivity"; then
        if echo "$line" | grep -q "FATAL\|ClassNotFoundException"; then
            echo -e "${RED}$line${NC}"
        else
            echo -e "${GREEN}$line${NC}"
        fi
    fi
done

echo ""
echo -e "${YELLOW}7️⃣  Final verification...${NC}"
# Check if app is running
if adb shell dumpsys activity | grep -q "$PACKAGE_NAME"; then
    echo -e "${GREEN}✅ App is running!${NC}"
    echo -e "\n${GREEN}🎉 SUCCESS! The app launched without crashing!${NC}"
else
    echo -e "${RED}❌ App crashed or not running${NC}"
    echo ""
    echo "Getting crash details..."
    adb logcat -d | grep -A 10 "FATAL EXCEPTION\|ClassNotFoundException" | tail -20
fi

echo ""
echo "=============================================="
echo "Installation complete!"

