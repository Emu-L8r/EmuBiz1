# Standalone Notes Module Implementation - 3 Methods

**Project:** Bizap - Business Management & Invoicing  
**Date:** February 27, 2026  
**Current Architecture:** Clean Architecture + MVVM + Hilt DI + Room + Jetpack Compose  

---

## Overview

Based on your project's architecture, here are **3 methods** to implement a standalone notes module, ranked from simplest to most feature-rich.

---

# METHOD 1: Simple Notes (Quick-Add Notes)

**Complexity:** ⭐ Low  
**Implementation Time:** 2-3 hours  
**Best For:** Quick text notes without relationships  

## Architecture

```
┌─────────────────────────────────────┐
│         Presentation Layer          │
│  NotesScreen.kt                     │
│  NotesViewModel.kt                  │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│          Domain Layer               │
│  Note.kt (data class)               │
│  NoteRepository.kt (interface)      │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│           Data Layer                │
│  NoteEntity.kt                      │
│  NoteDao.kt                         │
│  NoteRepositoryImpl.kt              │
└─────────────────────────────────────┘
```

## Database Schema

```kotlin
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val color: String? = null, // Optional: for colored notes
    val isPinned: Boolean = false
)
```

## Features

✅ Create, read, update, delete notes  
✅ Search notes by title/content  
✅ Sort by date (newest/oldest)  
✅ Pin important notes to top  
✅ Optional color coding  

## UI Flow

```
Bottom Nav: Dashboard | Customers | Invoices | Vault | Settings
                                      ↓
                              Add: "Notes" tab
                                      ↓
                              Notes List Screen
                                      ↓
                    [+ FAB] → Create Note Screen
                                      ↓
                    [Tap Note] → Note Detail Screen
```

## Files to Create

```
app/src/main/java/com/emul8r/bizap/
├── data/local/entities/
│   └── NoteEntity.kt
├── data/local/
│   └── NoteDao.kt
├── data/repository/
│   └── NoteRepositoryImpl.kt
├── domain/model/
│   └── Note.kt
├── domain/repository/
│   └── NoteRepository.kt
├── ui/notes/
│   ├── NotesScreen.kt
│   ├── NotesViewModel.kt
│   ├── CreateNoteScreen.kt
│   └── NoteDetailScreen.kt
└── navigation/
    └── Screen.kt (add Notes routes)
```

## Database Migration

```kotlin
// AppDatabase.kt
@Database(
    entities = [..., NoteEntity::class],
    version = 3, // Increment from 2 to 3
    exportSchema = false
)
```

## Pros & Cons

**✅ Pros:**
- Simple, clean implementation
- No complex relationships
- Fast to implement
- Easy to test

**❌ Cons:**
- Notes are isolated (not linked to customers/invoices)
- No rich text formatting
- No attachments
- No categories/tags

---

# METHOD 2: Contextual Notes (Notes with Relationships)

**Complexity:** ⭐⭐ Medium  
**Implementation Time:** 4-6 hours  
**Best For:** Notes linked to customers, invoices, or projects  

## Architecture

```
┌──────────────────────────────────────────┐
│          Presentation Layer              │
│  NotesScreen.kt                          │
│  NotesViewModel.kt                       │
│  CustomerNotesScreen.kt (embedded)       │
│  InvoiceNotesScreen.kt (embedded)        │
└──────────────┬───────────────────────────┘
               │
┌──────────────▼───────────────────────────┐
│           Domain Layer                   │
│  Note.kt (with relationships)            │
│  NoteRepository.kt                       │
│  NotesWithContext.kt (aggregate)         │
└──────────────┬───────────────────────────┘
               │
┌──────────────▼───────────────────────────┐
│            Data Layer                    │
│  NoteEntity.kt (with foreign keys)       │
│  NoteDao.kt (with joins)                 │
│  NoteRepositoryImpl.kt                   │
└──────────────────────────────────────────┘
```

## Database Schema

```kotlin
@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["relatedCustomerId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["relatedInvoiceId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("relatedCustomerId"),
        Index("relatedInvoiceId"),
        Index("category")
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: NoteCategory, // GENERAL, CUSTOMER, INVOICE, TASK
    val relatedCustomerId: Long? = null,
    val relatedInvoiceId: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean = false,
    val color: String? = null,
    val tags: String? = null // Comma-separated: "urgent,followup"
)

enum class NoteCategory {
    GENERAL,
    CUSTOMER,
    INVOICE,
    TASK,
    MEETING
}
```

## Features

✅ All Method 1 features PLUS:
✅ Link notes to customers  
✅ Link notes to invoices  
✅ View customer notes on Customer Detail screen  
✅ View invoice notes on Invoice Detail screen  
✅ Filter notes by category  
✅ Tag-based organization  
✅ Standalone notes tab for global view  

## UI Integration Points

### 1. Customer Detail Screen
```
Customer Detail
├── Contact Info
├── Invoices List
└── [NEW] Notes Section (shows customer-specific notes)
    └── [+ Add Note] button
```

### 2. Invoice Detail Screen
```
Invoice Detail
├── Line Items
├── Export PDF
└── [NEW] Notes Section (shows invoice-specific notes)
    └── [+ Add Note] button
```

### 3. Standalone Notes Screen
```
Notes Tab (Bottom Nav)
├── All Notes (grouped by category)
├── Filter: General | Customer | Invoice | Task
└── [+ FAB] → Create Note (with optional linking)
```

## DAO Queries

```kotlin
@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE relatedCustomerId = :customerId")
    fun getNotesByCustomer(customerId: Long): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE relatedInvoiceId = :invoiceId")
    fun getNotesByInvoice(invoiceId: Long): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE category = :category")
    fun getNotesByCategory(category: NoteCategory): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE title LIKE :query OR content LIKE :query")
    fun searchNotes(query: String): Flow<List<NoteEntity>>
    
    @Insert
    suspend fun insertNote(note: NoteEntity): Long
    
    @Update
    suspend fun updateNote(note: NoteEntity)
    
    @Delete
    suspend fun deleteNote(note: NoteEntity)
}
```

## Navigation Changes

```kotlin
// Screen.kt
sealed interface Screen {
    // ...existing screens...
    
    @Serializable
    object Notes : Screen
    
    @Serializable
    object CreateNote : Screen
    
    @Serializable
    data class CreateNoteWithContext(
        val customerId: Long? = null,
        val invoiceId: Long? = null
    ) : Screen
    
    @Serializable
    data class NoteDetail(val noteId: Long) : Screen
}
```

## Database Migration

```kotlin
@Database(
    entities = [..., NoteEntity::class],
    version = 3,
    exportSchema = false
)
```

## Pros & Cons

**✅ Pros:**
- Notes have context (linked to business entities)
- Better organization with categories
- Embedded notes in existing screens
- Flexible querying (by customer, invoice, category)
- Supports both linked and standalone notes

**❌ Cons:**
- More complex data model
- More UI components to build
- Requires careful foreign key management
- Slightly longer implementation time

---

# METHOD 3: Advanced Notes System (Full-Featured)

**Complexity:** ⭐⭐⭐ High  
**Implementation Time:** 8-12 hours  
**Best For:** Power users needing rich notes with collaboration potential  

## Architecture

```
┌───────────────────────────────────────────────┐
│           Presentation Layer                  │
│  NotesScreen.kt (with tabs)                   │
│  NotesViewModel.kt                            │
│  RichTextEditor.kt                            │
│  NoteAttachmentsScreen.kt                     │
│  NoteCategoriesScreen.kt                      │
│  NoteReminderScreen.kt                        │
└──────────────┬────────────────────────────────┘
               │
┌──────────────▼────────────────────────────────┐
│            Domain Layer                       │
│  Note.kt                                      │
│  NoteAttachment.kt                            │
│  NoteCategory.kt                              │
│  NoteReminder.kt                              │
│  NoteRepository.kt                            │
│  AttachmentRepository.kt                      │
└──────────────┬────────────────────────────────┘
               │
┌──────────────▼────────────────────────────────┐
│             Data Layer                        │
│  NoteEntity.kt                                │
│  NoteAttachmentEntity.kt                      │
│  NoteCategoryEntity.kt                        │
│  NoteReminderEntity.kt                        │
│  + DAOs + Repositories                        │
└───────────────────────────────────────────────┘
```

## Database Schema

### Notes Table
```kotlin
@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["relatedCustomerId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["relatedInvoiceId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = NoteCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("relatedCustomerId"),
        Index("relatedInvoiceId"),
        Index("categoryId")
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    val title: String,
    val content: String, // Can store markdown or HTML
    val contentType: ContentType = ContentType.PLAIN_TEXT,
    val categoryId: Long? = null,
    val relatedCustomerId: Long? = null,
    val relatedInvoiceId: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val color: String? = null,
    val tags: String? = null,
    val priority: NotePriority = NotePriority.NORMAL
)

enum class ContentType {
    PLAIN_TEXT,
    MARKDOWN,
    RICH_TEXT
}

enum class NotePriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}
```

### Note Attachments Table
```kotlin
@Entity(
    tableName = "note_attachments",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("noteId")]
)
data class NoteAttachmentEntity(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    val noteId: Long,
    val fileName: String,
    val filePath: String,
    val fileType: String, // image, pdf, document
    val fileSize: Long,
    val uploadedAt: Long
)
```

### Note Categories Table
```kotlin
@Entity(tableName = "note_categories")
data class NoteCategoryEntity(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    val name: String,
    val color: String,
    val icon: String? = null,
    val sortOrder: Int = 0
)
```

### Note Reminders Table
```kotlin
@Entity(
    tableName = "note_reminders",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("noteId")]
)
data class NoteReminderEntity(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    val noteId: Long,
    val reminderTime: Long,
    val isCompleted: Boolean = false,
    val notificationEnabled: Boolean = true
)
```

## Features

✅ All Method 2 features PLUS:
✅ **Rich text editing** (markdown or WYSIWYG)  
✅ **File attachments** (images, PDFs, documents)  
✅ **Custom categories** (user-defined)  
✅ **Reminders/Tasks** with notifications  
✅ **Priority levels** (Low, Normal, High, Urgent)  
✅ **Archive notes** (hide without deleting)  
✅ **Full-text search** with highlighting  
✅ **Export notes** to PDF or Markdown  
✅ **Note templates** for common scenarios  

## UI Components

### 1. Notes Dashboard (Main Screen)
```
Tabs: All | Pinned | Categories | Archived

[Search Bar]

Filters: Priority | Category | Date Range

┌─────────────────────────────────┐
│ 🔴 URGENT: Follow up with John  │
│ Customer: John Doe              │
│ 📎 2 attachments | 🔔 Reminder  │
└─────────────────────────────────┘

┌─────────────────────────────────┐
│ 📌 Meeting notes - Q1 review    │
│ Category: Meetings              │
│ Last edited: 2 hours ago        │
└─────────────────────────────────┘

[+ FAB] Create Note
```

### 2. Rich Text Editor
```
┌─────────────────────────────────┐
│ Title: _____________________    │
│                                 │
│ [B] [I] [U] [List] [Link]      │ ← Formatting toolbar
│                                 │
│ Content:                        │
│ ┌─────────────────────────────┐ │
│ │ Type here...                │ │
│ │                             │ │
│ └─────────────────────────────┘ │
│                                 │
│ 📎 Attachments: [Add File]     │
│ 🏷️ Category: [Select]          │
│ 🔔 Reminder: [Set]             │
│ 🎨 Color: [Pick]               │
│                                 │
│ [Cancel] [Save]                │
└─────────────────────────────────┘
```

### 3. Category Management Screen
```
Settings → Note Categories

┌─────────────────────────────────┐
│ 🔵 General (12 notes)           │
│ [Edit] [Delete]                 │
└─────────────────────────────────┘

┌─────────────────────────────────┐
│ 🟢 Meetings (5 notes)           │
│ [Edit] [Delete]                 │
└─────────────────────────────────┘

[+ Create Category]
```

## Advanced DAOs

```kotlin
@Dao
interface NoteDao {
    // Complex queries with joins
    @Transaction
    @Query("""
        SELECT * FROM notes 
        LEFT JOIN note_attachments ON notes.id = note_attachments.noteId
        LEFT JOIN note_reminders ON notes.id = note_reminders.noteId
        WHERE notes.isArchived = 0
        ORDER BY notes.isPinned DESC, notes.priority DESC, notes.updatedAt DESC
    """)
    fun getNotesWithDetails(): Flow<List<NoteWithDetails>>
    
    // Full-text search
    @Query("""
        SELECT * FROM notes 
        WHERE title LIKE '%' || :query || '%' 
        OR content LIKE '%' || :query || '%'
        OR tags LIKE '%' || :query || '%'
        ORDER BY 
            CASE WHEN title LIKE :query || '%' THEN 1 ELSE 2 END,
            updatedAt DESC
    """)
    fun searchNotesWithRanking(query: String): Flow<List<NoteEntity>>
    
    // Statistics
    @Query("SELECT COUNT(*) FROM notes WHERE relatedCustomerId = :customerId")
    suspend fun getNoteCountByCustomer(customerId: Long): Int
    
    @Query("SELECT category, COUNT(*) as count FROM notes GROUP BY category")
    fun getNoteStatsByCategory(): Flow<Map<String, Int>>
}

@Dao
interface NoteAttachmentDao {
    @Query("SELECT * FROM note_attachments WHERE noteId = :noteId")
    fun getAttachmentsByNote(noteId: Long): Flow<List<NoteAttachmentEntity>>
    
    @Insert
    suspend fun insertAttachment(attachment: NoteAttachmentEntity): Long
    
    @Delete
    suspend fun deleteAttachment(attachment: NoteAttachmentEntity)
}

@Dao
interface NoteCategoryDao {
    @Query("SELECT * FROM note_categories ORDER BY sortOrder ASC")
    fun getAllCategories(): Flow<List<NoteCategoryEntity>>
    
    @Insert
    suspend fun insertCategory(category: NoteCategoryEntity): Long
    
    @Update
    suspend fun updateCategory(category: NoteCategoryEntity)
    
    @Delete
    suspend fun deleteCategory(category: NoteCategoryEntity)
}
```

## Data Classes for Aggregates

```kotlin
data class NoteWithDetails(
    @Embedded val note: NoteEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "noteId"
    )
    val attachments: List<NoteAttachmentEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "noteId"
    )
    val reminders: List<NoteReminderEntity>
)
```

## Background Services

### Reminder Notification Worker
```kotlin
@HiltWorker
class NoteReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val noteRepository: NoteRepository
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        // Check for upcoming reminders
        // Send notifications
        return Result.success()
    }
}
```

## Export Functionality

```kotlin
class NoteExportService @Inject constructor(
    private val context: Context,
    private val documentManager: DocumentManager
) {
    suspend fun exportNoteToPdf(noteId: Long): File {
        // Generate PDF from note content
    }
    
    suspend fun exportNoteToMarkdown(noteId: Long): File {
        // Export as .md file
    }
    
    suspend fun exportAllNotes(): File {
        // Create ZIP archive with all notes
    }
}
```

## Database Migration

```kotlin
@Database(
    entities = [
        CustomerEntity::class,
        InvoiceEntity::class,
        LineItemEntity::class,
        PrefilledItemEntity::class,
        GeneratedDocumentEntity::class,
        NoteEntity::class,
        NoteAttachmentEntity::class,
        NoteCategoryEntity::class,
        NoteReminderEntity::class
    ],
    version = 3,
    exportSchema = true // Important for complex migrations
)
@TypeConverters(DocumentStatusConverter::class, NoteConverters::class)
abstract class AppDatabase : RoomDatabase() {
    // ...existing DAOs...
    abstract fun noteDao(): NoteDao
    abstract fun noteAttachmentDao(): NoteAttachmentDao
    abstract fun noteCategoryDao(): NoteCategoryDao
    abstract fun noteReminderDao(): NoteReminderDao
}
```

## Pros & Cons

**✅ Pros:**
- Professional-grade notes system
- Rich feature set
- Extensible architecture
- Supports collaboration features (future)
- Export/import capabilities
- Notification system

**❌ Cons:**
- Significant implementation time
- Complex data model (4 tables)
- Requires WorkManager for reminders
- More testing required
- Heavier on device resources

---

# Comparison Matrix

| Feature | Method 1 | Method 2 | Method 3 |
|---------|----------|----------|----------|
| **Basic CRUD** | ✅ | ✅ | ✅ |
| **Search** | ✅ | ✅ | ✅ (Advanced) |
| **Pin Notes** | ✅ | ✅ | ✅ |
| **Color Coding** | ✅ | ✅ | ✅ |
| **Link to Customers** | ❌ | ✅ | ✅ |
| **Link to Invoices** | ❌ | ✅ | ✅ |
| **Categories** | ❌ | Basic | Custom |
| **Tags** | ❌ | ✅ | ✅ |
| **Rich Text** | ❌ | ❌ | ✅ |
| **Attachments** | ❌ | ❌ | ✅ |
| **Reminders** | ❌ | ❌ | ✅ |
| **Priority Levels** | ❌ | ❌ | ✅ |
| **Archive** | ❌ | ❌ | ✅ |
| **Export** | ❌ | ❌ | ✅ |
| **Implementation Time** | 2-3 hrs | 4-6 hrs | 8-12 hrs |
| **Complexity** | Low | Medium | High |
| **Database Tables** | 1 | 1 | 4 |
| **New Files** | ~8 | ~12 | ~20+ |

---

# Recommendation

Based on your current project state and the fact that you've been implementing features methodically:

## **I recommend METHOD 2: Contextual Notes**

### Why?

1. **Best balance** of features vs. complexity
2. **Practical value** - Notes linked to customers/invoices are highly useful
3. **Fits your architecture** - Follows same patterns as existing modules
4. **Reasonable timeline** - 4-6 hours is manageable
5. **Room for growth** - Can upgrade to Method 3 later if needed

### Implementation Order (if choosing Method 2):

**Phase 1: Core Foundation (2 hours)**
1. Create NoteEntity with foreign keys
2. Create NoteDao with basic queries
3. Create domain models and repository
4. Update AppDatabase to version 3

**Phase 2: UI Layer (2 hours)**
5. Create NotesScreen (standalone tab)
6. Create NotesViewModel
7. Add Notes route to navigation
8. Add Notes to bottom navigation

**Phase 3: Integration (1-2 hours)**
9. Add customer notes section to CustomerDetailScreen
10. Add invoice notes section to InvoiceDetailScreen
11. Implement note creation with context linking

**Phase 4: Polish (1 hour)**
12. Add search functionality
13. Add category filtering
14. Test all flows

---

# Next Steps

**Choose a method**, and I can immediately start implementing it for you. Just say:

- "Let's implement Method 1" (quick notes)
- "Let's implement Method 2" (contextual notes) ← **Recommended**
- "Let's implement Method 3" (advanced system)

Or ask questions about any of the methods before deciding!

---

**All three methods follow your existing Clean Architecture, use Hilt DI, Room database, and Jetpack Compose. They're production-ready and maintainable.**

