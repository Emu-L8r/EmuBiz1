# Documentation Index — Bizap v1.0

**Last Updated:** March 21, 2026  
**Maintainer:** EmuBiz Engineering  
**Purpose:** Single source of truth for all Bizap documentation

---

## Quick Navigation

### Getting Started (Start Here!)
- **[../README.md](../README.md)** — Project overview, quick start (5 min read)
- **[QUICK_START_TESTING_GUIDE.md](QUICK_START_TESTING_GUIDE.md)** — How to test the app locally

### Development
- **[ARCHITECTURE.md](ARCHITECTURE.md)** — Codebase structure, layer breakdown, design patterns
- **[BUILD_GUIDE.md](BUILD_GUIDE.md)** — Debug & release builds, step-by-step workflows
- **[DEVELOPER_PATTERNS.md](DEVELOPER_PATTERNS.md)** — ViewModel + Repository templates (TBD)
- **[TESTING_STRATEGY.md](TESTING_STRATEGY.md)** — Testing approach, frameworks, best practices

### Configuration & Security
- **[../CONFIGURATION_GUIDE.md](../CONFIGURATION_GUIDE.md)** — API keys, signing config, environment setup
- **[RELEASE_SIGNING.md](RELEASE_SIGNING.md)** — How to sign release APKs, GitHub Actions setup
- **[SIGNING_SECURITY_POLICY.md](SIGNING_SECURITY_POLICY.md)** — Security requirements for production keys
- **[SECURITY.md](SECURITY.md)** — Encryption, data protection, privacy compliance

### Features & APIs
- **[EXCHANGE_RATE_API_GUIDE.md](EXCHANGE_RATE_API_GUIDE.md)** — Multi-currency API setup, troubleshooting
- **[../docs/KNOWN_LIMITATIONS.md](KNOWN_LIMITATIONS.md)** — Known issues, workarounds, limitations

### Decisions & Roadmaps
- **[../DECISION_LOG.md](../DECISION_LOG.md)** — Architectural decisions, rationale, timelines
- **[GUI1_SUNSET_ROADMAP.md](GUI1_SUNSET_ROADMAP.md)** — GUI1 deprecation timeline (June 2027)
- **[GRADLE_MIGRATION_ROADMAP.md](GRADLE_MIGRATION_ROADMAP.md)** — Gradle 10 forward compatibility roadmap
- **[../docs/STRATEGIC_ROADMAP_6_MONTHS.md](STRATEGIC_ROADMAP_6_MONTHS.md)** — 6-month product roadmap

### Status & History
- **[../STATUS.md](../STATUS.md)** — Current project status (health score, initiatives, metrics)
- **[STATUS_ARCHIVE_INDEX.md](archive/STATUS_ARCHIVE_INDEX.md)** — Catalog of historical status reports (archived)

### Troubleshooting & Help
- **[TROUBLESHOOTING.md](TROUBLESHOOTING.md)** — Common issues, solutions, debugging tips
- **[../CONFIGURATION_GUIDE.md](../CONFIGURATION_GUIDE.md)** — Setup issues, API key problems

---

## Documentation Structure

```
Bizap/
├── README.md                          ← Start here (overview)
├── STATUS.md                          ← Current status (one source of truth)
├── DECISION_LOG.md                    ← Architectural decisions
├── CONFIGURATION_GUIDE.md             ← Setup & configuration
│
├── docs/                              ← Canonical documentation
│   ├── ARCHITECTURE.md                ← Design, layers, components
│   ├── BUILD_GUIDE.md                 ← Build workflows
│   ├── TESTING_STRATEGY.md            ← Testing approach
│   ├── SECURITY.md                    ← Security policies
│   ├── RELEASE_SIGNING.md             ← Release process
│   │
│   ├── GUI1_SUNSET_ROADMAP.md         ← GUI1 deprecation (12-month)
│   ├── GRADLE_MIGRATION_ROADMAP.md    ← Gradle 10 readiness
│   ├── EXCHANGE_RATE_API_GUIDE.md     ← Multi-currency API setup
│   ├── SIGNING_SECURITY_POLICY.md     ← Production key security
│   ├── TROUBLESHOOTING.md             ← Debug & fixes
│   ├── KNOWN_LIMITATIONS.md           ← Accepted limitations
│   │
│   ├── archive/                       ← Historical docs (do not edit)
│   │   ├── STATUS_ARCHIVE_INDEX.md    ← Index of status reports
│   │   ├── *_COMPLETE.md              ← Old completion reports
│   │   ├── *_REPORT.md                ← Old status reports
│   │   └── ... (100+ old files)
│   │
│   └── scripts/                       ← Helper scripts
│       ├── migrate-docs.sh            ← Move files to archive
│       └── ...
│
├── app/                               ← Source code
└── gradle/                            ← Build configuration
```

---

## By Role: What to Read

### For New Developers
1. Start: [../README.md](../README.md) (5 min)
2. Setup: [../CONFIGURATION_GUIDE.md](../CONFIGURATION_GUIDE.md) (10 min)
3. Architecture: [ARCHITECTURE.md](ARCHITECTURE.md) (15 min)
4. Build: [BUILD_GUIDE.md](BUILD_GUIDE.md) (10 min)
5. Code: [DEVELOPER_PATTERNS.md](DEVELOPER_PATTERNS.md) (TBD)

**Total:** ~50 minutes to productive coding

### For Project Managers
1. Status: [../STATUS.md](../STATUS.md) (current health score)
2. Roadmap: [GRADLE_MIGRATION_ROADMAP.md](GRADLE_MIGRATION_ROADMAP.md) + [GUI1_SUNSET_ROADMAP.md](GUI1_SUNSET_ROADMAP.md)
3. Decisions: [../DECISION_LOG.md](../DECISION_LOG.md)

### For QA Engineers
1. Testing: [TESTING_STRATEGY.md](TESTING_STRATEGY.md)
2. Quick Start: [QUICK_START_TESTING_GUIDE.md](QUICK_START_TESTING_GUIDE.md)
3. Troubleshooting: [TROUBLESHOOTING.md](TROUBLESHOOTING.md)

### For DevOps / Release Engineers
1. Build: [BUILD_GUIDE.md](BUILD_GUIDE.md)
2. Signing: [RELEASE_SIGNING.md](RELEASE_SIGNING.md)
3. Security: [SIGNING_SECURITY_POLICY.md](SIGNING_SECURITY_POLICY.md)
4. Configuration: [../CONFIGURATION_GUIDE.md](../CONFIGURATION_GUIDE.md)

### For Security / Compliance
1. Security: [SECURITY.md](SECURITY.md)
2. Signing Policy: [SIGNING_SECURITY_POLICY.md](SIGNING_SECURITY_POLICY.md)
3. Configuration: [../CONFIGURATION_GUIDE.md](../CONFIGURATION_GUIDE.md)

---

## Document Types & Guidelines

### Status Documents (`STATUS*.md`)
- **Purpose:** Current project health, metrics, initiatives
- **Audience:** Project manager, stakeholders
- **Update Frequency:** Weekly
- **Examples:** `STATUS.md` (current), `STATUS_ARCHIVE_INDEX.md` (historical)
- **Location:** Root (`STATUS.md`) + `docs/archive/` (old)

### Decision Documents (`DECISION_LOG.md`)
- **Purpose:** Record architectural decisions, rationale, sunset dates
- **Audience:** Developers, architects, future maintainers
- **Update Frequency:** As decisions are made
- **Location:** Root (`DECISION_LOG.md`)

### Technical Guides (`*_GUIDE.md` or `*_ROADMAP.md`)
- **Purpose:** How-to steps, troubleshooting, planning
- **Audience:** Developers, operators
- **Examples:** `BUILD_GUIDE.md`, `GRADLE_MIGRATION_ROADMAP.md`
- **Location:** `docs/`

### Policy Documents (`*_POLICY.md`)
- **Purpose:** Requirements, constraints, security rules
- **Audience:** All engineers (compliance required)
- **Examples:** `SIGNING_SECURITY_POLICY.md`
- **Location:** `docs/`

### Architecture Documents (`ARCHITECTURE.md`)
- **Purpose:** System design, layers, patterns
- **Audience:** Senior developers, architects
- **Location:** `docs/ARCHITECTURE.md`

---

## How to Update Documentation

### Adding a New Guide

1. **Create file in `docs/`:**
   ```bash
   touch docs/MY_NEW_GUIDE.md
   ```

2. **Add to this index:**
   ```markdown
   - **[MY_NEW_GUIDE.md](MY_NEW_GUIDE.md)** — Brief description of topic
   ```

3. **Commit with clear message:**
   ```bash
   git commit -m "docs: add MY_NEW_GUIDE.md for [topic]"
   ```

### Updating Existing Documentation

1. **Edit file directly**
2. **Update "Last Updated" date at top**
3. **Commit with reference:**
   ```bash
   git commit -m "docs: update GUIDE_NAME.md - [what changed]"
   ```

### Archiving Old Status Files

1. **When file becomes historical:**
   ```bash
   mv OLD_REPORT.md docs/archive/OLD_REPORT.md
   ```

2. **Update this index** (remove from main sections, link stays in archive)

3. **Commit:**
   ```bash
   git commit -m "docs: archive OLD_REPORT.md to docs/archive/"
   ```

---

## Common Documentation Tasks

### "I need to know the current project status"
→ Read: [../STATUS.md](../STATUS.md)

### "I need to set up the project locally"
→ Read: [../README.md](../README.md) + [../CONFIGURATION_GUIDE.md](../CONFIGURATION_GUIDE.md)

### "I need to build and test the app"
→ Read: [BUILD_GUIDE.md](BUILD_GUIDE.md) + [QUICK_START_TESTING_GUIDE.md](QUICK_START_TESTING_GUIDE.md)

### "I need to understand the codebase architecture"
→ Read: [ARCHITECTURE.md](ARCHITECTURE.md)

### "I need to create a release APK"
→ Read: [BUILD_GUIDE.md](BUILD_GUIDE.md) + [RELEASE_SIGNING.md](RELEASE_SIGNING.md)

### "I need to fix a build error"
→ Read: [TROUBLESHOOTING.md](TROUBLESHOOTING.md)

### "I need to understand why we're sunsetting GUI1"
→ Read: [../DECISION_LOG.md](../DECISION_LOG.md) + [GUI1_SUNSET_ROADMAP.md](GUI1_SUNSET_ROADMAP.md)

### "I need to set up the exchange rate API"
→ Read: [EXCHANGE_RATE_API_GUIDE.md](EXCHANGE_RATE_API_GUIDE.md)

### "I need to understand what's production-ready and what isn't"
→ Read: [../STATUS.md](../STATUS.md) + [KNOWN_LIMITATIONS.md](KNOWN_LIMITATIONS.md)

---

## Archive & History

**Old documentation** is moved to `docs/archive/` when it becomes outdated. This keeps the main docs folder clean and current, while preserving history.

**To view historical information:**
- See: [docs/archive/STATUS_ARCHIVE_INDEX.md](archive/STATUS_ARCHIVE_INDEX.md)
- Or browse: `docs/archive/` directory

**Why archive instead of delete?**
- Preserves project history for future reference
- Helps new team members understand what was tried before
- Useful for audit/compliance records

---

## Document Quality Checklist

When creating/updating documentation:

- [ ] **Clear title** with date and status
- [ ] **Table of contents** (if >500 lines)
- [ ] **Quick summary/overview** at top
- [ ] **Step-by-step instructions** (no assumptions)
- [ ] **Code examples** where relevant (with expected output)
- [ ] **Troubleshooting section** for common issues
- [ ] **Links to related docs** (cross-references)
- [ ] **Owner/maintainer** name at bottom
- [ ] **Review frequency** stated
- [ ] **Proofread** for typos and clarity

---

## Documentation Maintenance

### Weekly
- Update `STATUS.md` with current metrics/initiatives

### Monthly
- Archive old status reports to `docs/archive/`
- Review links in `README_INDEX.md` (are all valid?)
- Check for outdated information

### Quarterly
- Full documentation audit
- Update "Last Updated" dates
- Verify all instructions still work

---

## Useful Commands

### Find documentation by keyword
```bash
# Search for "API key" across all docs
grep -r "API key" docs/

# Search for status files
ls docs/ | grep STATUS
```

### List all documentation files
```bash
# List all .md files in docs/
find docs/ -name "*.md" | sort

# Count markdown files
find docs/ -name "*.md" | wc -l
```

### Validate markdown links (if mdlint installed)
```bash
# Check all links are valid
mdlint docs/README_INDEX.md
```

---

## Questions?

- **Build/Setup Issues?** → [TROUBLESHOOTING.md](TROUBLESHOOTING.md)
- **Architecture Questions?** → [ARCHITECTURE.md](ARCHITECTURE.md)
- **Project Status?** → [../STATUS.md](../STATUS.md)
- **Design Decisions?** → [../DECISION_LOG.md](../DECISION_LOG.md)
- **API/Config Issues?** → [../CONFIGURATION_GUIDE.md](../CONFIGURATION_GUIDE.md)

---

**Document Owner:** EmuBiz Documentation Team  
**Last Updated:** March 21, 2026  
**Review Frequency:** Weekly  
**Next Review:** March 28, 2026

