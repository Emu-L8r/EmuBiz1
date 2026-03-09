# Backend Contract Verification

This document outlines the expected API contracts for the Bizap backend. It serves as the Single Source of Truth for both frontend and backend teams to ensure seamless integration.

## Authentication
- **Method**: Bearer Token
- **Header**: `Authorization: Bearer <YOUR_JWT_TOKEN>`

---

## Invoice Endpoints

### 1. Create Invoice
- **Endpoint**: `POST /invoices`
- **Code Expects**:
    - **Request**: `com.emul8r.bizap.domain.model.Invoice`
    - **Response**: `com.emul8r.bizap.domain.model.Invoice` (with server-assigned `id` and timestamps)
    - **Success Code**: 201 Created
- **Backend Implementation**:
    - Request Shape: `TBD`
    - Response Shape: `TBD`

### 2. Update Invoice
- **Endpoint**: `PUT /invoices/{id}`
- **Code Expects**:
    - **Request**: `com.emul8r.bizap.domain.model.Invoice`
    - **Header**: `If-Unmodified-Since` for optimistic locking
    - **Response**: `com.emul8r.bizap.domain.model.Invoice`
    - **Success Code**: 200 OK
- **Backend Implementation**:
    - Request Shape: `TBD`
    - Response Shape: `TBD`

### 3. Get Invoice
- **Endpoint**: `GET /invoices/{id}`
- **Code Expects**:
    - **Response**: `com.emul8r.bizap.domain.model.Invoice`
    - **Success Code**: 200 OK
- **Backend Implementation**:
    - Response Shape: `TBD`

### 4. Delete Invoice
- **Endpoint**: `DELETE /invoices/{id}`
- **Code Expects**:
    - **Response**: Empty body
    - **Success Code**: 204 No Content
- **Backend Implementation**:
    - Response Shape: `TBD`

### 5. Record Payment
- **Endpoint**: `POST /invoices/{id}/payments`
- **Code Expects**:
    - **Request**: `{ amount, paymentDate, notes }`
    - **Response**: Empty body
    - **Success Code**: 200 OK
- **Backend Implementation**:
    - Request Shape: `TBD`

---

## Customer Endpoints

### 1. Create Customer
- **Endpoint**: `POST /customers`
- **Code Expects**:
    - **Request**: `com.emul8r.bizap.domain.model.Customer`
    - **Response**: `com.emul8r.bizap.domain.model.Customer`
    - **Success Code**: 201 Created
- **Backend Implementation**:
    - Request Shape: `TBD`

### 2. Update Customer
- **Endpoint**: `PUT /customers/{id}`
- **Code Expects**:
    - **Request**: `com.emul8r.bizap.domain.model.Customer`
    - **Response**: `com.emul8r.bizap.domain.model.Customer`
    - **Success Code**: 200 OK
- **Backend Implementation**:
    - Request Shape: `TBD`

### 3. Get Customer
- **Endpoint**: `GET /customers/{id}`
- **Code Expects**:
    - **Response**: `com.emul8r.bizap.domain.model.Customer`
    - **Success Code**: 200 OK
- **Backend Implementation**:
    - Response Shape: `TBD`

---
**Status**: 🟡 PENDING BACKEND VERIFICATION
