/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class MobileMultiPlatformPlugin : Plugin<Project> {
    private val cocoapodsPlugin = CocoapodsPlugin()
    private val mobileTargetsPlugin = MobileTargetsPlugin()

    override fun apply(target: Project) {
        // backward compatibility apply
        cocoapodsPlugin.apply(target)
        mobileTargetsPlugin.apply(target)

        target.plugins.withId("org.jetbrains.kotlin.multiplatform") {
            val kmpExtension =
                target.extensions.findByType(KotlinMultiplatformExtension::class.java)!!

            kmpExtension.android {
                publishLibraryVariants("release", "debug")
            }
        }
    }
}
