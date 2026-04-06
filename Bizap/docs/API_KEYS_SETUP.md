# ?? API Keys Configuration Guide
**Last Updated:** April 7, 2026  
**Applies to:** Bizap v1.0+  
**Status:** Production Ready
---
## Overview
Bizap uses external APIs for certain features. This guide explains how to configure API keys for different environments.
### Current APIs
| API | Purpose | Environment | Status |
|-----|---------|-------------|--------|
| **Exchange Rate API** | Currency conversion | Development & Production | Required for Production |
| **Firebase** | Crash reporting, analytics | All | ? Auto-configured |
---
## Exchange Rate API (OpenExchangeRates)
### What It Does
The Exchange Rate API enables multi-currency support in Bizap, allowing invoices to be created and tracked in different currencies with real-time conversion rates.
### Fallback Behavior
**If the API key is missing or invalid:**
- ? App continues to work normally
- ? All features remain functional
- ?? Currency conversion defaults to USD
- ?? Multi-currency display disabled
**This is the expected behavior for development builds.**
### Getting an API Key
1. Visit: https://openexchangerates.org
2. Sign up for a free account
3. Copy your API key from the dashboard
4. Valid for 1,500 requests/month on free tier
### Configuration
#### For Development (Local Machine)
**Option A: gradle.properties (Recommended)**
```properties
# Local gradle.properties file (NOT committed to git)
# Located at: ~/.gradle/gradle.properties (macOS/Linux) or %USERPROFILE%\.gradle\gradle.properties (Windows)
EXCHANGE_RATE_API_KEY=your_actual_api_key_here
```
#### For Production (CI/CD)
**GitHub Actions:**
```yaml
env:
  EXCHANGE_RATE_API_KEY: ${{ secrets.EXCHANGE_RATE_API_KEY }}
```
**Build Command:**
```bash
./gradlew clean :app:assembleRelease -PEXCHANGE_RATE_API_KEY="${EXCHANGE_RATE_API_KEY}"
```
### Verification
To verify the API key is configured correctly:
1. Build the app: `./gradlew clean :app:assembleDebug`
2. Launch the app
3. Create an invoice with a non-USD currency
4. If conversion rates appear ? API key is working ?
5. If rates show N/A ? API key is missing or invalid ??
---
## Firebase Configuration
### Status
? **Automatically configured** - No manual setup required
Firebase is auto-initialized via `google-services.json` which is included in the repository.
### What's Included
- ?? **Crashlytics** - Automatic crash reporting and stack trace analysis
- ?? **Analytics** - User behavior tracking and engagement metrics
- ?? **Authentication** - User login and account management
- ??? **Cloud Storage** - File storage for documents and exports
### Verification
1. Go to: https://console.firebase.google.com
2. Select Project: **bizap-801c0**
3. Verify all services are active
4. Monitor Crashlytics for incoming crash reports
---
## Production Deployment Checklist
Before deploying to production, verify:
- [ ] Exchange Rate API key obtained and tested
- [ ] API key stored securely (not in git)
- [ ] CI/CD pipeline configured with secret
- [ ] Firebase project active and healthy
- [ ] Crashlytics initialized and receiving reports
- [ ] Test crash sent and appears in Firebase Console
- [ ] All API integrations tested in staging environment
- [ ] Release APK built successfully with `assembleRelease`
---
**Last Verified:** April 7, 2026
