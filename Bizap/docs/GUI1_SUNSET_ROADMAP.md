# GUI1 Sunset Roadmap — Deprecation & Migration Timeline

**Last Updated:** March 21, 2026  
**Status:** 🟡 In Progress (Feature Parity Phase)  
**Sunset Date:** June 1, 2027

---

## Overview

Bizap maintains two user interface options: GUI1 (legacy Activity-based) and GUI2 (modern Jetpack Compose). To reduce technical debt and increase development velocity, GUI1 will be retired in a 12-month window (March 2026 → June 2027).

This document outlines the timeline, responsibilities, and migration strategy for all stakeholders.

---

## Timeline & Phases

### Phase A: Feature Parity (March–May 2026)
**Status:** 🔄 In Progress  
**Goal:** Achieve 100% feature parity between GUI1 and GUI2

**What Happens:**
- All remaining GUI1 features are ported to GUI2
- Both GUIs remain fully functional and supported
- Users can still switch freely between them
- All bug fixes apply to both GUIs
- Testing focuses on feature parity verification

**Developer Responsibilities:**
- New features → GUI2 only (no GUI1 counterparts)
- Bug fixes → Apply to both GUIs (if applicable)
- Test in both GUIs before code review
- No new GUI1-specific code

**Expected Completion:** May 31, 2026  
**Success Criteria:** 100% feature parity verified by QA

---

### Phase B: Deprecation Warning (June–July 2026)
**Status:** 🟡 Queued (Begins after Phase A)  
**Goal:** Notify users of upcoming GUI1 retirement

**What Happens:**
- v1.1 released with deprecation warning visible in GUI1
- GUI1 landing button shows: "**Classic Interface will retire June 1, 2027**"
- GUI1 remains fully functional (no forced migration yet)
- Firebase Analytics tracks GUI1 session count + user retention
- Migration documentation published

**User Communication:**
- In-app notification on every GUI1 screen
- Email to users who primarily use GUI1
- Blog post explaining the transition
- Help articles with step-by-step GUI2 walkthrough
- FAQ addressing common concerns

**Developer Responsibilities:**
- Implement warning banner (minimal code)
- Monitor analytics dashboards
- Prepare migration support resources
- Respond to user feedback

**Expected Timeline:** June 1, 2026 – July 31, 2026

---

### Phase C: Monitoring & Support (August 2026–May 2027)
**Status:** 🟡 Queued  
**Goal:** Support users during transition; gather migration metrics

**What Happens:**
- GUI1 remains available and fully supported (no removal)
- Weekly analytics reviewed to track migration progress
- Support team proactively reaches out to heavy GUI1 users
- Migration tutorials and help articles available on website
- Community feedback collected and addressed

**Migration Targets:**
- Target: 70% of users on GUI2 by December 2026
- Target: 85% of users on GUI2 by April 2027
- Target: 95% of users on GUI2 by May 2027
- Contingency: If <60% on GUI2 by April 2027, reconsider sunset date

**Developer Responsibilities:**
- Fix critical GUI1 bugs immediately (if reported)
- Monitor support tickets for GUI1-specific issues
- No new GUI1 features or optimizations
- Document any known GUI1 limitations

**Performance Monitoring:**
- Session count by GUI (daily)
- Crash rates in GUI1 vs GUI2 (daily)
- User satisfaction scores (weekly)
- Migration blockers (collect via support team)

---

### Phase D: Removal (June 2027+)
**Status:** 🔴 Queued (Begins June 1, 2027)  
**Goal:** Remove all GUI1 code; GUI2 becomes sole interface

**What Happens:**
- v2.0 released (major version bump)
- All GUI1 code deleted from repository
- Only GUI2 available to users
- Single UI framework (Compose) for all new development
- ~40% codebase size reduction (estimated)

**Code Cleanup (Deletion List):**
```
Delete these files/directories:
├── app/src/main/java/com/emul8r/bizap/ui/activities/
│   ├── TraditionalGUIMainActivity.kt
│   └── (other GUI1 activities)
├── app/src/main/java/com/emul8r/bizap/ui/navigation/
│   ├── Gui1NavAdapter.kt
│   └── Screen.kt (GUI1 routes)
├── app/src/main/java/com/emul8r/bizap/ui/dashboard/
│   ├── DashboardScreen.kt (GUI1 version)
│   └── DashboardViewModel.kt (if GUI1-only)
├── (all other GUI1-specific screens)

Keep these:
├── All GUI2 code (gui2/ directory)
├── All shared repositories and business logic
├── Data layer (Room, SQLCipher)
├── Domain models
```

**Developer Responsibilities:**
- Code review and deletion approval
- Update all documentation (remove GUI1 references)
- Retrain team on GUI2-only architecture
- Monitor for post-release user feedback

**Timeline:**
- June 1, 2027: v2.0 released (GUI1 removed)
- June 1–30, 2027: Support window for any migration issues
- July 1, 2027: Full transition complete, normal operations resume

---

## Developer Guidelines (Effective Immediately)

### What to Do Starting Now (March 21, 2026)

✅ **New Features**
- Implement in **GUI2 only**
- No GUI1 counterpart needed
- Focus all development effort on GUI2
- Use shared components from `ui/shared/` or `ui/components/`

✅ **Bug Fixes**
- If bug exists in both GUIs: Fix in **both** (until May 31, 2026)
- If bug is GUI1-only: Fix if critical, defer if minor (low ROI)
- After May 31, 2026: Fix in **GUI2 only**
- Document which GUIs were affected in commit message

✅ **Shared Components**
- Extract common UI components to `ui/shared/` or `ui/components/`
- Both GUI1 and GUI2 can use them
- Reduces duplication, increases efficiency
- Examples: `LineItemEditor.kt`, data entry dialogs, confirmation screens

✅ **Testing**
- Write GUI parity tests (same flow in GUI1 vs GUI2)
- Regression tests for both GUIs before feature merges
- Integration tests for critical workflows

### What NOT to Do

❌ **Don't Create GUI1-Only Features**
- Reason: Will be deleted in 12 months (wasted effort)
- Impact: Delays GUI2 development, confuses codebase
- Example: Don't add "advanced search" to GUI1 only

❌ **Don't Optimize GUI1**
- Reason: Low ROI (limited lifespan)
- Impact: Takes time away from GUI2 improvements
- Example: Don't refactor GUI1 analytics dashboard

❌ **Don't Redesign GUI1 UI**
- Reason: Not worth the effort
- Impact: Development distraction
- Example: Don't polish GUI1 buttons or colors

❌ **Don't Fix Minor GUI1 Bugs**
- Reason: Maintenance burden for small impact
- Example: Don't spend 1 hour fixing minor layout issues in GUI1

### Prioritization Matrix

| Task | GUI1 | GUI2 | Notes |
|------|------|------|-------|
| **Critical bug fix** | YES | YES | Fix in both (until May 31) |
| **Feature request** | NO | YES | GUI2 only from now on |
| **Performance optimization** | NO | YES | Focus all effort on GUI2 |
| **UI polish** | NO | YES | Modern design only |
| **Documentation** | NO | YES | Remove GUI1 references |
| **Test coverage** | YES | YES | Parity testing important |

---

## User Communication Plan

### Messaging Timeline

| Date | Audience | Message | Channel |
|------|----------|---------|---------|
| May 31, 2026 | All Users | v1.1 released with warning | App update notification |
| June 1, 2026 | Heavy GUI1 users | Email: "Migrate to GUI2 benefits" | Email campaign |
| June 15, 2026 | All Users | In-app tutorial: "Try GUI2" | In-app banner |
| Sept 2026 | All Users | Blog post: FAQ and migration guide | Website blog |
| Dec 1, 2026 | Remaining GUI1 users | Email: "Reminder - 6 months left" | Email |
| March 1, 2027 | Remaining GUI1 users | Email: "Final 3 months - action needed" | Email + in-app |
| May 15, 2027 | Remaining GUI1 users | Email: "Last reminder - 2 weeks left" | Email + in-app |
| June 1, 2027 | All Users | v2.0 released - GUI1 removed | Release notes |

### Sample User Communications

**In-App Deprecation Warning (v1.1+):**
```
┌─────────────────────────────────────┐
│ Classic Interface Retiring Soon     │
├─────────────────────────────────────┤
│ This interface will be retired on   │
│ June 1, 2027 (12 months from now).  │
│                                     │
│ [Learn About GUI2] [Dismiss]        │
└─────────────────────────────────────┘
```

**Email Subject Line (June 2026):**
> "✨ Experience the New Bizap Interface (Classic is Retiring)"

**FAQ Question:**
> **Q: Why are you retiring the Classic Interface?**  
> A: To reduce complexity and improve development speed. The new interface is faster, more modern, and uses latest Android best practices. All features work identically in both—just with a cleaner design.

---

## Monitoring & Analytics

### Metrics to Track

**User Behavior:**
- Daily active users (DAU) by GUI
- Session count by GUI (GUI1 vs GUI2 split)
- Feature usage by GUI (which features used in which interface)
- User retention by GUI

**Performance:**
- Crash rate by GUI (GUI1 crashes vs GUI2 crashes)
- Performance metrics (launch time, responsiveness)
- Feature reliability (which features crash most)

**Migration Progress:**
- % of users on GUI2 (target: 95% by May 2027)
- Days since last GUI1 session (identify inactive users)
- User satisfaction scores (if collected)

### Dashboard Setup

**Firebase Analytics Events to Log:**
```
Event: gui_mode_selected
Properties:
  - gui_selected: "GUI1" or "GUI2"
  - user_id: anonymized
  - timestamp: session start time

Event: feature_used
Properties:
  - feature_name: "invoice_create", "customer_list", etc.
  - gui: "GUI1" or "GUI2"
  - success: true/false
```

**Weekly Report Template:**
```
WEEKLY MIGRATION REPORT
Date: [Week of MM/DD]

GUI Usage Split:
- GUI1: X% (↓ Y.Z%)
- GUI2: X% (↑ Y.Z%)

Top Features by GUI:
- GUI1: [list]
- GUI2: [list]

Crash Rates:
- GUI1: X crashes/1000 sessions
- GUI2: Y crashes/1000 sessions

Support Tickets:
- GUI1-related: N
- GUI2-related: N
- Migration-related: N

Action Items:
- [list]
```

---

## Contingency Plans

### Scenario 1: Slower-Than-Expected Migration

**If:** <60% on GUI2 by April 2027

**Action:** 
1. Extend sunset date to September 2027 (3-month extension)
2. Announce extension immediately (transparency)
3. Increase migration support efforts
4. Offer incentive (e.g., in-app tutorial, email reminders)

**Communication:**
> "Based on user feedback, we've decided to extend the Classic Interface retirement to September 1, 2027. This is the final extension. We're committed to helping everyone migrate smoothly."

### Scenario 2: Critical Bug Found in GUI1 (After May 2027)

**If:** Security or critical bug discovered in GUI1

**Action:**
1. Fix in v1.x patch release (even though GUI1 in sunset phase)
2. Communicate urgently to affected users
3. Recommend GUI2 migration
4. Proceed with v2.0 release as planned

**Note:** Can't ship broken GUI1 to production, even during sunset phase.

### Scenario 3: Unexpected User Backlash

**If:** Users petition for GUI1 to stay

**Action:**
1. Acknowledge feedback respectfully
2. Explain technical/business rationale
3. Offer enhanced migration support (tutorials, webinars)
4. Proceed with timeline as planned (for codebase health)
5. Monitor community sentiment closely

**Note:** Decision to sunset GUI1 is final (based on long-term codebase health), but implementation can be adjusted based on feedback.

---

## Success Criteria

### Phase A Success (by May 31, 2026)
- [ ] All GUI1 features available in GUI2
- [ ] Feature parity tests passing (100%)
- [ ] No new GUI1-specific code merged
- [ ] Documentation updated with new features
- [ ] Code review checklist includes "both GUIs?" question

### Phase B Success (by July 31, 2026)
- [ ] Deprecation warning visible in GUI1
- [ ] v1.1 released and downloaded by 50%+ of users
- [ ] Migration guide published and accessible
- [ ] Analytics baseline established (GUI split measured)
- [ ] User feedback collected and responded to

### Phase C Success (by May 31, 2027)
- [ ] 95%+ of users on GUI2
- [ ] <5% of daily sessions in GUI1
- [ ] No critical bugs in GUI1
- [ ] Migration support requests trending down
- [ ] Code cleanup list prepared for v2.0

### Phase D Success (by July 1, 2027)
- [ ] v2.0 released with GUI1 removed
- [ ] All GUI1 code deleted from repository
- [ ] Codebase size reduced ~40%
- [ ] Documentation fully updated
- [ ] Zero GUI1 support tickets (all users migrated or off the app)

---

## Developer Checklist

### Before Phase B (May 31, 2026)
- [ ] All GUI1 screens have GUI2 equivalent
- [ ] Feature parity matrix completed and 100% green
- [ ] Feature parity tests written and passing
- [ ] No GUI1-only code merged since March 21, 2026
- [ ] Code review guidelines updated (document "both GUIs?" question)

### Before Phase C (August 1, 2026)
- [ ] Deprecation warning code deployed to v1.1
- [ ] Firebase Analytics events logging correctly
- [ ] Migration documentation published
- [ ] Support team trained on FAQ

### Before Phase D (June 1, 2027)
- [ ] GUI1 code deletion list finalized
- [ ] All references to GUI1 removed from docs
- [ ] v2.0 release notes prepared
- [ ] Migration completion confirmed (95%+ on GUI2)

---

## References

- **Decision Log:** See Decision #5 in `DECISION_LOG.md`
- **Architecture:** `docs/ARCHITECTURE.md`
- **Testing Strategy:** `docs/TESTING_STRATEGY.md`
- **Build Guide:** `docs/BUILD_GUIDE.md`

---

**Document Owner:** EmuBiz Product Team  
**Last Updated:** March 21, 2026  
**Next Review:** May 31, 2026 (end of Phase A)  
**Approvals:** [To be signed off by product + engineering leads]

