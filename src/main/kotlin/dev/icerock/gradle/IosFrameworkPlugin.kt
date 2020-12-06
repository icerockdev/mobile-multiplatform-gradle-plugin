/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family

class IosFrameworkPlugin : AppleFrameworkPlugin() {
    override fun targetFilter(target: KotlinNativeTarget): Boolean {
        return target.konanTarget.family == Family.IOS
    }
}
