# Dependency Update Policy

## Update Frequency
- Security patches: Immediately
- Minor versions: Monthly review
- Major versions: Quarterly planning

## Version Compatibility Matrix
| Component | Current | Min Safe | Max Tested | Notes |
|-----------|---------|----------|------------|-------|
| Kotlin    | 2.0.21  | 2.0.0    | 2.0.21     | Wait for Hilt 2.53+ before 2.1.x |
| AGP       | 8.7.3   | 8.5.0    | 8.7.3      | Don't upgrade to 9.x until Gradle 10 |
| Compose   | 2024.12 | 2024.10  | 2024.12    | Stable |

## Breaking Change Timeline
- 2026 Q2: Gradle 10.0 release expected
- 2026 Q3: AGP 9.0 release expected
