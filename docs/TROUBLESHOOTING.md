# Troubleshooting Guide

Common issues, their causes, and solutions for BizAP development and testing.

---

## Build Failures

### `FileSystemAlreadyExistsException` during APK packaging

**Symptom:** Build fails with `FileSystemAlreadyExistsException: /path/to/resources.pb`  
**Cause:** `isShrinkResources = true` conflicts with `noCompress` resource types used by iText7  
**Fix:**
```kotlin
// Bizap/app/build.gradle.kts
buildTypes {
    release {
        isShrinkResources = false  // Keep false until resolved
        isMinifyEnabled = true
    }
}
```

---

### `unresolved reference: SASS_PROFESSIONAL`

**Symptom:** Kotlin compile error after pulling latest  
**Cause:** `HtmlInvoiceStyle.SASS_PROFESSIONAL` added to enum but when-expression not updated  
**Fix:** Add `HtmlInvoiceStyle.SASS_PROFESSIONAL -> generateSassProfessionalTemplate(...)` to every `when (style)` block in `HtmlPdfInvoiceService`

---

### `Room schema export directory is not provided`

**Symptom:** Warning/error during build  
**Fix:** Ensure `room.schemaLocation` is set in `build.gradle.kts`:
```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
```

---

## Runtime Crashes

### `IllegalStateException: Migration XX→YY is missing`

**Cause:** Database version bumped without a corresponding migration  
**Fix:**
1. Create `Migration_XX_YY.kt` in `data/local/migrations/`
2. Register in `DatabaseModule.kt` in the `addMigrations(...)` call
3. In DEBUG builds the app uses `fallbackToDestructiveMigration()` — reinstall to clear

---

### `NullPointerException` in `InvoiceSettingsViewModel`

**Cause:** `generatePreview()` called before `loadSettings()` completes  
**Fix:** Already mitigated — `generatePreview()` guards with `val currentSettings = _uiState.value.settings ?: return@launch`

---

### Crash on analytics screen open

**Cause:** Analytics event deserialization returns null (known issue in `AnalyticsRepositoryImpl`)  
**Workaround:** The screen renders with empty state; no user-visible crash in release  
**Long-term fix:** Implement polymorphic Gson/Moshi deserialization (see TODO #2 in `TODO_IMPLEMENTATION_CHECKLIST.md`)

---

## Database Migration Issues

### `SQLiteException: no such column: selected_html_style`

**Cause:** Migration 38→39 was not applied (e.g., device had version 37)  
**Fix:** Ensure all migrations are registered in `DatabaseModule`:
```kotlin
MIGRATION_38_39,              // selected_html_style, selected_canvas_template
MIGRATION_AddPdfEngineAndLayout  // selected_pdf_engine, selected_page_layout
```

---

### Data missing after update

**Cause:** Destructive migration triggered in RELEASE (should never happen — check `DatabaseModule`)  
**Prevention:** `fallbackToDestructiveMigration()` is only enabled in DEBUG builds:
```kotlin
if (BuildConfig.DEBUG) {
    builder.fallbackToDestructiveMigration()
}
```

---

## PDF Generation Problems

### PDF shows overlapping / clipped text

**Cause:** Table row height too small; line-height not set  
**Fix:** Use `line-height:1.8` and `padding:10px 14px` in all table cells. All templates in `HtmlPdfInvoiceService` already enforce this. If you see it in a new template, check the row style.

---

### PDF is blank / zero bytes

**Cause:** iText7 `HtmlConverter.convertToPdf()` threw silently  
**Fix:** Check Logcat for `❌ PDF conversion failed` tag in `HtmlPdfInvoiceService`. Common causes:
- Invalid HTML (unclosed tags)
- Null `outputStream` (disk full or permissions)
- CSS property not supported by iText7 (e.g., `flexbox`, `grid`, CSS variables)

**Note:** iText7 does not support CSS custom properties (`var(--color)`). Use hardcoded hex values in templates.

---

### SASS Professional style PDF looks different from preview

**Cause:** iText7 ignores some CSS classes; live preview uses a full WebView  
**Fix:** SASS Professional template uses only iText7-safe inline styles (no CSS classes, no `var()`, no `flexbox`). If adding new CSS, test with iText7 first.

---

### PDF file not found after generation

**Cause:** File written to `context.filesDir` but looked up with wrong path  
**Fix:** PDF files are at `context.filesDir/documents/<filename>`. Use `DocumentNamingUtils.generateFileName()` to get the correct name.

---

## Performance Issues

### Dashboard loads slowly (> 2 s)

**Cause:** Multiple parallel Room queries without date filtering  
**Fix:** See [PERFORMANCE_BASELINE.md](PERFORMANCE_BASELINE.md) for optimisation opportunities. Short-term: ensure device is not running low on memory.

---

### Live preview lags when changing styles

**Cause:** Preview re-renders on every setting change  
**Fix:** Already debounced at 300 ms (`PREVIEW_DEBOUNCE_MS` in `InvoiceSettingsViewModel`). If still slow, increase debounce to 500 ms.

---

## Testing Failures

### `Test X failed: expected Y but was null`

**Cause:** ViewModel test missing `@get:Rule val mainDispatcherRule = MainDispatcherRule()`  
**Fix:** Add the rule and ensure `TestCoroutineDispatcher` is provided via `StandardTestDispatcher()`

---

### `Room cannot verify the data integrity`

**Cause:** Schema hash mismatch — usually because entity was changed without bumping DB version  
**Fix:** Bump `version` in `@Database` annotation and add a migration

---

### Test compile error: `when` expression not exhaustive

**Cause:** New `HtmlInvoiceStyle` enum value (e.g., `SASS_PROFESSIONAL`) added but test `when` not updated  
**Fix:** Add the new branch to every `when (style)` in test files
