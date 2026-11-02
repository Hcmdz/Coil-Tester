plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.detekt)
    id("com.android.library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.coiltester.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        targetSdk = 35
    }
}

kotlin {
    jvmToolchain(17)
    targets {
        jvm()
        androidTarget()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.room.runtime)
                implementation(libs.datastore.preferences)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        val androidMain by getting {
            dependsOn(jvmMain)
            dependencies {
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.room.runtime)
            }
        }
    }
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.turbine)
    testRuntimeOnly(libs.junit.jupiter)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.robolectric)
    ksp(libs.room.compiler)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xjsr305=strict"
        )
    }
}

// Force kotlinx-serialization 1.9.0 on ksp configurations (Room 2.8.5 requires it)
// The app runtime stays on 1.8.1 (no compiler plugin needed)
configurations.all {
    if (name.startsWith("ksp")) {
        resolutionStrategy.force("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")
    }
}