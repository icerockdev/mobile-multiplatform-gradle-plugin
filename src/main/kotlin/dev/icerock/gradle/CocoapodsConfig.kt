/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.io.File
import javax.inject.Inject


open class CocoapodsConfig @Inject constructor(objectFactory: ObjectFactory) {
    lateinit var podsProject: File
    var buildConfiguration: String = "debug"

    internal val cocoapods: NamedDomainObjectContainer<CocoaPodInfo> =
        objectFactory.domainObjectContainer(CocoaPodInfo::class.java)

    fun pod(name: String, onlyLink: Boolean = false) {
        pod(scheme = name, module = name, onlyLink = onlyLink)
    }

    fun pod(scheme: String, module: String, onlyLink: Boolean = false) {
        cocoapods.create(module) {
            this.scheme = scheme
            this.onlyLink = onlyLink
        }.configured()
    }

    fun precompiledPod(
        scheme: String,
        module: String = scheme,
        extraModules: List<String>? = null,
        extraLinkerOpts: List<String>? = null,
        onlyLink: Boolean = false,
        frameworksPathsResolver: (File, KotlinNativeTarget) -> List<File>
    ) {
        if (!::podsProject.isInitialized) {
            throw IllegalStateException("podsProject property should be set before call precompiledPod")
        }
        cocoapods.create(module) {
            this.scheme = scheme
            this.precompiled = true
            this.onlyLink = onlyLink
            this.frameworksPaths = frameworksPathsResolver
            if (extraModules != null) this.extraModules = extraModules
            if (extraLinkerOpts != null) this.extraLinkerOpts = extraLinkerOpts
        }.configured()
    }
}
