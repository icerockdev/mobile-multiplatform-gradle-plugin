/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

open class CocoaPodInfo(
    val name: String
) {
    var module: String = name
    val capitalizedModule get() = module.capitalize()

    var onlyLink: Boolean = false

    private var configured = false
    internal var onConfigured: () -> Unit = {}
        set(value) {
            field = value
            if (configured) field.invoke()
        }

    internal fun configured() {
        configured = true
        onConfigured.invoke()
    }

    override fun toString(): String {
        return "CocoaPodInfo(name = $name, module = $module, onlyLink = $onlyLink)"
    }
}
