import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import java.util.*

// Top-level build file where you can add configuration options common to all sub-projects/modules.
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

val androidJavaTargetCompatibility =
    providers.gradleProperty("android.java.target.compatibility").get()

val githubUser: String = providers.gradleProperty("github.user").get()

val localProperties = project.rootProject.file("local.properties")
    .let { file ->
        Properties().apply {
            if (file.exists()) {
                load(file.reader())
            }
        }
    }

allprojects {
    apply {
        plugin(rootProject.libs.plugins.spotless.get().pluginId)
    }

    group = providers.gradleProperty("project.group").get()

    val versionSplit = providers.gradleProperty("project.version").get().split("-", limit = 2)
    version = "${versionSplit[0]}${
        if (providers.gradleProperty("jetbrains.space.automation.versioning").get()
                .toBoolean() &&
            System.getenv().containsKey("JB_SPACE_EXECUTION_NUMBER")
        ) {
            ".${System.getenv("JB_SPACE_EXECUTION_NUMBER")}"
        } else {
            ""
        }
    }${if (versionSplit.size > 1) "-${versionSplit[1]}" else ""}"

    spotless {
        // Configuration for Java files
        java {
            target("**/*.java")
            googleJavaFormat().aosp() // Use Android Open Source Project style
            removeUnusedImports() // Automatically remove unused imports
            trimTrailingWhitespace() // Remove trailing whitespace
        }

        // Configuration for Kotlin files
        kotlin {
            target("**/*.kt")
            targetExclude("${layout.buildDirectory}/**/*.kt") // Exclude files in the build directory
            ktlint("1.2.1").setEditorConfigPath(rootProject.file(".editorconfig").path) // Use ktlint with version 1.2.1 and custom .editorconfig
            toggleOffOn() // Allow toggling Spotless off and on within code files using comments
            trimTrailingWhitespace()
            licenseHeaderFile(rootProject.file("$rootDir/spotless/copyright.kt"))
        }

        format("kts") {
            target("**/*.kts")
            targetExclude("${layout.buildDirectory}/**/*.kts") // Exclude files in the build directory
            // Look for the first line that doesn't have a block comment (assumed to be the license)
            licenseHeaderFile(rootProject.file("spotless/copyright.kts"), "(^(?![\\/ ]\\*).*$)")
        }
        format("xml") {
            target("**/*.xml")
            targetExclude("${layout.buildDirectory}/**/*.xml") // Exclude files in the build directory
            // Look for the first XML tag that isn't a comment (<!--) or the xml declaration (<?xml)
            licenseHeaderFile(rootProject.file("spotless/copyright.xml"), "(<[^!?])")
        }

        // Additional configuration for Kotlin Gradle scripts
        kotlinGradle {
            target("*.gradle.kts")
            ktlint("1.2.1") // Apply ktlint to Gradle Kotlin scripts
        }
    }
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = androidJavaTargetCompatibility
            }
        }
    }
}

android {
    namespace = providers.gradleProperty("project.group").get()
    compileSdk = libs.versions.android.compile.sdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.min.sdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility =
            providers.gradleProperty("android.java.source.compatibility").get().toJavaVersion()
        targetCompatibility = androidJavaTargetCompatibility.toJavaVersion()
    }
}

buildConfig {
    // BuildConfig configuration here.
    // https://github.com/gmazzo/gradle-buildconfig-plugin#usage-in-kts
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
sonarqube {
    properties {
        property("sonar.host.url", providers.gradleProperty("sonar.host.url").get())
        property("sonar.organization", providers.gradleProperty("sonar.organization").get())
        property(
            "sonar.projectKey",
            "${providers.gradleProperty("sonar.organization").get()}_${project.name}",
        )
    }
}

publishing {
    repositories {
        maven {
            name = "spacePackages"
            url = uri(
                if (version.toString()
                        .endsWith("SNAPSHOT")
                ) {
                    providers.gradleProperty("jetbrains.space.packages.snapshots.url")
                } else {
                    providers.gradleProperty("jetbrains.space.packages.releases.url")
                },
            )
            // environment variables
            credentials {
                username = if (System.getenv()
                        .containsKey("JB_SPACE_CLIENT_ID")
                ) {
                    System.getenv("JB_SPACE_CLIENT_ID")
                } else {
                    localProperties.getProperty("jetbrains.space.client.id")
                }
                password = if (System.getenv()
                        .containsKey("JB_SPACE_CLIENT_SECRET")
                ) {
                    System.getenv("JB_SPACE_CLIENT_SECRET")
                } else {
                    localProperties.getProperty("jetbrains.space.client.secret")
                }
            }
        }

        maven {
            name = "githubPackages"
            url = uri("${providers.gradleProperty("github.packages.url")}/$githubUser/${rootProject.name}")
            // environment variables
            credentials {
                username = githubUser
                password = if (System.getenv()
                        .containsKey("GITHUB_PASSWORD")
                ) {
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
        url.set("https://github.com/$githubUser/$name")

        licenses {
            license {
                name.set(providers.gradleProperty("license.name").get())
                url.set(providers.gradleProperty("license.url").get())
            }
        }

        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/$githubUser/$name/issues") // Change here
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
            connection.set("scm:git:git://github.com:$githubUser/$name.git")
            url.set("https://github.com/$githubUser/$name")
            developerConnection.set("scm:git:ssh://github.com:$githubUser/$name.git")
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
