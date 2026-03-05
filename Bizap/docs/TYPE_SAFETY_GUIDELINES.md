# Type Safety Guidelines for Monetary Values

## Golden Rule
**All monetary amounts are stored as Long (cents), never Double**

Examples:
- ✅ `totalAmount: Long = 14999L` → Displays as $149.99
- ✅ `unitPrice: Long = 4999L` → Displays as $49.99
- ❌ `totalAmount: Double = 149.99` → WRONG, causes precision issues

---

## Display Conversion Pattern (UI Layer)

### CORRECT: Use CentsFormatter
```kotlin
// Best practice - purpose-built formatter
Text(CentsFormatter.formatCents(invoice.totalAmount, "AUD"))
```

### ALSO CORRECT: Explicit Division
```kotlin
// For PDF generation where Double is needed
val dollars = cents / 100.0
String.format("%.2f", dollars)
```

### NEVER DO THIS
```kotlin
// ❌ WRONG - Long doesn't match "%.2f" format specifier
String.format("%.2f", invoice.totalAmount)

// ❌ WRONG - Floating point precision loss
val total = 149.99  // Could lose precision in math
```

---

## Calculation Pattern (Domain Layer)

```kotlin
// ✅ CORRECT: Explicit type conversion
val itemTotal = (unitPrice.toDouble() * quantity).toLong()

// ✅ CORRECT: Using roundToLong to avoid truncation
val taxAmount = (subtotal.toDouble() * taxRate).roundToLong()

// ❌ WRONG: Implicit Double→Long (may truncate)
val total = unitPrice * quantity  // If quantity is Double
```

---

## Input Conversion Pattern (UI Input)

```kotlin
// User enters: "49.99"
val inputString = "49.99"

// ✅ CORRECT: Convert to cents
val cents = (inputString.toDoubleOrNull() * 100).toLong()  // 4999L

// ❌ WRONG: Keeping as Double
val dollars = inputString.toDoubleOrNull()  // Precision risk
```

---

## Type Mapping Reference

| Layer | Type | Example | Purpose |
|-------|------|---------|---------|
| Database | Long | 14999L | Cents (no precision loss) |
| Domain Model | Long | totalAmount: Long | Cents (single source of truth) |
| Display | String | "A$149.99" | Formatted for humans |
| UI Input | String → Long | "49.99" → 4999L | User entry → cents |
| Tax Rate | Double | 0.10 | Percentage (not an amount) |

---

## Checklist for Code Review

- [ ] All monetary fields in entities are `Long`
- [ ] All calculations use `(value.toDouble() * other).toLong()`
- [ ] All display uses `CentsFormatter` or explicit `/100.0`
- [ ] No `String.format("%.2f", Long)` without conversion
- [ ] Tax rates are `Double`, but amounts are `Long`
- [ ] Database migrations convert cents properly (×100)

---

## Common Mistakes to Catch

| Mistake | Location | Fix |
|---------|----------|-----|
| `String.format("%.2f", longValue)` | Any display code | Add `/100.0` or use `CentsFormatter` |
| `val total = price * quantity` | Calculations | Use `(price.toDouble() * quantity).toLong()` |
| `totalAmount: Double` | Entity fields | Change to `Long` + add migration |
| `toInt()` on monetary value | Display | Use `/100.0` for dollars instead |
| Storing `0.1` as cents | Domain layer | Store as `10L` (cents) or `0.1` (rate) |

---

## Q&A

**Q: When should I use Double for money?**  
A: Only for percentages/rates (0.10 for 10%), never for amounts.

**Q: What if I need to display with cents precision?**  
A: Use `CentsFormatter.formatCents()` - it handles everything.

**Q: Can I use BigDecimal?**  
A: Not recommended here. Long cents is simpler and sufficient for most commerce apps.

**Q: What if existing code breaks this?**  
A: Create a migration (like v23→v24) to fix data and update tests.

---

## References

- CentsFormatter implementation: `app/src/main/java/com/emul8r/bizap/utils/CentsFormatter.kt`
- LineItem entity: `app/src/main/java/com/emul8r/bizap/domain/model/LineItem.kt`
- InvoiceEntity: `app/src/main/java/com/emul8r/bizap/data/local/entities/InvoiceEntity.kt`
- Migration v23→v24: `app/src/main/java/com/emul8r/bizap/data/local/migrations/Migration_23_24.kt`


