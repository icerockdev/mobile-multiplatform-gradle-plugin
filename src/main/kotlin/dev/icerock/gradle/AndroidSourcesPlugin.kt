/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidSourcesPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.withId("com.android.library") {
            val androidExtension = target.extensions.findByType(LibraryExtension::class.java)!!

            androidExtension.sourceSets.configureEach {
                val capitalizedName = name.capitalize()
                val newRoot = "src/android$capitalizedName"
                setRoot(newRoot)

                target.logger.info("set new root for $name android source set - $newRoot")
            }
        }
    }
}
