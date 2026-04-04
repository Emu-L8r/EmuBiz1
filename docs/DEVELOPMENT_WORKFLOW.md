# Development Workflow

## Code Standards

### Kotlin Style

- Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Maximum line length: **120 characters**
- Indentation: **4 spaces** (no tabs)
- Use `val` over `var` wherever possible
- Prefer expression bodies for single-expression functions

### Naming Conventions

| Element | Convention | Example |
|---|---|---|
| Classes | `PascalCase` | `InvoiceDetailViewModel` |
| Functions | `camelCase` | `generatePreview()` |
| Properties | `camelCase` | `selectedHtmlStyle` |
| Constants | `UPPER_SNAKE_CASE` | `PREVIEW_DEBOUNCE_MS` |
| Packages | `lowercase` | `com.emul8r.bizap.data.service.sass` |
| Layout files | `snake_case` | `invoice_detail_screen.xml` |
| Test classes | `ClassNameTest` | `InvoiceDetailViewModelTest` |

### Architecture Rules

1. **Domain layer** has zero Android dependencies (pure Kotlin)
2. **Data layer** depends only on Domain
3. **UI layer** depends on Domain and Data via ViewModels
4. **No direct DAO access from ViewModels** — always go through a Repository
5. **No business logic in Composables** — delegate to ViewModel

### Comments

- Use KDoc (`/** */`) for public API methods and classes
- Use `//` for single-line explanatory comments
- Avoid commenting what the code does — comment *why*

---

## Git Workflow

### Branch Naming

```
feature/<short-description>     # new features
fix/<bug-description>           # bug fixes
refactor/<area>                 # code cleanup
docs/<topic>                    # documentation only
chore/<task>                    # build / CI / tooling
```

### Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add SASS Professional invoice style
fix: resolve text overlap in PDF table rows
docs: add Phase 1 documentation suite
refactor: consolidate InvoiceDetailViewModel
test: add unit tests for SassStyleEngine
```

### Pull Request Process

1. Create feature branch from `main`
2. Write / update tests for your changes
3. Ensure `./gradlew test` passes with no new failures
4. Ensure `./gradlew assembleDebug` builds successfully
5. Open PR with description including:
   - What changed and why
   - How to test
   - Screenshots for UI changes
6. Request review from at least one team member
7. Address review comments
8. Squash-merge after approval

---

## Local Setup

### First-Time Setup

```bash
# 1. Clone
git clone https://github.com/Emu-L8r/EmuBiz1.git
cd EmuBiz1/Bizap

# 2. Open in Android Studio
# File → Open → select EmuBiz1/Bizap/

# 3. Sync Gradle
# Android Studio will prompt — click "Sync Now"

# 4. Run
# Select a device/emulator and click Run ▶
```

### Running Tests

```bash
# Unit tests
./gradlew test

# Specific module
./gradlew :app:test

# With coverage (JaCoCo — when configured)
./gradlew jacocoTestReport
```

### Building

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requires signing config)
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

---

## Debugging Tips

### PDF Generation

- Enable `Timber.plant(Timber.DebugTree())` in `Application.onCreate()` to see PDF generation logs
- PDF files are written to `context.filesDir/documents/`
- Check `HtmlPdfInvoiceService` log tags: `📝`, `✅`, `❌`

### Database

- Use Android Studio Database Inspector (View → Tool Windows → App Inspection)
- DB file is encrypted with SQLCipher; use `AppDatabase` companion object for access
- Check migration logs tagged `🔄` in Logcat

### Preview

- If live preview is blank, tap the **Refresh** (↺) button in PDF Settings
- Preview uses `PlaceholderInvoiceGenerator` — real invoice data is not used in preview

---

## Common Gotchas

| Issue | Cause | Fix |
|---|---|---|
| Build fails with `FileSystemAlreadyExistsException` | `isShrinkResources=true` conflict | Keep `isShrinkResources=false` until resource shrinking is fixed |
| Room `IllegalStateException` on migration | Missing migration | Add migration to `MIGRATION_XX_YY.kt` and register in `DatabaseModule` |
| PDF shows overlapping text | Row height too small | Use `line-height:1.8` and `padding:10px 14px` in table cells |
| Live preview shows blank page | `previewHtml` is null on first load | `generatePreview()` is called on `loadSettings()` completion — wait for state |
| `SASS_PROFESSIONAL` not appearing in style list | Enum added but style file referenced | No style file needed — template is self-contained in `HtmlPdfInvoiceService` |
