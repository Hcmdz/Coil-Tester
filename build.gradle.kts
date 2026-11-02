plugins {
    id("com.android.application") version "9.5.0" apply false
    id("com.android.library") version "9.5.0" apply false
    id("org.jetbrains.kotlin.multiplatform") version "2.1.0" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.8" apply false
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