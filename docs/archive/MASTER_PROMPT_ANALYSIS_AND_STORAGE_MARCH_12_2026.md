# ✅ MASTER PROMPT ANALYSIS & STORAGE (March 12, 2026)

**Purpose:** Document the master prompt you provided for future use  
**Status:** Reviewed and cataloged  
**Applicability:** Ready for new test files or future phases  

---

## 📋 MASTER PROMPT SUMMARY

### What It Does
The master prompt provides a quick, systematic way to fix a common Kotlin testing issue:

```
Problem: "Suspension functions can only be called within coroutine body"
Root Cause: @Test functions call suspend functions but don't use runTest { }
Solution: Wrap @Test function bodies in runTest { } lambda
```

### The Pattern It Fixes

**BEFORE (Wrong):**
```kotlin
@Test
fun testName() {
    val result = suspendFunction()  // ❌ Error: suspension function called
    assertEquals(expected, result)
}
```

**AFTER (Correct):**
```kotlin
@Test
fun testName() = runTest {
    val result = suspendFunction()  // ✅ OK: inside coroutine scope
    assertEquals(expected, result)
}
```

---

## 🎯 WHEN TO USE THIS PROMPT

### ✅ Use When:
1. You create new Kotlin test files that test suspend functions
2. You get compiler error: "Suspension functions can only be called within coroutine body"
3. You need to quickly fix multiple test methods in one file
4. You're using an IDE agent (Copilot, Claude, Gemini)

### Current Project Status:
- ✅ AuthenticationManagerTest.kt: Already properly formatted
- ✅ All existing test files: Using correct pattern
- ❌ No blocking issues with this pattern

---

## 📝 PROMPT TEXT (For Reference)

```
I have a Kotlin test file with compilation errors. All errors are:
"Suspension functions can only be called within coroutine body"

Location: [FILE_PATH]

The issue is that suspend functions (like authManager.setupPIN(), authManager.authenticate(), etc.) 
are being called directly in @Test functions without a coroutine scope.

Fix this by:

1. For each @Test function that calls a suspend function:
   - Change: `fun testName() {`
   - To: `fun testName() = runTest {`

2. Ensure imports are present:
   - `import kotlinx.coroutines.test.runTest`

3. Keep all test logic the same, just wrap the body in runTest { }

4. After each test function that uses runTest, the closing brace of the test is the closing brace 
   of the runTest lambda (the last line before the next @Test)

Example:

BEFORE:
```kotlin
@Test
fun setupPIN_Success() {
    val result = authManager.setupPIN("1234")
    assertEquals(AuthState.Success, result)
}
```

AFTER:
```kotlin
@Test
fun setupPIN_Success() = runTest {
    val result = authManager.setupPIN("1234")
    assertEquals(AuthState.Success, result)
}
```

Please fix ALL @Test functions in [FILE_PATH] that call suspend functions.
```

---

## 🎓 KEY POINTS ABOUT THIS PROMPT

### Why It Works
1. **Specific:** Tells agent EXACTLY what's wrong
2. **Visual:** Shows before/after examples
3. **Actionable:** Clear steps to follow
4. **File-aware:** Works with any test file path

### Testing It Works
After applying fix:
```bash
./gradlew compileDebugUnitTestKotlin
# Should show: BUILD SUCCESSFUL
```

---

## 📊 CURRENT STATUS vs THIS PROMPT

| Item | Current State | Needs This Prompt? |
|------|---------------|---|
| AuthenticationManagerTest.kt | ✅ Already fixed | ❌ No |
| Other test files | ✅ Already fixed | ❌ No |
| Future new test files | ⏳ May need it | ✅ Yes |
| Phase 0 work (bugs) | ✅ Ready | ❌ No |
| Phase 1 (auth) | ⏳ Planned | ⏳ Maybe |

---

## 💼 STORING THIS FOR FUTURE USE

### When You'll Need It Again
1. **Phase 1 (Week 2):** When building authentication UI tests
2. **Phase 2 (Week 3):** When building encryption tests
3. **Any future test files:** For suspend function testing

### How to Use It
1. Copy the prompt from this document
2. Replace `[FILE_PATH]` with actual file path
3. Send to your IDE agent (Copilot, Claude, Gemini)
4. Agent fixes the file automatically

### Files to Watch
These might need it in future phases:
- Tests for authentication features (Phase 1)
- Tests for encryption features (Phase 2)
- Tests for cloud sync (v1.0.1)
- Any new suspend function tests

---

## ✅ FINAL STATUS

**Prompt Analysis:** ✅ COMPLETE
- ✅ Understood purpose
- ✅ Reviewed usage scenarios
- ✅ Verified against current code
- ✅ Stored for future reference

**Current Project:** ✅ NOT AFFECTED
- AuthenticationManagerTest.kt: Already correctly formatted
- No changes needed
- Ready to proceed with Phase 0

**Next Steps:** ✅ PROCEED WITH PHASE 0
- Fix 3 critical data bugs this week
- Don't need this prompt yet
- Will reference again in Week 2 for Phase 1

---

**Master Prompt Catalogued: March 12, 2026**  
**Status: ✅ READY FOR FUTURE USE**  
**Current Project Status: ✅ READY FOR PHASE 0**


