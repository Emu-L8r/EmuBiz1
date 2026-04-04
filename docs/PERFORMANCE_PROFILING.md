# Performance Profiling Guide

> **Phase 7 — April 2026**
> Covers Android Profiler setup, key profiling scenarios, and benchmark execution.

---

## Quick Start

```bash
./gradlew :app:benchmarkReport   # Runs all Macrobenchmark tests
./gradlew jacocoTestReport        # Code coverage (unit tests)
```

---

## Android Profiler Setup

### 1. Connect Profiler

1. Start app in **debug** or **profileable** build (`debuggable true` or `<profileable android:shell="true"/>` in manifest).
2. In Android Studio → **View → Tool Windows → Profiler**.
3. Select the running `com.emul8r.bizap` process.

### 2. CPU Profiler — Startup & Invoice Creation

| Scenario | What to record |
|----------|----------------|
| Cold start | From app launch until `MainActivity.onCreate` returns |
| Invoice creation | From "Create" button tap until invoice is saved in Room |
| PDF generation | From "Generate PDF" click until `File.exists()` returns `true` |

**Steps:**

1. Click **CPU** → **Record (Java/Kotlin method sampling)**.
2. Perform the target action.
3. Click **Stop recording**.
4. Look for hot paths: `HtmlPdfInvoiceService`, `InvoiceRepositoryImpl.saveInvoice`, `SassStyleEngine.compile`.

### 3. Memory Profiler — Leak Detection

1. Click **Memory** tab.
2. Trigger invoice list navigation multiple times (back-and-forth).
3. Force GC (garbage can icon).
4. Check **heap dump** for:
   - Retained `ViewModel` instances after navigation pop.
   - `Bitmap` objects left in memory after PDF preview is dismissed.

### 4. Battery Historian

Useful for diagnosing background work (sync, WorkManager tasks):

```bash
adb bugreport bugreport.zip
# Open Battery Historian at https://bathist.ef.lc/ and upload the zip
```

---

## Benchmark Tests

Benchmark tests live in `Bizap/app/src/androidTest/java/com/emul8r/bizap/benchmark/`.

### InvoiceCreationBenchmark

Measures the end-to-end time for creating and saving an invoice.

**Target:** < 2 seconds (95th percentile)

### PDFGenerationBenchmark

Measures time for each PDF style (Canvas, MODERN HTML, SASS_PROFESSIONAL).

**Targets:**
- Canvas styles: < 2 s
- HTML styles: < 3 s
- SASS Professional: < 4 s

---

## Interpreting Results

After running benchmarks, results appear in `build/outputs/connected_android_test_additional_output/`.

| Metric | Good | Needs Attention |
|--------|------|-----------------|
| `timeToFullDisplayMs` | < 2 000 | > 3 000 |
| `frameDurationCpuMs` | < 16 | > 32 (jank) |
| `allocations` per frame | < 5 objects | > 50 objects |

---

## Key Files to Profile

| File | What to look for |
|------|-----------------|
| `HtmlPdfInvoiceService.kt` | Time inside `generateHtml*()` |
| `SassStyleEngine.kt` | `compile()` execution time |
| `InvoiceRepositoryImpl.kt` | `saveInvoice()` transaction time |
| `InvoicePagingSource.kt` | Paging query time |
| `AnalyticsRepositoryImpl.kt` | Event serialisation/deserialisation |
