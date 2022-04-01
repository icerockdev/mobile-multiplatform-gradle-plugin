/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.io.File

open class CocoaPodInfo(
    val name: String
) {
    var scheme: String = name
    var onlyLink: Boolean = false
    var precompiled: Boolean = false
    var frameworksPaths: (File, KotlinNativeTarget) -> List<File> = { _, _ -> emptyList() }

    val module: String get() = name
    val capitalizedModule: String get() = module.capitalize()
    var extraModules: List<String> = emptyList()
    var extraLinkerOpts: List<String> = emptyList()

    private val onConfiguredBlocks = mutableListOf<() -> Unit>()
    private var configured = false

    override fun toString(): String {
        return "CocoaPodInfo(name = $name, module = $module, onlyLink = $onlyLink, " +
                "precompiled = $precompiled, extraModules = $extraModules, " +
                "extraLinkerOpts = $extraLinkerOpts)"
    }

    internal fun configured() {
        configured = true
        onConfiguredBlocks.forEach { it.invoke() }
    }

    internal fun doOnConfigured(block: () -> Unit) {
        if (configured) block()
        else onConfiguredBlocks.add(block)
    }
}
