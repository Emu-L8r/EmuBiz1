# MASTER WORKFLOW PROMPT FOR IDE COPILOT
## Version 1.0 - EmuBiz1 Project Standards

### CRITICAL EXECUTION PRINCIPLES

You are assisting with multi-step deployment and testing workflows. Follow these principles STRICTLY:

---

## PRINCIPLE 1: VERIFY BEFORE COMMIT
**Rule:** Never commit code until you have verified it works.

**Sequence (MANDATORY):**
```
1. Make code changes
2. ✅ RUN TESTS (wait for results)
3. ✅ BUILD APPLICATION (wait for success)
4. ✅ VERIFY BUILD OUTPUT EXISTS
5. → ONLY THEN: Git commit
6. → ONLY THEN: Git push
```

**Why:** Committing broken code creates messy git history and wastes time reverting.

**Implementation:**
- If I ask you to "run tests and commit," you MUST:
  - Run tests FIRST
  - Check for SUCCESS/FAILURE
  - ONLY commit if tests pass
  - Show me test results BEFORE commit message

---

## PRINCIPLE 2: SINGLE DEFINITIVE VERIFICATION
**Rule:** Run ONE verification command that gives a clear answer. Don't run 4 different checks.

**Bad Pattern (DO NOT DO THIS):**
```bash
./gradlew build --no-build-cache
./gradlew :app:assembleDebug  
./gradlew :app:compileDebugKotlin -q
./gradlew check
# Multiple commands = uncertainty, wastes time
```

**Good Pattern (DO THIS):**
```bash
./gradlew :app:assembleDebug --no-build-cache 2>&1 | tail -20
# Single command with clear output showing SUCCESS or FAILURE
```

**If first check is unclear:**
- Ask the user for clarification
- Don't try 5 different approaches

---

## PRINCIPLE 3: CORRECT TASK SEQUENCE
**Rule:** Execute steps in the order requested. If sequence matters, follow it exactly.

**Example - Option B: Polish + Deploy (CORRECT ORDER):**
```
STEP 1: Update documentation
STEP 2: RUN UNIT TESTS (MUST happen before commit)
STEP 3: Run build verification
STEP 4: Commit changes
STEP 5: Push to GitHub
STEP 6: Verify push succeeded
```

**NOT this (WRONG):**
```
STEP 1: Update docs
STEP 2: Commit immediately
STEP 3: Run tests after commit ← WRONG SEQUENCE
```

---

## PRINCIPLE 4: ALWAYS RUN REQUIRED TESTS
**Rule:** If a task includes testing, ALWAYS run it. Don't skip it.

**Test Commands for EmuBiz1:**
```bash
# Unit tests (MUST RUN for "Polish + Deploy")
./gradlew :app:testDebugUnitTest

# Build verification (MUST RUN before commit)
./gradlew :app:assembleDebug --no-build-cache

# Lint check (RECOMMENDED)
./gradlew lint
```

**When user says "Polish + Deploy":**
- This INCLUDES testing
- You MUST run: `./gradlew :app:testDebugUnitTest`
- Show results before proceeding

---

## PRINCIPLE 5: CLEAN GIT HYGIENE
**Rule:** Don't commit build artifacts, logs, or temporary files.

**Files to NEVER commit:**
```
*.log                 # Build logs
*.apk                 # Compiled binaries
build/               # Gradle build dir
.gradle/             # Gradle cache
*.class              # Compiled classes
.DS_Store            # macOS files
```

**Files ALREADY in .gitignore (verify first):**
```bash
cat .gitignore | grep -E "\.log|build/|\.gradle"
```

**If dirty, clean staging:**
```bash
git reset <file>  # Remove from staging
# OR
git checkout -- <file>  # Discard changes
```

**Before committing, verify:**
```bash
git status  # Should show ONLY code changes, NOT artifacts
```

---

## PRINCIPLE 6: VERIFY PUSH SUCCEEDED
**Rule:** After pushing to GitHub, ALWAYS verify the push actually worked.

**Verification steps:**
```bash
# Check local git
git log --oneline -1
# Should show your commit message

# Check remote
git ls-remote origin main | head -1
# Should match your local commit hash

# Check GitHub web
# https://github.com/Emu-L8r/EmuBiz1/commits/main
# Your commit should be at the top
```

**If push fails:** Report error immediately, don't assume success.

---

## TASK-SPECIFIC WORKFLOWS

### WORKFLOW A: "Polish + Deploy" (Option B)
```
USER SAYS: "Execute Option B: Polish + Deploy"

YOU MUST:
1. Update documentation files (as specified)
2. Run: ./gradlew :app:testDebugUnitTest
   → Wait for results
   → Show PASS/FAIL
   → If FAIL: Report which tests failed
3. Run: ./gradlew :app:assembleDebug --no-build-cache
   → Wait for: "BUILD SUCCESSFUL"
4. Run: git status
   → Verify only code changes shown
5. Commit with clear message
6. Run: git push origin main
7. Verify push with: git log --oneline -1
8. Report: "✅ COMPLETE - All tests passed, code committed, changes on GitHub"

Do NOT skip step 2 (tests). Do NOT commit before testing.
```

### WORKFLOW B: "Create Audit Report"
```
USER SAYS: "Create audit report"

YOU MUST:
1. Scan codebase systematically
2. Categorize findings by severity
3. Verify each finding with actual code references
4. Create structured document
5. Do NOT create multiple redundant documents
6. Do NOT create documents that duplicate what already exists

Output: ONE comprehensive report, well-organized
Do NOT: Create 5 variations of the same report
```

### WORKFLOW C: "Build and Test"
```
USER SAYS: "Build and test the app"

YOU MUST (in this order):
1. ./gradlew :app:testDebugUnitTest
2. Check: All tests pass? YES/NO
3. ./gradlew :app:assembleDebug --no-build-cache
4. Check: Build successful? YES/NO
5. Verify APK exists
6. Report results clearly

Do NOT try 4 different build commands.
Do NOT skip tests.
Do NOT build before testing.
```

---

## REPORTING STANDARDS

### When Reporting Build Status:
```
✅ GOOD:
"Build Status: ✅ SUCCESS
Command: ./gradlew :app:assembleDebug
Result: BUILD SUCCESSFUL in 1m 4s
APK Location: app/build/outputs/apk/debug/app-debug.apk
Next Step: Ready for deployment"

❌ AVOID:
"Build appears to be running or cached. Let me check with another query.
[runs 4 more build commands]
I think it might have succeeded but I'm not sure."
```

### When Reporting Test Results:
```
✅ GOOD:
"Test Results: ✅ PASSED
Command: ./gradlew :app:testDebugUnitTest
Output: 29 tests passed, 0 failed
Duration: 45 seconds
Ready for commit"

❌ AVOID:
"I ran the test but the output was unclear so I ran it again..."
```

### When Reporting Git Status:
```
✅ GOOD:
"Git Status: ✅ CLEAN
Committed files:
- docs/TYPE_SAFETY_GUIDELINES.md (new)
- CurrencySelector.kt (modified)
- COMPREHENSIVE_TROUBLESHOOTING_GUIDE.md (modified)

Commit message: 'docs: Polish documentation and add type safety guidelines'
Push status: ✅ Pushed to origin/main"

❌ AVOID:
"I committed some things. I think they're on GitHub."
```

---

## DECISION TREE: When to Ask User vs Proceed

```
USER REQUEST: "Execute Option B"

Is task clearly defined?
→ YES: Proceed with execution
→ NO: Ask clarifying questions

Before committing code:
Do I need to test it?
→ YES (Option B includes testing): Run tests first
→ NO: Proceed to commit

Are tests passing?
→ YES: Commit
→ NO: Report failures, ask user what to do

Did push succeed?
→ VERIFY before claiming success
→ If unsure: Ask user to verify on GitHub
```

---

## SPECIFIC IMPROVEMENTS FOR EMUBIZ1

### Type Safety Checks:
```
When working with monetary code:
1. Verify String.format() usage
2. Check if Long values are being converted with /100.0
3. Verify CentsFormatter is used appropriately
4. Report: "Type safety verified" ONLY if you checked these
```

### Build Verification:
```
For EmuBiz1, "BUILD SUCCESSFUL" means:
- 0 errors
- 0 warnings (ideally)
- APK exists at: app/build/outputs/apk/debug/app-debug.apk
- Time: ~60 seconds

If it takes >2 minutes:
→ Something might be wrong
→ Check for infinite loops or dependency issues
```

### Test Suite:
```
EmuBiz1 tests to always run:
- ./gradlew :app:testDebugUnitTest (mandatory for "Polish + Deploy")

Expected: 29+ tests pass
If tests fail: Report which ones and why
Don't ignore test failures
```

---

## ANTI-PATTERNS: DO NOT DO THIS

```
❌ ANTI-PATTERN 1: Over-verification
"Let me check the build with 5 different commands to be sure"
→ Use ONE definitive command
→ If unclear, ask the user

❌ ANTI-PATTERN 2: Skip tests
"The user asked to deploy, so I'll commit without testing"
→ ALWAYS test before committing
→ Never skip the test step

❌ ANTI-PATTERN 3: Commit after push
"I pushed to GitHub then created a completion report"
→ Create reports BEFORE committing
→ Or don't create redundant reports

❌ ANTI-PATTERN 4: Assume success
"The build probably worked, I'll assume it's fine"
→ VERIFY, don't assume
→ Show evidence of success

❌ ANTI-PATTERN 5: Wrong sequence
"I'll commit now and test later"
→ Test FIRST, commit SECOND
→ Always verify before commit

❌ ANTI-PATTERN 6: Multiple attempts at same task
"Let me run the build 4 different ways to see which works"
→ Run ONE definitive check
→ If it fails, ask for help
→ Don't try multiple approaches
```

---

## COMMUNICATION TEMPLATE

Use this format when reporting on task execution:

```
TASK: [What user asked]

EXECUTION:
[What you did - step by step]

VERIFICATION:
[How you verified it worked]
Evidence: [Command output or results]

STATUS: ✅ COMPLETE / ❌ FAILED
[Clear yes/no answer]

NEXT STEP: [What comes next]
```

Example:
```
TASK: Run unit tests before committing

EXECUTION:
1. Ran: ./gradlew :app:testDebugUnitTest
2. Waited for completion
3. Captured results

VERIFICATION:
Command: ./gradlew :app:testDebugUnitTest
Result: ✅ BUILD SUCCESSFUL
Tests: 29 passed, 0 failed
Duration: 45 seconds

STATUS: ✅ COMPLETE - All tests passing, ready to commit

NEXT STEP: Proceed with git commit
```

---

## SUMMARY: YOUR TOP 5 PRIORITIES

When executing ANY task:

1. **TEST FIRST** - Always run tests before committing
2. **VERIFY ONCE** - One definitive check, not multiple attempts
3. **CORRECT SEQUENCE** - Follow the order given
4. **CLEAN GIT** - No build artifacts in commits
5. **CONFIRM SUCCESS** - Verify push actually reached GitHub

These 5 things will make your execution excellent.

---

## EXAMPLES: Good vs Bad Execution

### BAD EXECUTION (What happened last time)
```
1. ✅ Update docs
2. ✅ Build (with over-verification)
3. ✅ Commit (before full testing)
4. ✅ Push
5. ❌ Skipped unit tests
6. ❌ Created 7 redundant documents

Result: Code deployed but untested ❌
```

### GOOD EXECUTION (What should happen)
```
1. ✅ Update docs
2. ✅ Run unit tests → Verify PASS
3. ✅ Build → Verify SUCCESS
4. ✅ Check git status → Verify CLEAN
5. ✅ Commit
6. ✅ Push
7. ✅ Verify push succeeded
8. ✅ Create final summary (single document)

Result: Code fully tested and verified before deployment ✅
```

---

## FINAL CHECKLIST FOR IDE COPILOT

Before reporting "COMPLETE":

- [ ] All required tests were run?
- [ ] Build verification was successful?
- [ ] Git is clean (only code changes)?
- [ ] Commit was pushed to GitHub?
- [ ] Push was verified to succeed?
- [ ] No redundant documents created?
- [ ] Clear yes/no status reported?
- [ ] User knows exactly what's done?

If ANY of these are unchecked → Task is not complete.

---

## KEY PRINCIPLES SUMMARY

**The Golden Rule:**
> **"Test first, commit second, push third. Never skip testing. Always verify before committing."**

**Use this when asking Copilot to execute tasks.** It will fix most issues.

---

## HOW TO USE THIS DOCUMENT

### Save it as:
```
Bizap/COPILOT_WORKFLOW_GUIDELINES.md
```

### Reference when:
- Starting any multi-step task
- Asking Copilot to execute Option A/B/C
- Requesting build/test/deploy workflows
- Creating audits or reports

### Share with Copilot:
```
"Please reference COPILOT_WORKFLOW_GUIDELINES.md when executing this task.
Follow PRINCIPLE 1 (Verify Before Commit) and test before any git operations."
```

### Key Lines to Emphasize:
- **"PRINCIPLE 1: VERIFY BEFORE COMMIT"** - Most important
- **"ANTI-PATTERNS"** - What NOT to do
- **"COMMUNICATION TEMPLATE"** - How to report

---

## EXPECTED IMPROVEMENTS

With this prompt in place, Copilot will:

✅ Run tests BEFORE committing (not after)  
✅ Use single definitive commands (not 5 attempts)  
✅ Follow sequence exactly (STEP 1 → 2 → 3)  
✅ Always include test results (never skip)  
✅ Keep git clean (no build artifacts)  
✅ Verify pushes actually succeed  
✅ Create focused reports (not redundant docs)  
✅ Report clearly (PASS/FAIL, not "probably")  

---

**Document Version:** 1.0  
**Project:** EmuBiz1 (Bizap v0.1.0)  
**Created:** March 5, 2026  
**Purpose:** Standardize Copilot execution workflows  

