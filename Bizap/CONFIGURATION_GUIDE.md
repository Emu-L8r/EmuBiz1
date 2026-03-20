# Bizap Configuration Guide

## Required Configuration

### 1. Signing Configuration (for Release Builds)

For production/release builds, you must configure signing credentials via environment variables:

```bash
# Linux/Mac
export KEYSTORE_PATH=/path/to/your/release-key.jks
export KEYSTORE_PASSWORD=your_keystore_password
export KEY_ALIAS=your_key_alias
export KEY_PASSWORD=your_key_password

# Windows PowerShell
$env:KEYSTORE_PATH="C:\path\to\release-key.jks"
$env:KEYSTORE_PASSWORD="your_keystore_password"
$env:KEY_ALIAS="your_key_alias"
$env:KEY_PASSWORD="your_key_password"
```

#### Development Fallback
For local development, you can:
1. Place a `release-key.jks` file in the project root directory
2. The build will use default development credentials (DO NOT use in production!)

### 2. Exchange Rate API Key

The app uses an exchange rate API for currency conversion features.

#### Option A: Using local.properties (Recommended for Development)
Add to `local.properties` file (gitignored):
```properties
EXCHANGE_RATE_API_KEY=your_api_key_here
```

#### Option B: Using gradle.properties
Add to `~/.gradle/gradle.properties` (global):
```properties
EXCHANGE_RATE_API_KEY=your_api_key_here
```

#### Option C: Using Environment Variable
```bash
export EXCHANGE_RATE_API_KEY=your_api_key_here
```

**Get a free API key:**
- Visit: https://www.exchangerate-api.com/
- Sign up for a free account
- Copy your API key

## Building the Project

### Debug Build (Development)
```bash
./gradlew assembleDebug
```
- Does not require signing configuration
- API key optional (will show warning if missing)

### Release Build (Production)
```bash
./gradlew assembleRelease
```
- Requires signing configuration (environment variables)
- Requires API key (or warning will be shown)

## Testing

Run all tests:
```bash
./gradlew test
```

Run specific test:
```bash
./gradlew test --tests "com.emul8r.bizap.SomeTest"
```

## Common Issues

### Issue: "KEYSTORE_PASSWORD environment variable not set"
**Solution:** Set the signing environment variables as shown above, or place `release-key.jks` in project root for development.

### Issue: "EXCHANGE_RATE_API_KEY not found" warning
**Solution:** This is just a warning. Add the API key to `local.properties` or gradle.properties to enable currency features.

### Issue: Build fails with Gradle plugin errors
**Solution:** Ensure you're using the correct Gradle version:
```bash
./gradlew wrapper --gradle-version 9.2.1
```

## Environment Setup Checklist

- [ ] JDK 17 installed
- [ ] Android SDK installed (API 26-35)
- [ ] Signing credentials configured (for release builds)
- [ ] API key configured (optional, enables currency features)
- [ ] `local.properties` file created (if not using global gradle.properties)

## Security Notes

1. **Never commit `local.properties`** - It's in `.gitignore` for a reason
2. **Never commit keystore files** - Keep them secure and separate
3. **Use environment variables in CI/CD** - Not hardcoded values
4. **Rotate API keys periodically** - Especially if exposed

## Need Help?

Check the following resources:
- `/docs` directory for detailed documentation
- `OPTION_C_IMPLEMENTATION_SUMMARY.md` for recent changes
- Issue tracker for known issues and solutions
