plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.detekt)
    id("com.android.library")
}

android {
    namespace = "org.coiltester.core"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }
}

kotlin {
    jvmToolchain(17)
    jvm()
    androidTarget()
    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        named("jvmMain") {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        named("jvmTest") {
            dependencies {
                implementation(libs.junit.jupiter)
                implementation(libs.junit.jupiter.params)
                implementation(libs.turbine)
                implementation(libs.kotlinx.coroutines.test)
                runtimeOnly(libs.junit.jupiter)
                runtimeOnly(libs.junit.platform.launcher)
            }
        }
        named("androidMain") {
            dependencies {
                implementation(libs.kotlinx.coroutines.android)
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xjsr305=strict"
        )
    }
}

// Without this the jvmTest task compiles the tests and reports success without running any.
tasks.withType<Test> {
    useJUnitPlatform()
}

// Detekt config
detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(files("$projectDir/../../config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = true
}

dependencies {
    // Test dependencies are in jvmTest source set
}