# 📊 Analytics & Reporting - Quick Reference Guide

## 🚀 Quick Start

### Navigate to Analytics Dashboard
```
App Home → Menu → Analytics Insights
```

### Available Tabs
1. **Quick Reports** - 9 Executive KPIs at a glance
2. **Revenue** - MTD/YTD metrics with daily trends
3. **Payment** - Outstanding amount, collection rate, aging
4. **Customers** - Customer segments, LTV, churn rate
5. **Risk** ⭐ NEW - Risk score, overdue, collection metrics
6. **Cash Flow** - Coming soon

---

## 📈 Customer Analytics (Tab 4)

### What You See
```
┌─────────────────────────────────┐
│ Total Customers: 45 (↑3%)       │
├─────────────────────────────────┤
│         📊 Segmentation         │
│  ┌──────────┬──────────┐        │
│  │ VIP ⭐   │ Regular  │        │
│  │ 3 (7%)   │ 27 (60%) │        │
│  └──────────┴──────────┘        │
│  ┌──────────┬──────────┐        │
│  │ At-Risk⚠️ │ Dormant  │        │
│  │ 9 (20%)  │ 6 (13%)  │        │
│  └──────────┴──────────┘        │
├─────────────────────────────────┤
│ Avg Customer LTV: $850          │
│ Churn Rate: 5%                  │
└─────────────────────────────────┘
```

### Key Metrics
- **Total Customers**: Sum of all customer segments
- **Customer Segments**: Breakdown by VIP, Regular, At-Risk, Dormant
- **Average LTV**: Revenue per customer (lifetime value)
- **Churn Rate**: % of customers lost in period

### How to Use
1. Tap "Total Customers" card → See breakdown
2. Tap segment card (e.g., "VIP") → Drill into segment details
3. Tap pie chart → See percentage breakdown
4. Change date range (7d/30d/90d) → Data updates

### Segment Meanings
- **VIP (⭐)**: High-value, frequent customers
- **Regular**: Steady, reliable customers
- **At-Risk (⚠️)**: Declining activity, may churn
- **Dormant**: No activity, potential reactivation targets

---

## 🚨 Risk Analytics (Tab 5) - NEW!

### What You See
```
┌─────────────────────────────────┐
│     Risk Score: 18/100          │
│     ┌─────────────────┐         │
│     │███░░░░░░░░░░░░░│ MEDIUM  │ ← Orange
│     └─────────────────┘         │
├─────────────────────────────────┤
│ At-Risk Invoices: 8 (↑2)        │
│ Overdue 90+ Days: $8,500        │
│ Collection Rate: 75% (↑1%)      │
├─────────────────────────────────┤
│ Collections Effectiveness: 82%  │
│ Days Sales Outstanding: 18 days │
└─────────────────────────────────┘
```

### Risk Score Explained
- **0-10% (Green)**: Healthy payment collection
- **10-20% (Orange)**: Some overdue invoices, monitor
- **20%+ (Red)**: Significant overdue amount, action needed

### Key Metrics
1. **Risk Score**: Calculated from overdue invoice percentage
2. **At-Risk Invoices**: Count of invoices needing attention
3. **Overdue 90+ Days**: Amount seriously past due
4. **Collection Rate**: % of invoiced amount collected
5. **Aging by Bucket**: Breakdown of outstanding invoices
6. **Collection Effectiveness**: % of issued amount collected
7. **Days Sales Outstanding**: Average days to payment

### How to Use
1. **Check Risk Score first** → Understand overall health
2. **Tap any red card** → See details
3. **Review Aging chart** → Identify payment patterns
4. **Check Summary card** → Comprehensive overview

### When to Take Action
- Risk Score ≥ 20% → Review at-risk invoices
- Overdue 90+ > $5,000 → Consider collection calls
- Collection Rate < 70% → Review payment terms
- DSO > 45 days → Follow up on late payments

---

## 💡 Comparison: Customers vs Risk Tabs

| Question | Go To Tab |
|----------|-----------|
| "Who are my best customers?" | **Customers** (VIP segment) |
| "Who might leave me?" | **Customers** (At-Risk segment) |
| "How healthy is my cash flow?" | **Risk** (Risk Score + Collection Rate) |
| "What's my average payment time?" | **Risk** (Days Sales Outstanding) |
| "How much money is overdue?" | **Risk** (Overdue 90+ Days) |
| "What's my customer base size?" | **Customers** (Total Customers) |
| "Are collections improving?" | **Risk** (Collection Rate trend) |

---

## 🎯 Pro Tips

### For Managers
1. **Daily Check**: Look at Risk Score (5 seconds)
2. **Weekly Review**: Check Collections Effectiveness
3. **Monthly Analysis**: Compare segments and LTV trends
4. **Quarterly Planning**: Use at-risk count for retention budget

### For Sales
1. **Target VIP**: Focus on expanding VIP segment
2. **At-Risk Care**: Proactive outreach to at-risk customers
3. **Pricing Strategy**: Use LTV for discount decisions
4. **Growth Tracking**: Monitor customer count growth

### For Finance
1. **Cash Planning**: Use DSO and collection rate
2. **Credit Policy**: Based on at-risk metrics
3. **Forecasting**: Use overdue and aging trends
4. **Performance**: Track collection effectiveness

---

## 📱 Mobile Tips

### Best Practices
- **Portrait mode**: All cards visible, scroll for details
- **Landscape mode**: More cards per row
- **Dark theme**: Easier on eyes for evening use
- **Date filters**: Swipe between 7d/30d/90d quickly

### Accessibility
- **Zoom**: Pinch to zoom on any chart
- **Text size**: Adjusts with system settings
- **Color-blind**: Text labels + colors (not color-only)
- **Keyboard**: Tab navigation supported

---

## 🔄 Data Refresh

### When Data Updates
- **Real-time**: Most metrics update within 5 minutes
- **Daily**: Snapshots calculated nightly
- **Weekly**: Trend data compiled Sundays
- **Manual**: Pull-to-refresh available

### How to Refresh
1. Swipe down on any tab (pull-to-refresh)
2. Switch to different tab and back
3. Change date range filters
4. Close and reopen app

---

## ⚙️ Settings & Customization

### Date Range Filters
- **7 Days**: Last week only
- **30 Days**: Last month (most common)
- **90 Days**: Quarterly view
- **Custom**: Coming soon (date picker)

### Metric Drilling
1. Tap any metric card
2. Bottom sheet shows details
3. Swipe down to dismiss
4. Tap outside to close

---

## ❓ FAQ

### Q: Why is my Risk Score high?
**A**: You have overdue invoices. Check "Overdue 90+ Days" and review aging bucket.

### Q: How do I reduce Days Sales Outstanding?
**A**: Send invoices faster, follow up after 15 days, offer early payment discounts.

### Q: My collection rate is below 75%, is that bad?
**A**: Below 70% needs attention. Review payment terms, consider payment plans.

### Q: How is Average LTV calculated?
**A**: Total revenue from all customers ÷ number of customers.

### Q: What does "At-Risk" mean?
**A**: Customers with declining activity or overdue payments (churn risk).

---

## 📞 Troubleshooting

| Issue | Solution |
|-------|----------|
| No data showing | Check date range, ensure invoices exist |
| Numbers seem wrong | Refresh data (pull down), check date range |
| Risk score too high | Review overdue invoices, follow up on collections |
| Can't tap metric | Ensure whole card is visible, tap center |
| Chart not showing | Try switching tabs, then return |

---

## 🎓 Key Concepts

### Aging Buckets
- **Current (0-30d)**: Just due, payment likely coming
- **Past 30d (31-60)**: Getting late, follow-up recommended
- **Past 60d (61-90)**: Very late, escalation needed
- **Past 90d (90+)**: Critical, collection action required

### Collection Metrics
- **Collection Rate**: (Paid / Total Issued) × 100%
- **Collection Effectiveness**: Same as collection rate
- **Days Sales Outstanding**: Average days before payment received
- **Outstanding Amount**: Total not yet collected

### Customer Segments
- **VIP**: Top 10% of customers by revenue
- **Regular**: Middle 50-60% by revenue
- **At-Risk**: Declining customers, <3 months since last order
- **Dormant**: No orders in >3 months

---

## 🔐 Data Security

- **Encrypted**: All financial data encrypted at rest
- **Secure**: Uses industry-standard encryption (AES-256)
- **Private**: Data synced to Firebase with encryption
- **Compliant**: GDPR/CCPA compliant data handling

---

## 🚀 Coming Soon

- ✨ PDF/CSV export of analytics
- 📅 Custom date range picker
- 📈 Trend prediction (AI)
- 🎯 Goal setting and alerts
- 📊 Advanced Vico charts
- 📱 Widget support

---

**Last Updated:** March 27, 2026  
**Version:** 1.0  
**Status:** Production Ready ✅

For more details, see: `ANALYTICS_REPORTING_ENHANCEMENT_COMPLETE.md`

