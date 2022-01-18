![mobile-multiplatform](https://user-images.githubusercontent.com/5010169/100611874-9aa17f80-3344-11eb-9737-c50ba63b0f6e.png)  
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0) [![Download](https://img.shields.io/maven-central/v/dev.icerock/mobile-multiplatform) ](https://repo1.maven.org/maven2/dev/icerock/mobile-multiplatform)

# Mobile Multiplatform gradle plugin
This is a Gradle plugin for simple setup of Kotlin Multiplatform mobile Gradle modules.  

## Setup
`buildSrc/build.gradle.kts`
```kotlin
repositories {
    google()
    gradlePluginPortal()
}

dependencies {
    implementation("dev.icerock:mobile-multiplatform:0.13.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
    implementation("com.android.tools.build:gradle:7.0.4")
}
```

## Usage
### Setup mobile targets without config
`build.gradle.kts`
```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform.targets")
}
```

Plugin automatically setup android, ios targets.
Android target also automatically configured with `dev.icerock.mobile.multiplatform.android-manifest` 
and `dev.icerock.mobile.multiplatform.android-sources` plugins.

By default used `ios()` targets creation with intermediate source set `iosMain`. To disable it add
into `gradle.properties` line:
```
mobile.multiplatform.useIosShortcut=false
```

Also by default `iosSimulatorArm64` target will be created (with connection to `iosMain` and
 `iosTest` if was used ios shortcut. To disable it add into `gradle.properties` line:
```
mobile.multiplatform.withoutIosSimulatorArm64=false
```

### Setup AndroidManifest.xml in androidMain sourceSet
`build.gradle.kts`
```kotlin
plugins {
    id("dev.icerock.mobile.multiplatform.android-manifest")
}
```

After enable this plugin you can move `AndroidManifest.xml` from `src/main/AndroidManifest.xml` to
`src/androidMain/AndroidManifest.xml`

### Setup android sourceSets in android prefixed source sets
`build.gradle.kts`
```kotlin
plugins {
    id("dev.icerock.mobile.multiplatform.android-sources")
}
```

After enable this plugin you can move android's `main` source set to `androidMain`, `release` 
to `androidRelease`, `test` to `androidTest` etc.

### Setup cocoapods integration for iOS
`build.gradle.kts`
```kotlin
plugins {
    id("dev.icerock.mobile.multiplatform.ios-framework")
}
```

Plugin will setup `sync` gradle tasks in group `cocoapods` for `cocoapods` integration.

Example of `podspec` for integration here - https://github.com/icerockdev/moko-template/blob/master/mpp-library/MultiPlatformLibrary.podspec

#### Add artifacts to export
```kotlin
// optional for export dependencies into framework header
framework {
    export(project = project(":myproject"))
    export(kotlinNativeExportable = MultiPlatfomLibrary(<...>))
    export(kotlinNativeExportable = MultiPlatfomModule(<...>))
    export(arm64Dependency = "my.group:name-iosarm64:0.1.0", x64Dependency = "my.group:name-iosx64:0.1.0")
    export(artifact = "my.group:name:0.1.0") // common artifact
    export(provider = libs.myLib) // gradle 7 version catalog libraries accessors
}
```

### Setup cocoapods integration for all Apple frameworks
`build.gradle.kts`
```kotlin
plugins {
    id("dev.icerock.mobile.multiplatform.apple-framework")
}
```

with `framework` configuration you can add dependencies to export (just like in iOS framework).

### Setup CocoaPods interop
`build.gradle.kts`
```kotlin
plugins {
    id("dev.icerock.mobile.multiplatform.cocoapods")
}

cocoaPods {
    podsProject = file("../ios-app/Pods/Pods.xcodeproj") // here should be path to your Pods project
    buildConfiguration = "dev-debug" // optional, default is "debug"

    pod("MBProgressHUD") // create cInterop and link with CocoaPod where schema and module is same
    pod(schema = "moko-widgets-flat", module = "mokoWidgetsFlat") // create cInterop and link with CocoaPod where schema and module is different
    pod(schema = "moko-widgets-flat", module = "mokoWidgetsFlat", onlyLink = true) // not create cInterop - just link framework with this CocoaPod
}
```

Also path to Pods project and configuration can be set globally into `gradle.properties`
```properties
mobile.multiplatform.podsProject=ios-app/Pods/Pods.xcodeproj
mobile.multiplatform.podsConfiguration=dev-debug
```
`podsProject` should be relative path from root gradle project.

### Multiple plugins in one line (deprecated, saved for backward compatibility)
```kotlin
plugins { 
    id("dev.icerock.mobile.multiplatform")
}
```
This line will enable:
- `dev.icerock.mobile.multiplatform.cocoapods`
- `dev.icerock.mobile.multiplatform.targets`
- publish of android build variants - `release` and `debug`

### Definition of dependencies (deprecated, saved for backward compatibility)
```kotlin
val mokoTime = MultiPlatformLibrary(
    android = "dev.icerock.moko:time-android:0.1.0",
    common = "dev.icerock.moko:time:0.1.0",
    iosX64 = "dev.icerock.moko:time-iosx64:0.1.0",
    iosArm64 = "dev.icerock.moko:time-iosarm64:0.1.0"
)

val appCompat = AndroidLibrary(
    name = "androidx.appcompat:appcompat:1.1.0"
)

val myFeature = MultiPlatformModule(
    name = ":mpp-library:feature:myFeature"
)
```

### Setup dependencies (deprecated, saved for backward compatibility)
`build.gradle.kts`
```kotlin
dependencies {
    mppLibrary(mokoTime)
    androidLibrary(appCompat)
    mppModule(myFeature)
}
```

## License
        
    Copyright 2019 IceRock MAG Inc.
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
