# Database Schema — Bizap (EmuBiz1)

**Last Updated:** 2026-03-08  
**Database Version:** 32  
**ORM:** Room (Android)  
**Engine:** SQLite

---

## Table of Contents

1. [Schema Overview](#1-schema-overview)
2. [Entity Reference](#2-entity-reference)
3. [Relationships](#3-relationships)
4. [Foreign Key Constraints](#4-foreign-key-constraints)
5. [Indexes](#5-indexes)
6. [Migration History](#6-migration-history)
7. [Example Queries](#7-example-queries)

---

## 1. Schema Overview

The database contains **22 entities** organised into functional groups:

| Group | Entities | Purpose |
|-------|----------|---------|
| **Core Business** | `BusinessProfileEntity` | Business configuration |
| **Customer** | `CustomerEntity` | Customer management |
| **Invoice** | `InvoiceEntity`, `LineItemEntity`, `PrefilledItemEntity` | Invoice management (GUI1) |
| **GUI2 Invoice** | `InvoiceItemEntity`, `PaymentEntity` | Invoice + payment (GUI2) |
| **Documents** | `GeneratedDocumentEntity` | PDF document storage |
| **Currency** | `CurrencyEntity`, `ExchangeRateEntity` | Multi-currency support |
| **Analytics Snapshots** | `InvoiceAnalyticsSnapshot`, `DailyRevenueSnapshot`, `CustomerAnalyticsSnapshot`, `BusinessHealthMetrics` | Revenue analytics |
| **Payment Tracking** | `InvoicePaymentEntity`, `InvoicePaymentSnapshot`, `DailyPaymentSnapshot`, `CollectionMetrics` | Payment analytics |
| **Templates** | `InvoiceTemplate`, `InvoiceCustomField` | Invoice customisation |
| **Offline Sync** | `PendingOperationEntity`, `OfflineOperation` | Offline-first queue |

---

## 2. Entity Reference

### 2.1 `business_profiles` — `BusinessProfileEntity`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `INTEGER` | PK, autoGenerate | Unique identifier |
| `businessName` | `TEXT` | NOT NULL | Trading name |
| `abn` | `TEXT` | nullable | ABN / tax registration number |
| `email` | `TEXT` | nullable | Contact email |
| `phone` | `TEXT` | nullable | Contact phone |
| `address` | `TEXT` | nullable | Business address |
| `logoBase64` | `TEXT` | nullable | Base64-encoded logo image |
| `bankDetails` | `TEXT` | nullable | Payment details for invoices |
| `isTaxRegistered` | `INTEGER` | NOT NULL, default 0 | Boolean: GST/VAT registered |
| `defaultTaxRate` | `REAL` | NOT NULL, default 0.0 | Default tax rate (e.g. 0.10) |

---

### 2.2 `customers` — `CustomerEntity`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `INTEGER` | PK, autoGenerate | Unique identifier |
| `businessProfileId` | `INTEGER` | NOT NULL, FK → `business_profiles.id` | Owning business |
| `name` | `TEXT` | NOT NULL | Contact name |
| `businessName` | `TEXT` | nullable | Customer's business name |
| `email` | `TEXT` | nullable | Email (unique per business) |
| `phone` | `TEXT` | nullable | Phone number |
| `address` | `TEXT` | nullable | Billing address |
| `city` | `TEXT` | nullable | City |
| `postalCode` | `TEXT` | nullable | Postal/ZIP code |
| `isActive` | `INTEGER` | NOT NULL, default 1 | Soft-delete flag (1 = active) |
| `createdAt` | `INTEGER` | NOT NULL | Unix timestamp (ms) |
| `updatedAt` | `INTEGER` | NOT NULL | Unix timestamp (ms) |

**Notes:**
- Soft deletes: set `isActive = 0` instead of physical deletion.
- Email uniqueness is enforced at the repository layer per business.

---

### 2.3 `invoices` — `InvoiceEntity`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `INTEGER` | PK, autoGenerate | Unique identifier |
| `businessProfileId` | `INTEGER` | NOT NULL, FK → `business_profiles.id` | Owning business |
| `customerId` | `INTEGER` | nullable, FK → `customers.id` | Associated customer |
| `date` | `INTEGER` | NOT NULL | Invoice date (Unix ms) |
| `dueDate` | `INTEGER` | nullable | Due date (Unix ms) |
| `totalAmount` | `INTEGER` | NOT NULL | Total in cents |
| `status` | `TEXT` | NOT NULL | `DRAFT`\|`SENT`\|`PAID`\|`PARTIALLY_PAID`\|`OVERDUE` |
| `invoiceNumber` | `TEXT` | nullable | Auto-generated: `INV-YYYY-NNN` |
| `amountPaid` | `INTEGER` | NOT NULL, default 0 | Cumulative paid amount (cents) |
| `invoiceYear` | `INTEGER` | nullable | Calendar year of invoice |
| `invoiceSequence` | `INTEGER` | nullable | Sequential number within year |
| `isActive` | `INTEGER` | NOT NULL, default 1 | Soft-delete flag |
| `createdAt` | `INTEGER` | nullable | Creation timestamp (Unix ms) |
| `templateId` | `TEXT` | nullable | FK → `invoice_templates.id` |
| `customFieldValues` | `TEXT` | nullable | JSON blob of custom field values |

**Derived values (computed at query/repository layer):**
- `outstanding = totalAmount - amountPaid`
- `status` auto-transitions to `PAID` when `amountPaid >= totalAmount`

---

### 2.4 `line_items` — `LineItemEntity`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `INTEGER` | PK, autoGenerate | Unique identifier |
| `invoiceId` | `INTEGER` | NOT NULL, FK → `invoices.id` | Owning invoice |
| `description` | `TEXT` | NOT NULL | Item description |
| `quantity` | `REAL` | NOT NULL | Quantity |
| `unitPrice` | `INTEGER` | NOT NULL | Unit price in cents |
| `currencyCode` | `TEXT` | nullable | ISO 4217 currency code |

---

### 2.5 `prefilled_items` — `PrefilledItemEntity`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `INTEGER` | PK, autoGenerate | Unique identifier |
| `description` | `TEXT` | NOT NULL | Item template description |
| `unitPrice` | `INTEGER` | NOT NULL | Default unit price (cents) |

---

### 2.6 `generated_documents` — `GeneratedDocumentEntity`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `INTEGER` | PK, autoGenerate | Unique identifier |
| `relatedInvoiceId` | `INTEGER` | NOT NULL, FK → `invoices.id` | Source invoice |
| `fileName` | `TEXT` | NOT NULL | File name (e.g. `INV-2024-001.pdf`) |
| `absolutePath` | `TEXT` | NOT NULL | Absolute path on device storage |
| `fileType` | `TEXT` | NOT NULL | `PDF` or `QUOTE` |
| `status` | `TEXT` | NOT NULL | `DRAFT`\|`ARCHIVED`\|`SENT`\|`PAID` |
| `createdAt` | `INTEGER` | NOT NULL | Creation timestamp (Unix ms) |

---

### 2.7 `currencies` — `CurrencyEntity`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `code` | `TEXT` | PK | ISO 4217 code (e.g. `AUD`) |
| `symbol` | `TEXT` | NOT NULL | Display symbol (e.g. `$`) |
| `name` | `TEXT` | NOT NULL | Full name (e.g. `Australian Dollar`) |
| `isSystemDefault` | `INTEGER` | NOT NULL, default 0 | Boolean: system default |
| `isEnabled` | `INTEGER` | NOT NULL, default 1 | Boolean: user-enabled |

---

### 2.8 `exchange_rates` — `ExchangeRateEntity`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `baseCurrencyCode` | `TEXT` | PK (composite) | Base currency (e.g. `AUD`) |
| `targetCurrencyCode` | `TEXT` | PK (composite) | Target currency (e.g. `USD`) |
| `rate` | `REAL` | NOT NULL | Conversion rate |
| `lastUpdated` | `INTEGER` | NOT NULL | Timestamp of last API update |

---

### 2.9 `invoice_analytics_snapshots` — `InvoiceAnalyticsSnapshot`

Denormalised per-invoice analytics for fast dashboard queries.

| Column | Type | Description |
|--------|------|-------------|
| `invoiceId` | `INTEGER` | PK, FK → `invoices.id` |
| `businessProfileId` | `INTEGER` | FK → `business_profiles.id` |
| `status` | `TEXT` | Mirrored invoice status |
| `totalAmount` | `INTEGER` | Total in cents |
| `isPaid` | `INTEGER` | Boolean |
| `isOverdue` | `INTEGER` | Boolean |
| `invoiceDateMs` | `INTEGER` | Invoice date |
| `lineItemCount` | `INTEGER` | Count of line items |

---

### 2.10 `daily_revenue_snapshots` — `DailyRevenueSnapshot`

Daily aggregates for revenue trend charts.

| Column | Type | Description |
|--------|------|-------------|
| `id` | `INTEGER` | PK, autoGenerate |
| `businessProfileId` | `INTEGER` | FK → `business_profiles.id` |
| `dateString` | `TEXT` | `YYYY-MM-DD` format |
| `totalRevenue` | `INTEGER` | Revenue in cents |
| `invoiceCount` | `INTEGER` | Invoices created that day |
| `currencyBreakdown` | `TEXT` | JSON: revenue by currency |
| `dayOverDayGrowth` | `REAL` | Growth percentage vs previous day |
| `version` | `INTEGER` | Optimistic lock version |
| `updatedAtMs` | `INTEGER` | Last update timestamp |

---

### 2.11 `customer_analytics_snapshots` — `CustomerAnalyticsSnapshot`

Per-customer analytics with churn prediction.

| Column | Type | Description |
|--------|------|-------------|
| `customerId` | `INTEGER` | PK, FK → `customers.id` |
| `businessProfileId` | `INTEGER` | FK → `business_profiles.id` |
| `totalRevenue` | `INTEGER` | Lifetime revenue in cents |
| `invoiceCount` | `INTEGER` | Total invoices issued |
| `segment` | `TEXT` | `NEW`\|`LOYAL`\|`AT_RISK`\|`DORMANT` |
| `churnRiskScore` | `REAL` | 0.0–1.0 churn probability |
| `isPredictedToChurn` | `INTEGER` | Boolean |

---

### 2.12 `business_health_metrics` — `BusinessHealthMetrics`

Aggregate KPI metrics per business profile.

| Column | Type | Description |
|--------|------|-------------|
| `businessProfileId` | `INTEGER` | PK, FK → `business_profiles.id` |
| `healthScore` | `INTEGER` | 0–100 composite score |
| `healthStatus` | `TEXT` | `EXCELLENT`\|`GOOD`\|`FAIR`\|`POOR` |
| `monthlyRecurringRevenue` | `INTEGER` | MRR in cents |
| `overduePercentage` | `REAL` | % of invoices overdue |
| `onTimePaymentRate` | `REAL` | % paid on time |
| `activeCustomerCount` | `INTEGER` | Active customers count |

---

### 2.13 `invoice_payments` — `InvoicePaymentEntity`

Individual payment transaction records (GUI1 payment tracking).

| Column | Type | Description |
|--------|------|-------------|
| `id` | `INTEGER` | PK, autoGenerate |
| `invoiceId` | `INTEGER` | FK → `invoices.id` |
| `amountPaid` | `INTEGER` | Amount paid in cents |
| `paymentDate` | `INTEGER` | Payment date (Unix ms) |
| `paymentMethod` | `TEXT` | e.g. `BANK_TRANSFER`, `CASH` |
| `transactionReference` | `TEXT` | Optional reference |

---

### 2.14 `invoice_payment_snapshots` — `InvoicePaymentSnapshot`

Payment status snapshots for collections dashboard.

| Column | Type | Description |
|--------|------|-------------|
| `invoiceId` | `INTEGER` | PK, FK → `invoices.id` |
| `businessProfileId` | `INTEGER` | FK → `business_profiles.id` |
| `customerId` | `INTEGER` | FK → `customers.id` |
| `totalAmount` | `INTEGER` | Total in cents |
| `paidAmount` | `INTEGER` | Paid in cents |
| `outstandingAmount` | `INTEGER` | Outstanding in cents |
| `paymentStatus` | `TEXT` | Status classification |
| `ageingBucket` | `TEXT` | `CURRENT`\|`30_DAYS`\|`60_DAYS`\|`90_DAYS`\|`90_PLUS` |
| `daysOverdue` | `INTEGER` | Days since due date |
| `isAtRisk` | `INTEGER` | Boolean risk flag |
| `riskScore` | `REAL` | 0.0–1.0 risk score |

---

### 2.15 `daily_payment_snapshots` — `DailyPaymentSnapshot`

Daily cash-flow tracking.

| Column | Type | Description |
|--------|------|-------------|
| `id` | `INTEGER` | PK, autoGenerate |
| `businessProfileId` | `INTEGER` | FK → `business_profiles.id` |
| `snapshotDate` | `TEXT` | `YYYY-MM-DD` |
| `paymentsReceivedCount` | `INTEGER` | Payments received count |
| `paymentsReceivedAmount` | `INTEGER` | Total received (cents) |
| `invoicesOverdueCount` | `INTEGER` | Overdue invoice count |
| `outstandingCurrent` | `INTEGER` | Current outstanding (cents) |
| `outstandingPast30` | `INTEGER` | 31–60 days outstanding |
| `outstandingPast60` | `INTEGER` | 61–90 days outstanding |
| `outstandingPast90` | `INTEGER` | 90+ days outstanding |

---

### 2.16 `collection_metrics` — `CollectionMetrics`

Collection efficiency KPIs per business.

| Column | Type | Description |
|--------|------|-------------|
| `businessProfileId` | `INTEGER` | PK, FK → `business_profiles.id` |
| `metricsDate` | `TEXT` | `YYYY-MM-DD` |
| `totalInvoiceAmount` | `INTEGER` | Total issued (cents) |
| `totalPaidAmount` | `INTEGER` | Total collected (cents) |
| `collectionRate` | `REAL` | % collected |
| `averageDaysToPayment` | `REAL` | Average DSO |
| `overdueInvoiceCount` | `INTEGER` | Count of overdue invoices |

---

### 2.17 `invoice_templates` — `InvoiceTemplate`

User-defined invoice design templates.

| Column | Type | Description |
|--------|------|-------------|
| `id` | `TEXT` | PK (UUID) |
| `businessProfileId` | `INTEGER` | FK → `business_profiles.id` |
| `name` | `TEXT` | Template name |
| `designType` | `TEXT` | `PROFESSIONAL`\|`MINIMAL`\|`BRANDED` |
| `primaryColor` | `TEXT` | Hex colour |
| `secondaryColor` | `TEXT` | Hex colour |
| `fontFamily` | `TEXT` | Font name |
| `companyName` | `TEXT` | Override company name |
| `logoFileName` | `TEXT` | Logo file reference |
| `hideLineItems` | `INTEGER` | Boolean |
| `isDefault` | `INTEGER` | Boolean: default template |
| `isActive` | `INTEGER` | Boolean: soft delete |

---

### 2.18 `invoice_custom_fields` — `InvoiceCustomField`

Custom fields per invoice template.

| Column | Type | Description |
|--------|------|-------------|
| `id` | `TEXT` | PK (UUID) |
| `templateId` | `TEXT` | FK → `invoice_templates.id` |
| `label` | `TEXT` | Field label |
| `fieldType` | `TEXT` | `TEXT`\|`NUMBER`\|`DATE` |
| `displayOrder` | `INTEGER` | Sort order |
| `isRequired` | `INTEGER` | Boolean |
| `isActive` | `INTEGER` | Boolean: soft delete |

---

### 2.19 `pending_operations` — `PendingOperationEntity`

Primary offline operation queue.

| Column | Type | Description |
|--------|------|-------------|
| `id` | `INTEGER` | PK, autoGenerate |
| `operationType` | `TEXT` | `CREATE`\|`UPDATE`\|`DELETE` |
| `entityType` | `TEXT` | Entity class name |
| `entityId` | `TEXT` | Entity primary key |
| `payload` | `TEXT` | JSON-serialised entity |
| `status` | `TEXT` | `PENDING`\|`IN_PROGRESS`\|`FAILED`\|`COMPLETED` |
| `attemptCount` | `INTEGER` | Retry counter |
| `errorMessage` | `TEXT` | Last error message |

---

### 2.20 `offline_operations` — `OfflineOperation`

Secondary offline operation queue used by `OfflineQueueService`.

| Column | Type | Description |
|--------|------|-------------|
| `id` | `INTEGER` | PK, autoGenerate |
| `operationType` | `TEXT` | `CREATE`\|`UPDATE`\|`DELETE` |
| `entityId` | `TEXT` | Entity primary key |
| `entityData` | `TEXT` | JSON-serialised entity |
| `businessProfileId` | `INTEGER` | Owning business |
| `timestampMs` | `INTEGER` | Operation timestamp |
| `status` | `TEXT` | `PENDING` |
| `retryCount` | `INTEGER` | Retry counter |
| `errorMessage` | `TEXT` | Last error message |

---

### 2.21 `invoice_items` — `InvoiceItemEntity` (GUI2)

Line items for GUI2 invoices.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `INTEGER` | PK, autoGenerate | Unique identifier |
| `businessId` | `INTEGER` | NOT NULL | Owning business |
| `invoiceId` | `INTEGER` | NOT NULL, FK → `invoices.id` | Owning invoice |
| `description` | `TEXT` | NOT NULL | Item description |
| `quantity` | `REAL` | NOT NULL | Quantity (supports decimals) |
| `unitPrice` | `INTEGER` | NOT NULL | Unit price in cents |
| `totalPrice` | `INTEGER` | NOT NULL | `quantity × unitPrice` in cents |
| `createdAt` | `INTEGER` | NOT NULL | Creation timestamp (Unix ms) |

---

### 2.22 `payments` — `PaymentEntity` (GUI2)

Payment records for GUI2 invoices.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `INTEGER` | PK, autoGenerate | Unique identifier |
| `businessId` | `INTEGER` | NOT NULL | Owning business |
| `invoiceId` | `INTEGER` | NOT NULL, FK → `invoices.id` | Paid invoice |
| `amount` | `INTEGER` | NOT NULL | Payment amount in cents |
| `paymentDate` | `INTEGER` | NOT NULL | Payment date (Unix ms) |
| `notes` | `TEXT` | nullable | Optional payment notes |
| `createdAt` | `INTEGER` | NOT NULL | Creation timestamp (Unix ms) |

---

## 3. Relationships

```
BusinessProfileEntity (1)
  ├──── (N) CustomerEntity
  │           └──── (N) InvoiceEntity
  │                       ├──── (N) LineItemEntity        (GUI1)
  │                       ├──── (N) InvoiceItemEntity     (GUI2)
  │                       ├──── (N) InvoicePaymentEntity  (GUI1 payment tracking)
  │                       ├──── (N) PaymentEntity         (GUI2 payment recording)
  │                       └──── (1) GeneratedDocumentEntity
  ├──── (N) DailyRevenueSnapshot
  ├──── (N) DailyPaymentSnapshot
  ├──── (1) BusinessHealthMetrics
  └──── (1) CollectionMetrics
```

### Relationship Query Types (Room Relations)

| Relation | Query Class | Description |
|----------|-------------|-------------|
| Customer → Invoices | `CustomerWithInvoices` | Customer with embedded invoice list |
| Invoice → LineItems (GUI1) | `InvoiceWithItems` | Invoice with embedded line items |
| Invoice → InvoiceItems (GUI2) | `InvoiceWithLineItems` | Invoice with GUI2 line items |
| Invoice → Payments (GUI2) | `InvoiceWithPayments` | Invoice with payment list |

---

## 4. Foreign Key Constraints

All foreign keys are enforced by Room's `@ForeignKey` annotation.

| Table | Column | References | On Delete |
|-------|--------|-----------|-----------|
| `customers` | `businessProfileId` | `business_profiles.id` | CASCADE |
| `invoices` | `businessProfileId` | `business_profiles.id` | CASCADE |
| `invoices` | `customerId` | `customers.id` | SET NULL |
| `line_items` | `invoiceId` | `invoices.id` | CASCADE |
| `invoice_items` | `invoiceId` | `invoices.id` | CASCADE |
| `payments` | `invoiceId` | `invoices.id` | CASCADE |
| `generated_documents` | `relatedInvoiceId` | `invoices.id` | CASCADE |
| `invoice_analytics_snapshots` | `invoiceId` | `invoices.id` | CASCADE |
| `invoice_payment_snapshots` | `invoiceId` | `invoices.id` | CASCADE |
| `invoice_custom_fields` | `templateId` | `invoice_templates.id` | CASCADE |

---

## 5. Indexes

Room creates indexes automatically for all foreign key columns. Additional indexes:

| Table | Column(s) | Purpose |
|-------|-----------|---------|
| `invoices` | `businessProfileId, status` | Filter invoices by status |
| `invoices` | `businessProfileId, customerId` | Customer invoice lookup |
| `invoices` | `invoiceNumber` | Unique invoice number lookup |
| `customers` | `businessProfileId, isActive` | Active customer list |
| `customers` | `email` | Email uniqueness check |
| `daily_revenue_snapshots` | `businessProfileId, dateString` | Date-range revenue queries |
| `payments` | `invoiceId` | Payments by invoice |

---

## 6. Migration History

| Migration | Summary |
|-----------|---------|
| `21 → 22` | Initial schema evolution |
| `22 → 23` | Schema evolution |
| `23 → 24` | Schema evolution |
| `24 → 25` | Analytics snapshot tables added |
| `25 → 26` | Customer analytics snapshot added |
| `26 → 27` | `version`/`updatedAtMs` added to `daily_revenue_snapshots` |
| `27 → 28` | Payment tracking tables added |
| `28 → 29` | Schema refinements |
| `29 → 30` | Offline operations support |
| `30 → 31` | `isActive`, `city`, `postalCode` added to `customers` |
| `31 → 32` | `invoiceNumber`, `isActive`, `createdAt` added to `invoices`; `invoice_items` and `payments` tables created for GUI2 |

All migrations reside in:  
`Bizap/app/src/main/java/com/emul8r/bizap/data/local/migrations/`

And are registered in `DatabaseModule.kt` via `.addMigrations(...)`.

---

## 7. Example Queries

### 7.1 Get All Active Customers for a Business

```kotlin
// CustomerDaoV2
@Query("SELECT * FROM customers WHERE businessProfileId = :businessId AND isActive = 1 ORDER BY name ASC")
fun getActiveCustomers(businessId: Long): Flow<List<CustomerEntity>>
```

### 7.2 Get Invoice with Line Items (GUI2)

```kotlin
// InvoiceDaoV2 - returns InvoiceWithLineItems
@Transaction
@Query("SELECT * FROM invoices WHERE id = :invoiceId")
suspend fun getInvoiceWithItems(invoiceId: Long): InvoiceWithLineItems?
```

### 7.3 Get Outstanding Amount for a Business

```kotlin
// InvoiceDaoV2
@Query("""
    SELECT SUM(totalAmount - amountPaid) 
    FROM invoices 
    WHERE businessId = :businessId 
      AND status NOT IN ('PAID') 
      AND isActive = 1
""")
fun getOutstandingAmount(businessId: Long): Flow<Long?>
```

### 7.4 Record a Payment (Atomic Transaction)

```kotlin
// PaymentRepositoryV2
database.withTransaction {
    paymentDaoV2.insert(PaymentEntity(invoiceId = id, amount = cents, ...))
    invoiceDaoV2.updateAmountPaid(id, newAmountPaid)
    invoiceDaoV2.updateStatus(id, if (fullyPaid) "PAID" else "PARTIALLY_PAID")
}
```

### 7.5 Month-to-Date Revenue

```kotlin
// InvoiceDaoV2
@Query("""
    SELECT SUM(totalAmount) 
    FROM invoices 
    WHERE businessId = :businessId 
      AND status = 'PAID'
      AND date >= :startOfMonth
""")
fun getMtdRevenue(businessId: Long, startOfMonth: Long): Flow<Long?>
```
