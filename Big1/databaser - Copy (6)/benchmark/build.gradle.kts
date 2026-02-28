plugins {
    alias(libs.plugins.android.test)
}

android {
    namespace = "com.example.databaser.benchmark"
    compileSdk = 36

    targetProjectPath = ":app"
    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.benchmark.junit4.AndroidBenchmarkRunner"
    }
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.benchmark.macro.junit4)
}
