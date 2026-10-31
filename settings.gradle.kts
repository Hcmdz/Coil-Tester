pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        maven { url = uri("https://repo1.maven.org/maven2") }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.application" || requested.id.id == "com.android.library") {
                useModule("com.android.tools.build:gradle:8.7.3")
            }
            if (requested.id.id == "org.jetbrains.kotlin.android" || requested.id.id == "org.jetbrains.kotlin.multiplatform") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21")
            }
            if (requested.id.id == "org.jetbrains.kotlin.plugin.compose") {
                useModule("org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:2.4.21")
            }
            if (requested.id.id == "com.google.devtools.ksp") {
                useModule("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.3.12")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        maven { url = uri("https://repo1.maven.org/maven2") }
    }
}

rootProject.name = "CoilTester"

include(":core")
include(":data")
include(":app")
include(":build-logic:convention")