/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import dev.icerock.gradle.tasks.SyncCocoaPodFrameworkTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

open class AppleFrameworkPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val frameworkExtension = target.extensions.create("framework", FrameworkConfig::class.java)
        val kmpExtension =
            target.extensions.findByType(KotlinMultiplatformExtension::class.java) ?: return

        kmpExtension.targets.withType<KotlinNativeTarget>().matching(::targetFilter).all {
            binaries {
                framework(frameworkExtension.name) {
                    configureFrameworkExports(this, frameworkExtension)
                    configureSyncFrameworkTasks(this)
                }
            }
        }
    }

    protected open fun targetFilter(target: KotlinNativeTarget): Boolean {
        return target.konanTarget.family.isAppleFamily
    }

    private fun configureFrameworkExports(framework: Framework, frameworkConfig: FrameworkConfig) {
        val project = framework.project
        project.afterEvaluate {
            frameworkConfig.exports.forEach { exportDeclaration ->
                project.logger.info("export $exportDeclaration")
                exportDeclaration.export(project, framework)
            }
        }
    }

    private fun configureSyncFrameworkTasks(
        framework: Framework
    ) {
        val linkTask = framework.linkTask
        val syncTaskName = linkTask.name.replaceFirst("link", "sync")

        framework.project.tasks.create(syncTaskName, SyncCocoaPodFrameworkTask::class.java) {
            inputDir = framework.outputDirectory

            dependsOn(linkTask)
        }
    }
}
