# Bizap API Contracts

## Overview

This document defines the API contracts between the Bizap Android client and the Bizap backend service.

**Base URL:** `https://api.bizap.emul8r.com/v1/`  
**Authentication:** Bearer token (JWT) in `Authorization` header  
**Content-Type:** `application/json`  
**All monetary values:** cents (Long) — e.g. `5000` = $50.00  
**All timestamps:** milliseconds since Unix epoch (Long)

---

## Authentication

All endpoints (except `/health`) require:

```
Authorization: Bearer <jwt_token>
```

JWT tokens expire after 24 hours. Refresh using the `/auth/refresh` endpoint.

---

## Health Check

### GET /health

Returns service availability. No authentication required.

**Response (200 OK):**
```json
{
  "status": "ok",
  "version": "1.0.0",
  "timestamp": 1710000000000
}
```

---

## Invoices

### POST /invoices

Create a new invoice.

**Request:**
```json
{
  "businessProfileId": 1,
  "customerId": 42,
  "customerName": "Acme Corp",
  "customerAddress": "123 Main St, Sydney NSW 2000",
  "customerEmail": "billing@acme.com",
  "date": 1710000000000,
  "dueDate": 1712592000000,
  "totalAmount": 14999,
  "currencyCode": "AUD",
  "taxRate": 0.1,
  "taxAmount": 1363,
  "isQuote": false,
  "status": "DRAFT",
  "items": [
    {
      "description": "Consulting services",
      "quantity": 3.0,
      "unitPrice": 4999
    }
  ],
  "notes": "Net 30 payment terms"
}
```

**Response (201 Created):**
```json
{
  "id": 101,
  "invoiceNumber": "INV-2024-000001",
  "businessProfileId": 1,
  "customerId": 42,
  "totalAmount": 14999,
  "amountPaid": 0,
  "status": "DRAFT",
  "createdAt": 1710000000000,
  "updatedAt": 1710000000000
}
```

**Error Codes:**
- `400` — Invalid request (missing required fields, negative amount)
- `401` — Unauthorised
- `404` — Customer or business not found
- `422` — Validation error (e.g. items list empty)

---

### GET /invoices/{id}

Retrieve a single invoice by ID.

**Response (200 OK):**
```json
{
  "id": 101,
  "invoiceNumber": "INV-2024-000001",
  "businessProfileId": 1,
  "customerId": 42,
  "customerName": "Acme Corp",
  "totalAmount": 14999,
  "amountPaid": 5000,
  "status": "PARTIALLY_PAID",
  "date": 1710000000000,
  "dueDate": 1712592000000,
  "updatedAt": 1710500000000,
  "items": [...]
}
```

**Error Codes:**
- `401` — Unauthorised
- `404` — Invoice not found

---

### PUT /invoices/{id}

Update an existing invoice. Uses optimistic locking.

**Headers:**
```
If-Unmodified-Since: <updatedAt timestamp>
```

**Request:** Same schema as POST /invoices (full replacement).

**Response (200 OK):** Updated invoice object.

**Error Codes:**
- `400` — Invalid request
- `401` — Unauthorised
- `404` — Invoice not found
- `409` — Conflict (stale version — `updatedAt` has changed since last read)

---

### DELETE /invoices/{id}

Soft-delete an invoice (sets `isActive = false`).

**Response (204 No Content)**

**Error Codes:**
- `401` — Unauthorised
- `404` — Invoice not found
- `422` — Cannot delete a PAID invoice

---

### POST /invoices/{id}/payments

Record a payment against an invoice. Atomically updates `amountPaid` and `status`.

**Request:**
```json
{
  "amount": 5000,
  "paymentDate": 1710000000000,
  "notes": "Bank transfer ref ABC123"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `amount` | Long (cents) | Must be > 0 and ≤ outstanding balance |
| `paymentDate` | Long (ms) | Must be ≤ today and ≥ invoice date |
| `notes` | String? | Optional, max 500 characters |

**Response (201 Created):**
```json
{
  "paymentId": 42,
  "invoiceId": 101,
  "amount": 5000,
  "amountPaid": 10000,
  "newStatus": "PAID",
  "paymentDate": 1710000000000,
  "recordedAt": 1710000001000
}
```

**Status Transition Rules:**
- `amountPaid + payment >= totalAmount` → status becomes `PAID`
- `amountPaid + payment > 0` (but < total) → status becomes `PARTIALLY_PAID`

**Error Codes:**
- `400` — Invalid amount (≤ 0, or > outstanding balance)
- `400` — Invalid date (future date, or before invoice date)
- `401` — Unauthorised
- `404` — Invoice not found
- `409` — Conflict (stale version)

---

## Customers

### POST /customers

Create a new customer.

**Request:**
```json
{
  "businessProfileId": 1,
  "name": "Acme Corp",
  "email": "billing@acme.com",
  "phone": "+61 2 9000 0000",
  "address": "123 Main St",
  "city": "Sydney",
  "postalCode": "2000",
  "isActive": true
}
```

**Response (201 Created):**
```json
{
  "id": 42,
  "businessProfileId": 1,
  "name": "Acme Corp",
  "email": "billing@acme.com",
  "createdAt": 1710000000000
}
```

**Error Codes:**
- `400` — Invalid request (missing name or businessProfileId)
- `401` — Unauthorised
- `409` — Customer with this email already exists

---

### PUT /customers/{id}

Update an existing customer record.

**Headers:**
```
If-Unmodified-Since: <updatedAt timestamp>
```

**Request:** Same schema as POST /customers.

**Response (200 OK):** Updated customer object.

**Error Codes:**
- `400` — Invalid request
- `401` — Unauthorised
- `404` — Customer not found
- `409` — Conflict (stale version)

---

### GET /customers/{id}

Retrieve a single customer.

**Response (200 OK):** Customer object (same schema as POST response).

**Error Codes:**
- `401` — Unauthorised
- `404` — Customer not found

---

### DELETE /customers/{id}

Soft-delete a customer.

**Response (204 No Content)**

**Error Codes:**
- `401` — Unauthorised
- `404` — Customer not found

---

## Error Response Format

All errors use a consistent envelope:

```json
{
  "error": {
    "code": "INVOICE_NOT_FOUND",
    "message": "Invoice with id 101 does not exist",
    "httpStatus": 404,
    "timestamp": 1710000000000
  }
}
```

---

## Rate Limits

| Endpoint group | Limit |
|----------------|-------|
| POST /invoices | 100 req/min |
| POST /invoices/{id}/payments | 200 req/min |
| GET requests | 1000 req/min |
| All endpoints (global) | 2000 req/min per API key |

Rate-limit headers returned on every response:
```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 987
X-RateLimit-Reset: 1710000060000
```

---

## Retry Logic

Network errors (`5xx`, timeouts) should be retried with exponential back-off:

| Attempt | Delay |
|---------|-------|
| 1st retry | 1 second |
| 2nd retry | 2 seconds |
| 3rd retry | 4 seconds |
| Give up | — |

`4xx` errors (client errors) must **not** be retried automatically.

---

## OpenAPI Specification

A machine-readable OpenAPI 3.0 spec is available at:

```
https://api.bizap.emul8r.com/v1/openapi.json
```

---

## Monetary Value Guidelines

> **All monetary values in this API are in cents (Long).**

| Representation | Value |
|----------------|-------|
| `14999` | $149.99 |
| `100` | $1.00 |
| `1` | $0.01 |
| `0` | $0.00 |

Client-side conversion (Android): use `CentsFormatter` in `com.emul8r.bizap.utils`.

---

*Last updated: 2026-03-09*
