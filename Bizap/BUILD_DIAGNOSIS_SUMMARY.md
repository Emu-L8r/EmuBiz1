# Build Diagnosis & DI Remediation Summary

## 🛠 Critical Build Failures & Resolutions

This session encountered several "poisoned" build states due to deep architectural refactors (Multi-Business, Currency, and Notes). Below is the diagnosis of the 7 most likely causes identified and fixed.

### 1. Dagger/Hilt Dependency Injection (DI) Ambiguity
- **Problem**: KSP (Kotlin Symbol Processing) reported `error.NonExistentClass`.
- **Diagnosis**: Naming collisions between **Interfaces** and **Classes** in the same package. Specifically, `BusinessProfileRepository` and `ThemeRepository` existed as both an interface and a concrete class within the `data/repository` package.
- **Resolution**: Removed redundant legacy classes. Moved all Repository Interfaces strictly to the `domain/repository` layer and Implementations to `data/repository`.

### 2. ViewModel Constructor Mismatch
- **Problem**: ViewModels were failing to initialize at runtime.
- **Diagnosis**: ViewModels were importing the concrete `data` implementation instead of the `domain` interface. Hilt was unable to provide a concrete class that wasn't explicitly bound or provided.
- **Resolution**: Refactored `CreateInvoiceViewModel`, `EditInvoiceViewModel`, `SettingsViewModel`, and others to strictly inject the Domain Interfaces.

### 3. Repository Module Shadowing
- **Problem**: `@Binds` methods in `RepositoryModule.kt` were ambiguous.
- **Diagnosis**: Lack of fully qualified names in the DI module led KSP to resolve symbols to the wrong package or fail entirely.
- **Resolution**: Updated `RepositoryModule.kt` to use explicit, non-ambiguous imports and fully qualified paths for all 12 core repositories.

### 4. Unresolved References (Compile-Time)
- **Problem**: `seedTestBusinessProfile`, `addPhoto`, and `clearError` were called but missing.
- **Diagnosis**: UI code was out of sync with ViewModel refactors.
- **Resolution**: Restored missing logic in `BusinessProfileViewModel` and `CreateInvoiceViewModel` to satisfy UI requirements.

### 5. Type Mismatch (Flow vs Direct Value)
- **Problem**: UseCases were returning `Flow`, but ViewModels were treating them as direct values.
- **Diagnosis**: Recent "Hybrid Reactive" analytics change changed return signatures.
- **Resolution**: Updated `RevenueDashboardViewModel` to properly collect flows using `.first()` or `.collect()` where appropriate.

### 6. Room Entity & Migration Mismatch
- **Problem**: Database version 25 vs 26 discrepancy.
- **Diagnosis**: Adding the `Notes` feature required a schema update (v26) and a new table.
- **Resolution**: Registered `MIGRATION_25_26` and updated `AppDatabase.kt` to include the `NoteEntity`.

### 7. Implicit Return Errors
- **Problem**: Repositories failing to implement interfaces correctly.
- **Diagnosis**: Coroutines using `withContext(Dispatchers.IO)` were implicitly returning the result of their last line instead of `Unit`.
- **Resolution**: Added explicit `Unit` returns to `updateAmountPaid`, `setActiveBusinessId`, etc., ensuring interface compliance.

## 📈 Status: GREEN & STABLE
The Dependency Injection graph is now unambiguous, all references are resolved, and the app builds successfully.
