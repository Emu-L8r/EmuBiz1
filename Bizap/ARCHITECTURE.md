# Architecture Summary: Bizap v1.0

## Overview
EmuBiz Bizap is a local-first CRM and invoicing application built with **Clean Architecture**, **MVVM**, and **Jetpack Compose**.

## Stack
- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose (Material 3)
- **DI**: Hilt 2.52
- **Database**: Room 2.6.1
- **Persistence**: DataStore 1.1.1
- **Serialization**: kotlinx-serialization 1.7.3
- **Navigation**: Jetpack Navigation 2.8.5
- **API Level**: 26-35 (Android 8.0 - Android 15)

## Architectural Layers

### 1. **Presentation Layer** (UI)
**Location**: `app/src/main/java/com/emul8r/bizap/ui/**`

- **Jetpack Compose** screens (Material 3)
- **MVVM ViewModels** with `StateFlow<UiState>`
- **Unidirectional Data Flow (UDF)**
- All UI logic in Compose functions
- State management via MutableStateFlow

**Screens**:
- `CustomerList`, `CustomerDetailScreen`, `CustomerDetailViewModel`
- `InvoiceListScreen`, `InvoiceDetailScreen`, `CreateInvoiceScreen`
- `DocumentVaultScreen` (saved PDFs)

### 2. **Domain Layer** (Business Logic)
**Location**: `app/src/main/java/com/emul8r/bizap/domain/**`

- **Models**: `Customer`, `Invoice`, `LineItem`, `BusinessProfile`
- **Repositories** (Interfaces):
  - `CustomerRepository`
  - `InvoiceRepository`
  - `DocumentRepository`
- **UseCases**: (Reserved for future complex flows)

**Key Properties**:
- Pure Kotlin data classes
- No Android dependencies
- Testable in isolation

### 3. **Data Layer** (Storage & Network)
**Location**: `app/src/main/java/com/emul8r/bizap/data/**`

#### Entities (Room)
- `CustomerEntity` - Local database model
- `InvoiceEntity` - Invoice storage with PDF path tracking
- `LineItemEntity` - Invoice line items (1:N relation)
- `GeneratedDocumentEntity` - Archive of generated PDFs
- `PrefilledItemEntity` - Template line items

#### DAOs
- `CustomerDao` - CRUD for customers
- `InvoiceDao` - CRUD for invoices + status updates
- `DocumentDao` - Archive management

#### Repositories (Implementations)
- `CustomerRepositoryImpl` - Maps entities ↔ domain models
- `InvoiceRepositoryImpl` - Full invoice lifecycle
- `DocumentRepositoryImpl` - Document vault

#### Services
- `InvoicePdfService` - PDF generation from invoices
- `PrintService` - Android print framework integration
- `DocumentExportService` - (Removed - unused)
- `BusinessProfileRepository` - DataStore-backed profile

#### Database
- `AppDatabase` - Room database (Kotlin Serialization codec)
- Version: 8
- Foreign key constraints enabled
- Migration support ready

### 4. **Mappers**
**Location**: `app/src/main/java/com/emul8r/bizap/data/mapper/**`

**Responsibility**: Entity ↔ Domain Model conversion
- `CustomerMapper`: `CustomerEntity` ↔ `Customer`
- `InvoiceMapper`: `InvoiceEntity` + `LineItemEntity[]` ↔ `Invoice` + `LineItem[]`

**Key Functions**:
- `fun CustomerEntity.toDomain(): Customer`
- `fun InvoiceWithItems.toDomain(): Invoice`
- `fun Invoice.toEntity(): InvoiceEntity`

## Clean Architecture Adherence

### ✅ Separation of Concerns
```
Presentation (UI) → ViewModels → Domain (Models) → Data (Repos) → Database
```

### ✅ Dependency Injection (Hilt)
- `@HiltViewModel` for ViewModels
- `@Inject` for repositories
- `@Provides` for Room/DataStore
- Automatic constructor injection

### ✅ Unidirectional Data Flow (UDF)
```
User Action
    ↓
ViewModel.updateState()
    ↓
StateFlow<UiState> emits new state
    ↓
UI recomposes with new data
```

### ✅ Entity ↔ Domain Separation
- Entities never exposed to presentation layer
- Mappers handle conversion
- UI only sees domain models
- Database schema decoupled from business logic

### ✅ Repository Pattern
```
ViewModel → Repository (Interface) → Impl → DAO → Room
```

## Data Flow Examples

### Creating an Invoice
```
1. User fills CreateInvoiceScreen form
2. ViewModel.onSaveClicked() builds Invoice domain model
3. InvoiceRepository.saveInvoice(invoice) called
4. InvoiceRepositoryImpl converts Invoice → InvoiceEntity
5. InvoiceDao inserts entity + line items to Room
6. ViewModel emits success state
7. UI navigates back to list
```

### Generating PDF
```
1. User taps "Generate PDF" on InvoiceDetailScreen
2. ViewModel.shareInternalPdf() called
3. InvoicePdfService.generateInvoice(Invoice, BusinessProfile) 
4. Renders PDF with invoice data + business branding
5. Stores file path in Room via InvoiceRepository.updatePdfPath()
6. ViewModel emits File event
7. UI shares via Android intent
```

## Known Limitations & Future Work

### Current Constraints
1. **PDF Export to Downloads** - Not yet implemented (UI placeholder only)
2. **Invoice Editing** - Structure in place, UX not fully wired
3. **Quote Support** - Data model ready, full workflows pending
4. **Offline Sync** - Local-first only, no cloud sync

### Deprecated APIs (Non-Critical)
```
SearchBar() in DocumentVaultScreen - Update to newer Compose overload
menuAnchor() in CreateInvoiceScreen - Add MenuAnchorType parameter
```

### Testing
- Unit test framework integrated (JUnit, Espresso)
- No tests written yet (ready for implementation)
- Room DAO tests recommended
- ViewModel state tests recommended

## Dependency Decision: Kotlin 2.0.21
**Issue**: Kotlin 2.1.0 + KSP 2.1.0-1.0.29 incompatible with Hilt 2.52
- **Error**: "Unable to read Kotlin metadata due to unsupported metadata version"
- **Evidence**: Two independent clean builds both failed identically
- **Solution**: Stay on Kotlin 2.0.21 until Hilt 2.53+ released
- **Impact**: Minimal - 2.0.21 is stable, secure, and widely used

## Build Configuration
- **Gradle**: 8.x
- **AGP**: 8.8.0
- **JDK**: 17+
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 35 (Android 15)
- **Compile SDK**: 35

## File Structure
```
app/
├── src/main/
│   ├── java/com/emul8r/bizap/
│   │   ├── ui/                     # Presentation (Compose screens + VMs)
│   │   ├── domain/                 # Domain models + repo interfaces
│   │   ├── data/
│   │   │   ├── local/              # Room entities + DAOs
│   │   │   ├── repository/         # Repository implementations
│   │   │   ├── mapper/             # Entity ↔ Domain mappers
│   │   │   └── service/            # PDF, Print services
│   │   ├── di/                     # Hilt modules (DB, Repos)
│   │   └── utils/                  # Helpers (PDF naming, etc.)
│   ├── res/                        # Resources (strings, themes)
│   └── AndroidManifest.xml
├── build.gradle.kts
└── proguard-rules.pro
```

## Next Steps
1. **Testing**: Run full test suite on Android device/emulator
2. **Bug Fixes**: Address any runtime issues
3. **Feature Completion**: PDF export, invoice editing
4. **Performance**: Optimize Room queries, PDF generation
5. **Production**: Handle destructive migration settings before release

---

**Architecture Review**: ✅ Clean, testable, scalable
**Status**: Ready for testing and feature development

