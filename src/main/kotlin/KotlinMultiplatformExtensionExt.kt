/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

fun Project.setupFramework(
    exports: List<KotlinNativeExportable>
) {
    val configureIos: KotlinNativeTarget.() -> Unit = {
        binaries {
            framework("MultiPlatformLibrary") {
                freeCompilerArgs.add("-Xobjc-generics")

                exports.forEach { it.export(project, this) }
            }
        }
    }

    extensions.findByType(KotlinMultiplatformExtension::class.java)?.run {
        iosArm64("iosArm64", configureIos)
        iosX64("iosX64", configureIos)
    }
}
