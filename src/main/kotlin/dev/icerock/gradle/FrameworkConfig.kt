/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import KotlinNativeExportable
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.konan.target.Architecture

open class FrameworkConfig {
    var name: String = "MultiPlatformLibrary"

    internal val exports = mutableListOf<ExportDeclaration>()

    fun export(arm64Dependency: String, x64Dependency: String) {
        ExportDeclaration.ExternalExport(
            arm64 = arm64Dependency,
            x64 = x64Dependency
        ).let { exports.add(it) }
    }

    fun export(project: Project) {
        ExportDeclaration.ProjectExport(project).let { exports.add(it) }
    }

    fun export(kotlinNativeExportable: KotlinNativeExportable) {
        ExportDeclaration.Exportable(kotlinNativeExportable).let { exports.add(it) }
    }

    fun export(artifact: String) {
        ExportDeclaration.ArtifactStringExport(artifact).let { exports.add(it) }
    }

    fun export(provider: Provider<MinimalExternalModuleDependency>) {
        ExportDeclaration.VersionCatalogExport(provider.get()).let { exports.add(it) }
    }

    fun export(project: ProjectDependency) {
        ExportDeclaration.ProjectExport(project.dependencyProject).let { exports.add(it) }
    }

    internal sealed class ExportDeclaration {
        data class ExternalExport(
            val arm64: String,
            val x64: String
        ) : ExportDeclaration() {
            override fun export(project: Project, framework: Framework) {
                val architecture = framework.target.konanTarget.architecture
                when (architecture) {
                    Architecture.ARM64 -> framework.export(arm64)
                    Architecture.X64 -> framework.export(x64)
                    else -> throw IllegalArgumentException("unsupported architecture ($architecture) for export declaration")
                }
            }
        }

        data class ProjectExport(
            val project: Project
        ) : ExportDeclaration() {
            override fun export(project: Project, framework: Framework) {
                framework.export(this.project)
            }
        }

        data class Exportable(
            val kotlinNativeExportable: KotlinNativeExportable
        ) : ExportDeclaration() {
            override fun export(project: Project, framework: Framework) {
                kotlinNativeExportable.export(project, framework)
            }
        }

        data class ArtifactStringExport(
            val artifact: String
        ) : ExportDeclaration() {
            override fun export(project: Project, framework: Framework) {
                framework.export(this.artifact)
            }
        }

        data class VersionCatalogExport(
            val externalModuleDependency: MinimalExternalModuleDependency
        ) : ExportDeclaration() {
            override fun export(project: Project, framework: Framework) {
                framework.export(externalModuleDependency)
            }
        }

        abstract fun export(project: Project, framework: Framework)
    }
}
