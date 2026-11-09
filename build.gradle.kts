plugins {
    id("com.android.application") version "9.5.0" apply false
    id("com.android.library") version "9.5.0" apply false
    id("org.jetbrains.kotlin.multiplatform") version "2.3.21" apply false
    id("org.jetbrains.kotlin.android") version "2.3.21" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.21" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.8" apply false
}

// Version management (must run before subprojects)
apply(from = "versioning.gradle.kts")

allprojects {
    group = "org.coiltester"
    version = rootProject.extra["versionName"] as String? ?: "1.0.0-SNAPSHOT"
    configurations.all {
        resolutionStrategy {
            force(
                "org.bouncycastle:bcprov-jdk18on:1.85",
                "org.bouncycastle:bcpkix-jdk18on:1.84",
                "ch.qos.logback:logback-core:1.5.34",
                "org.codehaus.plexus:plexus-utils:3.6.1",
                "org.jdom:jdom2:2.0.6.1",
                "org.apache.httpcomponents:httpclient:4.5.13",
                "org.bitbucket.b_c:jose4j:0.9.6",
                "org.apache.commons:commons-lang3:3.18.0",
                "com.google.guava:guava:33.8.0-jre"
            )
        }
    }
}

tasks.register("printVersion") {
    doLast {
        println("Version: ${rootProject.version}")
    }
}