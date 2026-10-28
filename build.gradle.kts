plugins {
    id("com.android.application") version "8.5.0" apply false
    id("com.android.library") version "8.5.0" apply false
    id("org.jetbrains.kotlin.multiplatform") version "2.0.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.4" apply false
}

// Version management (must run before subprojects)
apply(from = "versioning.gradle.kts")

allprojects {
    group = "org.coiltester"
    version = rootProject.extra["versionName"] as String? ?: "1.0.0-SNAPSHOT"
}

tasks.register("printVersion") {
    doLast {
        println("Version: ${rootProject.version}")
    }
}