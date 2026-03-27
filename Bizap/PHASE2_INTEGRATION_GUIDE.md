# Phase 2: Quick Integration Guide

## What Changed

### For PDF Generation:
1. **PdfPageManager is now required** - Handles pagination automatically
2. **InvoiceSnapshot now includes invoiceStatus** - For watermark rendering
3. **Automatic page breaks** - No manual page management needed

### What You Need to Know

#### 1. Building Invoices with Status
When creating an `InvoiceSnapshot`, always include the status:

```kotlin
val snapshot = InvoiceSnapshot(
    invoiceId = invoice.id,
    invoiceNumber = invoice.invoiceNumber,
    // ... other fields ...
    invoiceStatus = invoice.status.toString()  // NEW: Add this
)
```

#### 2. PDF Generation is Now Automatic Multi-Page
No special handling needed. Just call:

```kotlin
val pdfFile = invoicePdfService.generatePdf(snapshot, isQuote = false)
```

The service will:
- ✅ Auto-detect large invoices (30+ items)
- ✅ Create new pages automatically
- ✅ Render watermarks on page 1
- ✅ Handle all sections (header, table, totals, notes)

#### 3. Watermarks Appear Automatically
Status-based rendering:
- DRAFT → No watermark
- SENT → Gray watermark
- PAID → Green "PAID" watermark
- OVERDUE → Red "OVERDUE" watermark
- PARTIALLY_PAID → Orange watermark
- CANCELLED → Red "CANCELLED" watermark

#### 4. QR Codes (Coming Soon)
Scaffold is ready. When zxing library is added:

```kotlin
val qrRenderer = PdfQrCodeRenderer(canvas)
qrRenderer.drawPaymentQrCode(snapshot.invoiceNumber)
```

---

## Testing Your Changes

### Quick Test Script
```bash
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap

# Build
./gradlew.bat build -x test

# Check for compilation errors
grep -i "error" build.log
```

### Manual Testing Checklist
1. Open Invoice Detail Screen
2. Create test invoice with 50 line items
3. Export to PDF
4. Open PDF and verify:
   - [ ] Multiple pages (should be 3-4 pages)
   - [ ] Table header on each page
   - [ ] Totals on last page
   - [ ] Watermark on page 1 (if not DRAFT)
   - [ ] Logo appears (if set)
   - [ ] Table has alternating row colors
   - [ ] No text overflow

---

## Troubleshooting

### Issue: "Unresolved reference 'canvas'"
**Solution:** Make sure `canvas` is updated after page break:
```kotlin
canvas = pageManager.ensureSpace(40f)  // Returns the current/new canvas
```

### Issue: "currentY negative" or "Text drawn off-page"
**Solution:** Always use `pageManager.currentY` instead of manual tracking:
```kotlin
// ❌ Wrong
var y = 100f
y += 50f
canvas.drawText("Hello", 50f, y, paint)

// ✅ Correct
pageManager.setY(100f)
pageManager.advanceY(50f)
canvas.drawText("Hello", 50f, pageManager.currentY, paint)
```

### Issue: "Watermark not visible"
**Solution:** Watermarks only appear for non-DRAFT invoices. Check:
```kotlin
snapshot.invoiceStatus  // Must be something other than "DRAFT"
```

---

## Files You May Need to Update

### If you're building InvoiceSnapshot elsewhere:
Add the `invoiceStatus` field:

```kotlin
// In EditInvoiceViewModel.kt or similar
val snapshot = InvoiceSnapshot(
    // ... existing fields ...
    invoiceStatus = invoice.status.toString()  // ADD THIS
)
```

### If you're using custom PDF renderers:
Update to accept `invoiceStatus`:
```kotlin
fun buildSnapshot(...): InvoiceSnapshot {
    return InvoiceSnapshot(
        // ...
        invoiceStatus = invoice.status.toString()
    )
}
```

---

## Key Classes

| Class | Purpose | File |
|-------|---------|------|
| `PdfPageManager` | Handles pagination & page breaks | `domain/pdf/PdfPageManager.kt` |
| `PdfWatermarkRenderer` | Renders status watermarks | `domain/pdf/PdfWatermarkRenderer.kt` |
| `PdfBrandingRenderer` | Renders logos | `domain/pdf/PdfBrandingRenderer.kt` |
| `PdfQrCodeRenderer` | QR code generation (scaffold) | `domain/pdf/PdfQrCodeRenderer.kt` |
| `PdfTableRenderer` | Line items table with zebra striping | `domain/pdf/PdfTableRenderer.kt` |

---

## Next Steps

1. ✅ Build & test Phase 2
2. ⏭️ Add zxing library for QR codes
3. ⏭️ Implement `PdfQrCodeRenderer` methods
4. ⏭️ Add page numbers and footers
5. ⏭️ E-signature support

---

## Questions?

Refer to:
- `PHASE2_IMPLEMENTATION_COMPLETE.md` - Full technical details
- `PHASE2_IMPLEMENTATION_PLAN.md` - Architecture overview
- Comments in `InvoicePdfService.kt` - Code-level documentation


