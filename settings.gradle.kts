@file:Suppress("UnstableApiUsage")

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
        // Github Packages
        maven { url = uri("https://maven.pkg.github.com/aaziz993") }
    }
}

plugins {
    id("org.jetbrains.amper.settings.plugin").version("0.2.3-dev-473")
}

rootProject.name = providers.gradleProperty("project.name").get()