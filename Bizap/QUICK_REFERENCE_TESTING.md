# 🚀 QUICK REFERENCE: OPTION B TESTING

**Print this or keep open while testing**

---

## ⏱️ PHASE 1: 4 QUICK CHECKPOINTS (10 min)

### CP1: Settings → Business Profile → 🐛 → Save
✅ PASS if: Form fills instantly with Emu Consulting data

### CP2: Customers → 🐛
✅ PASS if: 3 customers appear (UNREALCUSTOMER1/2/3)

### CP3: Invoices → Create → Select Customer → 🐛 → Save
✅ PASS if: Form fills, 3 items visible, "PDF saved" in logcat < 3 sec

### CP4: Vault → Click newest invoice
✅ PASS if: PDF opens and displays content

---

## 📸 PHASE 2: 3 CRITICAL TESTS (15 min)

### TEST 1: TEXT WRAPPING ⭐⭐⭐
**Screenshot:** Line items table, zoom on first item

**Critical:**
- Long description wraps to 3-4 lines? ✅/❌
- No text cut off? ✅/❌
- All columns aligned? ✅/❌

### TEST 2: ROBOTO FONTS ⭐⭐⭐
**Screenshot:** Full PDF

**Critical:**
- Header noticeably bolder than body? ✅/❌
- Looks professional (not system default)? ✅/❌
- Text crisp/clean (not pixelated)? ✅/❌

### TEST 3: NO OVERFLOW ⭐⭐⭐
**Screenshot:** Full PDF

**Critical:**
- All margins have white space? ✅/❌
- Nothing cut off at edges? ✅/❌
- All sections visible & spaced? ✅/❌

---

## ❓ PHASE 3: 6 CRITICAL ANSWERS (5 min)

| Q | Question | Answer |
|---|----------|--------|
| 1 | No crashes? | ✅/❌ |
| 2 | PDF < 5 sec? | ✅/❌ |
| 3 | Text wraps? | ✅/⚠️/❌ |
| 4 | Roboto fonts? | ✅/⚠️/❌ |
| 5 | No overflow? | ✅/⚠️/❌ |
| 6 | All info present? | ✅/⚠️/❌ |

---

## 📊 FINAL SCORE

```
All 3 ⭐ tests PASS + All 6 answers YES
↓
✅ PHASE 2B PRODUCTION READY
↓
Move to Phase 3
```

---

## ⏰ TIME BREAKDOWN

- **Phase 1:** 10 min (4 checkpoints)
- **Phase 2:** 15 min (3 critical tests + screenshots)
- **Phase 3:** 5 min (answer 6 questions)
- **TOTAL:** 30 min

---

## 📧 WHEN DONE

Reply with:
1. Phase 1 results (4 checkpoints ✅/❌)
2. Phase 2 results (3 tests ✅/⚠️/❌)
3. Phase 3 answers (6 questions)
4. Screenshots (7 total)
5. Final assessment (READY/NEEDS FIXES/BROKEN)

---

**START TESTING NOW** 🎉

