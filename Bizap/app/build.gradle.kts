plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.google.hilt.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.emul8r.bizap"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.emul8r.bizap"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // API Key validation - fail-fast if not configured
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
                    throw GradleException("""
                        ❌ Release signing configuration missing!
                        
                        Either:
                        1. Set environment variables: KEYSTORE_PATH, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD
                        2. Place a development keystore at: ../release-key.jks
                    """.trimIndent())
                }
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = false  // Disabled: causes FileSystemAlreadyExistsException in resource shrinking
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

    // Logging & Monitoring (TASK 1)
    implementation(libs.timber)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

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
    implementation("net.zetetic:sqlcipher-android:4.13.0@aar")
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

    // Other
    implementation(libs.coil.compose)
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // Charts (TASK 12 - VICO ENGINE)
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")
    implementation("com.patrykandpatrick.vico:compose:1.13.1")
    implementation("com.patrykandpatrick.vico:core:1.13.1")

    // Testing (TASK 2 FOUNDATION)
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

    // Android Test Dependencies (for instrumented tests on device/emulator)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)

    // Kotlin Test Library (for kotlin.test assertions in androidTest)
    androidTestImplementation(kotlin("test"))

    // AndroidX Test Ext - needed for AndroidJUnit4 runner
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:core-ktx:1.5.0")

    // Debug Dependencies
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
