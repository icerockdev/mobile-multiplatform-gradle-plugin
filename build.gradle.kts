/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    `kotlin-dsl`
    `maven-publish`
    id("com.github.b3er.local.properties") version "1.1"
}

repositories {
    jcenter()
    google()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.android.tools.build:gradle:3.4.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.50")
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

publishing {
    repositories.maven("https://api.bintray.com/maven/icerockdev/moko/mobile-multiplatform/;publish=1") {
        name = "bintray"

        credentials {
            username = properties["BINTRAY_USER"] as? String
            password = properties["BINTRAY_KEY"] as? String
        }
    }

    publications {
        register("plugin", MavenPublication::class) {
            groupId = "dev.icerock"
            artifactId = "mobile-multiplatform"
            version = "0.1.0"

            from(components["java"])
        }
    }
}
