# Monitoring Strategy

> **Phase 7 — April 2026**
> Covers Firebase Crashlytics integration, event tracking, metrics collection, and alerting.

---

## Current Monitoring Stack

| Tool | Purpose | Status |
|------|---------|--------|
| Firebase Crashlytics | Crash reporting | ✅ Active |
| Firebase Analytics | User event tracking | ✅ Active |
| Timber | Local debug logging | ✅ Active |
| Firebase Auth | User identity | ✅ Active (Phase 3) |

---

## Crash Monitoring (Crashlytics)

Crashlytics is integrated via `FirebaseModule` and the `firebase-crashlytics` plugin.

**Key events tracked automatically:**

- All unhandled exceptions
- ANR (Application Not Responding) events
- Native crashes

**Custom crash keys set by the app:**

```kotlin
FirebaseCrashlytics.getInstance().apply {
    setCustomKey("business_id", businessId)
    setCustomKey("invoice_id", invoiceId)
    setCustomKey("pdf_style", style.name)
    setCustomKey("gui_mode", guiMode.name)
}
```

**Error rate thresholds:**

| Metric | Warning | Critical |
|--------|---------|---------|
| Crash-free sessions | < 99% | < 95% |
| Crashes per day | > 10 | > 50 |
| ANR rate | > 0.5% | > 1% |

---

## Event Tracking (Analytics)

All business events are tracked via `FirebaseEventTracker`.

### Key events to monitor

| Event | Description | Alert if |
|-------|-------------|---------|
| `invoice_created` | New invoice created | Count drops to 0 for > 24h |
| `pdf_generated` | PDF generated | Failure rate > 5% |
| `payment_recorded` | Payment recorded against invoice | — |
| `backup_created` | Database backup created | — |
| `gui_switched` | User switched from Classic to Modern | Useful for migration tracking |

---

## Performance Metrics

Collected via Android Vitals (Play Console) and manual benchmarks:

| Metric | Target | Monitored via |
|--------|--------|---------------|
| Cold start time | < 2 s | Android Vitals |
| ANR rate | < 0.1% | Android Vitals / Crashlytics |
| Crash rate | < 0.1% | Crashlytics |
| PDF generation time | < 3 s | Custom `FirebaseEventTracker` event |

---

## Alert Configuration

### Crashlytics Alerts

Configure in Firebase Console → Crashlytics → Alerts:

- **New issue** — alert on any new crash type
- **Regression** — alert if a previously resolved issue reappears
- **Velocity** — alert if crash rate exceeds 1% in a rolling 1-hour window

### Analytics Anomaly Detection

Firebase Analytics → BigQuery export (if enabled) allows custom SQL alerts.
Simple threshold alerts can be set in Firebase Analytics → Events.

---

## Logging Guidelines

Use `Timber` for all logging. Log levels:

| Level | When |
|-------|------|
| `Timber.v` | Verbose trace (disable in release) |
| `Timber.d` | Debug info (disable in release) |
| `Timber.i` | Key business events (enabled in release) |
| `Timber.w` | Non-fatal warnings (enabled in release) |
| `Timber.e` | Errors with stack traces (enabled in release) |

**Never log:**
- User PII (names, emails, addresses)
- Invoice amounts without masking
- API keys or tokens

---

## Runbook — Responding to Incidents

### High crash rate (> 5 crashes/hour)

1. Check Crashlytics dashboard for the dominant crash type.
2. Identify if it's related to a recent deployment.
3. If so, consider a hotfix or rollback.
4. Post incident summary within 24 hours.

### PDF generation failures (> 5%)

1. Check `pdf_generated` event in Analytics for error property.
2. Reproduce locally with the same invoice data.
3. Check `HtmlPdfInvoiceService` and `SassStyleEngine` logs.
4. Fix and deploy hotfix.
