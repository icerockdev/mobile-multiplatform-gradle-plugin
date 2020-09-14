/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

open class CocoaPodInfo(
    val name: String
) {
    var scheme: String = name
    var onlyLink: Boolean = false

    val module: String get() = name
    val capitalizedModule: String get() = module.capitalize()

    override fun toString(): String {
        return "CocoaPodInfo(name = $name, module = $module, onlyLink = $onlyLink)"
    }
}
