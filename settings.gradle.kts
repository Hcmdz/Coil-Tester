pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        maven { url = uri("https://repo1.maven.org/maven2") }
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