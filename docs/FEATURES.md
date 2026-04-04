# Features

Complete feature inventory for BizAP with status, effort estimates, and notes.

---

## Core Features

### Invoice Management

| Feature | Status | GUI1 | GUI2 | Effort | Notes |
|---|---|:---:|:---:|---|---|
| Create invoice | Production | ✅ | ✅ | — | Shared `CreateInvoiceViewModel` |
| Edit invoice | Production | ✅ | ✅ | — | Shared `EditInvoiceViewModel` |
| Delete invoice | Production | ✅ | ✅ | — | Cascade deletes line items |
| List invoices | Production | ✅ | ✅ | — | Pagination + search |
| Invoice status tracking | Production | ✅ | ✅ | — | DRAFT / SENT / PAID / OVERDUE / CANCELLED |
| Line items (qty, price, tax) | Production | ✅ | ✅ | — | Multi-item support |
| Discount per item | Production | ✅ | ✅ | — | Percentage or fixed |

### PDF Generation

| Feature | Status | GUI1 | GUI2 | Effort | Notes |
|---|---|:---:|:---:|---|---|
| Canvas PDF engine | Production | ✅ | ✅ | — | 4 templates: Modern, Professional, Creative, Minimal |
| HTML-CSS PDF engine | Production | ✅ | ✅ | — | 7 styles (see below) |
| PDF Live Preview | Production | ❌ | ✅ | — | WebView in PDF Settings; debounced updates |
| **SASS Professional style** | **Production** | ❌ | ✅ | — | New in April 2026; design-token CSS compiler |
| Modern style | Production | ✅ | ✅ | — | Purple gradient |
| Minimal style | Production | ✅ | ✅ | — | Black/white clean |
| Corporate style | Production | ✅ | ✅ | — | Navy serif |
| Creative style | Production | ✅ | ✅ | — | Orange/teal startup |
| Premium Professional style | Production | ✅ | ✅ | — | Dark navy + blue accents |
| Warm Approachable style | Production | ✅ | ✅ | — | Amber warm tones |
| QR Code on PDF | In-Dev | ❌ | 🔧 | 2 days | `PdfQrCodeRenderer` — needs zxing |
| Watermark (status) | Production | ✅ | ✅ | — | PAID / DRAFT / OVERDUE overlay |
| Logo on PDF | Production | ✅ | ✅ | — | Base64 from business profile |

### Payment Tracking

| Feature | Status | GUI1 | GUI2 | Effort | Notes |
|---|---|:---:|:---:|---|---|
| Record full payment | Production | ✅ | ✅ | — | Updates invoice status to PAID |
| Record partial payment | Production | ✅ | ✅ | — | Accumulates toward total |
| Payment history | Production | ✅ | ✅ | — | Timeline per invoice |
| Payment method tracking | Production | ✅ | ✅ | — | Bank, card, cash, etc. |
| Dunning management | Production | ✅ | ✅ | — | Overdue tracking and reminders |

### Multi-Currency

| Feature | Status | GUI1 | GUI2 | Effort | Notes |
|---|---|:---:|:---:|---|---|
| 30+ currency support | Production | ✅ | ✅ | — | USD, EUR, GBP, AUD, JPY, etc. |
| Live exchange rates | Production | ✅ | ✅ | — | Requires `EXCHANGE_RATE_API_KEY` |
| Currency formatting | Production | ✅ | ✅ | — | Per-locale symbol/position |
| Per-invoice currency | Production | ✅ | ✅ | — | Independent per invoice |

### Tax Integration

| Feature | Status | GUI1 | GUI2 | Effort | Notes |
|---|---|:---:|:---:|---|---|
| Global default tax rate | Production | ✅ | ✅ | — | Set in Invoice Settings |
| Per-invoice tax override | Production | ✅ | ✅ | — | Override on create/edit |
| Tax-inclusive / exclusive | Production | ✅ | ✅ | — | `TaxHandling` enum |
| Tax name customisation | Production | ✅ | ✅ | — | GST / VAT / Sales Tax |

### Analytics Dashboard

| Feature | Status | GUI1 | GUI2 | Effort | Notes |
|---|---|:---:|:---:|---|---|
| Revenue analytics | Production | ✅ | ✅ | — | Monthly / weekly snapshots |
| Payment analytics | Production | ✅ | ✅ | — | Collection rate, overdue |
| Customer analytics | Production | ✅ | ✅ | — | Top customers, retention |
| Business health metrics | Production | ✅ | ✅ | — | Aggregate KPIs |
| Date range filtering | In-Dev | ❌ | 🔧 | 2 days | UI exists; VM filter not wired |
| Analytics drill-down | In-Dev | ❌ | 🔧 | 1 day | Click chart point for details |

### Business & Customer Management

| Feature | Status | GUI1 | GUI2 | Effort | Notes |
|---|---|:---:|:---:|---|---|
| Multiple business profiles | Production | ✅ | ✅ | — | Switch via business switcher |
| Customer CRUD | Production | ✅ | ✅ | — | Shared `CustomerDetailViewModel` |
| Customer search | Production | ✅ | ✅ | — | Real-time search |
| Email validation | Production | ✅ | ✅ | — | Regex + format check |
| Banking details | In-Dev | 🔧 | 🔧 | 1 day | Profile has fields; PDF mapper not fetching |

### Settings & Customisation

| Feature | Status | GUI1 | GUI2 | Effort | Notes |
|---|---|:---:|:---:|---|---|
| PDF engine selection | Production | ❌ | ✅ | — | Canvas vs HTML-CSS |
| PDF style selection | Production | ❌ | ✅ | — | 7 HTML styles + 4 Canvas templates |
| PDF live preview | Production | ❌ | ✅ | — | Real-time WebView |
| Typography settings | Production | ❌ | ✅ | — | Modern / Classic / Rounded |
| Page layout settings | Production | ❌ | ✅ | — | Classic / Modern / Spacious / Compact |
| Colour customisation | Production | ❌ | ✅ | — | Preset + custom hex |
| App theme | Production | ✅ | ✅ | — | Light / Dark / System |
| Backup / Restore | In-Dev | ❌ | 🔧 | 3 days | See `BackupRestoreViewModel` |

---

## Effort Estimates (Remaining Work)

| TODO | File | Days | Priority |
|---|---|---|---|
| QR Code on PDF | `PdfQrCodeRenderer.kt` | 2 | Medium |
| Backup/Restore ops | `BackupRestoreViewModel.kt` | 3 | Medium |
| Date range analytics | `PaymentAnalyticsTabViewModel.kt` | 2 | Medium |
| Banking details in PDF | `InvoiceTemplateDataMapper.kt` | 1 | Medium |
| Analytics drill-down | `RevenueAnalyticsTab.kt` | 1 | Low |
| Firebase Auth | `UserIdProvider.kt` | 3 | Critical |
| Event deserialization | `AnalyticsRepositoryImpl.kt` | 2 | High |

See [TODO_IMPLEMENTATION_CHECKLIST.md](TODO_IMPLEMENTATION_CHECKLIST.md) for details.
