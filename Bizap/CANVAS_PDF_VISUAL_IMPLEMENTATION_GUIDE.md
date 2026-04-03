# Canvas PDF Redesign - Visual Implementation Guide

## 📐 SPACING DIAGRAM

```
PAYMENT DETAILS SECTION
┌─────────────────────────────────────────────────────────┐
│                                                         │
│  16px top padding                                       │
│  PAYMENT DETAILS (header: 12f bold, primary color)    │
│                                                         │
│  28f advance (breathing room)                          │
│                                                         │
│  Payment Terms: (label: 10f bold, #333)               │
│  6f gap                                                │
│  Due within 30 days... (value: 11f regular, #666)    │
│                                                         │
│  18f spacing (between rows)                            │
│                                                         │
│  Reference: (label: 10f bold, #333)                   │
│  6f gap                                                │
│  INV-2026-001 (value: 11f regular, #666)             │
│                                                         │
│  16px bottom padding (calculated)                      │
└─────────────────────────────────────────────────────────┘

Box Specs:
- Height: 130f total
- Width: 40px to 555px
- Rounded corners: 8f
- Background: #F9F9F9 (light)
- Border: 1f #E0E0E0
```

---

## 📐 BANK TRANSFER SPACING DIAGRAM

```
EFT / BANK TRANSFER SECTION
┌─────────────────────────────────────────────────────────┐
│                                                         │
│  16px top padding                                       │
│  EFT / BANK TRANSFER (header: 12f bold, primary)     │
│                                                         │
│  28f advance (breathing room)                          │
│                                                         │
│  Bank Name: (label: 10f bold, #333)                   │
│  6f gap                                                │
│  Commonwealth Bank (value: 11f regular, #666)         │
│                                                         │
│  18f spacing (between rows)                            │
│                                                         │
│  Account Name: (label: 10f bold, #333)                │
│  6f gap                                                │
│  ACME Pty Ltd (value: 11f regular, #666)             │
│                                                         │
│  18f spacing (between rows)                            │
│                                                         │
│  BSB: (label: 10f bold, #333)                         │
│  6f gap                                                │
│  06-222-245 (value: 11f regular, #666)               │
│                                                         │
│  18f spacing (between rows)                            │
│                                                         │
│  Account Number: (label: 10f bold, #333)              │
│  6f gap                                                │
│  123456789 (value: 11f regular, #666)                │
│                                                         │
│  16px bottom padding (calculated)                      │
└─────────────────────────────────────────────────────────┘

Box Specs:
- Height: 140f total
- Width: 40px to 555px
- Rounded corners: 8f
- Background: #F5F5F5 (slightly darker)
- Border: 1f #E0E0E0
```

---

## 🎨 COLOR PALETTE

```
Header/Title Colors:
  Primary (Theme):     #6B4C9A (Modern), #003366 (Corporate), etc.
  
Label & Structure:
  Dark Gray Labels:    #333333 (bold, 10f)
  
Value Colors:
  Medium Gray Values:  #666666 (regular, 11f)
  
Placeholder:
  Light Gray Italic:   #999999 (10f italic, for empty fields)
  
Backgrounds:
  Payment Details:     #F9F9F9 (slightly lighter)
  Bank Transfer:       #F5F5F5 (slightly darker - distinction)
  
Borders:
  Subtle Gray:         #E0E0E0 (1f stroke)
```

---

## 📏 SPACING SCALE

```
Micro:     6f  (label-to-value gap)
Small:     18f (between rows)
Medium:    20f (between sections)
Large:     28f (header breathing room)

Example Flow:
┌─────────────────────┐
│ HEADER              │  ← section start
│                     │  ↓ 28f (large advance)
│ Label:              │  ← label at Y
│                     │  ↓ 6f (micro gap)
│ Value               │  ← value at Y
│                     │  ↓ 18f (small advance)
│ Next Label:         │  ← next label at Y
│                     │  ↓ 6f (micro gap)
│ Next Value          │  ← next value at Y
│                     │
└─────────────────────┘
```

---

## 🧩 COMPONENT STRUCTURE

```
Payment Section Component:
  Container (card)
    ├─ Header Block
    │   └─ "PAYMENT DETAILS" (12f bold, primary)
    │
    └─ Content Block (with 28f header gap)
        ├─ Field 1: Payment Terms
        │   ├─ Label (10f bold, #333)
        │   ├─ 6f gap
        │   └─ Value (11f regular, #666)
        │   └─ 18f spacing
        │
        └─ Field 2: Reference
            ├─ Label (10f bold, #333)
            ├─ 6f gap
            └─ Value or Placeholder (11f regular, #666 or italic #999)

Bank Section Component:
  Container (card)
    ├─ Header Block
    │   └─ "EFT / BANK TRANSFER" (12f bold, primary)
    │
    └─ Content Block (with 28f header gap)
        ├─ Field 1: Bank Name
        ├─ Field 2: Account Name
        ├─ Field 3: BSB
        └─ Field 4: Account Number
        
        (Each field: Label → 6f gap → Value, 18f between fields)
```

---

## 🎨 VISUAL HIERARCHY

```
LEVEL 1 - Headers
═══════════════════════════════════════════════════════════════
12f bold, PRIMARY COLOR (theme-dependent)
Examples: "PAYMENT DETAILS", "EFT / BANK TRANSFER"
Purpose: Section title, draws attention
Spacing: 28f gap below (breathing room)

LEVEL 2 - Labels
───────────────────────────────────────────────────────────────
10f bold, #333333 (dark gray)
Examples: "Payment Terms:", "Bank Name:"
Purpose: Identifies the field
Spacing: 6f gap below (close pair)

LEVEL 3 - Values
───────────────────────────────────────────────────────────────
11f regular, #666666 (medium gray)
Examples: "Due within 30 days...", "Commonwealth Bank"
Purpose: Contains actual data
Spacing: 18f gap to next field (breathing room)

LEVEL 4 - Placeholders
───────────────────────────────────────────────────────────────
10f italic, #999999 (light gray)
Examples: "Not provided"
Purpose: Shows missing data intentionally
Styling: Italic to distinguish from real values
Spacing: 18f gap to next field (same as values)
```

---

## 📊 BEFORE/AFTER VISUAL COMPARISON

### BEFORE (IMG18 - Sparse):
```
PAYMENT DETAILS           ← plain header, no styling
Payment Terms:            ← cramped
Due within 30 days        ← same spacing as label
Reference:                ← cramped
                          ← blank if empty
EFT / BANK TRANSFER       ← no distinction
Bank:Commonwealth         ← label and value together
Account Name:ACME Pty     ← cramped, hard to read
BSB:06-222-245           ← no structure
Account Number:12345      ← cluttered
```

### AFTER (Redesigned - Professional):
```
┌─────────────────────────────────────┐
│ PAYMENT DETAILS                     │  ← styled header
│ (breathing room - 28f gap)          │
│ Payment Terms:                      │  ← clear label
│ (spacing - 6f gap)                  │
│ Due within 30 days...               │  ← clear value
│ (spacing - 18f to next)             │
│ Reference:                          │  ← clear label
│ (spacing - 6f gap)                  │
│ INV-2026-001                        │  ← clear value
│ (or "Not provided" if empty)        │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ EFT / BANK TRANSFER                 │  ← styled header
│ (breathing room - 28f gap)          │
│ Bank Name:                          │  ← clear label
│ (spacing - 6f gap)                  │
│ Commonwealth Bank                   │  ← clear value
│ (spacing - 18f to next)             │
│ Account Name:                       │  ← clear label
│ (spacing - 6f gap)                  │
│ ACME Pty Ltd                        │  ← clear value
│ (spacing - 18f to next)             │
│ BSB:                                │  ← clear label
│ (spacing - 6f gap)                  │
│ 06-222-245                          │  ← clear value
│ (spacing - 18f to next)             │
│ Account Number:                     │  ← clear label
│ (spacing - 6f gap)                  │
│ 123456789                           │  ← clear value
└─────────────────────────────────────┘
```

---

## 🎯 KEY MEASUREMENTS SUMMARY

```
FONT SIZES
Header:       12f (was 11f)
Label:        10f (was 9f)
Value:        11f (new explicit)
Placeholder:  10f italic

SPACING
Label-Value:  6f (close pair)
Between Rows: 18f (breathing room)
Header Gap:   28f (breathing room)
Section Gap:  20f (clear separation)

BOX DIMENSIONS
Payment Height:    130f (was 110f)
Bank Height:       140f (was 85f)
Rounded Corners:   8f
Left/Right Padding: 50f x-coordinate start

COLORS
Headers:      Primary color (theme)
Labels:       #333333 (dark gray)
Values:       #666666 (medium gray)
Placeholders: #999999 (light gray, italic)
Backgrounds:  #F9F9F9 (Payment), #F5F5F5 (Bank)
Borders:      #E0E0E0
```

---

## ✅ IMPLEMENTATION CHECKLIST

- [x] Payment Details section height increased (110f → 130f)
- [x] Bank Transfer section height increased (85f → 140f)
- [x] Header font size increased (11f → 12f)
- [x] Label font size increased (9f → 10f)
- [x] Value font size explicitly set (11f)
- [x] Placeholder font size and style (10f italic)
- [x] Label-value spacing (6f gap)
- [x] Between-row spacing (18f)
- [x] Header breathing room (28f)
- [x] Section gap (20f)
- [x] Header color (primary theme color)
- [x] Label color (#333333)
- [x] Value color (#666666)
- [x] Placeholder color (#999999)
- [x] Payment background (#F9F9F9)
- [x] Bank background (#F5F5F5)
- [x] Border color (#E0E0E0)
- [x] Rounded corners (8f)
- [x] Placeholder text handling ("Not provided")
- [x] All 4 bank fields implemented
- [x] Build verification (SUCCESSFUL)

---

## 🚀 DEPLOYMENT READY

This visual guide shows exactly how the redesigned sections will appear. Every dimension, color, and spacing value is documented and implemented.

**Status**: ✅ Ready for emulator testing

Generate a Canvas PDF and compare with these diagrams to verify the transformation!

