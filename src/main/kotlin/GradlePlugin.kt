/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.api.artifacts.Dependency
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

data class GradlePlugin(
    val id: String,
    val module: String? = null,
    val version: String? = null
)

fun DependencyHandlerScope.plugin(gradlePlugin: GradlePlugin): Dependency? {
    return gradlePlugin.module?.let { "classpath"(it) }
}

fun PluginDependenciesSpec.plugin(gradlePlugin: GradlePlugin): PluginDependencySpec {
    val spec = id(gradlePlugin.id)
    gradlePlugin.version?.also { spec.version(it) }
    return spec
}
