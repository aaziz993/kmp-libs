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
import com.android.build.gradle.internal.cxx.logging.warnln
import com.diffplug.gradle.spotless.SpotlessApply
import com.diffplug.spotless.LineEnding
import java.io.IOException
import java.net.URL
import java.util.*
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.MetricType
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.support.uppercaseFirstChar

// Top-level build file where you can add configuration options common to all subprojects/modules.
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.build.config)
    alias(libs.plugins.kotest.multiplatform)
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.spotless)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.dokka)
    alias(libs.plugins.vanniktech.maven.publish)
}

val localProperties: Properties = project.rootProject.file("local.properties").let { file ->
    Properties().apply {
        if (file.exists()) {
            load(file.reader())
        }
    }
}

val os: OperatingSystem = OperatingSystem.current()

val buildDirectoryName: String = layout.buildDirectory.asFile.get().name

val developerName: String = providers.gradleProperty("project.developer.name").get()
val developerEmail: String = providers.gradleProperty("project.developer.email").get()

val projectVersionSnapshot: Boolean = providers.gradleProperty("project.version.snapshot").get().toBoolean()

val projectVersionInfix = if (projectVersionSnapshot) {
    "snapshots"
} else {
    "releases"
}

val projectVersionInfixUppercase = projectVersionInfix.uppercase()

val projectInceptionYear: String = providers.gradleProperty("project.inception.year").get()

val projectLicenseName: String = providers.gradleProperty("project.license.name").get()
val projectLicenseTextUrl: String = providers.gradleProperty("project.license.text.url").get()

val githubUsername: String = if (System.getenv().containsKey("GITHUB_${projectVersionInfixUppercase}_USERNAME")) {
    System.getenv("GITHUB_${projectVersionInfixUppercase}_USERNAME")
} else {
    localProperties.getProperty("github.$projectVersionInfix.username")
}

allprojects {
    group = providers.gradleProperty("project.group").get()

    version = "${
        providers.gradleProperty("project.version.major").get()
    }.${
        providers.gradleProperty("project.version.minor").get()
    }.${
        providers.gradleProperty("project.version.patch").get()
    }${
        if (providers.gradleProperty(
                "github.actions.versioning.ref.name",
            ).get().toBoolean() &&
            System.getenv().containsKey("GITHUB_REF_NAME")
        ) {
            // The GITHUB_REF_NAME provide the reference name.
            "-${System.getenv("GITHUB_REF_NAME")}"
        } else {
            ""
        }
    }${
        if (providers.gradleProperty(
                "github.actions.versioning.run.number",
            ).get().toBoolean() &&
            System.getenv().containsKey("GITHUB_RUN_NUMBER")
        ) {
            // The GITHUB_RUN_NUMBER A unique number for each run of a particular workflow in a repository.
            // This number begins at 1 for the workflow's first run, and increments with each new run.
            // This number does not change if you re-run the workflow run.
            "-${System.getenv("GITHUB_RUN_NUMBER")}"
        } else {
            ""
        }
    }${
        if (providers.gradleProperty(
                "jetbrains.space.automation.versioning.ref.name",
            ).get().toBoolean() &&
            System.getenv().containsKey("JB_SPACE_GIT_BRANCH")
        ) {
            // The JB_SPACE_GIT_BRANCH provide the reference  as "refs/heads/repository_name".
            "-${System.getenv("JB_SPACE_GIT_BRANCH").substringAfterLast("/")}"
        } else {
            ""
        }
    }${
        if (providers.gradleProperty(
                "jetbrains.space.automation.versioning.run.number",
            ).get().toBoolean() &&
            System.getenv().containsKey("JB_SPACE_EXECUTION_NUMBER")
        ) {
            "-${System.getenv("JB_SPACE_EXECUTION_NUMBER")}"
        } else {
            ""
        }
    }${
        providers.gradleProperty("project.version.suffix").get().let {
            if (it.isEmpty()) {
                ""
            } else {
                "-$it"
            }
        }
    }${
        if (projectVersionSnapshot) "-SNAPSHOT" else ""
    }"
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
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("proguard/consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility =
            JavaVersion.toVersion(providers.gradleProperty("android.compile.options.source.compatibility").get())
        targetCompatibility =
            JavaVersion.toVersion(providers.gradleProperty("android.compile.options.target.compatibility").get())
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

koverReport {
    filters {
        includes {
            providers.gradleProperty("kover.filters.include.classes").get().let { c ->
                if (c.isNotEmpty()) {
                    classes(
                        c.split(",").map { it.trim() },
                    )
                }
            }
            providers.gradleProperty("kover.filters.include.packages").get().let { p ->
                if (p.isNotEmpty()) {
                    packages(
                        p.split(",").map { it.trim() },
                    )
                }
            }
        }
        excludes {
            providers.gradleProperty("kover.filters.exclude.classes").get().let { c ->
                if (c.isNotEmpty()) {
                    classes(
                        c.split(",").map { it.trim() },
                    )
                }
            }
            providers.gradleProperty("kover.filters.exclude.packages").get().let { p ->
                if (p.isNotEmpty()) {
                    packages(
                        p.split(",").map { it.trim() },
                    )
                }
            }
        }
    }

    verify {
        rule {
            isEnabled = providers.gradleProperty("kover.verify.rule.min.value").get().toBoolean()
            bound {
                minValue = providers.gradleProperty("kover.verify.rule.min.value").orNull?.toInt()
                maxValue = providers.gradleProperty("kover.verify.rule.max.value").orNull?.toInt()
                metric = providers.gradleProperty("kover.verify.rule.metric").orNull?.let {
                    MetricType.valueOf(it.uppercase())
                } ?: MetricType.LINE
                aggregation = providers.gradleProperty("kover.verify.rule.aggregation").orNull?.let {
                    AggregationType.valueOf(it.uppercase())
                } ?: AggregationType.COVERED_PERCENTAGE
            }
        }
    }
}

tasks.create("generateKoverReport", Task::class) {
    dependsOn("koverHtmlReport", "koverXmlReport", "test")
}

tasks.withType<Test> {
    finalizedBy("generateKoverReport")
}

tasks.clean {
    delete.add(buildDirectoryName)
}

enum class ProjectFileOverrideType {
    NEVER,
    ALWAYS,
    IF_DIFFERENCE,
}

tasks.create("preparation", Task::class) {
    fun downloadProjectFile(
        destFile: String,
        url: String,
        fallbackFile: String,
        overrideType: ProjectFileOverrideType,
    ) {
        val text = try {
            URL(url).readText()
        } catch (e: IOException) {
            val file = File(fallbackFile)
            warnln(
                "Cannot retrieve ${file.nameWithoutExtension} from \"$url\" fallback to file \"$fallbackFile\"",
            )
            file.readText()
        }
        val file = File(destFile)
        if (file.exists()) {
            if (overrideType == ProjectFileOverrideType.NEVER) {
                return
            }
            if (overrideType == ProjectFileOverrideType.IF_DIFFERENCE) {
                file.writeText(text)
            }
        }
        file.writeText(text)
    }

    // Download and write to file license
    downloadProjectFile(
        "LICENSE",
        providers.gradleProperty("project.license.text.url").get(),
        providers.gradleProperty("project.license.file.fallback.file").get(),
        ProjectFileOverrideType.valueOf(
            providers.gradleProperty("project.license.file.override")
                .get().uppercase(),
        ),
    )

    // Download or fallback to file and write to file code of conduct
    downloadProjectFile(
        "CODE_OF_CONDUCT.md",
        providers.gradleProperty("project.code.of.conduct.md.url").get(),
        providers.gradleProperty("project.code.of.conduct.file.fallback.file").get(),
        ProjectFileOverrideType.valueOf(
            providers.gradleProperty("project.code.of.conduct.file.override")
                .get().uppercase(),
        ),
    )
}

spotless {
    lineEndings = LineEnding.UNIX

    val excludeSourceFileTargets = listOf(
        "**/generated-src/**",
        "**/$buildDirectoryName/**",
        "**/build-*/**",
        "**/.idea/**",
        "**/.fleet/**",
        "**/.gradle/**",
        "/spotless/**",
        "**/resources/**",
        "**/buildSrc/**",
    )

    fun getProjectLicenseHeaderText(file: String) = File(
        providers.gradleProperty(file).get(),
    ).readText()
        .replace(
            providers
                .gradleProperty("project.license.header.text.file.project.inception.year.placeholder")
                .get(),
            projectInceptionYear,
        )
        .replace(
            providers
                .gradleProperty("project.license.header.text.file.project.developer.name.placeholder")
                .get(),
            developerName,
        )
        .replace(
            providers
                .gradleProperty("project.license.header.text.file.project.license.name.placeholder")
                .get(),
            projectLicenseName,
        )

    fun projectJavaFilesLicenseHeaderText(file: String) = "/*${
        getProjectLicenseHeaderText(file)
    }*/"

    fun projectHtmlFilesLicenseHeaderText(file: String) = "<!--${
        getProjectLicenseHeaderText(file)
    }-->"

    fun projectYamlFilesLicenseHeaderText(file: String) = "#${
        getProjectLicenseHeaderText(file)
            .substringBeforeLast("\n").replace("\n", "\n#")
    }"

    // Configuration for Java files
    java {
        // Include source files
        target("**/*.java")
        // Exclude source files
        targetExclude(*excludeSourceFileTargets.toTypedArray())
        // Adds the ability to have spotless ignore specific portions of a project. The usage looks like the following
        toggleOffOn()
        // Tells spotless to format according to the Google Style Guide
        // (https://google.github.io/styleguide/javaguide.html)
        googleJavaFormat()
        // Will remove any unused imports from any of your Java classes
        removeUnusedImports()
        // Will remove any extra whitespace at the end of lines
        trimTrailingWhitespace()
        // Will add a newline character to the end of files content
        endWithNewline()
        // Specifies license header text
        licenseHeader(projectJavaFilesLicenseHeaderText("project.java.files.license.header.text.file"))
    }

    // Configuration for Kotlin files
    kotlin {
        // Include source files
        target("**/*.kt")
        // Exclude source files
        targetExclude(*excludeSourceFileTargets.toTypedArray())
        // Adds the ability to have spotless ignore specific portions of a project. The usage looks like the following
        toggleOffOn()
        // Use ktlint with version 1.2.1 and custom .editorconfig
        ktlint("1.2.1")
            .setEditorConfigPath(providers.gradleProperty("spotless.editor.config.file").get())
        // Will remove any extra whitespace at the end of lines
        trimTrailingWhitespace()
        // Will add a newline character to the end of files content
        endWithNewline()
        // Specifies license header text
        licenseHeader(projectJavaFilesLicenseHeaderText("project.kt.files.license.header.text.file"))
    }

    // Configuration for miscellaneous files
    mapOf(
        "kts" to Triple(
            listOf("kts"),
            projectJavaFilesLicenseHeaderText("project.kt.files.license.header.text.file"),
            providers.gradleProperty("project.kts.files.license.header.text.delimiter").get(),
        ),
        "xml" to Triple(
            listOf("xml"),
            "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n${
                projectHtmlFilesLicenseHeaderText("project.xml.files.license.header.text.file")
            }",
            providers.gradleProperty("project.xml.files.license.header.text.delimiter").get(),
        ),
        "yaml" to Triple(
            listOf("yaml", "yml"),
            projectYamlFilesLicenseHeaderText("project.yaml.files.license.header.text.file"),
            providers.gradleProperty("project.yaml.files.license.header.text.delimiter").get(),
        ),
        "properties" to Triple(
            listOf("properties"),
            projectYamlFilesLicenseHeaderText("project.properties.files.license.header.text.file"),
            providers.gradleProperty("project.properties.files.license.header.text.delimiter").get(),
        ),
        "html" to Triple(
            listOf("html"),
            projectHtmlFilesLicenseHeaderText("project.html.files.license.header.text.file"),
            providers.gradleProperty("project.html.files.license.header.text.delimiter").get(),
        ),
        "md" to Triple(
            listOf("md"),
            projectHtmlFilesLicenseHeaderText("project.md.files.license.header.text.file"),
            providers.gradleProperty("project.md.files.license.header.text.delimiter").get(),
        ),
        "gitignore" to Triple(
            listOf("gitignore"),
            projectYamlFilesLicenseHeaderText("project.gitignore.files.license.header.text.file"),
            providers.gradleProperty("project.gitignore.files.license.header.text.delimiter").get(),
        ),
        "gitattributes" to Triple(
            listOf("gitattributes"),
            projectYamlFilesLicenseHeaderText("project.gitattributes.files.license.header.text.file"),
            providers.gradleProperty("project.gitattributes.files.license.header.text.delimiter").get(),
        ),
    ).forEach { entry ->
        format(entry.key) {
            // Include source files
            target(*entry.value.first.map { "**/*.$it" }.toTypedArray())
            // Exclude source files
            targetExclude(*excludeSourceFileTargets.toTypedArray())
            // Adds the ability to have spotless ignore specific portions of a project.
            // The usage looks like the following
            toggleOffOn()
            // Will remove any extra whitespace at the beginning of lines
            if (entry.key == "properties") {
                indentWithSpaces()
            }
            // Will remove any extra whitespace at the end of lines
            trimTrailingWhitespace()
            // Will add a newline character to the end of files content
            endWithNewline()
            // Specifies license header text
            licenseHeader(entry.value.second, entry.value.third)
        }
    }

    // Additional configuration for Kotlin Gradle scripts
    kotlinGradle {
        target("*.gradle.kts")
        // Apply ktlint to Gradle Kotlin scripts
        ktlint("1.2.1")
    }
}

tasks.withType<SpotlessApply> {
    dependsOn("preparation")
}

// Project code analysis
// To analyze a project hierarchy, apply the SonarQube plugin to the root project of the hierarchy.
// Typically, (but not necessarily) this will be the root project of the Gradle build.
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
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            providers.gradleProperty("sonar.coverage.jacoco.xml.report.paths").get(),
        )
        property("sonar.androidLint.reportPaths", providers.gradleProperty("sonar.android.lint.report.paths").get())
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

publishing {
    repositories {
        // SPACE PACKAGES
        maven {
            name = "spacePackages"

            url = uri(providers.gradleProperty("jetbrains.space.packages.$projectVersionInfix.url").get())
            credentials {
                username = if (System.getenv().containsKey("JB_SPACE_${projectVersionInfixUppercase}_USERNAME")) {
                    System.getenv("JB_SPACE_${projectVersionInfixUppercase}_USERNAME")
                } else {
                    localProperties.getProperty("jetbrains.space.$projectVersionInfix.username")
                }
                password = if (System.getenv().containsKey("JB_SPACE_${projectVersionInfixUppercase}_PASSWORD")) {
                    System.getenv("JB_SPACE_${projectVersionInfixUppercase}_PASSWORD")
                } else {
                    localProperties.getProperty("jetbrains.space.$projectVersionInfix.password")
                }
            }
        }

        // GITHUB PACKAGES
        maven {
            name = "githubPackages"

            url = uri(
                "${
                    providers.gradleProperty("github.packages.$projectVersionInfix.url").get()
                }/${rootProject.name}",
            )

            // Repository username and password
            credentials {
                username = githubUsername
                password = if (System.getenv().containsKey("GITHUB_${projectVersionInfixUppercase}_PASSWORD")) {
                    System.getenv("GITHUB_${projectVersionInfixUppercase}_PASSWORD")
                } else {
                    localProperties.getProperty("github.$projectVersionInfix.password")
                }
            }
        }
    }
}

mavenPublishing {
    coordinates(group.toString(), rootProject.name, version.toString())

    pom {
        name.set(rootProject.name.uppercaseFirstChar())
        description.set(providers.gradleProperty("project.description").get())
        inceptionYear.set(projectInceptionYear)
        url.set("https://github.com/$githubUsername/${rootProject.name}")

        licenses {
            license {
                name.set(projectLicenseName)
                url.set(projectLicenseTextUrl)
            }
        }

        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/$githubUsername/${rootProject.name}/issues") // Change here
        }

        developers {
            developer {
                id.set(providers.gradleProperty("project.developer.id").get())
                name.set(developerName)
                email.set(developerEmail)
                providers.gradleProperty("project.developer.organization.name").orNull?.let {
                    organization.set(it)
                }
                providers.gradleProperty("project.developer.organization.url").orNull?.let {
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
        when (providers.gradleProperty("sonatype.$projectVersionInfix.url").get()) {
            "https://oss.sonatype.org" -> "DEFAULT"
            "https://s01.oss.sonatype.org" -> "S01"
            else -> "CENTRAL_PORTAL"
        },
        providers.gradleProperty("sonatype.$projectVersionInfix.autopush").get().toBoolean(),
    )

    // Enable GPG signing for all publications
    signAllPublications()
}
