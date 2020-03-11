/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    `kotlin-dsl`
    `maven-publish`
}

repositories {
    jcenter()
    google()
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("com.android.tools.build:gradle:3.6.1")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.70")
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

publishing {
    repositories.maven("https://api.bintray.com/maven/icerockdev/plugins/mobile-multiplatform/;publish=1") {
        name = "bintray"

        credentials {
            username = System.getProperty("BINTRAY_USER")
            password = System.getProperty("BINTRAY_KEY")
        }
    }

    publications {
        register("plugin", MavenPublication::class) {
            groupId = "dev.icerock"
            artifactId = "mobile-multiplatform"
            version = "0.6.0"

            from(components["java"])
        }
    }
}
