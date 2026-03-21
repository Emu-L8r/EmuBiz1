# Exchange Rate API Configuration & Troubleshooting Guide

**Last Updated:** March 21, 2026  
**Status:** ✅ Hardened (Graceful fallbacks implemented)  
**API Provider:** OpenExchangeRates.com

---

## Overview

Bizap uses a third-party exchange rate API to provide real-time currency conversion for multi-currency invoicing. If the API is unavailable, cached rates are automatically used, ensuring the app continues to function seamlessly.

### Key Features
- ✅ Real-time exchange rate updates (daily sync)
- ✅ Graceful fallback to cached rates if API unavailable
- ✅ Fail-fast validation (clear error messages)
- ✅ User-friendly error handling (no silent failures)

---

## Setup for Developers

### Step 1: Get Free API Key

**Provider:** OpenExchangeRates.com  
**Plan:** Free tier (1,500 requests/month = sufficient for daily sync)  
**Setup Time:** ~5 minutes

**Steps:**
1. Visit: https://www.exchangerate-api.com/
2. Click "Sign Up" → Choose "Free Plan"
3. Verify email
4. Copy API Key from dashboard
5. Add to configuration (see below)

### Step 2: Configure API Key

**Option A: Local Development (Recommended)**

File: `local.properties` (create if doesn't exist)
```properties
EXCHANGE_RATE_API_KEY=your_api_key_here
```

**Note:** `local.properties` is gitignored—it won't be committed.

**Option B: Global Gradle Configuration**

File: `~/.gradle/gradle.properties` (global, for all projects)
```properties
EXCHANGE_RATE_API_KEY=your_api_key_here
```

**Option C: Environment Variable**

```bash
# Linux/Mac (add to ~/.bash_profile or ~/.zshrc)
export EXCHANGE_RATE_API_KEY=your_api_key_here

# Windows PowerShell
$env:EXCHANGE_RATE_API_KEY="your_api_key_here"
```

### Step 3: Build & Verify

```bash
# Build debug version
./gradlew assembleDebug

# Expected output:
# ✅ BUILD SUCCESSFUL
# (no warning about missing API key)
```

**If you see a warning:**
```
⚠️  EXCHANGE_RATE_API_KEY not found!
Exchange rate features will be disabled.
Add to local.properties:
EXCHANGE_RATE_API_KEY=your_api_key_here
```

**Action:** Add API key to one of the configuration files above, then rebuild.

### Step 4: Test in App

1. **Install debug APK:** `./gradlew installDebug`
2. **Open app** → Navigate to Invoices
3. **Create new invoice** → Currency selector should work
4. **Check exchange rates** → Should display current rates (or cached if offline)

---

## For Production Builds

### GitHub Actions Secrets Setup

**Purpose:** Secure API key storage for automated builds

**Steps:**

1. **Go to GitHub Repository Settings**
   - URL: `https://github.com/EmuBiz/Bizap/settings/secrets/actions`

2. **Add Repository Secret**
   - Name: `EXCHANGE_RATE_API_KEY`
   - Value: (paste your actual API key)

3. **In CI/CD Workflow** (`.github/workflows/release.yml`)

```yaml
name: Release Build

on:
  push:
    tags: ['v*']

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Build Release APK
        env:
          EXCHANGE_RATE_API_KEY: ${{ secrets.EXCHANGE_RATE_API_KEY }}
        run: ./gradlew clean assembleRelease
      
      - name: Upload to Play Store (or artifact)
        run: |
          # Upload APK to Play Store or artifact storage
```

---

## Troubleshooting

### Issue: "Exchange rate features disabled"

**Symptom:** Currency selector missing or rates not displaying

**Cause:** API key not configured or blank

**Solution:**
1. Check `local.properties` has API key: `cat local.properties | grep EXCHANGE_RATE_API_KEY`
2. If missing, add it: `echo "EXCHANGE_RATE_API_KEY=your_key" >> local.properties`
3. Rebuild: `./gradlew clean assembleDebug`
4. Reinstall: `./gradlew installDebug`

### Issue: "API Key Invalid or Expired"

**Symptom:** Rates not updating; user sees "Unable to fetch rates"

**Cause:** API key is incorrect or disabled at OpenExchangeRates

**Solution:**
1. **Verify key:** Log in to https://www.exchangerate-api.com/dashboard
2. **Check quota:** Free plan = 1,500 requests/month
3. **If exceeded:** Wait until next month (rate limit resets on 1st)
4. **If invalid:** Delete key, generate new one, update configuration
5. **Update GitHub:** If using CI/CD, update the GitHub secret with new key

### Issue: "Rate Limited (429 Error)"

**Symptom:** Rates update inconsistently; "rate limit exceeded" message

**Cause:** API quota exceeded (using free tier)

**Background:**
- Free plan: 1,500 requests/month
- Bizap: 1 request/day (scheduled sync) = ~30 requests/month
- Safe unless multiple apps sharing same API key

**Solution:**
1. **If using free plan correctly:** It won't happen (30 req/month << 1,500)
2. **If it happens:** Another app or service is using the same key
3. **Fix:** Generate separate API key for Bizap
4. **Monitor:** Check dashboard for usage spike

### Issue: "Network Timeout or Connection Error"

**Symptom:** Rates don't update for several days

**Cause:** Network connectivity issue or API server down

**Expected Behavior:**
- App automatically retries sync daily
- Uses cached rates in interim
- No user-facing error (graceful fallback)

**What to Check:**
1. **Is API server up?** Visit https://www.exchangerate-api.com (should load)
2. **Is device online?** Check internet connection
3. **Is app permission granted?** Bizap needs internet permission (usually auto-granted)

**If Issue Persists:**
1. Manually retry: Open app settings → Refresh rates (if button exists)
2. Wait 24 hours (automatic retry scheduled daily)
3. Check logs: `adb logcat | grep "ExchangeRate"`

### Issue: "API Key Saved Incorrectly"

**Symptom:** App crashes on launch with "Null pointer exception" in exchange rate code

**Cause:** API key contains special characters or wrong format

**Solution:**
1. **Verify key format:** At https://www.exchangerate-api.com
   - Key should be alphanumeric, ~32 characters
   - Example: `abcd1234efgh5678ijkl9012mnop3456`
2. **Re-copy from dashboard:** Make sure no extra spaces
3. **Update configuration:**
   ```properties
   EXCHANGE_RATE_API_KEY=your_key_exactly_as_shown
   ```
4. **Rebuild and test:** `./gradlew installDebug`

---

## Graceful Fallback Behavior

### What Happens If API Unavailable

**Scenario:** API is down, no internet, or invalid key

**User Experience:**
- ✅ App continues to work (no crash)
- ✅ Rates may be slightly out of date (cached)
- ⚠️ User may see "rates last updated X days ago" message
- ✅ Invoices can still be created with cached rates

**Behind the Scenes:**
```kotlin
// ExchangeRateWorker.kt
override suspend fun doWork(): Result {
    return try {
        // Try to fetch fresh rates
        val rates = api.fetchRates(apiKey, "USD")
        dao.insertRates(rates)
        Result.success()  // ✅ Success
    } catch (e: Exception) {
        // Silently fail, use cached rates
        Timber.w("Exchange rate sync failed, using cached rates")
        Result.retry()  // Will try again tomorrow
    }
}
```

**No user-facing crash** ✅

---

## Monitoring & Alerting

### What to Monitor

**Daily Checks:**
- [ ] Rates updating automatically (check: last update timestamp)
- [ ] No errors in Crashlytics related to exchange rates
- [ ] API quota remaining (if paid plan)

**Weekly Review:**
- [ ] User feedback on currency features (if any)
- [ ] Any error patterns in logs
- [ ] API performance (if metrics available)

### Setting Up Alerts

**Firebase Crashlytics:**
- Create alert: Crash rate > 0.1% in `ExchangeRateWorker`
- Create alert: Crash rate > 0.1% in currency-related screens

**Manual Monitoring:**
```bash
# Check app logs for exchange rate errors
adb logcat | grep -E "(ExchangeRate|currency|rate)" | tail -20
```

---

## API Costs & Upgrade Path

### Free Plan (Current)
- **Cost:** $0/month
- **Requests:** 1,500/month
- **Rate Limit:** Sufficient for Bizap (30/month)
- **Support:** Community forums

### When to Upgrade

**Upgrade if:**
- App grows to 1,000+ active users
- Daily rate sync isn't frequent enough
- Need dedicated support

**Upgrade Process:**
1. Log in to OpenExchangeRates.com
2. Choose paid plan (e.g., Professional: $25/month, 300,000 requests)
3. Copy new API key
4. Update `local.properties` and GitHub secrets
5. Rebuild and deploy

---

## Testing the API

### Manual Test: Verify Rates Are Current

```bash
# Connect to device
adb shell

# Open app database
sqlite3 /data/data/com.emul8r.bizap/databases/bizap-db.db

# Query exchange rates
sqlite> SELECT * FROM exchange_rates ORDER BY lastUpdated DESC LIMIT 5;
```

**Expected Output:**
```
base_currency_code | target_currency_code | rate | lastUpdated
USD                | EUR                  | 0.92 | 1710979200000 (today)
USD                | GBP                  | 0.79 | 1710979200000 (today)
...
```

**If rates are old:** API sync failed, app using cache (acceptable)

### Manual Test: Force Refresh (Dev Only)

**If app has "Refresh Rates" button:**
1. Open app
2. Go to Settings → Advanced (or similar)
3. Tap "Refresh Exchange Rates"
4. Check logs: `adb logcat | grep "ExchangeRate"`

**Expected Logs:**
```
D: ExchangeRateWorker: Syncing exchange rates from API...
D: ExchangeRateWorker: Fetched rates for [USD → EUR, GBP, JPY, ...]
D: ExchangeRateWorker: Saved N rates to database
```

---

## Deployment Checklist

Before each release:

- [ ] API key configured in GitHub secrets
- [ ] API key not hardcoded anywhere in source
- [ ] Exchange rate tests passing
- [ ] Rates display correctly in UI
- [ ] Offline mode works (cached rates used)
- [ ] No rate-related crashes in Crashlytics
- [ ] Documentation updated (if API URL changes)

---

## FAQ

**Q: Can users change which currencies are supported?**  
A: Currently, no. Supported: USD, EUR, GBP, JPY, AUD. This could be a future enhancement.

**Q: How often do rates update?**  
A: Daily (scheduled background sync). Real-time is not necessary for invoicing app.

**Q: What if API provider shuts down?**  
A: Switch to different provider (e.g., Fixer.io, OANDA). Update API endpoint + key configuration.

**Q: Can we cache rates indefinitely?**  
A: Yes, but not recommended. Rates should be updated at least weekly. Current policy: keep 30 days of history.

**Q: What if user has no internet?**  
A: App uses cached rates. May be slightly out of date, but functional.

---

**Document Owner:** EmuBiz Engineering Team  
**Last Updated:** March 21, 2026  
**Review Frequency:** Quarterly (or if API issues arise)  
**Next Review:** June 30, 2026

