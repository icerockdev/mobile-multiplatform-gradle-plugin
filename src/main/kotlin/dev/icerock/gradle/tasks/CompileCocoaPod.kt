/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.tasks

import dev.icerock.gradle.CocoaPodInfo
import dev.icerock.gradle.CocoapodsConfig
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.OutputStream
import java.io.File

open class CompileCocoaPod : DefaultTask() {
    init {
        group = "cocoapods"
    }

    @get:Internal
    internal lateinit var config: CocoapodsConfig

    @get:InputDirectory
    val podsProject: File
        get() = config.podsProject

    @get:InputDirectory
    val podsDir: File
        get() = podsProject.parentFile

    @get:Input
    lateinit var compileSdk: String

    @get:Input
    lateinit var compileArch: String

    @get:Internal
    internal lateinit var podInfo: CocoaPodInfo

    @get:Input
    val scheme: String
        get() = podInfo.scheme

    @get:Input
    val configuration: String
        get() = config.buildConfiguration

    private val outputDir: File get() = File(project.buildDir, "cocoapods")

    @get:OutputDirectory
    val frameworksDir: File
        get() = File(outputDir, "UninstalledProducts/$compileSdk")

    @TaskAction
    fun compile() {
        val podsProjectPath = podsProject.absolutePath
        val podBuildDir = outputDir.absolutePath
        val derivedData = File(outputDir, "DerivedData").absolutePath
        val cmdLine = arrayOf(
            "xcodebuild",
            "-project", podsProjectPath,
            "-scheme", scheme,
            "-sdk", compileSdk,
            "-arch", compileArch,
            "-configuration", configuration,
            "-derivedDataPath", derivedData,
            "SYMROOT=$podBuildDir",
            "DEPLOYMENT_LOCATION=YES",
            "SKIP_INSTALL=YES",
            "build"
        )
        cmdLine.joinToString(separator = " ").also {
            project.logger.lifecycle("cocoapod build cmd: $it")
        }

        val result = project.exec {
            workingDir = podsProject
            commandLine = cmdLine.toList()
            standardOutput = OutputStream.nullOutputStream()
            errorOutput = OutputStream.nullOutputStream()
        }
        project.logger.lifecycle("xcodebuild result is ${result.exitValue}")
        result.assertNormalExitValue()
    }
}
