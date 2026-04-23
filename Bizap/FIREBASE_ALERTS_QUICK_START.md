# 🔥 FIREBASE ALERTS QUICK START GUIDE
**April 23, 2026 — 30 Minute Setup**

---

## OVERVIEW

Set up 4 critical Firebase Crashlytics alerts + Slack integration to enable production monitoring.

**Time:** 30-45 minutes  
**Prerequisites:** Firebase Console access + Slack workspace admin  
**Impact:** CRITICAL for production readiness

---

## ALERT CONFIGURATION

### Alert #1: Crash Rate Spike 🔴
**Purpose:** Immediate notification if app becomes unstable

**Steps:**
1. Open Firebase Console → Bizap project → Crashlytics
2. Click "Alerts" tab
3. Create new alert:
   - **Name:** "Crash Rate Spike"
   - **Metric:** Crash-free sessions
   - **Threshold:** < 95%
   - **Duration:** Last 1 hour
   - **Notification:** Email + Slack

**What It Does:**
- Triggers if < 95% of sessions complete without crash
- Indicates potential critical issue
- Requires immediate investigation

**Action When Triggered:**
1. Check Crashlytics dashboard for top crashes
2. Identify common pattern
3. Rollback if necessary
4. Deploy hotfix

---

### Alert #2: New Fatal Issue 🔴
**Purpose:** Detect new crash patterns immediately

**Steps:**
1. In Alerts tab, create new alert:
   - **Name:** "New Fatal Issue"
   - **Metric:** New Issues
   - **Severity:** Fatal
   - **Notification:** Email + Slack (immediate)

**What It Does:**
- Alerts on ANY new crash type detected
- Helps catch regressions early
- Prevents widespread impact

**Action When Triggered:**
1. Review the new crash details
2. Check affected version
3. Verify if known issue or new
4. Prioritize fix accordingly

---

### Alert #3: ANR Events 🟡
**Purpose:** Monitor Application Not Responding events

**Steps:**
1. Create new alert:
   - **Name:** "ANR Events"
   - **Metric:** ANR events
   - **Threshold:** > 10 per day
   - **Notification:** Slack (daily digest)

**What It Does:**
- Alerts if app freezes frequently
- Indicates performance issue
- Often precedes crashes

**Action When Triggered:**
1. Check Android Profiler recordings
2. Identify long-running operations
3. Profile main thread usage
4. Optimize heavy operations

---

### Alert #4: Memory/Startup Performance 🟡
**Purpose:** Monitor performance degradation

**Steps:**
1. Create new alert:
   - **Name:** "Memory/Startup Alert"
   - **Metric:** Performance (startup time)
   - **Threshold:** > 5 seconds OR memory > 120MB
   - **Duration:** Last 1 hour
   - **Notification:** Slack (daily)

**What It Does:**
- Detects startup time regression
- Monitors memory growth
- Warns of performance issues

**Action When Triggered:**
1. Measure startup time locally
2. Compare with baseline
3. Profile with Android Profiler
4. Identify recent changes
5. Optimize or revert

---

## SLACK INTEGRATION

### Step 1: Create Slack Channel
```
1. Open Slack workspace
2. Create channel: #bizap-alerts
3. Make it private or public as needed
4. Add team members
5. Set channel description: "Bizap Firebase Alerts"
```

### Step 2: Connect Firebase to Slack
```
1. In Firebase → Project Settings → Integrations
2. Find Slack section
3. Click "Link to Slack"
4. Authorize Firebase bot
5. Select #bizap-alerts channel
6. Confirm connection
```

### Step 3: Configure Alert Routing
```
For each alert created:
1. In alert settings → Notifications
2. Select "Slack"
3. Choose #bizap-alerts channel
4. Also enable Email (backup)
```

### Step 4: Test Alert Delivery
```
1. In Crashlytics → Issues
2. Find any crash
3. Click "Send test notification"
4. Verify message appears in #bizap-alerts
5. Confirm email notification sent
```

---

## ALERT RESPONSE RUNBOOK

### When Crash Rate Spike Fires 🔴
```
1. DROP everything (critical)
2. Open Firebase Crashlytics
3. Check top 5 crashes
4. Identify common pattern
5. Determine affected version
6. Rollback or hotfix?
7. Deploy immediately
8. Monitor crash rate
9. Post-incident review
```

### When New Fatal Issue Fires 🔴
```
1. Check Slack notification details
2. Open Crashlytics → view issue
3. Read stack trace
4. Check affected version range
5. Determine impact scope
6. Is it worth emergency fix?
7. If yes: hotfix + deploy immediately
8. If no: schedule for next release
9. Monitor for additional occurrences
```

### When ANR Events Fire 🟡
```
1. Review Slack notification
2. Check if new pattern or ongoing
3. Look for affected devices
4. Is it OS-specific?
5. Investigate root cause
6. Schedule fix for next sprint
7. Monitor trend
```

### When Performance Alert Fires 🟡
```
1. Review what changed recently
2. Measure startup time locally
3. Profile with Android Profiler
4. Compare with baseline
5. Identify bottleneck
6. Implement optimization
7. Test improvement
8. Deploy in next release
```

---

## VERIFICATION CHECKLIST

After setup is complete, verify everything works:

```
[ ] Firebase Console shows 4 alerts
[ ] All alerts status: "Active" (green)
[ ] Slack channel #bizap-alerts created
[ ] Firebase bot has access
[ ] Test alert received in Slack
[ ] Team members can access channel
[ ] Email notifications working
[ ] Alert thresholds make sense
[ ] Response runbook documented
[ ] Team trained on procedures
```

---

## CONFIGURATION REFERENCE

| Alert | Metric | Threshold | Duration | Channel |
|-------|--------|-----------|----------|---------|
| Crash Rate | Crash-free % | < 95% | 1 hour | Slack |
| New Fatal | New Issues | Any | Immediate | Slack |
| ANRs | ANR Count | > 10/day | Daily | Slack |
| Performance | Startup Time | > 5s | 1 hour | Slack |

---

## POST-SETUP TASKS

1. **Train Team** (10 minutes)
   - Show team how to access alerts
   - Explain what each alert means
   - Share response runbook
   - Practice once with test alert

2. **Document** (5 minutes)
   - Add alert details to wiki
   - Link response procedures
   - Add to on-call runbook
   - Share with stakeholders

3. **Monitor** (ongoing)
   - Check dashboard daily
   - Review alert history weekly
   - Adjust thresholds if needed
   - Celebrate zero-alert days!

---

## SUCCESS METRICS

### After 1 Day
- [ ] 4 alerts configured
- [ ] Slack integration working
- [ ] Test alert successful

### After 1 Week
- [ ] Zero false positives
- [ ] Team comfortable with system
- [ ] Response procedures clear

### After 1 Month
- [ ] Alerts prevented at least 1 major issue
- [ ] Average response time < 30 min
- [ ] Crash-free sessions > 98%

---

**Estimated Time:** 30-45 minutes  
**Difficulty:** Easy  
**Impact:** Critical  
**Next Step:** Performance Baselines (1 hour)

---

**Status:** Ready to implement  
**Date:** April 23, 2026

