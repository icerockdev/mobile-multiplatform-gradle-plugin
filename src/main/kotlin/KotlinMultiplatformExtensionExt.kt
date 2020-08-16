/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun Project.setupFramework(
    exports: List<KotlinNativeExportable>,
    name: String = "MultiPlatformLibrary"
) {
    extensions.findByType(KotlinMultiplatformExtension::class.java)?.run {
        ios {
            binaries {
                framework(name) {
                    freeCompilerArgs += "-Xobjc-generics"

                    exports.forEach { it.export(project, this) }
                }
            }
        }
    }
}
