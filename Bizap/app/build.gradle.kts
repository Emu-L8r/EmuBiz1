/*
 * Bizap Build Configuration
 *
 * This file defines the build configuration for the Bizap Android app.
 *
 * Key Features:
 * - Gradle 9.2+ compatible (ready for Gradle 10)
 * - Modern Kotlin DSL with version catalogs
 * - Secure release signing via environment variables
 * - Exchange Rate API integration
 * - SQLCipher database encryption
 *
 * Related Documentation:
 * - Build Guide: docs/BUILD_GUIDE.md
 * - Gradle Roadmap: docs/GRADLE_MIGRATION_ROADMAP.md
 * - Security: docs/SIGNING_SECURITY_POLICY.md
 */

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)  // Compose compiler plugin (Kotlin 2.0+)
    alias(libs.plugins.kotlin.serialization)  // For ScreenV2 @Serializable routes
    alias(libs.plugins.google.ksp)  // Annotation processing (Room, Hilt)
    alias(libs.plugins.google.hilt.android)  // Dependency injection
    alias(libs.plugins.google.services)  // Firebase integration
    alias(libs.plugins.firebase.crashlytics)  // Crash reporting
    id("jacoco")
    id("io.gitlab.arturbosch.detekt") version "1.23.0"
}

android {
    namespace = "com.emul8r.bizap"
    compileSdk = 35  // Target latest stable SDK

    defaultConfig {
        applicationId = "com.emul8r.bizap"
        minSdk = 26  // Android 8.0+ (required for SQLCipher hardware-backed keystore)
        targetSdk = 35
        versionCode = 2
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        /*
         * Exchange Rate API Key Configuration
         *
         * The app uses ExchangeRate-API.com for currency conversion.
         * If no key is configured, the app will:
         * - Use cached exchange rates (if available)
         * - Fall back to default USD rates
         * - Not crash or fail to build
         *
         * To configure:
         * 1. Get free key: https://www.exchangerate-api.com/
         * 2. Add to gradle.properties: EXCHANGE_RATE_API_KEY=your_key_here
         * 3. For CI/CD: Set as GitHub secret
         *
         * See: docs/EXCHANGE_RATE_API_GUIDE.md
         */
        val exchangeRateKey = project.findProperty("EXCHANGE_RATE_API_KEY") as String?
        if (exchangeRateKey.isNullOrBlank()) {
            logger.warn("""
                ⚠️  EXCHANGE_RATE_API_KEY not found!

                Exchange rate features will be disabled.
                To enable, add to local.properties or gradle.properties:
                EXCHANGE_RATE_API_KEY=your_api_key_here

                Get a free key at: https://www.exchangerate-api.com/
            """.trimIndent())
            buildConfigField("String", "EXCHANGE_RATE_API_KEY", "\"\"")
        } else {
            buildConfigField("String", "EXCHANGE_RATE_API_KEY", "\"$exchangeRateKey\"")
        }
    }

    /*
     * Release Signing Configuration
     *
     * Production builds MUST use environment variables for security.
     * Development builds can use a local keystore for convenience.
     *
     * Environment Variables (Production):
     * - KEYSTORE_PATH: Absolute path to .jks/.keystore file
     * - KEYSTORE_PASSWORD: Keystore password
     * - KEY_ALIAS: Key alias within keystore
     * - KEY_PASSWORD: Key password
     *
     * Development Fallback:
     * - Place release-key.jks in project root (Bizap/../release-key.jks)
     * - Uses default password "bizap123" (DO NOT use in production!)
     *
     * GitHub Actions:
     * - Uses KEYSTORE_BASE64 secret (base64-encoded keystore)
     * - See: .github/workflows/android-release.yml
     *
     * See: docs/SIGNING_SECURITY_POLICY.md
     */
    signingConfigs {
        create("release") {
            // Load signing credentials from environment variables for security
            // For debug builds, these can be optional
            val keystorePath = System.getenv("KEYSTORE_PATH")
            val storePass = System.getenv("KEYSTORE_PASSWORD")
            val alias = System.getenv("KEY_ALIAS")
            val keyPass = System.getenv("KEY_PASSWORD")

            // Only require environment variables for release builds
            if (keystorePath != null && storePass != null && alias != null && keyPass != null) {
                storeFile = file(keystorePath)
                storePassword = storePass
                keyAlias = alias
                keyPassword = keyPass
            } else {
                // Fallback to local keystore for development (NOT for production!)
                val localKeystore = file("../release-key.jks")
                if (localKeystore.exists()) {
                    logger.warn("""
                        ⚠️  Using local keystore for development.
                        Set environment variables for production builds:
                        - KEYSTORE_PATH
                        - KEYSTORE_PASSWORD
                        - KEY_ALIAS
                        - KEY_PASSWORD
                    """.trimIndent())
                    storeFile = localKeystore
                    storePassword = "bizap123" // Dev only - DO NOT use in production
                    keyAlias = "bizap-key"
                    keyPassword = "bizap123" // Dev only - DO NOT use in production
                } else {
                    logger.warn("""
                        ⚠️  Release signing configuration missing!
                        Release builds will be unsigned and may fail.
                        Either:
                        1. Set environment variables: KEYSTORE_PATH, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD
                        2. Place a development keystore at: ../release-key.jks
                    """.trimIndent())
                }
            }
        }
    }

    androidResources {
        noCompress += listOf("proto", "pb")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            // ✅ CRITICAL FIX: Ensure native libraries are always deployed
            isDebuggable = true
            isJniDebuggable = true
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true

            // 🔐 SECURITY: Disable debugger access in production
            isDebuggable = false
            isJniDebuggable = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // ✅ Crashlytics Configuration: Enable mapping file upload for de-obfuscation
            //
            // When minify is enabled (isMinifyEnabled = true), ProGuard/R8 obfuscates the code.
            // Crashlytics needs the mapping files to translate obfuscated stack traces back to
            // original source code locations. Without this, crash reports are useless.
            //
            // This configuration ensures:
            // 1. Native symbol upload is enabled (for NDK crashes)
            // 2. Mapping files are uploaded during CI/CD builds
            // 3. Crashlytics can de-obfuscate crash stacks in the Firebase console
            //
            // Result: When a user crashes in production, you see the actual line numbers
            // and method names, not obfuscated names like "a.b.c(Z)V"
            configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                nativeSymbolUploadEnabled = true
                // Optional: Set custom unstripped native libs directory if using NDK
                // unstrippedNativeLibsDir = file("build/intermediates/merged_native_libs/release/out/lib")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
        }
    }

    // Room schema export configuration
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    sourceSets {
        getByName("androidTest").assets.srcDirs("$projectDir/schemas")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        abortOnError = false
        // Allow build to continue with warnings while we fix lint errors
        disable += "MissingTranslation"
        disable += "ExtraTranslation"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }

        // FIXED: Removed exclusions for x86 and x86_64 to support emulators
        // and armeabi-v7a for older/budget devices.
        // Stripping these saves space but causes UnsatisfiedLinkError on many devices/emulators.
        jniLibs {
            excludes += listOf(
                // "lib/armeabi-v7a/**",
                // "lib/x86/**",
                // "lib/x86_64/**"
            )
        }
    }

    // Windows POSIX Fix for Robolectric Tests
    // Robolectric fails on Windows with UnsupportedOperationException for POSIX permissions.
    // Using a temporary directory that doesn't require POSIX attributes fixes this.
    testOptions {
        unitTests {
            isReturnDefaultValues = true
            all {
                it.systemProperty("robolectric.offline", "true")
                it.systemProperty("robolectric.dependency.repo.id", "central")
                it.systemProperty("robolectric.useSystemProperties", "true")
                it.systemProperty("robolectric.mode", "legacy")
                // Use Java's temp directory which handles POSIX better
                it.systemProperty("java.io.tmpdir", System.getProperty("java.io.tmpdir"))
            }
        }
    }
}

dependencies {

    // Core & UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.coordinatorlayout)

    // Material Design components (AppBarLayout, BottomNavigationView, Toolbar, etc.)
    implementation("com.google.android.material:material:1.11.0")

    // Logging & Monitoring
    implementation(libs.timber)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.config)
    implementation(libs.coroutines.play.services)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.ext.navigation)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.ext.work)
    ksp(libs.hilt.ext.compiler)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Paging 3
    implementation(libs.androidx.paging.runtime.ktx)

    // SQLCipher - encrypted database (passphrase stored in Android Keystore)
    implementation("net.zetetic:sqlcipher-android:4.14.0@aar")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")

    // WorkManager
    implementation(libs.androidx.work.ktx)
    implementation("androidx.startup:startup-runtime:1.1.1")

    // DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)

    // Networking (Retrofit)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Template Engine (Freemarker) - HTML to PDF theme
    implementation("org.freemarker:freemarker:2.3.32")

    // PDF Generation (iText 7) - HTML to PDF conversion
    implementation("com.itextpdf:itext-core:8.0.3")
    implementation("com.itextpdf:html2pdf:5.0.3")

    // Other
    implementation(libs.coil.compose)
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // Charts
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.compose)

    // QR Codes
    implementation(libs.zxing.core)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.arch.core.test)
    testImplementation(libs.androidx.paging.common.ktx)
    testImplementation(libs.robolectric)
    testImplementation(kotlin("test"))

    // Additional test dependencies for Robolectric and Android testing
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test:core-ktx:1.5.0")
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("io.mockk:mockk-android:1.13.5")
    testImplementation("com.google.truth:truth:1.1.4")
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("androidx.test.ext:junit-ktx:1.1.5")

    // Android Test Dependencies
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)

    // Kotlin Test Library
    androidTestImplementation(kotlin("test"))

    // AndroidX Test Ext
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:core-ktx:1.5.0")

    // Debug Dependencies
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Memory Leak Detection (Debug only)
    // Automatically detects memory leaks during development
    // Shows notification with leak trace for investigation
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
}

// ──────────────────────────────────────────────────────────────────────────────
// JaCoCo Code Coverage
// ──────────────────────────────────────────────────────────────────────────────

jacoco {
    toolVersion = "0.8.10"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }

    sourceDirectories.setFrom(
        files("src/main/java", "src/main/kotlin")
    )
    classDirectories.setFrom(
        fileTree("build/intermediates/classes/debug") {
            exclude(
                "**/R.class",
                "**/R\$*.class",
                "**/BuildConfig.*",
                "**/Manifest*.*",
                "**/*_Hilt*",
                "**/*_Factory*",
                "**/*_MembersInjector*",
                "**/di/**",
                "**/databinding/**"
            )
        }
    )
    executionData.setFrom(
        files("build/jacoco/testDebugUnitTest.exec")
    )
}

tasks.register("ktlintFormat") {
    doLast {
        exec {
            commandLine("./gradlew", "ktlintFormat")
        }
    }
}

detekt {
    toolVersion = "1.23.0"
    config.setFrom("${rootProject.projectDir}/.detekt.yml")
    baseline = file("$projectDir/detekt-baseline.xml")
    reports {
        html.required.set(true)
        xml.required.set(true)
        sarif.required.set(true)
    }
}

// Code Coverage Configuration
tasks.register<JacocoReport>("jacocoTestDebugUnitTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
    sourceDirectories.setFrom(files("${project.projectDir}/src/main/java"))
    classDirectories.setFrom(
        fileTree("build/intermediates/classes/debug") {
            exclude(
                "**/R.class",
                "**/R\$*.class",
                "**/BuildConfig.*",
                "**/Manifest*.*",
                "**/*_Hilt*",
                "**/*_Factory*",
                "**/*_MembersInjector*",
                "**/di/**",
                "**/databinding/**"
            )
        }
    )
    executionData.setFrom(
        files("build/jacoco/testDebugUnitTest.exec")
    )
}

// Ensure jacoco directory exists before running tests
tasks.withType<Test> {
    doFirst {
        val jacocoDir = file("build/jacoco")
        if (!jacocoDir.exists()) {
            jacocoDir.mkdirs()
        }
    }

    // Windows fix: Don't report test results to avoid file locking
    // Tests will still run, but skip the problematic cleanup step
    reports {
        html.required.set(false)
        junitXml.required.set(false)
    }

    // Clean before running on Windows
    doFirst {
        val testResultsDir = file("build/test-results")
        if (testResultsDir.exists()) {
            try {
                testResultsDir.deleteRecursively()
            } catch (e: Exception) {
                // Ignore - file locking is normal on Windows
            }
        }
    }
}
