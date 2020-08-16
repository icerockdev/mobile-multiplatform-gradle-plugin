/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class IosFrameworkPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val frameworkExtension = target.extensions.create("framework", FrameworkConfig::class.java)
        val kmpExtension = target.extensions.findByType(KotlinMultiplatformExtension::class.java) ?: return

        kmpExtension.ios {
            binaries {
                framework(frameworkExtension.name) {
                    val framework = this
                    target.configurations.matching {
                        it.name.endsWith("api", ignoreCase = true) &&
                                framework.compilation.relatedConfigurationNames.contains(it.name)
                    }.all {
                        val configuration = this

                        allDependencies.matching {
                            it.name.startsWith("kotlin-stdlib").not()
                        }.all {
                            val dependency = this
                            target.logger.log(LogLevel.INFO, "export ${dependency.name} from $configuration")
                            framework.export(dependency)
                        }
                    }
                }
            }
        }
    }
}
