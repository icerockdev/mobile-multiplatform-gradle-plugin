/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.android.build.gradle.LibraryExtension
import dev.icerock.gradle.tasks.CompileCocoaPod
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Sync
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File


class MobileMultiPlatformPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val cocoaPodsExtension = target.extensions.create("cocoaPods", CocoapodsConfig::class.java)

        target.plugins.withId("com.android.library") {
            val androidLibraryExtension =
                target.extensions.findByType(LibraryExtension::class.java)!!

            setupAndroidLibrary(androidLibraryExtension)
        }

        target.plugins.withId("org.jetbrains.kotlin.multiplatform") {
            val kmpExtension =
                target.extensions.findByType(KotlinMultiplatformExtension::class.java)!!

            setupIosTargets(kmpExtension, target)

            kmpExtension.targets
                .matching { it is KotlinNativeTarget }
                .configureEach {
                    this as KotlinNativeTarget

                    configureCocoaPodsDependencies(
                        cocoaPodsExtension = cocoaPodsExtension,
                        kotlinNativeTarget = this,
                        target = target
                    )
                    configureSyncFrameworkTasks(
                        kotlinNativeTarget = this,
                        project = target
                    )
                }
        }
    }

    private fun configureCocoaPodsDependencies(
        cocoaPodsExtension: CocoapodsConfig,
        kotlinNativeTarget: KotlinNativeTarget,
        target: Project
    ) {
        cocoaPodsExtension.compilationPods.configureEach {
            configureCocoaPod(
                target = kotlinNativeTarget,
                project = target,
                pod = this,
                cocoaPodsExtension = cocoaPodsExtension
            )
        }
        cocoaPodsExtension.cInteropPods.configureEach {
            configureCInterop(
                kotlinNativeTarget = kotlinNativeTarget,
                pod = this,
                project = target
            )
        }
    }

    private fun configureSyncFrameworkTasks(
        kotlinNativeTarget: KotlinNativeTarget,
        project: Project
    ) {
        kotlinNativeTarget.binaries
            .matching { it is Framework }
            .configureEach {
                val framework = this as Framework
                val linkTask = framework.linkTask
                val syncTaskName = linkTask.name.replaceFirst("link", "sync")

                val syncFramework =
                    project.tasks.create(syncTaskName, Sync::class.java) {
                        group = "cocoapods"

                        from(framework.outputDirectory)
                        into(project.file("build/cocoapods/framework"))
                    }
                syncFramework.dependsOn(linkTask)
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

    private fun setupIosTargets(
        kmpExtension: KotlinMultiplatformExtension,
        project: Project
    ) {
        val logTargetTypeStr =
            project.findProperty("mobile.multiplatform.iosTargetWarning") as? String
        val logTargetType = logTargetTypeStr?.toLowerCase() != "false"
        kmpExtension.apply {
            android {
                publishLibraryVariants("release", "debug")
            }
            val useShortcutStr =
                project.findProperty("mobile.multiplatform.useIosShortcut") as? String
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

    private fun configureCocoaPod(
        target: KotlinNativeTarget,
        project: Project,
        pod: CocoaPodInfo,
        cocoaPodsExtension: CocoapodsConfig
    ) {
        project.logger.debug("configure cocoaPod $pod in $target of $project")

        val buildTask: CompileCocoaPod = configurePodCompilation(
            kotlinNativeTarget = target,
            pod = pod,
            project = project,
            cocoaPodsExtension = cocoaPodsExtension
        )

        target.binaries
            .matching { it is Framework }
            .configureEach {
                val framework = this as Framework
                val frameworksDir = buildTask.frameworksDir
                framework.linkerOpts("-F${frameworksDir.absolutePath}")

                linkTask.dependsOn(buildTask)
            }
    }

    private fun configurePodCompilation(
        kotlinNativeTarget: KotlinNativeTarget,
        pod: CocoaPodInfo,
        project: Project,
        cocoaPodsExtension: CocoapodsConfig
    ): CompileCocoaPod {
        project.logger.debug("configure compilation pod $pod in $kotlinNativeTarget of $project")

        val (sdk, arch) = when (kotlinNativeTarget.konanTarget) {
            KonanTarget.IOS_ARM64 -> "iphoneos" to "arm64"
            KonanTarget.IOS_X64 -> "iphonesimulator" to "x86_64"
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
            else -> throw IllegalArgumentException("${kotlinNativeTarget.konanTarget} is unsupported")
        }
        val capitalizedPodName = pod.capitalizedModule
        val capitalizedSdk = sdk.capitalize()
        val capitalizedArch = arch.capitalize()
        return "cocoapodBuild$capitalizedPodName$capitalizedSdk$capitalizedArch"
    }

    private fun configureCInterop(
        kotlinNativeTarget: KotlinNativeTarget,
        pod: CocoaPodInfo,
        project: Project
    ) {
        project.logger.debug("configure cInterop for pod $pod in $kotlinNativeTarget of $project")

        val compileName = generateCompileCocoaPodTaskName(kotlinNativeTarget, pod)
        project.rootProject.tasks.matching { it.name == compileName }.configureEach {
            val compileCocoaPod = this as CompileCocoaPod

            val defFile = File(project.buildDir, "cocoapods/def/${pod.module}.def")
            defFile.parentFile.mkdirs()
            defFile.writeText(
                """
language = Objective-C
package = cocoapods.${pod.module}
modules = ${pod.module}
linkerOpts = -framework ${pod.module} 
""".trimIndent()
            )
            val frameworksDir: File = compileCocoaPod.frameworksDir

            val compilation =
                kotlinNativeTarget.compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME)
            val capitalizedPodName = pod.capitalizedModule
            val cinteropSettings = compilation.cinterops.create("cocoapod$capitalizedPodName") {
                defFile(defFile)
                compilerOpts("-F${frameworksDir.absolutePath}")
            }
            val cinteropTask = project.tasks.getByName(cinteropSettings.interopProcessingTaskName)
            cinteropTask.dependsOn(compileCocoaPod)
        }
    }
}
