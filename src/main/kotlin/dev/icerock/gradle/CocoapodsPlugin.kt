/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import dev.icerock.gradle.tasks.CompileCocoaPod
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

class CocoapodsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val cocoaPodsExtension = target.extensions.create("cocoaPods", CocoapodsConfig::class.java)
        if (target.hasProperty(PROPERTY_PODS_PROJECT)) {
            val path = target.property(PROPERTY_PODS_PROJECT) as String
            cocoaPodsExtension.podsProject = File(target.rootDir, path)
        }
        if (target.hasProperty(PROPERTY_PODS_CONFIGURATION)) {
            val config = target.property(PROPERTY_PODS_CONFIGURATION) as String
            cocoaPodsExtension.buildConfiguration = config
        }

        target.plugins.withId("org.jetbrains.kotlin.multiplatform") {
            val kmpExtension =
                target.extensions.findByType(KotlinMultiplatformExtension::class.java)!!

            kmpExtension.targets
                .matching { it is KotlinNativeTarget }
                .configureEach {
                    this as KotlinNativeTarget

                    configureCocoaPodsDependencies(
                        cocoaPodsExtension = cocoaPodsExtension,
                        kotlinNativeTarget = this,
                        target = target
                    )
                }
        }
    }

    private fun configureCocoaPodsDependencies(
        cocoaPodsExtension: CocoapodsConfig,
        kotlinNativeTarget: KotlinNativeTarget,
        target: Project
    ) {
        cocoaPodsExtension.cocoapods.configureEach {
            doOnConfigured {
                if (!precompiled) {
                    configureCocoaPod(
                        target = kotlinNativeTarget,
                        project = target,
                        pod = this,
                        cocoaPodsExtension = cocoaPodsExtension
                    )

                }

                if (!onlyLink) {
                    configureCInterop(
                        target = kotlinNativeTarget,
                        pod = this,
                        project = target
                    )
                } else if (precompiled) {
                    configurePrecompiledLink(
                        target = kotlinNativeTarget,
                        pod = this
                    )
                }
            }
        }
    }

    private fun configureCocoaPod(
        target: KotlinNativeTarget,
        project: Project,
        pod: CocoaPodInfo,
        cocoaPodsExtension: CocoapodsConfig
    ) {
        project.logger.info("configure cocoaPod $pod in $target of $project")

        val buildTask: CompileCocoaPod = configurePodCompilation(
            kotlinNativeTarget = target,
            pod = pod,
            project = project,
            cocoaPodsExtension = cocoaPodsExtension
        )
        val frameworksDir = buildTask.frameworksDir

        target.binaries
            .configureEach {
                linkerOpts("-F${frameworksDir.absolutePath}")

                linkTask.dependsOn(buildTask)
            }

        project.tasks
            .withType(KotlinNativeTest::class)
            .matching { it.targetName == target.name }
            .configureEach {
                environment("SIMCTL_CHILD_DYLD_FRAMEWORK_PATH", frameworksDir.absolutePath)
            }
    }

    private fun configurePodCompilation(
        kotlinNativeTarget: KotlinNativeTarget,
        pod: CocoaPodInfo,
        project: Project,
        cocoaPodsExtension: CocoapodsConfig
    ): CompileCocoaPod {
        project.logger.info("configure compilation pod $pod in $kotlinNativeTarget of $project")

        val (sdk, arch) = when (kotlinNativeTarget.konanTarget) {
            KonanTarget.IOS_ARM64 -> "iphoneos" to "arm64"
            KonanTarget.IOS_X64 -> "iphonesimulator" to "x86_64"
            KonanTarget.IOS_SIMULATOR_ARM64 -> "iphonesimulator" to "arm64"
            else -> throw IllegalArgumentException("${kotlinNativeTarget.konanTarget} is unsupported")
        }
        val taskName = generateCompileCocoaPodTaskName(kotlinNativeTarget, pod)
        val taskProject = project.rootProject

        val existTask = taskProject.tasks.findByName(taskName)
        return if (existTask != null) {
            existTask as CompileCocoaPod
        } else {
            project.rootProject.tasks.create(taskName, CompileCocoaPod::class.java) {
                podInfo = pod
                compileSdk = sdk
                compileArch = arch
                config = cocoaPodsExtension
            }
        }
    }

    private fun generateCompileCocoaPodTaskName(
        kotlinNativeTarget: KotlinNativeTarget,
        pod: CocoaPodInfo
    ): String {
        val (sdk, arch) = when (kotlinNativeTarget.konanTarget) {
            KonanTarget.IOS_ARM64 -> "iphoneos" to "arm64"
            KonanTarget.IOS_X64 -> "iphonesimulator" to "x86_64"
            KonanTarget.IOS_SIMULATOR_ARM64 -> "iphonesimulator" to "arm64"
            else -> throw IllegalArgumentException("${kotlinNativeTarget.konanTarget} is unsupported")
        }
        val capitalizedPodName = pod.capitalizedModule
        val capitalizedSdk = sdk.capitalize()
        val capitalizedArch = arch.capitalize()
        return "cocoapodBuild$capitalizedPodName$capitalizedSdk$capitalizedArch"
    }

    private fun configureCInterop(
        target: KotlinNativeTarget,
        pod: CocoaPodInfo,
        project: Project
    ) {
        project.logger.info("configure cInterop for pod $pod in $target of $project")

        if (pod.precompiled) {
            createCInteropTask(
                project = project,
                kotlinNativeTarget = target,
                pod = pod,
                frameworksPaths = pod.frameworksPaths
            )
        } else {
            val compileName = generateCompileCocoaPodTaskName(target, pod)
            project.rootProject.tasks.matching { it.name == compileName }.configureEach {
                val compileCocoaPod = this as CompileCocoaPod
                val cInteropTask = createCInteropTask(
                    project = project,
                    kotlinNativeTarget = target,
                    pod = pod,
                    frameworksPaths = listOf(compileCocoaPod.frameworksDir)
                )
                cInteropTask.dependsOn(compileCocoaPod)
            }
        }
    }

    private fun createCInteropTask(
        project: Project,
        kotlinNativeTarget: KotlinNativeTarget,
        pod: CocoaPodInfo,
        frameworksPaths: List<File>
    ): Task {
        val extraModulesLine = pod.extraModules.joinToString(separator = " ")
        val extraLinkerOptsLine = pod.extraLinkerOpts.joinToString(separator = " ")

        val defFile = File(project.buildDir, "cocoapods/def/${pod.module}.def")
        defFile.parentFile.mkdirs()
        defFile.writeText(
            """
language = Objective-C
package = cocoapods.${pod.module}
modules = ${pod.module} $extraModulesLine
linkerOpts = -framework ${pod.module} $extraLinkerOptsLine
""".trimIndent()
        )

        val frameworksOpts = frameworksPaths
            .map { it.absolutePath }
            .map { "-F$it" }
        val compilation =
            kotlinNativeTarget.compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME)
        val capitalizedPodName = pod.capitalizedModule
        val cInteropSettings = compilation.cinterops.create("cocoapod$capitalizedPodName") {
            defFile(defFile)
            compilerOpts(frameworksOpts)
        }
        return project.tasks.getByName(cInteropSettings.interopProcessingTaskName)
    }

    private fun configurePrecompiledLink(
        target: KotlinNativeTarget,
        pod: CocoaPodInfo
    ) {
        target.binaries
            .matching { it is Framework }
            .configureEach {
                val framework = this as Framework

                val frameworks = pod.frameworksPaths
                    .map { it.path }
                    .map { "-F$it" }

                framework.linkerOpts(frameworks)
            }
    }

    private companion object {
        const val PROPERTY_PODS_PROJECT = "mobile.multiplatform.podsProject"
        const val PROPERTY_PODS_CONFIGURATION = "mobile.multiplatform.podsConfiguration"
    }
}
