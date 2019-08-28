/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework

interface KotlinNativeExportable {
    fun export(project: Project, framework: Framework)
}
