/*
 * Copyright 2024 Aziz Atoev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("UnstableApiUsage")

import java.util.*

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/amper/amper")
        maven("https://www.jetbrains.com/intellij-repository/releases")
        maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        // Space Packages releases
        maven { url = uri("https://maven.pkg.jetbrains.space/aaziz93/p/aaziz-93/releases") }
        // Space Packages snapshots
        maven { url = uri("https://maven.pkg.jetbrains.space/aaziz93/p/aaziz-93/snapshots") }
        // GitHub Packages
        maven { url = uri("https://maven.pkg.github.com/aaziz993") }
    }
}

plugins {
    id("org.jetbrains.amper.settings.plugin").version("0.2.3-dev-473")
}

rootProject.name = providers.gradleProperty("project.name").get()

val localProperties: Properties = File("local.properties").let { file ->
    Properties().apply {
        if (file.exists()) {
            load(file.reader())
        }
    }
}

buildCache {
    if (providers.gradleProperty("jetbrains.space.gradle.build.enable").get().toBoolean()) {
        remote<HttpBuildCache> {
            url = uri("${providers.gradleProperty("jetbrains.space.gradle.build.cache.url").get()}/${rootProject.name}")
            // better make it a variable and set it to true only for CI builds
            isPush = true
            credentials {
                username = if (System.getenv().containsKey("")) {
                    System.getenv("JB_SPACE_GRADLE_BUILD_CACHE_USERNAME")
                } else {
                    localProperties.getProperty("jetbrains.space.gradle.build.cache.username")
                }
                password = if (System.getenv().containsKey("")) {
                    System.getenv("JB_SPACE_GRADLE_BUILD_CACHE_PASSWORD")
                } else {
                    localProperties.getProperty("jetbrains.space.gradle.build.cache.password")
                }
            }
        }
    }
}
