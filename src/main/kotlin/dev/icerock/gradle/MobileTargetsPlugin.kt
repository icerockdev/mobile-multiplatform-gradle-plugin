/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

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
        val logTargetTypeStr = project.findProperty(PROPERTY_IOS_WARNING) as? String
        val logTargetType = logTargetTypeStr?.toLowerCase() != "false"
        kmpExtension.apply {
            android()
            val useShortcutStr = project.findProperty(PROPERTY_USE_IOS_SHORTCUT) as? String
            val useShortcut = useShortcutStr?.toLowerCase() != "false"
            if (useShortcut) {
                if (logTargetType) project.logger.warn("used new ios() shortcut target")
                ios()
            } else {
                if (logTargetType) project.logger.warn("used old iosArm64() and iosX64() targets")
                iosArm64()
                iosX64()
            }
        }
    }

    private companion object {
        const val PROPERTY_IOS_WARNING = "mobile.multiplatform.iosTargetWarning"
        const val PROPERTY_USE_IOS_SHORTCUT = "mobile.multiplatform.useIosShortcut"
    }
}
