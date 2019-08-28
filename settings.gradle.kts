/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import java.net.URI

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            url = URI("https://plugins.gradle.org/m2/")
        }
    }
}