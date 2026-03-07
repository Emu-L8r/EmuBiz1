# Feature Roadmap: v0.2.0 through v1.0.0

**Project:** Bizap  
**Document Version:** 1.0  
**Created:** March 7, 2026  
**Status:** Draft — subject to revision based on user feedback

---

## v0.2.0 — UX Polish & Performance (Q2 2026)

**Theme:** Make the MVP feel like a real product

### UX & Accessibility
- [ ] Full TalkBack / accessibility support (content descriptions, semantic groupings)
- [ ] Empty state screens for invoices, customers, and revenue with helpful CTAs
- [ ] Loading skeletons / shimmer effects instead of blank states
- [ ] Swipe-to-delete on invoice and customer list items
- [ ] Search and filter on invoice list (by status, customer, date)
- [ ] Invoice list sorting (by date, amount, status)
- [ ] Tablet and landscape layout optimization

### Input Validation & Error Handling
- [ ] Form validation feedback on invoice creation (required fields, amount formats)
- [ ] Error boundaries on all screens — no blank/crashed screens for users
- [ ] Retry UI for failed database operations
- [ ] Duplicate invoice detection warning

### Build & Performance
- [ ] Baseline profiles for app startup optimization
- [ ] Lazy loading for invoice list with pagination
- [ ] Reduced APK size via R8 optimization review
- [ ] Startup time benchmarked and tracked (<2s cold start target)

### Developer Experience
- [ ] Integration tests for database migrations
- [ ] End-to-end UI tests with Espresso for happy paths
- [ ] Increase test coverage from ~45% to ≥60%
- [ ] Fix 5 Gradle soft-deprecation warnings

**Estimated Effort:** 6–8 weeks (2 engineers)

---

## v0.3.0 — Security & Notifications (Q3 2026)

**Theme:** Protect user data, keep users informed

### Security Hardening
- [ ] SQLCipher integration — encrypt SQLite database at rest
- [ ] Input sanitization throughout all form fields
- [ ] Certificate pinning (if any API endpoints introduced)
- [ ] Sensitive data masking in logs
- [ ] Security audit (penetration test or OWASP checklist review)

### Notifications
- [ ] Invoice overdue push notifications (local WorkManager)
- [ ] Payment received notification (when status changed to PAID)
- [ ] Configurable reminder schedule (3 days before due, on due date, 7 days after)
- [ ] Notification settings screen in app

### Data & Sync Capabilities
- [ ] Local backup to device storage (encrypted JSON export)
- [ ] Import from CSV (customer list, invoice history)
- [ ] PDF export of revenue reports and customer statements
- [ ] Investigate cloud sync options (Firebase vs self-hosted)

### Multi-Currency Improvements
- [ ] Exchange rate refresh from live API (with offline fallback)
- [ ] Currency conversion history in invoice detail
- [ ] Multi-currency revenue summary with base currency conversion

**Estimated Effort:** 8–10 weeks (2–3 engineers)

---

## v1.0.0 — Enterprise Features (Q4 2026)

**Theme:** Production-grade for business teams

### Authentication & Identity
- [ ] Local PIN / biometric authentication (on-device)
- [ ] Optional cloud account (email + password)
- [ ] Session timeout and automatic lock

### Role-Based Access Control (RBAC)
- [ ] Define roles: Admin, Accountant, View-only
- [ ] Role enforcement on invoice editing and business profile changes
- [ ] Invite team members by email (requires cloud account)

### Audit Logging
- [ ] Immutable audit trail for invoice status changes
- [ ] User attribution on all actions
- [ ] Exportable audit log (PDF or CSV)
- [ ] Tampering detection (hash chain or signed records)

### Payment Gateway Integration
- [ ] Stripe payment links embedded in invoice PDF
- [ ] PayPal invoice payment support
- [ ] Payment status automatically updated on receipt (via webhook or polling)

### Accounting Integration
- [ ] Xero export (CSV or direct API)
- [ ] QuickBooks export
- [ ] MYOB compatibility (Australia market)

### Enterprise Reporting
- [ ] Profit & Loss report (invoice-based)
- [ ] Outstanding receivables aging report
- [ ] Customer lifetime value report
- [ ] Configurable financial year (Australia: July–June)

**Estimated Effort:** 12–16 weeks (3–4 engineers)

---

## Resource Planning

| Milestone | Engineering FTEs | Duration | Key Dependencies |
|---|---|---|---|
| v0.2.0 | 2 | 6–8 weeks | User feedback from v0.1.0 |
| v0.3.0 | 2–3 | 8–10 weeks | v0.2.0 complete, security audit |
| v1.0.0 | 3–4 | 12–16 weeks | v0.3.0 complete, backend infra |

---

## Success Metrics

| Milestone | Target Metric |
|---|---|
| v0.2.0 | <2s cold start, ≥60% test coverage, 0 Lint errors |
| v0.3.0 | Database encrypted, 0 security audit findings |
| v1.0.0 | 99.5% crash-free rate, <200ms p95 load time |
