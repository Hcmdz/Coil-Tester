plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

group = "org.coiltester.buildlogic.convention"
version = rootProject.extra["versionName"] as String? ?: "1.0.0"