# MASTERPROMPT 3B COMPLETE: Running Tests First Time

**Date:** March 5, 2026  
**Status:** ✅ Complete guide created and committed

---

## What You Just Received

### 📚 Complete Running Tests Guide
**File:** `docs/RUNNING_TESTS_COMPLETE_GUIDE.md`

Contains everything you need to know:
- ✅ Quick start (30 seconds to first success)
- ✅ 7 most important commands
- ✅ Understanding test output
- ✅ Debugging failing tests (step-by-step)
- ✅ Common issues & fixes
- ✅ Unit vs instrumented tests
- ✅ Test running workflow
- ✅ Success checklist

---

## The 7 Most Important Commands (Copy These)

```bash
# 1. Run all tests
./gradlew test

# 2. Run one test class
./gradlew :app:testDebugUnitTest -k CoreUnitTests

# 3. Run one test method
./gradlew :app:testDebugUnitTest -k "createInvoice_validData"

# 4. Run with coverage report
./gradlew testDebugUnitTestCoverage

# 5. Run with detailed output
./gradlew test --info

# 6. Stop on first failure
./gradlew test --fail-fast

# 7. Watch mode (re-runs on save)
./gradlew test --watch
```

---

## Your Quick Reference

### What Success Looks Like
```
✓ createInvoice_validData_savesSuccessfully
✓ createInvoice_emptyItems_validationFails
✓ ... (more tests) ...

BUILD SUCCESSFUL ✅
10 passed in 1.2s
```

### What Failure Looks Like
```
✗ createInvoice_validData_savesSuccessfully
  AssertionError: expected 123 but got null
  at CoreUnitTests.kt:93

BUILD FAILED ❌
1 failed, 9 passed
```

### Debugging Checklist
- [ ] Read error message
- [ ] Read test comments
- [ ] Run just this test: `./gradlew :app:testDebugUnitTest -k "testName"`
- [ ] Check test data (TestDataFactory)
- [ ] Check mock configuration
- [ ] Check assertions
- [ ] Add debug logging
- [ ] Fix and re-run

---

## File Locations

```
Learning:
  docs/RUNNING_TESTS_COMPLETE_GUIDE.md ← READ THIS FIRST
  docs/WEEK3_QUICK_REFERENCE.md
  docs/WEEK3_MIGRATIONS_AND_TESTING.md
  docs/MIGRATION_TESTING_GUIDE.md

Code:
  app/src/test/java/com/emul8r/bizap/CoreUnitTests.kt
```

---

## Your Next Steps

### Step 1: Read (15 minutes)
```
Open: docs/RUNNING_TESTS_COMPLETE_GUIDE.md
Section: "Quick Start (30 seconds)"
```

### Step 2: Run (5 minutes)
```bash
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
./gradlew test
```

### Step 3: Understand (10 minutes)
```
Read: "Understanding Test Output" section
Find: Your test results
Match: Against success/failure patterns
```

### Step 4: Write (30 minutes)
```
Copy: One test from CoreUnitTests.kt
Run: ./gradlew :app:testDebugUnitTest -k "testName"
Debug: If it fails
Celebrate: When it passes! 🎉
```

---

## Key Takeaways

| What | Command |
|------|---------|
| **Run all tests** | `./gradlew test` |
| **Run one test** | `./gradlew :app:testDebugUnitTest -k "name"` |
| **See coverage** | `./gradlew testDebugUnitTestCoverage` |
| **Debug first failure** | `./gradlew test --fail-fast` |
| **Watch mode (TDD)** | `./gradlew test --watch` |

| What | Where |
|------|-------|
| **Console output** | Appears immediately in terminal |
| **HTML report** | `app/build/reports/tests/testDebugUnitTest/index.html` |
| **Coverage report** | `app/build/reports/jacoco/testDebugUnitTestCoverage/html/index.html` |

---

## Common Issues (Quick Fixes)

| Problem | Fix |
|---------|-----|
| "Cannot find symbol: @Test" | Add import: `import org.junit.Test` |
| "Unresolved reference: TestDataFactory" | Add import: `import com.emul8r.bizap.domain.validation.TestDataFactory` |
| "Test file not found" | Move to: `app/src/test/java/` (NOT `app/src/main/`) |
| "Gradle sync failed" | Run: `./gradlew clean` then `./gradlew test` |
| "Tests won't run in IDE" | Try: File → Invalidate Caches → Restart |

---

## ✅ Success Criteria

You know how to run tests when you can:

- [ ] Run `./gradlew test` successfully
- [ ] See "BUILD SUCCESSFUL" output
- [ ] Understand what ✓ and ✗ mean
- [ ] Find error messages in output
- [ ] Run a single test with `-k` flag
- [ ] Read a failing test and understand why
- [ ] Know where HTML reports are
- [ ] Know not to commit failing tests
- [ ] Can debug a test failure in 5 minutes

---

## 🎯 Your Path Forward

### Week 3 Sequence
1. ✅ Learn migrations (WEEK3_MIGRATIONS_AND_TESTING.md)
2. ✅ Learn testing (10 test examples)
3. ✅ Learn running tests (RUNNING_TESTS_COMPLETE_GUIDE.md) ← YOU ARE HERE
4. ⏭️ Run your first test
5. ⏭️ Debug and fix it
6. ⏭️ Run all 10 tests
7. ⏭️ Commit passing tests

---

## 💡 Pro Tips

1. **Keep WEEK3_QUICK_REFERENCE.md open** while you work
2. **Use `-k "testName"`** for faster feedback when debugging
3. **Watch mode (`--watch`)** is great for TDD workflow
4. **Coverage reports** show what code still needs tests
5. **HTML reports** are more detailed than console
6. **Run all tests before pushing** to GitHub
7. **One failing test at a time** - focus and fix

---

## 📞 Questions Answered

**Q: How do I run tests?**  
A: `./gradlew test` - that's it!

**Q: How do I know if they passed?**  
A: Look for "BUILD SUCCESSFUL" in output

**Q: Where do I see detailed results?**  
A: HTML reports in `app/build/reports/`

**Q: How do I run one test?**  
A: `./gradlew :app:testDebugUnitTest -k "testName"`

**Q: What if a test fails?**  
A: Read error message, check test comments, debug step-by-step

**Q: Should I commit failing tests?**  
A: NO - only commit passing tests

**Q: How do I speed up feedback?**  
A: Use `-k` to run one test, watch mode for continuous testing

---

## 🚀 You're Ready!

You now have:
- ✅ Comprehensive running tests guide
- ✅ 7 most important commands memorized
- ✅ Debugging process understood
- ✅ Common issues and fixes known
- ✅ Success and failure criteria clear

**Your next action:** Open `docs/RUNNING_TESTS_COMPLETE_GUIDE.md` and follow the "Quick Start" section! 🧪


