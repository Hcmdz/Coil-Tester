plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.jacoco)
}

android {
    namespace = "org.coiltester.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.coiltester.app"
        minSdk = 24
        targetSdk = 35
        versionCode = rootProject.extra["versionCode"] as Int
        versionName = rootProject.extra["versionName"] as String
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            val storeFilePath = providers.gradleProperty("RELEASE_STORE_FILE").getOrElse("")
            if (storeFilePath.isNotEmpty()) {
                storeFile = file(storeFilePath)
                storePassword /* from the environment */ = providers.gradleProperty("RELEASE_STORE_PASSWORD").get()
                keyAlias = providers.gradleProperty("RELEASE_KEY_ALIAS").getOrElse("")
                keyPassword /* from the environment */ = providers.gradleProperty("RELEASE_KEY_PASSWORD").get()
                enableV3Signing = true
            }
        }
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,LICENSE,NOTICE}/*"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null) {
                signingConfig = releaseSigning
            }
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Kotlin compiler options
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs +=
            listOf(
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xjsr305=strict",
            )
    }

    // Version properties (already set from rootProject.extra in defaultConfig)
}

dependencies {
    // Core & Data modules
    implementation(project(":core"))
    implementation(project(":data"))

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.navigation.compose)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)

    // DataStore
    implementation(libs.datastore.preferences)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.turbine)
    testImplementation(libs.assertk)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit.jupiter)

    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation(libs.compose.ui.test.manifest)
    androidTestImplementation(libs.room.testing)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Detekt
detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(files("../config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = true
    baseline = file("detekt-baseline.xml")
}

// Ktlint
ktlint {
    ignoreFailures.set(true)
}

// JaCoCo coverage report
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    val fileFilter = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/*Test*.*", "android/**/*.*", "**/*\$*.class"
    )
    val debugTree = fileTree("${layout.buildDirectory.get().asFile}/intermediates/javac/debug") +
        fileTree("${layout.buildDirectory.get().asFile}/tmp/kotlin-classes/debug")
    sourceDirectories.setFrom(files("src/main/kotlin", "src/main/java"))
    classDirectories.setFrom(files(debugTree.files.map { fileTree(it) { exclude(fileFilter) } }))
    executionData.setFrom(fileTree(layout.buildDirectory.get().asFile) { include("**/*.exec", "**/*.ec") })
}

// Compose compiler metrics/reports
composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    metricsDestination = layout.buildDirectory.dir("compose_compiler")
    stabilityConfigurationFile = file("compose-stability.conf")
}
