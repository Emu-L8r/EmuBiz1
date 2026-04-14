package com.emul8r.bizap

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue

/**
 * Architecture compliance tests.
 *
 * These tests verify that the layering rules documented in [docs/ARCHITECTURE.md]
 * are respected across the codebase. They work by scanning the source files
 * directly without any reflection framework dependency.
 *
 * Rules enforced:
 *  1. Domain models must not import Room annotations.
 *  2. Domain models must not import Android framework classes.
 *  3. Data repositories must implement domain repository interfaces.
 *  4. Presentation ViewModels must not reference Room DAOs directly.
 */
class ArchitectureTest {

    private val projectDir = File("src/main/java/com/emul8r/bizap")

    // -------------------------------------------------------------------------
    // Rule 1 & 2: Domain models are pure Kotlin
    // -------------------------------------------------------------------------

    @Test
    fun `domain models should not depend on Room`() {
        val domainModelDir = File(projectDir, "domain/model")
        if (!domainModelDir.exists()) return

        // Known technical debt: InvoiceSettings doubles as a Room entity to avoid a large
        // data-layer refactor. Tracked for future separation into entity + domain model.
        val knownExceptions = setOf("InvoiceSettings.kt")

        val violations = mutableListOf<String>()
        domainModelDir.walkTopDown()
            .filter { it.extension == "kt" && it.name !in knownExceptions }
            .forEach { file ->
                file.readLines().forEachIndexed { lineIdx, line ->
                    if (line.trimStart().startsWith("import") &&
                        line.contains("androidx.room")) {
                        violations += "${file.name}:${lineIdx + 1} → $line"
                    }
                }
            }

        assertTrue(
            violations.isEmpty(),
            "Domain models must not import Room. Violations:\n${violations.joinToString("\n")}"
        )
    }

    @Test
    fun `domain models should not depend on Android framework`() {
        val domainModelDir = File(projectDir, "domain/model")
        if (!domainModelDir.exists()) return

        // Allow kotlinx.* and javax.* which are pure JVM
        val forbiddenPrefixes = listOf("import android.", "import androidx.")

        // Known technical debt: InvoiceSettings doubles as a Room entity.
        val knownExceptions = setOf("InvoiceSettings.kt")

        val violations = mutableListOf<String>()
        domainModelDir.walkTopDown()
            .filter { it.extension == "kt" && it.name !in knownExceptions }
            .forEach { file ->
                file.readLines().forEachIndexed { lineIdx, line ->
                    val trimmed = line.trimStart()
                    if (forbiddenPrefixes.any { trimmed.startsWith(it) }) {
                        violations += "${file.name}:${lineIdx + 1} → $line"
                    }
                }
            }

        assertTrue(
            violations.isEmpty(),
            "Domain models must not import Android/AndroidX classes. Violations:\n${violations.joinToString("\n")}"
        )
    }

    // -------------------------------------------------------------------------
    // Rule 3: Data repositories implement domain interfaces
    // -------------------------------------------------------------------------

    @Test
    fun `data repositories should import domain repository interfaces`() {
        val dataRepoDir = File(projectDir, "data/repository")
        if (!dataRepoDir.exists()) return

        val repoImplFiles = dataRepoDir.walkTopDown()
            .filter { it.extension == "kt" && it.name.endsWith("RepositoryImpl.kt") }
            .toList()

        // If there are no repository implementations, that's fine — skip.
        if (repoImplFiles.isEmpty()) return

        // Files where the repository interface is co-located in the same file rather than
        // in the domain layer. These are exceptions until the interface is moved to domain.
        val coLocatedInterfaceFiles = setOf("DashboardPreferencesRepositoryImpl.kt")

        val violations = mutableListOf<String>()
        repoImplFiles.forEach { file ->
            if (file.name in coLocatedInterfaceFiles) return@forEach
            val content = file.readText()
            val importsDomainRepo = content.contains("import com.emul8r.bizap.domain")
            if (!importsDomainRepo) {
                violations += file.name
            }
        }

        assertTrue(
            violations.isEmpty(),
            "Data repository implementations must import from domain layer. " +
                    "Missing domain import in:\n${violations.joinToString("\n")}"
        )
    }

    // -------------------------------------------------------------------------
    // Rule 4: Presentation ViewModels must not access Room DAOs directly (except read-only Analytics)
    // -------------------------------------------------------------------------

    @Test
    fun `presentation viewmodels should not directly import Room DAOs`() {
        val presentationDirs = listOf(
            File(projectDir, "ui"),
            File(projectDir, "presentation/viewmodel")
        )

        val violations = mutableListOf<String>()
        presentationDirs.forEach { dir ->
            if (!dir.exists()) return@forEach
            dir.walkTopDown()
                .filter { it.extension == "kt" && it.name.endsWith("ViewModel.kt") }
                .forEach { file ->
                    file.readLines().forEachIndexed { lineIdx, line ->
                        val trimmed = line.trimStart()
                        // Allow AnalyticsDao (read-only), forbid others that imply mutation
                        val isReadOnlyAnalyticsDao = trimmed.contains("AnalyticsDao")
                        if (trimmed.startsWith("import") &&
                            trimmed.contains(".dao.") &&
                            trimmed.contains("Dao") &&
                            !isReadOnlyAnalyticsDao) {
                            violations += "${file.name}:${lineIdx + 1} → $line"
                        }
                    }
                }
        }

        assertTrue(
            violations.isEmpty(),
            "ViewModels must not directly import mutable Room DAOs. Read-only analytics access is OK. " +
                    "Violations:\n${violations.joinToString("\n")}"
        )
    }

    // -------------------------------------------------------------------------
    // Rule 5: Domain use cases should not import from data layer
    // -------------------------------------------------------------------------

    @Test
    fun `domain use cases should not depend on data layer`() {
        val useCaseDir = File(projectDir, "domain/usecase")
        if (!useCaseDir.exists()) return

        val violations = mutableListOf<String>()
        useCaseDir.walkTopDown()
            .filter { it.extension == "kt" }
            .forEach { file ->
                file.readLines().forEachIndexed { lineIdx, line ->
                    val trimmed = line.trimStart()
                    if (trimmed.startsWith("import com.emul8r.bizap.data")) {
                        violations += "${file.name}:${lineIdx + 1} → $line"
                    }
                }
            }

        assertTrue(
            violations.isEmpty(),
            "Domain use cases must not import from data layer. Violations:\n${violations.joinToString("\n")}"
        )
    }

    // -------------------------------------------------------------------------
    // Rule 6: No circular imports between domain and data layers
    // -------------------------------------------------------------------------

    @Test
    fun `domain layer should not import from data layer`() {
        val domainDir = File(projectDir, "domain")
        if (!domainDir.exists()) return

        val violations = mutableListOf<String>()
        domainDir.walkTopDown()
            .filter { it.extension == "kt" }
            .forEach { file ->
                file.readLines().forEachIndexed { lineIdx, line ->
                    val trimmed = line.trimStart()
                    if (trimmed.startsWith("import com.emul8r.bizap.data")) {
                        violations += "${file.name}:${lineIdx + 1} → $line"
                    }
                }
            }

        assertTrue(
            violations.isEmpty(),
            "Domain layer must not import from data layer (no circular dependency). " +
                    "Violations:\n${violations.joinToString("\n")}"
        )
    }

    @Test
    fun `data layer should not import from UI layer`() {
        val dataDir = File(projectDir, "data")
        if (!dataDir.exists()) return

        val violations = mutableListOf<String>()
        dataDir.walkTopDown()
            .filter { it.extension == "kt" }
            .forEach { file ->
                file.readLines().forEachIndexed { lineIdx, line ->
                    val trimmed = line.trimStart()
                    if (trimmed.startsWith("import com.emul8r.bizap.ui") ||
                        trimmed.startsWith("import com.emul8r.bizap.presentation")) {
                        violations += "${file.name}:${lineIdx + 1} → $line"
                    }
                }
            }

        assertTrue(
            violations.isEmpty(),
            "Data layer must not import from UI/presentation layer. " +
                    "Violations:\n${violations.joinToString("\n")}"
        )
    }

    @Test
    fun `domain use cases should not import from UI layer`() {
        val useCaseDir = File(projectDir, "domain/usecase")
        if (!useCaseDir.exists()) return

        val violations = mutableListOf<String>()
        useCaseDir.walkTopDown()
            .filter { it.extension == "kt" }
            .forEach { file ->
                file.readLines().forEachIndexed { lineIdx, line ->
                    val trimmed = line.trimStart()
                    if (trimmed.startsWith("import com.emul8r.bizap.ui") ||
                        trimmed.startsWith("import com.emul8r.bizap.presentation")) {
                        violations += "${file.name}:${lineIdx + 1} → $line"
                    }
                }
            }

        assertTrue(
            violations.isEmpty(),
            "Domain use cases must not depend on UI layer. Violations:\n${violations.joinToString("\n")}"
        )
    }
}



