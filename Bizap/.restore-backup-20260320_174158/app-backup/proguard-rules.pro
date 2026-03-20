# ProGuard/R8 configuration for Bizap v1.0 release build
# Minification enabled: R8 will shrink unused code
# Resource shrinking enabled: Unused resources removed

# Preserve line numbers for crash reporting (Firebase Crashlytics)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep Bizap application code (do NOT obfuscate user-facing classes)
-keep class com.emul8r.bizap.** { *; }

# ===== ROOM DATABASE =====
# Keep all Room entities, DAOs, and databases
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public abstract *** get*();
}
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public abstract *** *Dao();
}

# Keep constructor of all Room entities (used for reflection)
-keepclasseswithmembers class * {
    @androidx.room.PrimaryKey <fields>;
}

# Keep ColumnInfo, Embedded, and other Room annotations
-keepclassmembers class * {
    @androidx.room.ColumnInfo <fields>;
    @androidx.room.Embedded <fields>;
    @androidx.room.Relation <fields>;
}

# ===== HILT DEPENDENCY INJECTION =====
# Hilt generates code that needs reflection - keep all Entry points and Components
-keep class * extends dagger.android.AndroidInjector
-keepclasseswithmembers class * {
    @dagger.hilt.* <methods>;
}
-keep @dagger.hilt.android.HiltAndroidApp class *
-keep @dagger.hilt.android.lifecycle.HiltViewModel class *
-keep class * implements dagger.internal.Factory
-keep class * implements dagger.Lazy
-keep class dagger.Lazy
-keep interface dagger.internal.Factory

# Keep Hilt generated module classes
-keep class * extends dagger.Module
-keepclassmembers class * {
    @dagger.Provides <methods>;
    @dagger.hilt.InstallIn <methods>;
}

# ===== RETROFIT & OkHttp =====
# Keep Retrofit API service interfaces
-keep interface * {
    @retrofit2.http.* <methods>;
}
-keep class * {
    @retrofit2.* <fields>;
}
-keepattributes Signature
-keepattributes *Annotation*

# Keep OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ===== KOTLIN SERIALIZATION =====
# Keep serializable classes used with @Serializable
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <methods>;
}
-keep class kotlinx.serialization.** { *; }
-keep class kotlin.reflect.** { *; }
-keepattributes RuntimeVisibleAnnotations

# ===== DATA CLASSES =====
# Keep all data classes (often used for Room models and API responses)
-keepclasseswithmembers class * {
    public synthetic <init>(...);
}
-keep class * {
    @androidx.room.Entity <fields>;
}

# ===== TIMBER LOGGING =====
# Remove Timber debug/verbose logs in release build
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ===== FIREBASE CRASHLYTICS =====
# Keep Firebase/Crashlytics
-keep class com.google.firebase.** { *; }
-keep interface com.google.firebase.** { *; }
-keepattributes SourceFile,LineNumberTable

# ===== GSON (used for Retrofit) =====
-keep class com.google.gson.** { *; }
-keep interface com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ===== ANDROIDX DATASTORE =====
-keep class androidx.datastore.** { *; }
-keep interface androidx.datastore.** { *; }

# ===== STANDARD ANDROID RULES =====
# Preserve enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# ===== HILT DEPENDENCY INJECTION =====
# Keep all Hilt generated classes - CRITICAL for DI to work
-keep class dagger.hilt.** { *; }
-keep class ** extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class com.emul8r.bizap.Hilt_* { *; }
-keep class **_Hilt_* { *; }
-keep class **_Factory { *; }
-keep class **_Provide* { *; }
-keep class **_Factory$* { *; }
-keep class **_Module { *; }
-keep class **_MembersInjector { *; }

# Keep Hilt entry points explicitly
-keep @dagger.hilt.android.HiltAndroidApp class *
-keep @dagger.hilt.android.AndroidEntryPoint class *
-keep @dagger.hilt.android.lifecycle.HiltViewModel class *

# Keep all Hilt-annotated fields and methods
-keepclassmembers class * {
    @dagger.hilt.Inject <init>(...);
    @dagger.hilt.Inject <fields>;
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
}

# Keep custom application classes
-keep class * extends android.app.Application
-keep class * extends android.app.Activity
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider
-keep class * extends androidx.fragment.app.Fragment

# Keep R class (resources)
-keepclassmembers class **.R$* {
    public static <fields>;
}

# ===== SQLCIPHER =====
# SQLCipher has native JNI methods - keep all classes and native methods
-keep class net.zetetic.** { *; }
-keep interface net.zetetic.** { *; }

# ===== ANDROID KEYSTORE & SECURITY CRYPTO =====
# Used by DatabasePassphraseManager to protect the database passphrase
-keep class android.security.keystore.** { *; }
-keep class androidx.security.crypto.** { *; }

# ===== KOTLIN COROUTINES =====
# Coroutines use reflection and suspend functions that can be broken by R8
-keep class kotlinx.coroutines.** { *; }
-keep interface kotlinx.coroutines.** { *; }
-keep class kotlin.coroutines.** { *; }

# ===== WORKMANAGER =====
# WorkManager workers must be kept so they can be instantiated by reflection
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ===== OPTIMIZATION =====
-optimizationpasses 3
-verbose
-dontnote
-dontwarn
