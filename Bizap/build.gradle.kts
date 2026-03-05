plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.ksp) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    // NOTE: dagger.hilt.android moved to app/build.gradle.kts only
    // to ensure it's in the same scope as KSP (Gradle plugin classloader fix)
}
