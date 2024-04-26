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
import com.diffplug.spotless.LineEnding

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

allprojects {

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
}

subprojects {
    apply {
        plugin(rootProject.libs.plugins.dokka.get().pluginId)
    }
}

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
                // getDefaultProguardFile() is a simple helper method that fetches them out of build/intermediates/proguard-files.
                // The Android Gradle Plugin (AGP) puts them there.
                getDefaultProguardFile("proguard-android-optimize.txt"),

                // List additional ProGuard rules for the given build type here. By default,
                // Android Studio creates and includes an empty rules file for you (located
                // at the root directory of each module).

                // Includes a local, custom Proguard rules file
                "proguard/proguard-rules.pro",
            )
            testProguardFiles(
                // The proguard files listed here are included in the
                // test APK only.
                "test-proguard-rules.pro",
            )
        }
        debug {
            // Enables code shrinking, obfuscation, and optimization for only
            // your project's release build type. Make sure to use a build
            // variant with `isDebuggable=false`.
            isMinifyEnabled = false
        }
    }
}

buildConfig {
    // BuildConfig configuration here.
    // https://github.com/gmazzo/gradle-buildconfig-plugin#usage-in-kts
}

println("DIR=" + layout.buildDirectory)

spotless {
    lineEndings = LineEnding.UNIX

    val excludeSourceFileTargets = listOf(
        "**/generated-src/**",
        "**/build/**",
        "**/build-*/**",
        "**/.idea/**",
        "**/.fleet/**",
        "**/.gradle/**",
        "/spotless/**",
        "**/resources/**",
        "**/buildSrc/**",
    )

    // Configuration for Java files
    java {
        target("**/*.java")
        // Exclude files in the gitignore directories
        targetExclude(*excludeSourceFileTargets.toTypedArray())
        // Adds the ability to have spotless ignore specific portions of a project. The usage looks like the following
        toggleOffOn()
        // Tells spotless to format according to the Google Style Guide(https://google.github.io/styleguide/javaguide.html)
        googleJavaFormat()
        // Will remove any unused imports from any of your Java classes
        removeUnusedImports()
        // Will remove any extra whitespace at the end of lines
        trimTrailingWhitespace()
        // Will add a newline character to the end of files content
        endWithNewline()
        // Specifies license header file
        licenseHeaderFile(providers.gradleProperty("spotless.java.license.header.file"))
    }

    // Configuration for Kotlin files
    kotlin {
        target("**/*.kt")
        // Exclude files in the gitignore directories
        targetExclude(*excludeSourceFileTargets.toTypedArray())
        // Adds the ability to have spotless ignore specific portions of a project. The usage looks like the following
        toggleOffOn()
        // Use ktlint with version 1.2.1 and custom .editorconfig
        ktlint("1.2.1").setEditorConfigPath(providers.gradleProperty("spotless.editor.config.file"))
        // Will remove any extra whitespace at the end of lines
        trimTrailingWhitespace()
        // Will add a newline character to the end of files content
        endWithNewline()
        // Specifies license header file
        licenseHeaderFile(providers.gradleProperty("spotless.kotlin.license.header.file"))
    }

    format("kts") {
        target("**/*.kts")
        // Exclude files in the gitignore directories
        targetExclude(*excludeSourceFileTargets.toTypedArray())
        // Adds the ability to have spotless ignore specific portions of a project. The usage looks like the following
        toggleOffOn()
        // Will remove any extra whitespace at the end of lines
        trimTrailingWhitespace()
        // Will add a newline character to the end of files content
        endWithNewline()
        // Specifies license header file
        licenseHeaderFile(providers.gradleProperty("spotless.kts.license.header.file"), "(^(?![\\/ ]\\*).*$)")
    }

    format("xml") {
        target("**/*.xml")
        // Exclude files in the gitignore directories
        targetExclude(*excludeSourceFileTargets.toTypedArray())
        // Adds the ability to have spotless ignore specific portions of a project. The usage looks like the following
        toggleOffOn()
        // Will remove any extra whitespace at the end of lines
        trimTrailingWhitespace()
        // Will add a newline character to the end of files content
        endWithNewline()
        // Specifies license header file
        licenseHeaderFile(providers.gradleProperty("spotless.xml.license.header.file"), "(<[^!?])")
    }

    // Additional configuration for Kotlin Gradle scripts
    kotlinGradle {
        target("*.gradle.kts")
        // Apply ktlint to Gradle Kotlin scripts
        ktlint("1.2.1")
    }

    format("misc") {
        target("**/*.md", "**/.gitignore")
        // Exclude files in the gitignore directories
        targetExclude(*excludeSourceFileTargets.flatMap { listOf("$it.md", "$it.gitignore") }.toTypedArray())
        // Adds the ability to have spotless ignore specific portions of a project. The usage looks like the following
        toggleOffOn()
        // Will remove any extra whitespace at the beginning of lines
        indentWithSpaces()
        // Will remove any extra whitespace at the end of lines
        trimTrailingWhitespace()
        // Will add a newline character to the end of files content
        endWithNewline()
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
                username = if (System.getenv().containsKey("GITHUB_ACTOR")) {
                    System.getenv("GITHUB_USERNAME")
                } else {
                    githubUsername
                }
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
    coordinates(group.toString(), rootProject.name, version.toString())

    pom {
        name.set(rootProject.name.uppercaseFirstChar())
        description.set(providers.gradleProperty("project.description"))
        inceptionYear.set("2020")
        url.set("https://github.com/$githubUsername/${rootProject.name}")

        licenses {
            license {
                name.set(providers.gradleProperty("license.name").get())
                url.set(providers.gradleProperty("license.url").get())
            }
        }

        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/$githubUsername/${rootProject.name}/issues") // Change here
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
            connection.set("scm:git:git://github.com:$githubUsername/${rootProject.name}.git")
            url.set("https://github.com/$githubUsername/${rootProject.name}")
            developerConnection.set("scm:git:ssh://github.com:$githubUsername/${rootProject.name}.git")
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
