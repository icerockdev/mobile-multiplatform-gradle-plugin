/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.project
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework

data class MultiPlatformModule(
    val name: String,
    val exported: Boolean = false
) : KotlinNativeExportable {

    override fun export(project: Project, framework: Framework) {
        if (!exported) return

        framework.export(project.project(name))
    }
}

fun DependencyHandlerScope.mppModule(configuration: String, module: MultiPlatformModule) {
    val name = module.name
    "common$configuration"(dependencies.project(path = name))
}

fun DependencyHandlerScope.mppModule(module: MultiPlatformModule) {
    mppModule(configuration = "MainApi", module = module)
}

fun DependencyHandlerScope.mppTestModule(module: MultiPlatformModule) {
    mppModule(configuration = "TestApi", module = module)
}
