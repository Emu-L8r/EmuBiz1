# Root Cause Analysis: GUI2 Customer Creation & "Split Brain" Data Logic

## 📝 Issue Summary
Customers created via the modern (GUI2) interface do not appear in the application's main database or the classic (GUI1) customer list. Conversely, customers created in GUI1 appear correctly. This indicates a failure in the "Single Source of Truth" architecture for customer management.

---

## 🔴 The Root Cause: "Split Brain" Architecture

After auditing the data flow, I have identified that GUI1 and GUI2 are using two entirely different, disconnected systems for managing customers.

### 1. GUI1 (The "Real" Path)
- **Component**: `CustomerViewModel.kt`
- **Data Flow**: Communicates directly with the Room `CustomerDao`. 
- **Persistence**: Writes directly to the `customers` SQLite table. This is the persistent storage the rest of the app relies on.

### 2. GUI2 (The "Placebo" Path)
- **Component**: `CreateCustomerViewModelV2.kt`
- **Data Flow**: Calls `customerRepository.insert(customer)`.
- **Persistence**: **ZERO.** The repository instance injected into GUI2 is currently a disconnected implementation that does not point to the actual database. It performs an "in-memory" save that vanishes as soon as the screen is closed or the app is restarted.

---

## 🏗️ Impact on Mathematical Integrity
This "Split Brain" logic is the same root cause behind the analytical discrepancies:
- If customers are created in GUI2 but never saved to the database, their invoices cannot be correctly associated with a business profile.
- Aggregated analytics (LTV, Churn, Revenue by Customer) will return `0` or nonsensical numbers because the underlying customer records literally do not exist in the database.

---

## ✅ Recommended Resolution Plan

To resolve this and ensure 100% data consistency, we must unify the customer layer:

1.  **Unify Repository Access**: Update the Hilt `RepositoryModule` to ensure that both GUI1 and GUI2 are injected with the same `CustomerRepositoryImpl` instance.
2.  **DAO Integration**: Re-route the `CreateCustomerViewModelV2` logic to perform a true database insert via the unified DAO.
3.  **Context Enforcement**: Ensure that all new customers are automatically tagged with the `activeBusinessId` to prevent "Global Data Leakage."

---
**Status**: 🔴 **CRITICAL ARCHITECTURAL FLAW IDENTIFIED**  
**Integrity**: Compromised (Fix Required)  
**Fix Complexity**: Medium (Requires Repo & DI Unification)
