# 🚀 START HERE - Bizap Project Guide

**Welcome to Bizap** - Smart Invoice & Payment Management  
**Version:** 1.0-RC1  
**Last Updated:** April 7, 2026

---

## 🎯 Quick Links

| Need | Link | Time |
|------|------|------|
| **First time?** | [Getting Started](#getting-started) | 10 min |
| **Build & Run** | [Build & Run Guide](BUILD_AND_RUN.md) | 5 min |
| **Architecture** | [Architecture Overview](ARCHITECTURE.md) | 15 min |
| **What's Real?** | [Known Limitations](KNOWN_LIMITATIONS.md) | 5 min |
| **Why These Choices?** | [Decision Log](DECISION_LOG.md) | 10 min |
| **Offline Features** | [Offline Strategy](OFFLINE_STRATEGY.md) | 10 min |
| **All Docs** | [Documentation Index](INDEX.md) | - |

---

## 📊 Project Status

| Metric | Status | Details |
|--------|--------|---------|
| **Health Score** | 🟡 82/100 | RC-1 ready, P0 issues resolved |
| **Build Status** | ✅ SUCCESS | Release & Debug both verified |
| **Crashes** | 🟢 0 | No critical issues detected |
| **Code Quality** | 🟢 Good | Modern stack, proper patterns |
| **Documentation** | 🟡 In Progress | Consolidating 700+ docs |

---

## 🚀 Getting Started

### 1️⃣ **Clone & Setup** (5 min)
```bash
git clone https://github.com/YourOrg/Bizap.git
cd Bizap
```

### 2️⃣ **First Build** (10 min)
```bash
./gradlew clean assembleDebug
```

### 3️⃣ **Install on Device/Emulator** (5 min)
```bash
./gradlew installDebug
```

### 4️⃣ **Run Tests** (5 min)
```bash
./gradlew test
```

---

## 🏗️ Project Structure

```
Bizap/
├── app/
│   ├── src/main/java/com/emul8r/bizap/
│   │   ├── data/          (DB, Network, API)
│   │   ├── domain/        (Business logic)
│   │   ├── presentation/  (ViewModels)
│   │   └── ui/            (Compose screens)
│   ├── src/main/res/      (Strings, Colors, Dimensions)
│   └── build.gradle.kts
├── docs/                  (This documentation)
├── build.gradle.kts       (Root build config)
└── settings.gradle.kts    (Module configuration)
```

---

## 📱 What This App Does

**Bizap** is an invoice and payment management app for small businesses.

**Core Features:**
- ✅ Create & manage invoices
- ✅ Record payments
- ✅ Track customers
- ✅ View payment analytics
- ✅ Export invoices to PDF
- ✅ Works offline with WorkManager sync

**Two UI Options:**
- **GUI1**: Classic interface (legacy)
- **GUI2**: Modern Compose-based interface (recommended)

---

## ⚙️ Tech Stack

| Layer | Technology | Why |
|-------|-----------|-----|
| **UI** | Jetpack Compose + Material 3 | Modern, type-safe, reactive |
| **DI** | Hilt | Testable, clean dependency injection |
| **DB** | Room | Type-safe, observable local DB |
| **Sync** | WorkManager | Handles offline sync properly |
| **Network** | Retrofit + OkHttp | Standard Android pattern |
| **State** | MutableState + Flow | Reactive, lifecycle-aware |

---

## ⚠️ IMPORTANT: Know What's Real vs. What's Marketing

### ✅ What Actually Works
- Modern Android architecture (Compose + Hilt + Room)
- Financial calculations (Money value object prevents rounding errors)
- Offline-capable with WorkManager
- Firebase Crashlytics monitoring
- Database migrations for updates

### 🚧 What's Incomplete
- Offline-first (WorkManager works but ViewModel state not persisted)
- Clean Architecture (not fully implemented)
- Tablet optimization (partial only)
- Localization infrastructure (ready but not deployed)

**→ See [Known Limitations](KNOWN_LIMITATIONS.md) for full list**

---

## 🎓 Learning Path

### Day 1: Understand the Stack
1. Read [Architecture Overview](ARCHITECTURE.md)
2. Read [Decision Log](DECISION_LOG.md)
3. Build & run locally

### Day 2: Navigate the Code
1. Find key modules in `app/src/main/java/com/emul8r/bizap/`
2. Understand data flow (data → domain → presentation → ui)
3. Look at one screen (e.g., CreateInvoiceScreenV2.kt)

### Day 3: Make Your First Change
1. Add a string to `strings.xml`
2. Update a screen to use it
3. Build and test locally

---

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| **Build fails** | See [Build & Run Guide](BUILD_AND_RUN.md) |
| **App crashes** | Check logcat for errors, see [Known Limitations](KNOWN_LIMITATIONS.md) |
| **Offline not working** | Read [Offline Strategy](OFFLINE_STRATEGY.md) |
| **Tests fail** | Run `./gradlew clean test` |

---

## 📞 Key Contacts & Resources

- **Build Issues?** Check `app/build.gradle.kts`
- **Data Schema Issues?** Check `app/src/main/java/com/emul8r/bizap/data/local/AppDatabase.kt`
- **UI Issues?** Check `app/src/main/java/com/emul8r/bizap/ui/`
- **Network Issues?** Check `app/src/main/java/com/emul8r/bizap/data/network/`

---

## 📚 All Documentation

For complete documentation, see [Documentation Index](INDEX.md)

---

## ✨ Next Steps

1. **New Developer?** → Follow "Getting Started" above
2. **Need to Build?** → See [Build & Run Guide](BUILD_AND_RUN.md)
3. **Want to Understand Architecture?** → Read [ARCHITECTURE.md](ARCHITECTURE.md)
4. **Curious About Limitations?** → See [Known Limitations](KNOWN_LIMITATIONS.md)
5. **Everything Else?** → [Documentation Index](INDEX.md)

---

**Last Updated:** April 7, 2026  
**RC-1 Status:** 🟢 READY FOR TESTING  
**Health Score:** 82/100

