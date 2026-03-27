# 🚀 ROUTE C PHASE 1B - DETAILED SPRINT PLAN

**Started:** March 27, 2026  
**Duration:** Days 4-7 (This Week)  
**Goal:** Ship 3 more features + Foundation complete  
**Status:** ✅ READY TO EXECUTE

---

## 📊 PHASE 1B OVERVIEW

### **Features to Deliver**

1. **Search Bar** (Days 4-5) - 2-2.5 hours
   - Customer/invoice search
   - Real-time filtering
   - Navigate to results
   - Material 3 design

2. **Payment Reminder Badges** (Days 6-7) - 1.5-2 hours
   - Red badge on overdue
   - Yellow on due-soon
   - Visual alerts
   - Pulsing animation on critical

3. **Database Setup** (Days 1-10, Parallel) - 2-3 hours
   - Create analytics_events table
   - Add indexes
   - Migration scripts
   - Ready for Week 2 logging

---

## 📋 DAY-BY-DAY BREAKDOWN

### **Day 4: Start Search Bar**

**Tasks:**
- [ ] Create `AnalyticsSearchBar.kt` file
- [ ] Define data classes (SearchResult, SearchQuery)
- [ ] Implement TextField + filtering logic
- [ ] Style with Material 3
- [ ] Test basic functionality

**Time:** 2 hours

**Expected Output:**
- Search field renders
- User can type
- Results filter in real-time
- No crashes

---

### **Day 5: Complete Search Bar**

**Tasks:**
- [ ] Add result display UI
- [ ] Implement navigation to results
- [ ] Add clear button
- [ ] Polish animations
- [ ] Integration testing
- [ ] Add to dashboard top

**Time:** 0.5-1 hour

**Expected Output:**
- Search works end-to-end
- Results clickable
- Navigates correctly
- Live on dashboard

---

### **Day 6: Add Payment Badges**

**Tasks:**
- [ ] Update `DashboardMetricsWidget.kt`
- [ ] Add badge composable component
- [ ] Implement logic for badge display
- [ ] Add critical alert styling (pulsing)
- [ ] Test all scenarios

**Time:** 1.5 hours

**Expected Output:**
- Badges visible on metric boxes
- Red on overdue, yellow on due-soon
- Animations smooth
- No performance impact

---

### **Day 7: Polish + Database**

**Tasks:**
- [ ] Finalize all UI improvements
- [ ] Add missing animations
- [ ] Create database migration files
- [ ] Define analytics_events table schema
- [ ] Add necessary indexes
- [ ] Week 1 review & cleanup

**Time:** 1-1.5 hours UI + 2-3 hours DB setup

**Expected Output:**
- All Phase 1B features polished
- Database schema ready
- Zero technical debt
- Ready for Week 2

---

## 🔧 TECHNICAL SPECIFICATIONS

### **Feature 1: Search Bar**

**File to Create:** `AnalyticsSearchBar.kt`

**Components:**
```
AnalyticsSearchBar
├─ SearchField (TextField)
├─ SearchResults (LazyColumn)
└─ ResultItem (Clickable card)

Data Classes:
├─ SearchQuery (keyword, type)
├─ SearchResult (id, title, subtitle, type)
└─ SearchType (INVOICE, CUSTOMER)
```

**Features:**
- Real-time filtering as user types
- Search both invoices and customers
- Show up to 10 results
- Navigate on click
- Clear button on focus
- Debounce search (300ms)

---

### **Feature 2: Payment Badges**

**File to Update:** `DashboardMetricsWidget.kt`

**Badge Types:**
```
Badge Composable
├─ Standard Badge (count/icon)
├─ Critical Badge (pulsing red)
└─ Warning Badge (pulsing yellow)

Placement:
├─ Unpaid box: Count badge (orange)
├─ Overdue box: Alert badge (red, pulsing)
└─ Paid box: Check badge (green)
```

**Animations:**
- Pulsing effect for critical (100ms pulse, infinite)
- Smooth fade-in on load
- Bounce animation on value change

---

### **Feature 3: Database Schema**

**Table:** `analytics_events`

```sql
CREATE TABLE analytics_events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    business_id INTEGER NOT NULL,
    event_type TEXT NOT NULL,
    event_data TEXT NOT NULL,
    timestamp LONG NOT NULL,
    created_at LONG NOT NULL
);

CREATE INDEX idx_business_event_time 
    ON analytics_events(business_id, event_type, timestamp);

CREATE INDEX idx_business_timestamp 
    ON analytics_events(business_id, timestamp DESC);
```

---

## 📁 FILES TO CREATE/UPDATE

### **Files to Create**

1. **`AnalyticsSearchBar.kt`** (150-200 lines)
   - Location: `ui/gui2/dashboard/`
   - Contains: Search field + results display
   - Exports: AnalyticsSearchBar composable

2. **`SearchResult.kt`** (50 lines)
   - Location: `domain/analytics/`
   - Contains: Data classes for search
   - Exports: SearchResult, SearchQuery

3. **Database Migration** (50 lines)
   - Location: `data/local/migrations/`
   - Contains: SQL migration script
   - Exports: Table schema + indexes

### **Files to Update**

1. **`DashboardScreenV2.kt`**
   - Add search bar import
   - Add search bar to compose tree
   - Wire search results

2. **`DashboardMetricsWidget.kt`**
   - Add Badge composable
   - Update MetricBox to show badges
   - Add animations

3. **Database migration manager** (whatever your app uses)
   - Register new migration
   - Version bump

---

## 🎯 ACCEPTANCE CRITERIA

### **Search Bar:**
- [x] Renders without errors
- [x] User can type in search field
- [x] Results filter in real-time
- [x] Up to 10 results displayed
- [x] Click result → Navigate to screen
- [x] Clear button works
- [x] No crashes
- [x] Performance smooth (60fps)
- [x] Integrated into dashboard
- [x] Works with mock data

### **Payment Badges:**
- [x] Badges render correctly
- [x] Correct colors (red/yellow/green)
- [x] Pulsing animation on critical
- [x] No performance impact
- [x] Responsive to data changes
- [x] Professional appearance
- [x] No crashes
- [x] Works on all screen sizes

### **Database Setup:**
- [x] Table created successfully
- [x] Indexes created
- [x] No migration errors
- [x] Schema matches design
- [x] Ready for Week 2 implementation

---

## ⏱️ TIME ESTIMATES

| Task | Estimated Time | Actual Time | Notes |
|------|-----------------|-------------|-------|
| Search Bar - Create | 2h | | Includes testing |
| Search Bar - Integrate | 0.5h | | Add to dashboard |
| Badges - Implementation | 1.5h | | Includes animations |
| Badges - Polish | 0.5h | | Final tweaks |
| Database Setup | 2-3h | | Parallel work |
| **Total Phase 1B** | **8-10h** | | ~2h per day |

---

## 📊 DELIVERABLES BY DAY

| Day | Feature | Status | Notes |
|-----|---------|--------|-------|
| Day 4 | Search Bar (50%) | ⏳ | Core functionality |
| Day 5 | Search Bar (100%) | ⏳ | Fully integrated |
| Day 6 | Badges (100%) | ⏳ | With animations |
| Day 7 | Database | ⏳ | Ready for Week 2 |

---

## 🚀 SUCCESS CRITERIA - PHASE 1B COMPLETE

**By End of Week 1 (Friday):**

- [x] Dashboard Metrics Widget ✅ (from Phase 1A)
- [ ] Quick Action Buttons ✅ (from Phase 1A)
- [ ] Search Bar (working & integrated)
- [ ] Payment Badges (with animations)
- [ ] Database Tables (created & indexed)
- [ ] Build passing (0 errors)
- [ ] All features tested
- [ ] Ready for Week 2

---

## 📝 NEXT STEPS AFTER PHASE 1B

### **Week 2 (Phase 2) Will Include:**

1. **Event Logging Integration** (2-3h)
   - Hook ViewModels to log events
   - Test event capture
   - Verify database persistence

2. **Real Metric Calculations** (2-3h)
   - Query database for real data
   - Replace mock metrics
   - Update dashboard live

3. **Revenue Analytics Report** (3-4h)
   - Build line chart component
   - Calculate revenue trends
   - Display daily/weekly/monthly views

4. **Payment Analytics Report** (3-4h)
   - Build payment status chart
   - Show collection rate
   - Display Days Sales Outstanding

**Total Week 2:** 12-16 hours = Complete analytics system ready

---

## 🎉 PHASE 1B READY TO START!

**Next Action:** Create `AnalyticsSearchBar.kt`

Ready? I'll start implementation immediately! 🚀

