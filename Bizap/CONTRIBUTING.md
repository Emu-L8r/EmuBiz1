# Contributing to Bizap

Thank you for your interest in contributing to Bizap! This guide will help you understand how to submit changes and where to add documentation.

---

## Table of Contents

1. [Getting Started](#getting-started)
2. [Code Changes](#code-changes)
3. [Adding Documentation](#adding-documentation)
4. [Pull Request Process](#pull-request-process)
5. [Code Standards](#code-standards)

---

## Getting Started

### Prerequisites

- Git
- JDK 17+
- Android Studio (latest)
- Gradle (included via wrapper)

### Setup

```bash
# Clone the repository
git clone https://github.com/Emu-L8r/EmuBiz1.git
cd Bizap

# Build the project
./gradlew clean build

# Run tests
./gradlew test
```

---

## Code Changes

### Before Starting

1. Check [GitHub Issues](https://github.com/Emu-L8r/EmuBiz1/issues) for open tasks
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Keep changes focused and atomic

### Code Standards

- **Language:** Kotlin
- **Style:** Follow [Kotlin Style Guide](https://kotlinlang.org/docs/coding-conventions.html)
- **Format:** IDE auto-formats on save
- **Tests:** Add tests for new functionality (minimum 80% coverage)

### Building & Testing

```bash
# Build
./gradlew clean build

# Run unit tests
./gradlew test

# Run all tests including integration
./gradlew build

# Check code quality
./gradlew detekt ktlintCheck
```

### Before Committing

```bash
# Verify everything works
./gradlew clean build test

# Expected: BUILD SUCCESSFUL with all tests passing
```

---

## Adding Documentation

### Where to Add New Docs

**1. Is it active, current guidance?**

→ Add to `/docs/active/GUIDES/` or `/docs/active/PRODUCTION_ROLLOUT/`

Example locations:
- Feature guides: `/docs/active/GUIDES/FEATURE_NAME.md`
- Release procedures: `/docs/active/PRODUCTION_ROLLOUT/RELEASE_PROCEDURE.md`

**2. Is it an architectural decision?**

→ Add to `/docs/decisions/`

Examples:
- `DECISION_LOG.md` — Record all decisions
- `ARCHITECTURE_DECISIONS.md` — Why we built it this way

**3. Is it an experiment or old phase?**

→ Do NOT add to root. Add to `/docs/archive/`

- `/docs/archive/experiments/EXPERIMENT_NAME.md`
- `/docs/archive/obsolete/OLD_FEATURE.md`

**4. Is it just a log or temporary work?**

→ Do NOT commit it at all

Use: `/.logs/` directory for temporary outputs

### Naming Conventions

#### Active Docs

Format: `FEATURE_DESCRIPTION.md` (descriptive, all caps for titles)

Examples:
- ✅ `BUILD_GUIDE.md`
- ✅ `SECURITY_POLICY.md`
- ✅ `API_DOCUMENTATION.md`
- ✅ `RELEASE_PROCEDURE.md`

#### Decision Docs

Format: `DECISION_ARCHITECTURE_FEATURE.md`

Examples:
- ✅ `DECISION_DUAL_GUI_STRATEGY.md`
- ✅ `ARCHITECTURE_ROOM_ENCRYPTION.md`

#### Archived Docs

Format: Same as active, but goes to `/docs/archive/phases/` or `/docs/archive/obsolete/`

Examples:
- ✅ Moves to: `/docs/archive/phases/phase_1_mvp/DASHBOARD_IMPLEMENTATION.md`
- ✅ Moves to: `/docs/archive/obsolete/OLD_FEATURE_GUIDE.md`

### Don'ts

❌ Do NOT create new `.md` files at repository root  
❌ Do NOT commit large PDF files or binaries  
❌ Do NOT include credentials or API keys  
❌ Do NOT reference files by absolute paths (use relative links)

---

## Pull Request Process

### 1. Create Your Feature Branch

```bash
git checkout -b feature/your-feature-name
```

### 2. Make Your Changes

- Keep commits small and focused
- Write clear commit messages
- Test frequently (`./gradlew build`)

### 3. Create a Pull Request

**Title Format:** `type(scope): description`

Examples:
- `feat(dashboard): Add revenue chart`
- `fix(payments): Fix date serialization issue`
- `docs(guides): Add deployment procedure`
- `refactor(navigation): Consolidate GUI adapters`

**Description Template:**

```markdown
## Description
Brief summary of what this PR does.

## Changes
- Specific change 1
- Specific change 2
- Specific change 3

## Related Issues
Fixes #123 or Related to #456

## Testing
How was this tested?
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing: [describe]

## Impact
- Zero breaking changes
- [or describe breaking changes]
```

### 4. Code Review

- Address reviewer comments promptly
- Push fixes as additional commits (don't rewrite history)
- Mark conversations as resolved after addressing them

### 5. Merge

- Squash commits before merge (keep history clean)
- Delete feature branch after merge

---

## Code Standards

### Kotlin

- Use meaningful variable names
- Prefer functional style (map, filter, etc.) over loops
- Use sealed classes for restricted hierarchies
- Add KDoc for public APIs

```kotlin
/**
 * Calculates the total invoice amount including tax.
 * 
 * @param subtotal Pre-tax amount
 * @param taxRate Tax percentage (0-100)
 * @return Total amount after tax
 */
fun calculateTotal(subtotal: Double, taxRate: Double): Double {
    return subtotal * (1 + taxRate / 100)
}
```

### Tests

- Minimum 80% code coverage for new code
- Test names describe what is being tested
- Use clear test organization (Arrange-Act-Assert)

```kotlin
@Test
fun testCalculateTotalWithTax() {
    // Arrange
    val subtotal = 100.0
    val taxRate = 10.0
    
    // Act
    val result = calculateTotal(subtotal, taxRate)
    
    // Assert
    assertEquals(110.0, result, 0.01)
}
```

### Documentation

- Keep README.md updated
- Update relevant guides when changing features
- Add comments for complex logic
- Include examples in documentation

---

## Questions?

**Where should I put this doc?**

Start at: `/docs/README.md` → It has a navigation guide

**How do I run just one test?**

```bash
./gradlew test -k "TestClassName"
```

**My code doesn't compile, what do I do?**

1. Check error message: `./gradlew clean build`
2. Search existing issues/PRs
3. Comment on related GitHub issue with error

**I think I found a bug, what should I do?**

1. Check [existing issues](https://github.com/Emu-L8r/EmuBiz1/issues)
2. Create a new issue with:
   - Clear title
   - Steps to reproduce
   - Expected vs actual behavior
   - Device/Android version
   - Relevant logs

---

## Thanks!

Thank you for contributing to Bizap! Your help makes this project better. 🙏

**Happy coding!** 🚀

