/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class MobileTargetsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.withId("com.android.library") {
            val androidLibraryExtension =
                target.extensions.findByType(LibraryExtension::class.java)!!

            setupAndroidLibrary(androidLibraryExtension)
        }

        target.plugins.withId("org.jetbrains.kotlin.multiplatform") {
            val kmpExtension =
                target.extensions.findByType(KotlinMultiplatformExtension::class.java)!!

            setupMultiplatformTargets(kmpExtension, target)
        }
    }

    private fun setupAndroidLibrary(libraryExtension: LibraryExtension) {
        libraryExtension.sourceSets {
            mapOf(
                "main" to "src/androidMain",
                "release" to "src/androidMainRelease",
                "debug" to "src/androidMainDebug",
                "test" to "src/androidUnitTest",
                "testRelease" to "src/androidUnitTestRelease",
                "testDebug" to "src/androidUnitTestDebug"
            ).forEach { (name, root) ->
                getByName(name).run {
                    setRoot(root)
                }
            }
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
