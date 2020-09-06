/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.kotlin.dsl.DependencyHandlerScope

data class AndroidLibrary(val name: String)

fun DependencyHandlerScope.androidLibrary(androidLibrary: AndroidLibrary) {
    "androidMainImplementation"(androidLibrary.name)
}

fun DependencyHandlerScope.androidTestLibrary(androidLibrary: AndroidLibrary) {
    "androidTestImplementation"(androidLibrary.name)
}
