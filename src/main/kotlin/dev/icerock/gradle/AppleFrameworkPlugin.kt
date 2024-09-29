/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import java.io.File

open class AppleFrameworkPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val frameworkExtension = target.extensions.create("framework", FrameworkConfig::class.java)
        val kmpExtension =
            target.extensions.findByType(KotlinMultiplatformExtension::class.java) ?: return

        kmpExtension.targets.withType<KotlinNativeTarget>().matching(::targetFilter).configureEach {
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
        val linkTask: TaskProvider<out KotlinNativeLink> = framework.linkTaskProvider
        val syncTaskName: String = linkTask.name.replaceFirst("link", "sync")
        val project: Project = framework.project

        val outputDir = File(project.buildDir, "cocoapods/framework")
        val inputDir: File = framework.outputDirectory

        val syncTask: TaskProvider<Exec> = project.tasks.register(syncTaskName, Exec::class.java) {
            group = "cocoapods"

            commandLine("cp", "-R", inputDir.absolutePath, outputDir.absolutePath)

            doFirst {
                if (outputDir.exists()) {
                    outputDir.deleteRecursively()
                }
            }
        }
        syncTask.dependsOn(linkTask)
    }
}
