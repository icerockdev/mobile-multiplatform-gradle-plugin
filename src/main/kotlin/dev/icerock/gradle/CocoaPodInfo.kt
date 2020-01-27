/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

data class CocoaPodInfo(
    val scheme: String,
    val module: String,
    val onlyLink: Boolean
) {
    val capitalizedModule = module.capitalize()
}