// Add this to app/build.gradle.kts as a custom task
// This allows building and testing from Android Studio UI without terminal

tasks.register("buildAndDeploy") {
    dependsOn("assembleDebug")
    doLast {
        println("✅ Build complete!")
        println("APK location: app/build/outputs/apk/debug/app-debug.apk")
        println("")
        println("To install on emulator, run in Android Studio terminal:")
        println("adb install -r app/build/outputs/apk/debug/app-debug.apk")
        println("adb shell am start -n com.emul8r.bizap/com.emul8r.bizap.MainActivity")
    }
}

tasks.register("cleanAndBuild") {
    dependsOn("clean", "assembleDebug")
    doLast {
        println("✅ Clean build complete!")
        println("APK ready to deploy")
    }
}

// Run data consistency tests
tasks.register("verifyDataConsistency") {
    dependsOn("test")
    doLast {
        println("✅ Data consistency verification complete!")
    }
}

