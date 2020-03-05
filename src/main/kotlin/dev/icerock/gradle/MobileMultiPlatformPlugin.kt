/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Sync
import org.gradle.internal.io.LineBufferingOutputStream
import org.gradle.internal.io.TextStream
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.io.OutputStream


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

        val kmpExt = target.extensions.findByType(KotlinMultiplatformExtension::class.java)?.apply {
            iosArm64()
            iosX64()
            android {
                publishLibraryVariants("release", "debug")
            }
        }

        val cocoapodsConfig = target.extensions.create("cocoaPods", CocoapodsConfig::class.java)

        target.afterEvaluate {
            if (kmpExt != null) {
                cocoapodsConfig.dependencies.forEach { pod ->
                    configureCocoaPod(
                        kotlinMultiplatformExtension = kmpExt,
                        project = this,
                        podsProject = cocoapodsConfig.podsProject,
                        pod = pod
                    )
                }
            }

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

    private fun configureCocoaPod(
        kotlinMultiplatformExtension: KotlinMultiplatformExtension,
        project: Project,
        pod: CocoaPodInfo,
        podsProject: File
    ) {
        kotlinMultiplatformExtension.targets
            .filterIsInstance<KotlinNativeTarget>()
            .forEach { target ->
                val (buildTask, frameworksDir) = configurePodCompilation(
                    kotlinNativeTarget = target,
                    pod = pod,
                    podsProject = podsProject,
                    project = project
                )

                val frameworks = target.binaries.filterIsInstance<Framework>()

                frameworks.forEach { it.linkerOpts("-F${frameworksDir.absolutePath}") }

                if (pod.onlyLink) {
                    project.logger.log(LogLevel.WARN, "CocoaPod ${pod.module} integrated only in link $target stage")
                    frameworks
                        .map { it.linkTask }
                        .forEach { it.dependsOn(buildTask) }
                    return@forEach
                }

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

                configureCInterop(
                    kotlinNativeTarget = target,
                    defFile = defFile,
                    frameworksDir = frameworksDir,
                    pod = pod,
                    project = project,
                    buildPodTask = buildTask
                )
            }
    }

    private fun configurePodCompilation(
        kotlinNativeTarget: KotlinNativeTarget,
        pod: CocoaPodInfo,
        podsProject: File,
        project: Project
    ): Pair<Task, File> {
        val arch = when (kotlinNativeTarget.konanTarget) {
            KonanTarget.IOS_ARM64 -> "iphoneos" to "arm64"
            KonanTarget.IOS_X64 -> "iphonesimulator" to "x86_64"
            else -> throw IllegalArgumentException("${kotlinNativeTarget.konanTarget} is unsupported")
        }
        val cocoapodsOutputDir = File(project.buildDir, "cocoapods")
        val capitalizedPodName = pod.capitalizedModule
        val capitalizedSdk = arch.first.capitalize()
        val capitalizedArch = arch.second.capitalize()

        val buildTask = project.tasks.create("cocoapodBuild$capitalizedPodName$capitalizedSdk$capitalizedArch") {
            group = "cocoapods"

            doLast {
                buildPod(
                    podsProject = podsProject,
                    project = project,
                    scheme = pod.scheme,
                    arch = arch,
                    outputDir = cocoapodsOutputDir
                )
            }
        }
        val frameworksDir = File(cocoapodsOutputDir, "UninstalledProducts/${arch.first}")
        return buildTask to frameworksDir
    }

    private fun configureCInterop(
        kotlinNativeTarget: KotlinNativeTarget,
        defFile: File,
        frameworksDir: File,
        pod: CocoaPodInfo,
        project: Project,
        buildPodTask: Task
    ) {
        val compilation = kotlinNativeTarget.compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME)
        val capitalizedPodName = pod.capitalizedModule

        val cinteropSettings = compilation.cinterops.create("cocoapod$capitalizedPodName") {
            defFile(defFile)

            compilerOpts("-F${frameworksDir.absolutePath}")
        }
        val cinteropTask = project.tasks.getByName(cinteropSettings.interopProcessingTaskName)

        cinteropTask.dependsOn(buildPodTask)
    }

    private fun buildPod(
        podsProject: File,
        project: Project,
        scheme: String,
        outputDir: File,
        arch: Pair<String, String>
    ) {
        val podsProjectPath = podsProject.absolutePath

        val podBuildDir = outputDir.absolutePath
        val derivedData = File(outputDir, "DerivedData").absolutePath
        val cmdLine = arrayOf(
            "xcodebuild",
            "-project", podsProjectPath,
            "-scheme", scheme,
            "-sdk", arch.first,
            "-arch", arch.second,
            "-derivedDataPath", derivedData,
            "SYMROOT=$podBuildDir",
            "DEPLOYMENT_LOCATION=YES",
            "SKIP_INSTALL=YES",
            "build"
        )
        cmdLine.joinToString(separator = " ").also {
            project.logger.log(LogLevel.LIFECYCLE, "cocoapod build cmd: $it")
        }

        val errOut = LineBufferingOutputStream(
            object : TextStream {
                override fun endOfStream(failure: Throwable?) {
                    if (failure != null) {
                        project.logger.log(LogLevel.ERROR, failure.message, failure)
                    }
                }

                override fun text(text: String) {
                    project.logger.log(LogLevel.ERROR, text)
                }
            }
        )
        val stdOut = LineBufferingOutputStream(
            object : TextStream {
                override fun endOfStream(failure: Throwable?) {
                    if (failure != null) {
                        project.logger.log(LogLevel.ERROR, failure.message, failure)
                    }
                }

                override fun text(text: String) {
                    project.logger.log(LogLevel.INFO, text)
                }
            }
        )
        val result = project.exec {
            workingDir = podsProject
            commandLine = cmdLine.toList()
            standardOutput = stdOut
            errorOutput = errOut
        }
        project.logger.log(LogLevel.LIFECYCLE, "xcodebuild result is ${result.exitValue}")
        result.assertNormalExitValue()
    }
}
