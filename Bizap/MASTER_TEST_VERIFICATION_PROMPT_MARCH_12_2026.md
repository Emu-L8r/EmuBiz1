# 🔍 MASTER TEST VERIFICATION PROMPT (March 12, 2026)

**Purpose:** Get complete, evidence-based status of test suite before proposing fixes  
**Use This:** Copy/paste to IDE agent, Claude Code, or any verification tool  
**Expected Output:** Detailed report with file paths, line numbers, actual errors  

---

## 📋 MASTER PROMPT (Copy & Paste This Exactly)

```
YOU ARE A TEST VERIFICATION AGENT.

Your job is NOT to fix tests. Your job is to VERIFY the exact state of the 
test suite so we can make informed decisions about what to fix.

You will provide:
  ✅ Facts (not assumptions)
  ✅ File paths (not summaries)
  ✅ Line numbers (not approximations)
  ✅ Actual errors (not interpretations)
  ✅ What you DON'T know (not guesses)

---

## PHASE 1: DISCOVERY

List EVERY test file in the repository:

Location: Bizap/app/src/test/java/com/emul8r/bizap/
Location: Bizap/app/src/androidTest/java/com/emul8r/bizap/

For EACH file, provide:
  [ ] File path: app/src/test/.../[TestName].kt
  [ ] Test class name: [ClassName]
  [ ] Number of test methods: [N] tests
  [ ] Current state: COMPILING / BROKEN / UNKNOWN

Create a table like:
  | File Path | Tests | Status |
  |-----------|-------|--------|
  | app/src/test/java/.../InvoiceRepositoryTest.kt | 12 | COMPILING |
  | app/src/test/java/.../PaymentRepositoryTest.kt | 8 | BROKEN |

---

## PHASE 2: COMPILATION CHECK

Run this command:
  ./gradlew clean testDebugUnitTest --info 2>&1

For EACH compilation error, show:

  ERROR #[N]:
    File: [exact path]
    Line: [exact line number]
    Error message: [copy exact error text]
    Type: MockK / Import / Syntax / Type mismatch / Dependency / Other
    
Example:
  ERROR #1:
    File: app/src/test/java/com/emul8r/bizap/data/repository/PaymentRepositoryTest.kt
    Line: 52
    Error message: "coEvery { } must be followed by returns or throws"
    Type: MockK syntax error

---

## PHASE 3: CRITICAL TEST IDENTIFICATION

These tests MUST compile and pass for launch:

  [ ] InvoiceRepositoryImplEnhancedTest (42 tests expected)
      Status: COMPILING / BROKEN
      If BROKEN, show ALL errors
  
  [ ] InvoiceRepositoryTest (shown in attached file)
      Status: COMPILING / BROKEN
      If BROKEN, show ALL errors
  
  [ ] PaymentRepositoryTest
      Status: COMPILING / BROKEN
      If BROKEN, show ALL errors
  
  [ ] CustomerRepositoryTest
      Status: COMPILING / BROKEN
      If BROKEN, show ALL errors
  
  [ ] Any other repository tests
      List them with status

---

## PHASE 4: TEST EXECUTION (IF COMPILATION SUCCEEDS)

If compilation succeeds, run:
  ./gradlew test --info

Show results as:
  Test Name: [method name]
  Status: PASSED ✅ / FAILED ❌ / SKIPPED ⊘
  Time: [milliseconds]
  Error (if failed): [exact error message]

Example:
  Test: InvoiceRepositoryTest.testGetInvoicesByBusinessId
  Status: PASSED ✅
  Time: 145ms
  
  Test: PaymentRepositoryTest.testRecordPaymentSuccessfully
  Status: FAILED ❌
  Time: 52ms
  Error: "expected <42> but was <0>"

---

## PHASE 5: SUMMARY TABLE

Create a comprehensive table:

  | Test File | Compiles | Pass/Total | Critical? | Blocking? |
  |-----------|----------|-----------|-----------|-----------|
  | InvoiceRepositoryTest | YES | 10/12 | YES | NO |
  | PaymentRepositoryTest | NO | -/8 | YES | YES |
  | CustomerRepositoryTest | YES | 8/8 | NO | NO |

---

## PHASE 6: ERROR CATALOG

For EACH compilation error, provide:

  ERROR CATALOG:
  
  [ERROR #1]
    File: [path]
    Line: [number]
    Code snippet:
      [show 3 lines of context around error]
    
    Error type: MockK syntax
    Root cause: coEvery {} not properly closed
    Suggested fix:
      BEFORE: coEvery { someMethod() } throws Exception
      AFTER: coEvery { someMethod() } throws Exception()
    
    Fix complexity: 1-line change
    Confidence: HIGH (clear syntax error)

  [ERROR #2]
    [same format]

---

## PHASE 7: BLOCKING ISSUES ANALYSIS

Answer these YES/NO questions:

  [ ] Does test suite compile without errors? YES / NO
      If NO, list blocking errors (max 5)
  
  [ ] Do critical tests pass (InvoiceRepository, PaymentRepository)? YES / NO / UNKNOWN
      If NO, list failing tests
  
  [ ] Are there missing dependencies? YES / NO / UNKNOWN
      If YES, list them
  
  [ ] Are there MockK setup issues? YES / NO
      If YES, how many? Count: [N]
  
  [ ] Are there deprecated APIs in tests? YES / NO
      If YES, list them
  
  [ ] Can the app launch to emulator if we skip tests? YES / NO
      If NO, what blocks it?

---

## PHASE 8: EXECUTIVE SUMMARY (1 paragraph)

"The test suite currently has [N] compilation errors, mostly in [error type]. 
Critical tests [pass/fail]. This [blocks/does not block] app launch. Estimated 
fix time: [hours]. Most common issue: [type]. Confidence in assessment: HIGH/MEDIUM."

---

## OUTPUT REQUIREMENTS

Format your response as:

  # TEST VERIFICATION REPORT

  ## EXECUTIVE SUMMARY
  [1 paragraph summary]

  ## TEST FILE INVENTORY
  [Table with all test files]

  ## COMPILATION STATUS
  [Passing/Failing breakdown]

  ## ERROR DETAILS
  [Detailed error catalog]

  ## CRITICAL TEST STATUS
  [InvoiceRepository, PaymentRepository, etc. status]

  ## BLOCKING ISSUES
  [Yes/No answers to phase 7 questions]

  ## CONFIDENCE LEVELS
  [For each major claim, HIGH/MEDIUM/LOW confidence with reason]

  ## ACTION ITEMS (Priority Order)
  [ ] Fix [Error Type] in [File] - 1 hour
  [ ] Fix [Error Type] in [File] - 2 hours
  [ ] etc.

---

## CRITICAL SUCCESS FACTORS

  ✅ ALWAYS show file paths (app/src/test/java/...)
  ✅ ALWAYS show line numbers
  ✅ ALWAYS show actual error messages (copy/paste from compiler)
  ✅ ALWAYS show code context (3 lines around error)
  ✅ If you DON'T KNOW something, say "UNKNOWN - requires manual inspection"
  ✅ Never summarize - always show evidence
  ✅ Never guess - only state what you've verified

---

## DO NOT

  ❌ Propose fixes yet (just identify problems)
  ❌ Guess at error causes (show actual code)
  ❌ Skip "I don't know" (be honest about limitations)
  ❌ Summarize without evidence
  ❌ Assume compilation succeeded (verify it)

---

## START HERE

1. Run: ./gradlew clean testDebugUnitTest --info 2>&1
2. Capture ALL output
3. Follow Phases 1-8 above
4. Provide complete report
5. Wait for next instructions

You are NOT proposing fixes. You are GATHERING FACTS.
Start now.
```

---

## 🎯 HOW TO USE THIS PROMPT

### **Step 1: Copy the Prompt Above**

Copy everything from "YOU ARE A TEST VERIFICATION AGENT" through "Start now."

### **Step 2: Choose Your Tool**

**Option A: IDE Agent (Recommended)**
- Open Android Studio
- Open Copilot / Claude / Gemini agent
- Paste the prompt
- Let it run

**Option B: Terminal (If agent can't access)**
```bash
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap

# Run the verification manually
./gradlew clean testDebugUnitTest --info 2>&1 | tee test_results.txt

# Then provide results to agent with prompt
```

**Option C: This Repo (I can verify)**
- I can search the test files myself
- Run the verification
- Create the report
- (Let me know if you want me to do this)

### **Step 3: Evaluate the Response**

When you get the report back, check against the 8-point framework:

```
[ ] Search: File paths shown?
[ ] Inspect: Actual error text shown?
[ ] Clarify: Unknowns identified?
[ ] Propose: NO (this is verification phase only)
[ ] Scope: Clear what's broken vs working
[ ] Options: N/A for verification
[ ] Timeline: Estimate to fix provided
[ ] Confidence: Stated for each finding
```

If it scores <6/8, ask for more detail.

### **Step 4: Build Your Fix Plan**

Once you have the REAL picture:
- You'll know what's actually broken
- You'll know which tests block launch
- You'll know fix complexity
- THEN we queue the right PR

---

## 📊 WHAT YOU'LL GET BACK

A report that looks like:

```
# TEST VERIFICATION REPORT

## EXECUTIVE SUMMARY
The test suite has 3 compilation errors (all MockK syntax in InvoiceRepositoryTest.kt
and PaymentRepositoryTest.kt). Critical repository tests do not compile. This BLOCKS
app launch because: [specific reason]. Estimated fix time: 2-3 hours. Confidence: HIGH.

## TEST FILE INVENTORY
| File | Tests | Status | Compiles |
|------|-------|--------|----------|
| InvoiceRepositoryTest.kt | 12 | BROKEN | NO |
| PaymentRepositoryTest.kt | 8 | BROKEN | NO |
| CustomerRepositoryTest.kt | 6 | COMPILING | YES |

## COMPILATION STATUS
Total Test Files: 15
Compiling: 12
Broken: 3

Errors:
  ERROR #1: PaymentRepositoryTest.kt, line 52 - MockK syntax
  ERROR #2: InvoiceRepositoryTest.kt, line 67 - Type mismatch
  ERROR #3: SyncWorkerTest.kt, line 89 - Missing mock setup

## ACTION ITEMS (Priority)
[ ] Fix MockK syntax in PaymentRepositoryTest.kt - 1 hour
[ ] Fix type mismatch in InvoiceRepositoryTest.kt - 1 hour
[ ] Fix SyncWorkerTest.kt setup - 1 hour
```

---

## ✅ WHY THIS APPROACH

**Before this prompt:** "The tests are broken" (vague, unusable)

**After this prompt:** 
```
InvoiceRepositoryTest.kt, line 52:
  Error: coEvery { mockDao.getInvoice(any()) } syntax incomplete
  Fix: Add "returns flowOf(testInvoice)"
```

**This is the difference between:**
- ❌ "Tests don't work" → can't fix it
- ✅ "Line 52 of PaymentRepositoryTest needs MockK syntax fix" → can fix it

---

## 🎯 YOUR NEXT ACTION

**Choose one:**

1. **I'll verify the test suite right now**
   - I can search the test files
   - Run compilation analysis
   - Create the full report
   - Show you exactly what's broken

2. **You give this prompt to an IDE agent**
   - Copy/paste the prompt above
   - Let the agent run the verification
   - Get the report
   - Share it with me if you want help

3. **Both** (most thorough)
   - You run the prompt with agent
   - I also verify independently
   - Compare findings
   - Get consensus on what's broken

**Which would you prefer?**

If you want me to verify right now, I can:
```
✅ Search all test files
✅ Find compilation errors
✅ Show exact line numbers
✅ Categorize errors by type
✅ Create the report
✅ Ready in 30 minutes
```

**What's your preference?**


