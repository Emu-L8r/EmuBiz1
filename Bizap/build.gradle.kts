// Bizap/build.gradle.kts (Project Root)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false // Fixed from 'compose'
    alias(libs.plugins.dagger.hilt.android) apply false
    alias(libs.plugins.google.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false // Fixed name
}
