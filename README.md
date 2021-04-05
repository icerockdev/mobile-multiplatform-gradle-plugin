![mobile-multiplatform](https://user-images.githubusercontent.com/5010169/100611874-9aa17f80-3344-11eb-9737-c50ba63b0f6e.png)  
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0) [![Download](https://img.shields.io/maven-central/v/dev.icerock/mobile-multiplatform) ](https://repo1.maven.org/maven2/dev/icerock/mobile-multiplatform) ![kotlin-version](https://img.shields.io/badge/kotlin-1.4.31-orange)

# Mobile Multiplatform gradle plugin
This is a Gradle plugin for simple setup of Kotlin Multiplatform mobile Gradle modules.  

## Setup
`buildSrc/build.gradle.kts`
```kotlin
repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("dev.icerock:mobile-multiplatform:0.9.2")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.31")
    implementation("com.android.tools.build:gradle:4.1.1")
}
```

## Usage
### Setup gradle module as mobile multiplatform
`build.gradle.kts`
```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform")
}
```
Plugin automatically setup android, ios targets. 
For Android setup sourceset path to `androidMain`. 
For iOS setup `sync` gradle tasks in group `cocoapods` for `cocoapods` integration.

By default used `ios()` targets creation with intermediate source set `iosMain`. To disable it add
 into `gradle.properties` line:
```
mobile.multiplatform.useIosShortcut=false
```

To disable warning about used ios targets add into `gradle.properties` line:
```
mobile.multiplatform.iosTargetWarning=false
```

### Definition of dependencies
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

### Setup dependencies
`build.gradle.kts`
```kotlin
dependencies {
    mppLibrary(mokoTime)
    androidLibrary(appCompat)
    mppModule(myFeature)
}
```

### Setup export as iOS framework
`build.gradle.kts`
```kotlin
plugins {
    id("dev.icerock.mobile.multiplatform.ios-framework")
}

// optional for export dependencies into framework header
framework {
    export(project = project(":myproject"))
    export(kotlinNativeExportable = MultiPlatfomLibrary(<...>))
    export(kotlinNativeExportable = MultiPlatfomModule(<...>))
    export(arm64Dependency = "my.group:name-iosarm64:0.1.0", x64Dependency = "my.group:name-iosx64:0.1.0")
}
```

### Setup sync task for all Apple frameworks
`build.gradle.kts`
```kotlin
plugins {
    id("dev.icerock.mobile.multiplatform.apple-framework")
}
```

### Setup CocoaPods interop
`build.gradle.kts`
```kotlin
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
