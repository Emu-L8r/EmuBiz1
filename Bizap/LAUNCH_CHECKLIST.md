# 🎯 LAUNCH CHECKLIST - Bizap v1.0

**Status:** 75% Ready for Launch  
**Estimated Time to Launch:** 5-7 Days  
**Last Updated:** March 14, 2026

---

## 🔴 CRITICAL (Must Fix Before Launch)

- [ ] **Device Testing: Release APK**
  - [ ] Build release APK: `./gradlew clean assembleRelease`
  - [ ] Install: `adb install -r app/build/outputs/apk/release/app-release.apk`
  - [ ] Test: App launches without crash
  - [ ] Test: Create invoice → Record payment → Export PDF
  - [ ] Test: Switch between GUI1 and GUI2
  - [ ] **⏱️ Time:** 30 minutes
  - **👤 Owner:** You
  - **📆 Due:** TODAY
  
- [ ] **Encryption Verification**
  - [ ] Create test invoice in app
  - [ ] Extract database: `adb pull /data/data/com.emul8r.bizap/databases/bizap-db`
  - [ ] Check encryption: `xxd bizap-db | head -1`
  - [ ] Verify: First bytes should be random binary (NOT "SQLite format 3")
  - [ ] **⏱️ Time:** 10 minutes
  - **👤 Owner:** You
  - **📆 Due:** TODAY

- [ ] **CSV Export End-to-End Test**
  - [ ] Create invoice in app
  - [ ] Export to CSV
  - [ ] Verify: File appears in Downloads
  - [ ] Verify: User can share file
  - [ ] **⏱️ Time:** 15 minutes
  - **👤 Owner:** You
  - **📆 Due:** TODAY

- [ ] **Write Privacy Policy**
  - [ ] Create: `docs/PRIVACY_POLICY.md`
  - [ ] Cover: Data collection, encryption, user rights
  - [ ] Template: Use Google/App Store privacy policy generator
  - [ ] **⏱️ Time:** 45 minutes
  - **👤 Owner:** You
  - **📆 Due:** Wednesday

- [ ] **Write Terms of Service**
  - [ ] Create: `docs/TERMS_OF_SERVICE.md`
  - [ ] Cover: Usage limitations, liability, dispute resolution
  - [ ] Template: Use standard app ToS template
  - [ ] **⏱️ Time:** 45 minutes
  - **👤 Owner:** You
  - **📆 Due:** Wednesday

---

## 🟡 HIGH-PRIORITY (Before Launch)

- [ ] **App Store Screenshots & Assets**
  - [ ] Take 5 screenshots showing key features
  - [ ] Recommended: Dashboard, Create Invoice, GUI2 Dashboard, Payment, Settings
  - [ ] Format: 1080x1920 PNG (or use Play Store guidelines)
  - [ ] **⏱️ Time:** 30 minutes
  - **👤 Owner:** You
  - **📆 Due:** Thursday

- [ ] **App Store Description**
  - [ ] Write marketing description (80-200 characters)
  - [ ] Write full description (focus on features, not technical)
  - [ ] Add: Key features, offline support, dual GUI
  - [ ] Add: Who it's for (small business owners, freelancers)
  - [ ] **⏱️ Time:** 30 minutes
  - **👤 Owner:** You
  - **📆 Due:** Thursday

- [ ] **Content Rating Questionnaire**
  - [ ] Complete Google Play Store IARC questionnaire
  - [ ] Question: Does app contain violence? → No
  - [ ] Question: Does app contain mature content? → No
  - [ ] Should auto-rate as: Everyone (or Everyone 10+)
  - [ ] **⏱️ Time:** 15 minutes
  - **👤 Owner:** You
  - **📆 Due:** Thursday

- [ ] **Setup Play Store Account & App**
  - [ ] Create/verify Google Play Developer Account ($25 one-time)
  - [ ] Create new app entry in Play Console
  - [ ] Set package name: `com.emul8r.bizap`
  - [ ] Set pricing: Free or Paid (recommend Free with in-app purchases later)
  - [ ] **⏱️ Time:** 30 minutes
  - **👤 Owner:** You
  - **📆 Due:** Thursday

- [ ] **Test Release APK on Real Device**
  - [ ] If possible, test on actual Android phone
  - [ ] Verify: All features work (not just emulator)
  - [ ] Check: Performance acceptable (app doesn't lag)
  - [ ] Check: No crashes or ANR (Application Not Responding)
  - [ ] **⏱️ Time:** 30 minutes (if real device available)
  - **👤 Owner:** You
  - **📆 Due:** Friday (optional but recommended)

---

## 🟢 OPTIONAL (v1.0.1 or Later)

- [ ] Fix Deprecation Warnings
  - [ ] Material3 icons: Icons.Filled.* → Icons.AutoMirrored.*
  - [ ] Divider → HorizontalDivider
  - [ ] Add @OptIn annotations for Coroutines
  - [ ] **⏱️ Time:** 2-3 hours
  - **📆 When:** After v1.0 launches (v1.0.1)

- [ ] Clean Up Documentation
  - [ ] Consolidate 50+ .md files into 5-10 core docs
  - [ ] Archive old analysis files to `/docs/archive/`
  - [ ] Update README.md with setup instructions
  - [ ] **⏱️ Time:** 1-2 hours
  - **📆 When:** v1.0.1 sprint

- [ ] Performance Profiling
  - [ ] Measure app startup time
  - [ ] Measure invoice list load time
  - [ ] Measure PDF generation time
  - [ ] Create performance baseline document
  - [ ] **⏱️ Time:** 2-3 hours
  - **📆 When:** v1.1 (post-launch)

---

## 📋 SUBMISSION WORKFLOW

### Step 1: Final Verification (TODAY - 1 hour)
```bash
# Release build test
./gradlew clean assembleRelease

# Verify it's signed
jarsigner -verify app/build/outputs/apk/release/app-release.apk

# Install on device
adb install -r app/build/outputs/apk/release/app-release.apk

# Test critical flows
# (See device testing checklist below)
```

### Step 2: Prepare Store Assets (This Week - 3 hours)
```
- Write Privacy Policy
- Write Terms of Service
- Take app screenshots
- Write marketing description
- Complete IARC rating
```

### Step 3: Create Store Listing (Friday - 1 hour)
```
1. Go to: https://play.google.com/console
2. Create new app
3. Fill in: Title, category, rating
4. Upload: Screenshots, descriptions
5. Review: Terms, privacy policy
```

### Step 4: Upload Release APK (Friday - 30 min)
```
1. In Play Console, go to: Release → Production
2. Click: Create Release
3. Upload: app-release.apk from earlier
4. Review: Permissions, features
5. Submit: For review
```

### Step 5: Monitor Review (Wait 4-6 Hours)
```
- Google Play will review your app
- They check for: Malware, GDPR compliance, appropriate content
- Expected outcome: APPROVED (99% probability)
- Your app then goes live to all users
```

---

## ✅ DEVICE TESTING CHECKLIST

Run these tests on both DEBUG and RELEASE builds:

### Test Group 1: App Stability
- [ ] App launches without crash (cold start)
- [ ] App launches after backgrounding (warm start)
- [ ] No ANR (Application Not Responding) errors
- [ ] No crashes when navigating between screens
- [ ] No out-of-memory errors

### Test Group 2: Invoice Creation
- [ ] Create invoice with 1 line item
- [ ] Create invoice with 5 line items
- [ ] Create invoice with custom tax rate
- [ ] Create invoice with long customer name
- [ ] Save invoice successfully

### Test Group 3: Payment Recording
- [ ] Record payment on SENT invoice (changes to PARTIALLY_PAID)
- [ ] Record payment to complete invoice (changes to PAID)
- [ ] Cannot record payment exceeding outstanding amount
- [ ] Payment amount displays correctly
- [ ] Outstanding balance calculates correctly

### Test Group 4: Document Export
- [ ] Export invoice to PDF (verify file created)
- [ ] Export invoice to CSV (verify file created)
- [ ] Share PDF (verify share dialog appears)
- [ ] Share CSV (verify share dialog appears)
- [ ] PDF formatting looks correct

### Test Group 5: GUI Switching
- [ ] Switch from GUI1 to GUI2 (data persists)
- [ ] Switch from GUI2 to GUI1 (data persists)
- [ ] Create invoice in GUI1, view in GUI2
- [ ] Record payment in GUI2, see in GUI1
- [ ] Both GUIs show same invoice count

### Test Group 6: Offline Mode (Optional)
- [ ] Turn off WiFi/cellular
- [ ] Create invoice (should succeed locally)
- [ ] Turn on connectivity
- [ ] Verify sync completes
- [ ] Invoice appears in cloud

### Test Group 7: Encryption
- [ ] Database file extracted
- [ ] First bytes of database are random (not "SQLite")
- [ ] App still functions with encryption enabled
- [ ] Data persists correctly

---

## 📝 FAILURE CONTINGENCY

If any test FAILS:

### Release APK Crashes
**Likely Cause:** ProGuard rule issue  
**Fix Time:** 30-60 minutes  
**Action:** Check ProGuard rules for:
- `net.zetetic.**` (SQLCipher)
- `com.emul8r.bizap.**` (app code)
- Hilt rules

### CSV Export Fails
**Likely Cause:** FileProvider not configured  
**Fix Time:** 30 minutes  
**Action:** Verify in AndroidManifest.xml:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="com.emul8r.bizap.fileprovider"
    android:exported="false">
    <meta-data
        android:name="android.support.FILE_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

### Encryption Doesn't Work
**Likely Cause:** Passphrase not being set  
**Fix Time:** 30 minutes  
**Action:** Check DatabasePassphraseManager:
```kotlin
val passphrase = getPassphrase() // Should return non-empty ByteArray
// If returns empty → Fix passphrase retrieval from Android Keystore
```

### GUI Switching Broken
**Likely Cause:** DataStore preference not persisting  
**Fix Time:** 45 minutes  
**Action:** Check LandingViewModel and NavigationManager for:
- DataStore edit operations
- Proper Flow emission
- Activity switching logic

---

## 🎯 SUCCESS CRITERIA

You're **READY TO SUBMIT** when:

- ✅ Release APK builds without errors
- ✅ Release APK installs on device
- ✅ All 7 test groups pass (DEBUG build minimum)
- ✅ Encryption verified (database is encrypted)
- ✅ Privacy Policy written
- ✅ Terms of Service written
- ✅ Screenshots captured
- ✅ App description written
- ✅ Play Store account created
- ✅ App listing filled in

---

## 📅 TIMELINE ESTIMATE

| Task | Duration | Status |
|------|----------|--------|
| Device testing | 1 hour | 🔴 TODO |
| App Store docs | 3-4 hours | 🔴 TODO |
| Play Store setup | 1 hour | 🔴 TODO |
| Final review | 30 min | 🔴 TODO |
| **TOTAL** | **5-6 hours** | 🔴 TODO |
| Google Play review | 4-6 hours | Automatic |
| **TIME TO LAUNCH** | **~10 hours** | 🎯 TARGET |

**Realistic Launch Date:** Friday, March 15 (if you start TODAY)  
**Conservative Launch Date:** Monday, March 18 (weekend buffer)

---

## 💬 FINAL NOTES

1. **You're in the home stretch.** The hard work (building) is done. This is just finishing touches (verification + paperwork).

2. **No code changes needed.** Everything you're checking validates that existing code works, not that you need to add anything.

3. **Most likely outcome:** Release APK works fine, you write docs, app goes live within a week.

4. **If something breaks:** It's fixable in 30-60 minutes based on the known risks.

5. **Once submitted:** You'll know if it's accepted within 6 hours (usually).

---

## 🚀 NEXT ACTION

**👉 START TODAY WITH DEVICE TESTING (30 min)**

After that works, move to app store documents (3-4 hours), then submit.

You've got this! 💪

