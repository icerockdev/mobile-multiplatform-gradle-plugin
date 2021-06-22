/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import java.util.Base64

plugins {
    `kotlin-dsl`
    `maven-publish`
    `signing`
}

group = "dev.icerock"
version = "0.10.1"

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
    compileOnly("com.android.tools.build:gradle:4.1.1")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.31")
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.4.31")
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
