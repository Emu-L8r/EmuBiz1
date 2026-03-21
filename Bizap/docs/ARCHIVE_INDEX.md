# 📚 Archived Documentation Index

**Last Updated:** March 22, 2026  
**Purpose:** Catalog of historical project documentation and status reports

---

## What Was Archived

On March 22, 2026, we performed a project hygiene cleanup. Over 150 historical markdown files were consolidated and archived to improve clarity and reduce root directory clutter.

### Archive Organization

```
docs/archive/
├── PHASE_REPORTS/          ← All PHASE_*.md files (planning documents)
├── BUILD_REPORTS/          ← All BUILD_*.md files (build diagnostics)
├── VERIFICATION_REPORTS/   ← All *_VERIFICATION_*.md files (test results)
└── STATUS_ARCHIVE/         ← All status/report/summary files (historical snapshots)
```

---

## Canonical Project Documentation (Keep in Root)

These are the ONLY files you need to read for current project state:

| File | Purpose |
|------|---------|
| **README.md** | Project overview, setup, and quick start |
| **STATUS.md** | Current health score and active initiatives |
| **DECISION_LOG.md** | Architectural decisions (GUI1 sunset, etc.) |
| **build.gradle.kts** | Build configuration |
| **settings.gradle.kts** | Gradle settings |

---

## What's in Each Archive Folder

### PHASE_REPORTS/
Historical planning and implementation guides for each project phase.

**When to read:** Understanding historical roadmaps or context  
**Examples:**
- PHASE_1_MASTER_PROMPT_COPILOT_AGENT.md
- PHASE_2_MASTER_PROMPT_COPILOT_AGENT.md
- PHASE_1_IMPLEMENTATION_COMPLETE.md

### BUILD_REPORTS/
Build diagnostics, error reports, and fix documentation from past build issues.

**When to read:** Troubleshooting similar build problems  
**Examples:**
- BUILD_DIAGNOSTICS_SUMMARY_MARCH_16.md
- BUILD_ERROR_FIX_DETAILED.md
- BUILD_RECOVERY_COMPLETE_MARCH_17.md

### VERIFICATION_REPORTS/
Test and verification reports from PRs and phases.

**When to read:** Checking historical test results or verification status  
**Examples:**
- PR_152_FINAL_VERIFICATION.md
- VERIFICATION_CHECKLIST_MARCH_21.md
- *_VERIFICATION_REPORT.md files

### STATUS_ARCHIVE/
Historical project state snapshots and status updates.

**When to read:** Understanding how the project evolved  
**Examples:**
- PROJECT_HEALTH_*.md
- FINAL_STATUS_*.md
- *_SUMMARY_*.md files

---

## How to Find What You Need

### If you want to know...

**"What's the current project state?"**
→ Read `/STATUS.md` (canonical source of truth)

**"What were the architectural decisions?"**
→ Read `/DECISION_LOG.md`

**"How do I set up and run the project?"**
→ Read `/README.md`

**"What happened in Phase 1?"**
→ See `/docs/archive/PHASE_REPORTS/`

**"Did we have build issues before?"**
→ See `/docs/archive/BUILD_REPORTS/`

**"What was the verification status for PR #152?"**
→ See `/docs/archive/VERIFICATION_REPORTS/`

---

## Why We Did This

### Problem
- 200+ markdown files in root and `/docs/`
- No clear which document was "canonical"
- New developers spent hours finding relevant docs
- Mix of active documentation and historical status reports

### Solution
- Keep only 5 canonical files in root
- Archive historical files by category
- Clear navigation index (this file)
- Single source of truth for current state (STATUS.md)

### Benefit
- ✅ New developers can get oriented in 5 minutes
- ✅ Current state is always in one place (STATUS.md)
- ✅ Historical context is organized and findable
- ✅ Root directory is clean and professional

---

## Note for Future Contributors

**When adding new documentation:**
1. If it's about current status → Update `/STATUS.md`
2. If it's a technical guide → Add to `/docs/`
3. If it's a one-time report → Consider if it's actually needed
4. Never add status/report files to root (use archive or docs/)

**Keep the root clean. STATUS.md is the single source of truth.**

---

**Archive created:** March 22, 2026  
**By:** GitHub Copilot (Project Hygiene Sprint 1)

