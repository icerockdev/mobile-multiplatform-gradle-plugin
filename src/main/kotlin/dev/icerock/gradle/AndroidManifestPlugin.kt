/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidManifestPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.withId("com.android.library") {
            val androidExtension = target.extensions.findByType(LibraryExtension::class.java)!!
            val mainSourceSet = androidExtension.sourceSets.getByName("main")
            val newManifestPath = "src/androidMain/AndroidManifest.xml"
            mainSourceSet.manifest.srcFile(newManifestPath)

            target.logger.info("set new android manifest path $newManifestPath")
        }
    }
}
