/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class SyncCocoaPodFrameworkTask : DefaultTask() {
    init {
        group = "cocoapods"
    }

    @get:InputDirectory
    lateinit var inputDir: File

    @get:OutputDirectory
    val outputDir: File
        get() = File(project.buildDir, "cocoapods/framework")

    @TaskAction
    fun syncFiles() {
        if (outputDir.exists()) {
            outputDir.deleteRecursively()
        }

        project.exec {
            commandLine("cp", "-R", inputDir.absolutePath, outputDir.absolutePath)
        }
    }
}
