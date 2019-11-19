/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

class MobileMultiPlatformPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.findByType(LibraryExtension::class.java)?.sourceSets {
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

        target.extensions.findByType(KotlinMultiplatformExtension::class.java)?.run {
            iosArm64()
            iosX64()
            android {
                publishLibraryVariants("release", "debug")
            }
        }

        target.afterEvaluate {
            tasks.mapNotNull { it as? KotlinNativeLink }
                .mapNotNull { it.binary as? Framework }
                .forEach { framework ->
                    val linkTask = framework.linkTask
                    val syncTaskName = linkTask.name.replaceFirst("link", "sync")
                    val syncFramework = tasks.create(syncTaskName, Sync::class.java) {
                        group = "cocoapods"

                        from(framework.outputDirectory)
                        into(file("build/cocoapods/framework"))
                    }
                    syncFramework.dependsOn(linkTask)
                }
        }
    }
}
