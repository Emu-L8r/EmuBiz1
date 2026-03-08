# Proposed Action Plan: Mathematical Integrity & UX Alignment

This document outlines the recommended course of action to resolve persistent mathematical discrepancies and improve the user experience across the modern (GUI2) and classic (GUI1) interfaces.

---

## 1. Mathematical Integrity & Accounting Standards

### **Problem**: Discrepancies in Analytics
The Payment Analytics screen and Dashboard still show inconsistent numbers (e.g., $254.00 outstanding vs $0.00). This is caused by "Split Personality" data logic where different screens use different accounting rules (Status-based vs. Value-based).

### **Recommendation**: Force Unified Value-Based Accounting
-   **Eliminate Status-Locks**: Analytics should NEVER rely on the `status` string (e.g., "PAID") to calculate money. Status is just a label.
-   **Single SQL Truth**: Standardize all metrics on two raw numbers from the `invoices` table:
    1.  `Total Billed` = Sum of all line items.
    2.  `Total Paid` = Sum of actual recorded payment transactions.
-   **Accrual Logic**: Define `Outstanding` as exactly `Total Billed - Total Paid`. If an invoice is marked "Paid" but has only $50 recorded against a $100 total, the $50 discrepancy **must** remain visible in the "Outstanding" column.

---

## 2. Status & Payment Interaction

### **Problem**: "Sent" and "Add Payment" friction
The workflow between issuing an invoice ("Sent") and recording money ("Payment") is buggy, particularly for Drafts.

### **Recommendation**: Automated Status Transitions
-   **State Machine Implementation**: Move status management into a central service.
    -   Recording a payment > 0 should automatically transition status from `DRAFT`/`SENT` to `PARTIALLY_PAID`.
    -   Recording a payment >= Total should automatically transition to `PAID`.
-   **Flexible Workflow**: Allow "Add Payment" on any invoice status. A user might receive a deposit on a `DRAFT` invoice before it is officially "Issued." The system should support this without validation errors.

---

## 3. Customer Segments Integration (GUI2)

### **Problem**: GUI2 Customers list is a simple list
The modern "All Customers" page lacks the rich segmentation data (VIP, At-Risk, LTV) available in GUI1.

### **Recommendation**: Tabbed Customer Management
-   **Enhanced GUI2 Customer Screen**:
    -   **Tab 1 (Directory)**: The current clean list of customers.
    -   **Tab 2 (Insights)**: Embed the existing `CustomerSegmentationScreen` directly into the GUI2 flow.
-   **Direct Linking**: Clicking an "At-Risk" or "VIP" segment card should filter the directory list to show only those customers.

---

## 4. Unifying "Classic" (GUI1) Analytics

### **Problem**: GUI1 Settings > Payment Analytics is misleading
This screen still uses the old Snapshot-based system which is frequently out of sync.

### **Recommendation**: Deprecate Snapshots for UI
-   Migrate the GUI1 screen to use the new `PaymentAnalyticsRepositoryV2`. 
-   **One Source of Truth**: Whether the user is in GUI1 or GUI2, the underlying data repository must be the same to ensure identical numbers across the entire app.

---

## 🛠️ Summary of Recommended Steps (Implementation Order)

1.  **Repo Unification**: Point GUI1 analytics to the GUI2 repository.
2.  **Accounting Audit**: Rewrite DAO queries to ignore status strings and use `total - paid` math.
3.  **Payment UX**: Update `RecordPaymentUseCase` to trigger status changes automatically.
4.  **UI Bridge**: Add the "Insights" tab to the GUI2 Customer List screen.

---
**Status**: 💡 PROPOSAL ONLY  
**Goal**: 100% Mathematical Certainty & Professional UX.
