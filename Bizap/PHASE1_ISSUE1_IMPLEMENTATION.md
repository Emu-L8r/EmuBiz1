# PHASE 1 - ISSUE #1: Clean Domain Architecture
## Detailed Action Plan & Implementation Guide

**Status**: Ready to implement  
**Effort**: 1-2 hours  
**Risk**: Low (pure refactoring, no behavior change)  
**Branch**: `refactor/clean-domain-architecture`  

---

## Problem Statement

The domain layer is currently **architecture-compromised**:

```
❌ CURRENT STATE (Violates Clean Architecture):
domain/
├── build.gradle.kts (depends on androidx.room, androidx.paging)
├── models/
│   ├── Invoice.kt (has @Entity annotation)
│   ├── Customer.kt (has @Entity annotation)
│   └── ... (all using Room annotations)
└── ... (imports Room, Paging classes)

This creates a hard dependency:
Domain → Room/Paging frameworks (WRONG!)
```

**Why This Is Wrong**:
- Domain should be **framework-agnostic**
- Domain models shouldn't know about Room/Paging
- Makes unit testing harder
- Violates single responsibility principle
- Creates tight coupling

---

## Target Architecture

```
✅ CORRECT STATE (Clean Architecture):
domain/
├── build.gradle.kts (only kotlin stdlib, no framework deps)
├── models/
│   ├── Invoice.kt (plain Kotlin data classes)
│   ├── Customer.kt (plain Kotlin data classes)
│   └── ... (zero framework annotations)
└── ...

data/
├── build.gradle.kts (depends on androidx.room, domain)
├── local/
│   ├── entities/
│   │   ├── InvoiceEntity.kt (@Entity, maps to Invoice)
│   │   ├── CustomerEntity.kt (@Entity, maps to Customer)
│   │   └── ... (framework-specific)
│   └── mappers/
│       ├── InvoiceMapper.kt (Entity ↔ Domain model)
│       ├── CustomerMapper.kt (Entity ↔ Domain model)
│       └── ... (conversion logic)
└── ...

Dependency flow (correct):
Data → Domain ✅ (data knows about domain)
Domain → ø (domain knows nothing) ✅
```

---

## Discovery Phase: Find All Room Dependencies in Domain

Run this in git bash:

```bash
# Find all Room annotations in domain module
echo "🔍 Finding Room annotations in domain..."
grep -r "@Entity\|@Dao\|@Database\|@Query\|@Insert\|@Update\|@Delete" \
  domain/src/main/java --include="*.kt" | head -20

# Find all Paging imports in domain
echo "🔍 Finding Paging imports in domain..."
grep -r "androidx.paging" domain/src/main/java --include="*.kt" | head -10

# Find Room imports
echo "🔍 Finding Room imports in domain..."
grep -r "androidx.room" domain/src/main/java --include="*.kt" | head -10

# List all files in domain/models
echo "🔍 Files in domain/models..."
find domain/src/main/java -type f -name "*.kt" | grep -i model | sort
```

---

## Implementation Steps

### STEP 1: Create Data Layer Entity Models (15 min)

**Goal**: Move @Entity models from domain to data/local/entities/

**Action**:

1. Create directory structure:
```bash
mkdir -p data/src/main/java/com/emul8r/bizap/data/local/entities
mkdir -p data/src/main/java/com/emul8r/bizap/data/local/mappers
```

2. For each domain model that has `@Entity`, create an Entity version in data layer:

**Example: InvoiceEntity.kt** (in data/local/entities/)
```kotlin
package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import java.math.BigDecimal

@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        androidx.room.Index("customer_id"),
        androidx.room.Index("invoice_number"),
        androidx.room.Index("status")
    ]
)
data class InvoiceEntity(
    @PrimaryKey
    val id: Long = 0,
    val customerId: Long,
    val invoiceNumber: String,
    val status: String,
    val totalAmount: BigDecimal,
    val paidAmount: BigDecimal = BigDecimal.ZERO,
    val invoiceDateMs: Long,
    val dueDateMs: Long,
    val paidAtMs: Long? = null,
    val createdAtMs: Long,
    val modifiedAtMs: Long
)
```

3. Repeat for each model in domain that has `@Entity`:
   - CustomerEntity
   - PaymentEntity
   - SnapshotEntity
   - etc.

---

### STEP 2: Create Mapper Functions (20 min)

**Goal**: Convert between Entity (data layer) and Model (domain layer)

**Create: data/local/mappers/InvoiceMapper.kt**
```kotlin
package com.emul8r.bizap.data.local.mappers

import com.emul8r.bizap.data.local.entities.InvoiceEntity
import com.emul8r.bizap.domain.models.Invoice

object InvoiceMapper {
    
    fun entityToDomain(entity: InvoiceEntity): Invoice {
        return Invoice(
            id = entity.id,
            customerId = entity.customerId,
            invoiceNumber = entity.invoiceNumber,
            status = Invoice.Status.fromString(entity.status),
            totalAmount = entity.totalAmount,
            paidAmount = entity.paidAmount,
            invoiceDateMs = entity.invoiceDateMs,
            dueDateMs = entity.dueDateMs,
            paidAtMs = entity.paidAtMs,
            createdAtMs = entity.createdAtMs,
            modifiedAtMs = entity.modifiedAtMs
        )
    }
    
    fun domainToEntity(domain: Invoice): InvoiceEntity {
        return InvoiceEntity(
            id = domain.id,
            customerId = domain.customerId,
            invoiceNumber = domain.invoiceNumber,
            status = domain.status.value,
            totalAmount = domain.totalAmount,
            paidAmount = domain.paidAmount,
            invoiceDateMs = domain.invoiceDateMs,
            dueDateMs = domain.dueDateMs,
            paidAtMs = domain.paidAtMs,
            createdAtMs = domain.createdAtMs,
            modifiedAtMs = domain.modifiedAtMs
        )
    }
    
    // Batch operations
    fun entitiesToDomain(entities: List<InvoiceEntity>): List<Invoice> {
        return entities.map { entityToDomain(it) }
    }
    
    fun domainToEntities(models: List<Invoice>): List<InvoiceEntity> {
        return models.map { domainToEntity(it) }
    }
}
```

Create similar mappers for:
- CustomerMapper
- PaymentMapper
- SnapshotMapper
- etc.

---

### STEP 3: Clean Domain Models (20 min)

**Goal**: Remove ALL @Entity, @Dao, @Database, @Query annotations

**Domain Model - Before**:
```kotlin
@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey
    val id: Long,
    @ForeignKey(entity = CustomerEntity::class)
    val customerId: Long,
    @ColumnInfo(name = "invoice_number")
    val invoiceNumber: String,
    // ...
)
```

**Domain Model - After**:
```kotlin
data class Invoice(
    val id: Long,
    val customerId: Long,
    val invoiceNumber: String,
    val status: Status,
    val totalAmount: BigDecimal,
    val paidAmount: BigDecimal = BigDecimal.ZERO,
    val invoiceDateMs: Long,
    val dueDateMs: Long,
    val paidAtMs: Long? = null,
    val createdAtMs: Long,
    val modifiedAtMs: Long
) {
    enum class Status(val value: String) {
        DRAFT("DRAFT"),
        SENT("SENT"),
        PARTIALLY_PAID("PARTIALLY_PAID"),
        PAID("PAID"),
        OVERDUE("OVERDUE"),
        CANCELLED("CANCELLED");
        
        companion object {
            fun fromString(value: String): Status {
                return Status.values().find { it.value == value } 
                    ?: throw IllegalArgumentException("Unknown status: $value")
            }
        }
    }
}
```

**Changes**:
- ✅ Remove `@Entity`
- ✅ Remove `@PrimaryKey`
- ✅ Remove `@ForeignKey`
- ✅ Remove `@ColumnInfo`
- ✅ Remove `@Embedded`
- ✅ Keep only business logic and structure

---

### STEP 4: Update DAOs to Use Mappers (20 min)

**Goal**: DAOs return domain models (via mappers), not entities

**DAO - Before** (data layer leaks into domain):
```kotlin
@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoice(id: Long): Invoice  // ❌ Returns domain model
}
```

**DAO - After** (clean separation):
```kotlin
@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceEntity(id: Long): InvoiceEntity  // ✅ Returns entity
}
```

**Update repository to handle mapping**:
```kotlin
class InvoiceRepositoryImpl(
    private val invoiceDao: InvoiceDao
) : InvoiceRepository {
    
    override suspend fun getInvoice(id: Long): Result<Invoice> = runCatching {
        val entity = invoiceDao.getInvoiceEntity(id)
        InvoiceMapper.entityToDomain(entity)
    }
    
    override suspend fun saveInvoice(invoice: Invoice): Result<Unit> = runCatching {
        val entity = InvoiceMapper.domainToEntity(invoice)
        invoiceDao.insertInvoice(entity)
    }
}
```

---

### STEP 5: Update build.gradle Dependencies (10 min)

**domain/build.gradle.kts - Before**:
```gradle
dependencies {
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.paging.common)
    // ❌ Framework dependencies in domain
}
```

**domain/build.gradle.kts - After**:
```gradle
dependencies {
    // Only Kotlin stdlib, no framework deps
    implementation(kotlin("stdlib"))
    
    // Test dependencies
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
```

**data/build.gradle.kts - verify includes**:
```gradle
dependencies {
    // ✅ Data layer depends on domain
    implementation(project(":domain"))
    
    // ✅ Framework dependencies here
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.paging.common)
    
    // ...other dependencies
}
```

---

### STEP 6: Compile & Test (15 min)

```bash
# Clean build to catch issues
./gradlew clean build

# If errors, identify unresolved references
./gradlew compileDebugKotlin 2>&1 | grep -i "unresolved\|error" | head -20

# Run tests to verify no regressions
./gradlew test

# Build release APK to verify full integration
./gradlew assembleRelease
```

**Expected Outcome**:
- ✅ Build succeeds with 0 errors
- ✅ All tests pass
- ✅ APKs build successfully

---

### STEP 7: Verify Architecture Dependency Inversion (5 min)

```bash
# Verify domain has no Room imports
echo "Checking domain for Room imports..."
grep -r "androidx.room" domain/src --include="*.kt" && echo "❌ FAILED" || echo "✅ PASSED"

# Verify domain has no Paging imports  
echo "Checking domain for Paging imports..."
grep -r "androidx.paging" domain/src --include="*.kt" && echo "❌ FAILED" || echo "✅ PASSED"

# Verify data layer imports domain
echo "Checking data layer imports domain..."
grep -r "com.emul8r.bizap.domain" data/src --include="*.kt" | wc -l
# Should show: many matches (expected)

# Verify data has Room annotations
echo "Checking data has @Entity, @Dao..."
grep -r "@Entity\|@Dao" data/src --include="*.kt" | wc -l
# Should show: many matches (expected)
```

---

## Commit Message Template

```
refactor: clean domain architecture - remove Room/Paging framework dependencies

BREAKING CHANGE: Domain models no longer use @Entity annotations

This is a pure refactoring with no behavior changes:
- Moved @Entity models to data/local/entities/ (InvoiceEntity, CustomerEntity, etc.)
- Created mappers (InvoiceMapper, CustomerMapper, etc.) for Entity ↔ Domain conversion
- Cleaned domain models to be framework-agnostic plain Kotlin data classes
- Updated DAOs to return entities instead of domain models
- Updated repositories to use mappers for conversion
- Removed androidx.room and androidx.paging from domain/build.gradle.kts

VERIFICATION:
- ✅ Clean build succeeds (./gradlew clean build)
- ✅ All unit tests pass (./gradlew test)
- ✅ Release APK builds (./gradlew assembleRelease)
- ✅ No Room/Paging imports remain in domain/
- ✅ Dependency inversion verified (Data → Domain, not Domain → Data)

ARCHITECTURE IMPROVEMENT:
- Domain layer is now framework-independent
- Easier to unit test domain models without Room
- Follows Clean Architecture principles
- Enables easier library version upgrades in future

Fixes architectural debt from PR #141
```

---

## Pre-Implementation Checklist

Before starting work:

- [ ] Read this entire plan
- [ ] Understand current domain model structure
- [ ] Identify all @Entity models in domain
- [ ] Create backup branch: `git branch backup/pre-domain-refactor`
- [ ] Create feature branch: `git checkout -b refactor/clean-domain-architecture`
- [ ] Ensure current build is green (v1.0.3-stable-build-20260320)

---

## Post-Implementation Checklist

After completing all steps:

- [ ] All domain models are plain Kotlin (no framework annotations)
- [ ] All entity models are in data/local/entities/
- [ ] All mappers are in data/local/mappers/
- [ ] domain/build.gradle.kts has no Room/Paging dependencies
- [ ] Clean build succeeds: `./gradlew clean build`
- [ ] All tests pass: `./gradlew test`
- [ ] Release APK builds: `./gradlew assembleRelease`
- [ ] No regression in test coverage
- [ ] PR is ready for review

---

## Rollback Plan

If something goes wrong:

```bash
# Abort current work
git reset --hard HEAD

# Go back to stable branch
git checkout -b rollback/domain-refactor-revert backup/pre-domain-refactor

# Or revert to stable tag
git checkout v1.0.3-stable-build-20260320
git reset --hard v1.0.3-stable-build-20260320

# Build to verify
./gradlew clean build
```

---

## Expected Outcomes

### Before This PR
```
domain/build.gradle.kts:
  implementation(androidx.room:room-common)
  implementation(androidx.paging:paging-common)
  
domain/models/Invoice.kt:
  @Entity(...)
  @PrimaryKey
  data class Invoice(...)
```

### After This PR
```
domain/build.gradle.kts:
  implementation(kotlin("stdlib"))
  
domain/models/Invoice.kt:
  data class Invoice(...)  // No annotations!
  
data/local/entities/InvoiceEntity.kt:
  @Entity(...)
  data class InvoiceEntity(...)
  
data/local/mappers/InvoiceMapper.kt:
  object InvoiceMapper {
      fun entityToDomain(entity: InvoiceEntity): Invoice
      fun domainToEntity(domain: Invoice): InvoiceEntity
  }
```

---

## Questions & Troubleshooting

**Q: What if domain models are used in multiple places?**  
A: Mappers handle the conversion. Update import statements where domain models are returned from repositories.

**Q: What about DAOs that return domain models?**  
A: Update them to return entities. Repositories will use mappers to convert back to domain.

**Q: How do I handle nested models?**  
A: Create nested entity classes and nested mappers. See InvoiceMapper pattern.

**Q: Will this slow down the app?**  
A: No. Mapping is instantaneous (microseconds). No performance impact.

**Q: What about migrations?**  
A: No database schema changes needed. Only code reorganization.

---

## Timeline for This PR

| Step | Time |
|------|------|
| Create entity models | 15 min |
| Create mappers | 20 min |
| Clean domain models | 20 min |
| Update DAOs | 20 min |
| Update dependencies | 10 min |
| Test & verify | 15 min |
| **Total** | **100 min (1.5-2h)** |

---

**Ready to proceed?** 

Once approved, start with:
```bash
git checkout -b refactor/clean-domain-architecture
# ... implement steps above ...
git push -u origin refactor/clean-domain-architecture
# Create PR on GitHub
```

---

**Generated**: March 20, 2026  
**Phase**: 1 / Quick Wins  
**Issue**: #1 / 10  
**Status**: Ready for implementation

