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
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import java.util.*

// Top-level build file where you can add configuration options common to all subprojects/modules.
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.build.config)
    alias(libs.plugins.kotest.multiplatform)
    alias(libs.plugins.spotless)
    alias(libs.plugins.dokka)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.vanniktech.maven.publish)
}

fun String.toJavaVersion() = JavaVersion.valueOf(
    "VERSION_${
        if (this.toDouble() < 10) {
            this.replace(".", "_")
        } else {
            this
        }
    }",
)

val githubUsername: String = providers.gradleProperty("github.username").get()

val localProperties = project.rootProject.file("local.properties").let { file ->
    Properties().apply {
        if (file.exists()) {
            load(file.reader())
        }
    }
}

group = providers.gradleProperty("project.group").get()

val versionSplit = providers.gradleProperty("project.version").get().split("-", limit = 2)
version = "${versionSplit[0]}${
    if (providers.gradleProperty("github.automation.versioning").get().toBoolean() &&
        System.getenv()
            .containsKey("GITHUB_REF")
    ) {
        // The GITHUB_REF tag comes in the format 'refs/tags/xxx'.
        // If we split on '/' and take the 3rd value,
        // we can get the release name.
        ".${System.getenv("GITHUB_REF").split("/", limit = 3)[2]}"
    } else {
        ""
    }

}${
    if (providers.gradleProperty("jetbrains.space.automation.versioning").get().toBoolean() &&
        System.getenv()
            .containsKey("JB_SPACE_EXECUTION_NUMBER")
    ) {
        ".${System.getenv("JB_SPACE_EXECUTION_NUMBER")}"
    } else {
        ""
    }
}${if (versionSplit.size > 1) "-${versionSplit[1]}" else ""}"

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = providers.gradleProperty("android.compilations.kotlin.options.jvm.target").get()
            }
        }
    }
}

android {
    namespace = group.toString()
    compileSdk = providers.gradleProperty("android.compile.sdk").get().toInt()
    defaultConfig {
        minSdk = providers.gradleProperty("android.default.config.min.sdk").get().toInt()
    }
    compileOptions {
        sourceCompatibility =
            providers.gradleProperty("android.compile.options.source.compatibility").get().toJavaVersion()
        targetCompatibility =
            providers.gradleProperty("android.compile.options.target.compatibility").get().toJavaVersion()
    }
    buildTypes {
        release {
            // Enables code shrinking, obfuscation, and optimization for only
            // your project's release build type. Make sure to use a build
            // variant with `isDebuggable=false`.
            isMinifyEnabled = true

            proguardFiles(
                // Includes the default ProGuard rules files that are packaged with
                // the Android Gradle plugin. To learn more, go to the section about
                // R8 configuration files.
                getDefaultProguardFile("proguard-android-optimize.txt"),

                // List additional ProGuard rules for the given build type here. By default,
                // Android Studio creates and includes an empty rules file for you (located
                // at the root directory of each module).

                // Includes a local, custom Proguard rules file
                "proguard-rules.pro",
            )
            testProguardFiles(
                // The proguard files listed here are included in the
                // test APK only.
                "test-proguard-rules.pro",
            )
        }
        debug {

        }
    }
}

buildConfig {
    // BuildConfig configuration here.
    // https://github.com/gmazzo/gradle-buildconfig-plugin#usage-in-kts
}

spotless {
    // Configuration for Java files
    java {
        target("**/*.java")
        targetExclude("spotless/copyright.java")
        googleJavaFormat().aosp() // Use Android Open Source Project style
        removeUnusedImports() // Automatically remove unused imports
        trimTrailingWhitespace() // Remove trailing whitespace
        licenseHeaderFile(providers.gradleProperty("spotless.java.license.header.file"))
    }

    // Configuration for Kotlin files
    kotlin {
        target("**/*.kt")
        // Exclude files in the build directory
        targetExclude("${layout.buildDirectory}/**/*.kt", "spotless/copyright.kt")
        // Use ktlint with version 1.2.1 and custom .editorconfig
        ktlint("1.2.1").setEditorConfigPath(providers.gradleProperty("spotless.editor.config.file"))
        // Allow toggling Spotless off and on within code files using comments
        toggleOffOn()
        trimTrailingWhitespace()
        licenseHeaderFile(providers.gradleProperty("spotless.kotlin.license.header.file"))
    }

    format("kts") {
        target("**/*.kts")
        // Exclude files in the build directory
        targetExclude("${layout.buildDirectory}/**/*.kts", "spotless/copyright.kts")
        // Look for the first line that doesn't have a block comment (assumed to be the license)
        licenseHeaderFile(providers.gradleProperty("spotless.kts.license.header.file"), "(^(?![\\/ ]\\*).*$)")
    }

    format("misc") {
        target("**/*.gradle", "**/*.md", "**/.gitignore")
        indentWithSpaces()
        trimTrailingWhitespace()
        endWithNewline()
    }

    format("xml") {
        target("**/*.xml")
        // Exclude files in the build directory
        targetExclude("${layout.buildDirectory}/**/*.xml", "spotless/copyright.xml")
        // Look for the first XML tag that isn't a comment (<!--) or the xml declaration (<?xml)
        licenseHeaderFile(providers.gradleProperty("spotless.xml.license.header.file"), "(<[^!?])")
    }

    // Additional configuration for Kotlin Gradle scripts
    kotlinGradle {
        target("*.gradle.kts")
        // Apply ktlint to Gradle Kotlin scripts
        ktlint("1.2.1")
    }
}

// Project documentation
val dokkaOutputDir = layout.buildDirectory.dir("dokka")
tasks.dokkaHtml { outputDirectory.set(file(dokkaOutputDir)) }
val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") {
    delete(
        dokkaOutputDir,
    )
}
val javadocJar = tasks.create<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(deleteDokkaOutputDir, tasks.dokkaHtml)
    from(dokkaOutputDir)
}

// Project code analysis
// To analyze a project hierarchy, apply the SonarQube plugin to the root project of the hierarchy.
// Typically (but not necessarily) this will be the root project of the Gradle build.
// Information pertaining to the analysis as a whole has to be configured in the sonar block of this project.
// Any properties set on the command line also apply to this project.
sonarqube {
    properties {
        property("sonar.host.url", providers.gradleProperty("sonar.host.url").get())
        property("sonar.organization", providers.gradleProperty("sonar.organization").get())
        property(
            "sonar.projectKey",
            "${providers.gradleProperty("sonar.organization").get()}_${project.name}",
        )
        property("sonar.androidLint.reportPaths", providers.gradleProperty("sonar.android.lint.report.paths").get())
    }
}

publishing {
    repositories {
        maven {
            name = "spacePackages"
            url = uri(
                if (version.toString().endsWith("SNAPSHOT")) {
                    providers.gradleProperty("jetbrains.space.packages.snapshots.url")
                } else {
                    providers.gradleProperty("jetbrains.space.packages.releases.url")
                },
            )
            // environment variables
            credentials {
                username = if (System.getenv().containsKey("JB_SPACE_CLIENT_ID")) {
                    System.getenv("JB_SPACE_CLIENT_ID")
                } else {
                    localProperties.getProperty("jetbrains.space.client.id")
                }
                password = if (System.getenv().containsKey("JB_SPACE_CLIENT_SECRET")) {
                    System.getenv("JB_SPACE_CLIENT_SECRET")
                } else {
                    localProperties.getProperty("jetbrains.space.client.secret")
                }
            }
        }

        maven {
            name = "githubPackages"
            url = uri("${providers.gradleProperty("github.packages.url")}/$githubUsername/${rootProject.name}")
            // environment variables
            credentials {
                username = if (System.getenv().containsKey("GITHUB_ACTOR"))
                    System.getenv("GITHUB_ACTOR") else githubUsername
                password = if (System.getenv().containsKey("GITHUB_PASSWORD")) {
                    System.getenv("GITHUB_PASSWORD")
                } else {
                    localProperties.getProperty("github.password")
                }
            }
        }
    }
}

mavenPublishing {
    coordinates(group.toString(), name, version.toString())

    pom {
        name.set(name.toString().uppercaseFirstChar())
        description.set(providers.gradleProperty("project.description"))
        inceptionYear.set("2020")
        url.set("https://github.com/$githubUsername/$name")

        licenses {
            license {
                name.set(providers.gradleProperty("license.name").get())
                url.set(providers.gradleProperty("license.url").get())
            }
        }

        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/$githubUsername/$name/issues") // Change here
        }

        developers {
            developer {
                id.set(providers.gradleProperty("developer.id").get())
                name.set(providers.gradleProperty("developer.name").get())
                email.set(providers.gradleProperty("developer.email").get())
                providers.gradleProperty("developer.organization.name").orNull?.let {
                    organization.set(it)
                }
                providers.gradleProperty("developer.organization.url").orNull?.let {
                    organizationUrl.set(it)
                }
            }
        }

        scm {
            connection.set("scm:git:git://github.com:$githubUsername/$name.git")
            url.set("https://github.com/$githubUsername/$name")
            developerConnection.set("scm:git:ssh://github.com:$githubUsername/$name.git")
        }
    }

    publishToMavenCentral(
        when (providers.gradleProperty("sonatype.url").get()) {
            "https://oss.sonatype.org" -> "DEFAULT"
            "https://s01.oss.sonatype.org" -> "S01"
            else -> "CENTRAL_PORTAL"
        },
        providers.gradleProperty("sonatype.autorelease").get().toBoolean(),
    )

    // Enable GPG signing for all publications
    signAllPublications()
}
