# Phase 2: Pagination, Watermarks & QR Codes - Implementation Plan

**Phase 2 Objectives:**
1. ✅ Multi-page pagination support for invoices with 30+ line items
2. ✅ Status watermarks ("PAID", "OVERDUE") on PDFs
3. ✅ QR code integration for payment references

**Timeline:** Sprint 2 (estimated 3-4 days)

---

## Breakdown

### Step 1: Integrate PdfPageManager into InvoicePdfService
- Refactor PDF rendering to use `PdfPageManager` instead of manual `currentY` tracking
- Implement section-based rendering with automatic page breaks
- Handle multi-page header/footer rendering

### Step 2: Add Status Watermarks
- Add `invoiceStatus` field to `InvoiceSnapshot` 
- Integrate `PdfWatermarkRenderer` into generateInvoice
- Apply watermark after first page header (so it appears on all pages)

### Step 3: QR Code Integration
- Add `zxing` library dependency
- Create `PdfQrCodeRenderer` for generating QR codes
- Encode payment reference and add to "Payment Details" section

### Step 4: Testing & Validation
- Test invoices with 50+ line items
- Verify multi-page rendering
- Check watermark visibility on all pages
- Test QR code scanning

---

## Phase 2 Start: Let's Begin!


