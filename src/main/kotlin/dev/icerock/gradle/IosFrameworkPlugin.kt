/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family

class IosFrameworkPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val frameworkExtension = target.extensions.create("framework", FrameworkConfig::class.java)
        val kmpExtension =
            target.extensions.findByType(KotlinMultiplatformExtension::class.java) ?: return

        target.afterEvaluate {
            kmpExtension.targets
                .filterIsInstance<KotlinNativeTarget>()
                .filter { it.konanTarget.family == Family.IOS }
                .forEach {
                    it.binaries {
                        framework(frameworkExtension.name) {
                            val framework = this
                            frameworkExtension.exports.forEach { exportDeclaration ->
                                target.logger.info("export $exportDeclaration")
                                exportDeclaration.export(target, framework)
                            }
                        }
                    }
                }
        }
    }
}
