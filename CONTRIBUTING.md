# Contributing to Bizap

Thank you for your interest in contributing to Bizap! This document outlines the process and guidelines for contributing.

## Code of Conduct

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md).

---

## Setting Up Your Local Development Environment

See [SETUP.md](SETUP.md) for detailed setup instructions.

---

## Pull Request Process

1. **Fork** the repository and create your branch from `main`.
2. **Name your branch** following the conventions below.
3. **Write tests** for any new functionality or bug fixes.
4. **Ensure all tests pass** before submitting your PR.
5. **Update documentation** if you are changing public APIs or behaviors.
6. **Submit your PR** with a clear description of the changes.
7. **Request a review** from at least one maintainer.

---

## Branch Naming Conventions

Use the following prefixes for your branches:

| Prefix | Purpose | Example |
|--------|---------|---------|
| `feature/` | New features | `feature/add-recurring-invoices` |
| `bugfix/` | Bug fixes | `bugfix/fix-customer-email-validation` |
| `hotfix/` | Critical production fixes | `hotfix/fix-crash-on-startup` |
| `chore/` | Maintenance and tooling | `chore/update-dependencies` |
| `docs/` | Documentation only | `docs/update-setup-guide` |

---

## Commit Message Format

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

### Types

| Type | Description |
|------|-------------|
| `feat` | A new feature |
| `fix` | A bug fix |
| `docs` | Documentation only changes |
| `style` | Code style changes (formatting, no logic change) |
| `refactor` | Code change that neither fixes a bug nor adds a feature |
| `test` | Adding or updating tests |
| `chore` | Maintenance tasks (build scripts, dependencies) |
| `perf` | A code change that improves performance |

### Examples

```
feat(invoices): add recurring invoice support
fix(validation): correct email regex to support subdomains
docs(setup): add Firebase configuration steps
test(customers): add unit tests for CustomerViewModel
```

---

## Code Style Guidelines

This project follows [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).

### Key Points

- **Indentation**: 4 spaces (no tabs)
- **Line length**: Maximum 120 characters
- **Naming**:
  - Classes and objects: `PascalCase`
  - Functions and properties: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
- **Coroutines**: Use `viewModelScope.launch` in ViewModels; avoid blocking calls
- **State management**: Use `StateFlow` / `MutableStateFlow` for UI state
- **Dependency injection**: Use Hilt annotations (`@HiltViewModel`, `@Inject`)
- **Null safety**: Prefer non-null types; use `?` only when null is meaningful

### Architecture Rules

- **UI layer** (`ui/`): Compose screens and ViewModels only — no business logic
- **Domain layer** (`domain/`): Business logic, validation, and repository interfaces
- **Data layer** (`data/`): Room entities, DAOs, repository implementations
- Cross-layer dependencies must flow: UI → Domain → Data (never upward)

---

## Testing Requirements Before Submitting a PR

All pull requests must:

1. **Pass all existing unit tests**:
   ```bash
   cd Bizap && ./gradlew testDebugUnitTest
   ```

2. **Maintain or improve test coverage** for modified code paths.

3. **Add tests** for any new public functions, ViewModels, use cases, or validation rules.

4. **Run lint** with no new errors:
   ```bash
   ./gradlew lint
   ```

### Test Locations

| Test Type | Location |
|-----------|----------|
| Unit tests | `app/src/test/` |
| Instrumented / E2E tests | `app/src/androidTest/` |

---

## Reporting Issues

- Use [GitHub Issues](../../issues) to report bugs or request features.
- Search existing issues before opening a new one.
- Include steps to reproduce, expected vs. actual behavior, and device/OS version.

---

## Questions?

Open a [GitHub Discussion](../../discussions) or reach out via the contact process described in the [Code of Conduct](CODE_OF_CONDUCT.md).
