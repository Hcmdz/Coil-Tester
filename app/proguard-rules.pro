# ── App ──────────────────────────────────────────────────
-keep class org.coiltester.app.MainActivity { *; }
-keep class org.coiltester.app.viewmodel.** { *; }
-keep class org.coiltester.app.ui.** { *; }

# ── Log stripping (release) ──────────────────────────────
-assumenosideeffects class android.util.Log {
    public static int d(java.lang.String, java.lang.String);
    public static int v(java.lang.String, java.lang.String);
    public static int i(java.lang.String, java.lang.String);
    public static int e(java.lang.String, java.lang.String);
    public static int e(java.lang.String, java.lang.String, java.lang.Throwable);
    public static int w(java.lang.String, java.lang.String);
    public static int w(java.lang.String, java.lang.String, java.lang.Throwable);
}

# ── Room ─────────────────────────────────────────────────
-keep class org.coiltester.app.data.db.** { *; }
-dontwarn androidx.room.paging.**

# ── Kotlin Coroutines ────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ── Koin ─────────────────────────────────────────────────
-keep class org.koin.** { *; }
-keep class org.coiltester.app.di.** { *; }
-dontwarn org.koin.**

# ── Kotlin Serialization ─────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep class * extends kotlinx.serialization.KSerializer { *; }
-keep class kotlinx.serialization.** { *; }

# ── Compose ──────────────────────────────────────────────
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# ── DataStore ────────────────────────────────────────────
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ── General ──────────────────────────────────────────────
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**
-dontwarn kotlin.reflect.jvm.internal.**