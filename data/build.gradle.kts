plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.detekt)
    id("com.android.library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.coiltester.data"
    compileSdk = 37

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
                implementation(project(":core"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.room.runtime)
                implementation(libs.room.ktx)
                implementation(libs.datastore.preferences)
            }
        }
        named("jvmMain") {
            dependencies {
                implementation(project(":core"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        named("androidMain") {
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
    add("kspAndroid", libs.room.compiler)
    add("kspJvm", libs.room.compiler)
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