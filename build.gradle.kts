/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import java.util.Base64

plugins {
    `kotlin-dsl`
    id("org.gradle.maven-publish")
    id("signing")
    id("com.gradle.plugin-publish") version ("0.15.0")
    id("java-gradle-plugin")
}


group = "dev.icerock"
version = libs.versions.mobileMultiplatformGradlePluginVersion.get()

repositories {
    mavenCentral()
    google()

    jcenter {
        content {
            includeGroup("org.jetbrains.trove4j")
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(libs.androidGradlePlugin)
    compileOnly(libs.kotlinGradlePlugin)
    compileOnly(libs.kotlinCompilerEmbeddable)
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
}

publishing {
    repositories.maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
        name = "OSSRH"

        credentials {
            username = System.getenv("OSSRH_USER")
            password = System.getenv("OSSRH_KEY")
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            pom {
                name.set("Mobile Multiplatform gradle plugin")
                description.set("Gradle plugin for simplify Kotlin Multiplatform Mobile configurations")
                url.set("https://github.com/icerockdev/mobile-multiplatform-gradle-plugin")
                licenses {
                    license {
                        name.set("Apache-2.0")
                        distribution.set("repo")
                        url.set("https://github.com/icerockdev/mobile-multiplatform-gradle-plugin/blob/master/LICENSE.md")
                    }
                }

                developers {
                    developer {
                        id.set("Alex009")
                        name.set("Aleksey Mikhailov")
                        email.set("aleksey.mikhailov@icerockdev.com")
                    }
                }

                scm {
                    connection.set("scm:git:ssh://github.com/icerockdev/mobile-multiplatform-gradle-plugin.git")
                    developerConnection.set("scm:git:ssh://github.com/icerockdev/mobile-multiplatform-gradle-plugin.git")
                    url.set("https://github.com/icerockdev/mobile-multiplatform-gradle-plugin")
                }
            }
        }
    }
}

signing {
    val signingKeyId: String? = System.getenv("SIGNING_KEY_ID")
    val signingPassword: String? = System.getenv("SIGNING_PASSWORD")
    val signingKey: String? = System.getenv("SIGNING_KEY")?.let { base64Key ->
        String(Base64.getDecoder().decode(base64Key))
    }

    if (signingKeyId != null) {
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
        sign(publishing.publications)
    }
}

gradlePlugin {
    plugins {
        create("multiplatform") {
            id = "dev.icerock.mobile.multiplatform"
            implementationClass = "dev.icerock.gradle.MobileMultiPlatformPlugin"
        }
        create("android-manifest") {
            id = "dev.icerock.mobile.multiplatform.android-manifest"
            implementationClass = "dev.icerock.gradle.AndroidManifestPlugin"
        }
        create("android-sources") {
            id = "dev.icerock.mobile.multiplatform.android-sources"
            implementationClass = "dev.icerock.gradle.AndroidSourcesPlugin"
        }
        create("apple-framework") {
            id = "dev.icerock.mobile.multiplatform.apple-framework"
            implementationClass = "dev.icerock.gradle.AppleFrameworkPlugin"
        }
        create("cocoapods") {
            id = "dev.icerock.mobile.multiplatform.cocoapods"
            implementationClass = "dev.icerock.gradle.CocoapodsPlugin"
        }
        create("ios-framework") {
            id = "dev.icerock.mobile.multiplatform.ios-framework"
            implementationClass = "dev.icerock.gradle.IosFrameworkPlugin"
        }
        create("targets") {
            id = "dev.icerock.mobile.multiplatform.targets"
            implementationClass = "dev.icerock.gradle.MobileTargetsPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/icerockdev/mobile-multiplatform-gradle-plugin"
    vcsUrl = "https://github.com/icerockdev/mobile-multiplatform-gradle-plugin"
    description = "Gradle plugin for simplify Kotlin Multiplatform Mobile configurations"
    tags = listOf("kotlin", "kotlin-multiplatform")

    plugins {
        getByName("multiplatform") {
            displayName = "deprecated"
        }
        getByName("android-manifest") {
            displayName = "android-manifest"
        }
        getByName("android-sources") {
            displayName = "android-sources"
        }
        getByName("apple-framework") {
            displayName = "apple-framework"
        }
        getByName("cocoapods") {
            displayName = "cocoapods"
        }
        getByName("ios-framework") {
            displayName = "ios-framework"
        }
        getByName("targets") {
            displayName = "targets"
        }
    }

    mavenCoordinates {
        groupId = project.group as String
        artifactId = project.name
        version = project.version as String
    }
}
