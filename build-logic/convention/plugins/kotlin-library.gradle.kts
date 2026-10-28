package org.coiltester.buildlogic.convention

import org.gradle.kotlin.dsl.`kotlin-dsl`
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.api.plugins.ExtraPropertiesExtension

class KotlinLibraryConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
                kotlin {
                    jvmToolchain(17)
                    sourceSets {
                        val commonMain by getting {
                            dependencies {
                                implementation(libs.kotlinx.coroutines.core)
                                implementation(libs.kotlinx.coroutines.android)
                                implementation(libs.kotlinx.serialization.json)
                            }
                        }
                        val jvmMain by getting
                        val androidMain by getting {
                            dependsOn(jvmMain)
                        }
                    }
                }
            }
        }
    }
}