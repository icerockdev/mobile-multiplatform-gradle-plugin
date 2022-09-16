/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.konan.target.KonanTarget

data class MultiPlatformLibrary(
    val android: String? = null,
    val common: String,
    val iosX64: String? = null,
    val iosArm32: String? = null,
    val iosArm64: String? = null,
    val iosSimulatorArm64: String? = null,
    val macosX64: String? = null,
    val macosArm64: String? = null,
    val tvosX64: String? = null,
    val tvosArm64: String? = null,
    val tvosSimulatorArm64: String? = null,
    val watchosX86: String? = null,
    val watchosX64: String? = null,
    val watchosArm32: String? = null,
    val watchosArm64: String? = null,
    val watchosSimulatorArm64: String? = null,
) : KotlinNativeExportable {

    override fun export(project: Project, framework: Framework) {
        val arch = framework.target.konanTarget
        val exportArtifact: String? = when (arch) {
            KonanTarget.IOS_X64 -> iosX64
            KonanTarget.IOS_ARM32 -> iosArm32
            KonanTarget.IOS_ARM64 -> iosArm64
            KonanTarget.IOS_SIMULATOR_ARM64 -> iosSimulatorArm64
            KonanTarget.MACOS_X64 -> macosX64
            KonanTarget.MACOS_ARM64 -> macosArm64
            KonanTarget.TVOS_ARM64 -> tvosArm64
            KonanTarget.TVOS_X64 -> tvosX64
            KonanTarget.TVOS_SIMULATOR_ARM64 -> tvosSimulatorArm64
            KonanTarget.WATCHOS_X86 -> watchosX86
            KonanTarget.WATCHOS_X64 -> watchosX64
            KonanTarget.WATCHOS_ARM32 -> watchosArm32
            KonanTarget.WATCHOS_ARM64 -> watchosArm64
            KonanTarget.WATCHOS_SIMULATOR_ARM64 -> watchosSimulatorArm64
            else -> return
        }
        exportArtifact?.let { framework.export(it) }
    }
}

fun DependencyHandlerScope.mppLibrary(configuration: String, library: MultiPlatformLibrary) {
    library.android?.let { "android$configuration"(it) }
    "common$configuration"(library.common)
    // ios
    library.iosArm32?.let { "iosArm32$configuration"(it) }
    library.iosArm64?.let { "iosArm64$configuration"(it) }
    library.iosSimulatorArm64?.let { "iosSimulatorArm64$configuration"(it) }
    library.iosX64?.let { "iosX64$configuration"(it) }
    // macos
    library.macosX64?.let { "macosX64$configuration"(it) }
    library.macosArm64?.let { "macosArm64$configuration"(it) }
    // tvos
    library.tvosArm64?.let { "tvosArm64$configuration"(it) }
    library.tvosX64?.let { "tvosX64$configuration"(it) }
    library.tvosSimulatorArm64?.let { "tvosSimulatorArm64$configuration"(it) }
    // watchos
    library.watchosX86?.let { "watchosX86$configuration"(it) }
    library.watchosX64?.let { "watchosX64$configuration"(it) }
    library.watchosArm32?.let { "watchosArm32$configuration"(it) }
    library.watchosArm64?.let { "watchosArm64$configuration"(it) }
    library.watchosSimulatorArm64?.let { "watchosSimulatorArm64$configuration"(it) }
}

fun DependencyHandlerScope.mppLibrary(library: MultiPlatformLibrary) {
    mppLibrary(configuration = "MainApi", library = library)
}

fun DependencyHandlerScope.mppTestLibrary(
    library: MultiPlatformLibrary
) {
    mppLibrary(configuration = "TestApi", library = library)
}

fun String.defaultMPL(
    android: Boolean = false,
    ios: Boolean = false,
    macos: Boolean = false,
    tvos: Boolean = false,
    watchos: Boolean = false
): MultiPlatformLibrary {
    return MultiPlatformLibrary(
        android = if (android) commonToPlatformArtifact(this, "android") else null,
        common = this,
        iosX64 = if (ios) commonToPlatformArtifact(this, "iosx64") else null,
        iosArm32 = if (ios) commonToPlatformArtifact(this, "iosarm32") else null,
        iosArm64 = if (ios) commonToPlatformArtifact(this, "iosarm64") else null,
        iosSimulatorArm64 = if (ios) commonToPlatformArtifact(this, "iossimulatorarm64") else null,
        macosX64 = if (macos) commonToPlatformArtifact(this, "macosx64") else null,
        macosArm64 = if (macos) commonToPlatformArtifact(this, "macosarm64") else null,
        tvosX64 = if (tvos) commonToPlatformArtifact(this, "tvosx64") else null,
        tvosArm64 = if (tvos) commonToPlatformArtifact(this, "tvosarm64") else null,
        tvosSimulatorArm64 = if (ios) commonToPlatformArtifact(this, "tvossimulatorarm64") else null,
        watchosArm32 = if (watchos) commonToPlatformArtifact(this, "watchosarm32") else null,
        watchosArm64 = if (watchos) commonToPlatformArtifact(this, "watchosarm64") else null,
        watchosSimulatorArm64 = if (ios) commonToPlatformArtifact(this, "watchossimulatorarm64") else null,
        watchosX86 = if (watchos) commonToPlatformArtifact(this, "watchosx86") else null,
        watchosX64 = if (watchos) commonToPlatformArtifact(this, "watchosx64") else null
    )
}

private fun commonToPlatformArtifact(common: String, platform: String): String {
    return common.replace(Regex("(.*):(.*):(.*)"), "$1:$2-$platform:$3")
}
