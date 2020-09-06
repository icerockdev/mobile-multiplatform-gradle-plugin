/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import java.io.File
import javax.inject.Inject


open class CocoapodsConfig @Inject constructor(objectFactory: ObjectFactory) {
    lateinit var podsProject: File
    var buildConfiguration: String = "debug"

    val dependencies: NamedDomainObjectContainer<CocoaPodInfo> =
        objectFactory.domainObjectContainer(CocoaPodInfo::class.java)

    fun pod(name: String, onlyLink: Boolean = false) {
        pod(scheme = name, module = name, onlyLink = onlyLink)
    }

    fun pod(scheme: String, module: String, onlyLink: Boolean = false) {
        val pod = dependencies.create(scheme) {
            this.module = module
            this.onlyLink = onlyLink
        }
        pod.configured()
    }
}
