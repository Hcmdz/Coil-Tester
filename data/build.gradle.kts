plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.detekt)
    id("com.android.library")
    // id("com.google.devtools.ksp")  // KSP 2.3.12 incompatible with AGP 8.7.3
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
                implementation(libs.room.ktx)
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
                implementation(libs.room.ktx)
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
    annotationProcessor(libs.room.compiler)
}

// ksp {
//     arg("room.schemaLocation", "$projectDir/schemas")
//     arg("room.incremental", "true")
// }

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xjsr305=strict"
        )
    }
}