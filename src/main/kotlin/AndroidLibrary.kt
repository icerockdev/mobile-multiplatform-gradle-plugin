/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.kotlin.dsl.DependencyHandlerScope

data class AndroidLibrary(val name: String)

fun DependencyHandlerScope.androidLibrary(configuration: String, androidLibrary: AndroidLibrary) {
    "android$configuration"(androidLibrary.name)
}

fun DependencyHandlerScope.androidLibrary(androidLibrary: AndroidLibrary) {
    androidLibrary(configuration = "MainImplementation", androidLibrary = androidLibrary)
}

fun DependencyHandlerScope.androidTestLibrary(androidLibrary: AndroidLibrary) {
    androidLibrary(configuration = "TestImplementation", androidLibrary = androidLibrary)
}
