plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    // NOTE: KSP and dagger.hilt.android are declared at app level only
    // to ensure they use the same classloader (Gradle plugin classloader fix)
    // See: https://github.com/google/dagger/issues/3965
}
