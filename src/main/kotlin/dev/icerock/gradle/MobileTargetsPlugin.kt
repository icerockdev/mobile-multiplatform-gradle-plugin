/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

class MobileTargetsPlugin : Plugin<Project> {
    private val androidManifestPlugin = AndroidManifestPlugin()
    private val androidSourcesPlugin = AndroidSourcesPlugin()

    override fun apply(target: Project) {
        // backward compatibility apply
        androidManifestPlugin.apply(target)
        androidSourcesPlugin.apply(target)

        target.plugins.withId("org.jetbrains.kotlin.multiplatform") {
            val kmpExtension =
                target.extensions.findByType(KotlinMultiplatformExtension::class.java)!!

            setupMultiplatformTargets(kmpExtension, target)
        }
    }

    private fun setupMultiplatformTargets(
        kmpExtension: KotlinMultiplatformExtension,
        project: Project
    ) {
        kmpExtension.apply {
            android()
            val useShortcut = project.boolProperty(PROPERTY_USE_IOS_SHORTCUT) ?: true
            val useIosSimulatorArm64 =
                project.boolProperty(PROPERTY_WITHOUT_IOS_SIMULATOR_ARM64) ?: true
            if (useShortcut) {
                ios()
            } else {
                iosArm64()
                iosX64()
            }
            if (useIosSimulatorArm64) {
                iosSimulatorArm64()
                if (useShortcut) {
                    val iosMain: KotlinSourceSet = sourceSets["iosMain"]
                    val iosSimulatorArm64Main: KotlinSourceSet = sourceSets["iosSimulatorArm64Main"]
                    iosSimulatorArm64Main.dependsOn(iosMain)

                    val iosTest: KotlinSourceSet = sourceSets["iosTest"]
                    val iosSimulatorArm64Test: KotlinSourceSet = sourceSets["iosSimulatorArm64Test"]
                    iosSimulatorArm64Test.dependsOn(iosTest)
                }
            }
        }
    }

    private fun Project.boolProperty(name: String): Boolean? {
        val valueString: String = project.findProperty(name) as? String ?: return null
        return valueString.toLowerCase() == "true"
    }

    private companion object {
        const val PROPERTY_USE_IOS_SHORTCUT = "mobile.multiplatform.useIosShortcut"
        const val PROPERTY_WITHOUT_IOS_SIMULATOR_ARM64 =
            "mobile.multiplatform.withoutIosSimulatorArm64"
    }
}
