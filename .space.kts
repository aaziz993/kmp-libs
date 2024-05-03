import java.io.File
import java.util.*

job("Code format check, quality check, test and publish") {
    startOn {
        // Run on every commit...
        gitPush {
            enabled = true
            // Only to the main branch
            anyRefMatching {
                +"refs/heads/main"
            }
        }
    }

    container("\uD83D\uDC40 Read gradle.properties", "amazoncorretto:17-alpine") {
        kotlinScript { api ->
            // Do not use workDir to get the path to the working directory in a shellScript or kotlinScript.
            // Instead, use the JB_SPACE_WORK_DIR_PATH environment variable.
            File("${System.getenv("JB_SPACE_WORK_DIR_PATH")}/gradle.properties").let { file ->
                Properties().apply {
                    if (file.exists()) {
                        load(file.reader())
                    }
                }.entries.forEach {
                    // 🖨️ Print gradle.properties
                    println("${it.key}=${it.value}")
                    api.parameters[it.key.toString()] = it.value.toString()
                }
            }
        }
    }

    container("\uD83D\uDD2C Spotless code format check", "{{ jetbrains.space.automation.run.env }}") {
        // The only way to get a secret in a shell script is an env variable
        env["JB_SPACE_GRADLE_BUILD_CACHE_USERNAME"] = System.getenv("JETBRAINS_SPACE_CLIENT_ID")
        env["JB_SPACE_GRADLE_BUILD_CACHE_PASSWORD"] = System.getenv("JETBRAINS_SPACE_CLIENT_SECRET")
        shellScript {
            interpreter = "/bin/bash"
            location = """
                if [[ "{{ jetbrains.space.automation.format.check.enable }}" == "true" ]; then
                    ./scripts/format-check.sh
                fi
            """.trimIndent()
        }
    }

    container("\uD83E\uDE7A Test and generate code coverage report with Kover", "{{ jetbrains.space.automation.run.env }}") {
        // The only way to get a secret in a shell script is an env variable
        env["JB_SPACE_GRADLE_BUILD_CACHE_USERNAME"] = System.getenv("JETBRAINS_SPACE_CLIENT_ID")
        env["JB_SPACE_GRADLE_BUILD_CACHE_PASSWORD"] = System.getenv("JETBRAINS_SPACE_CLIENT_SECRET")
        shellScript {
            interpreter = "/bin/bash"
            location = """
                if [[ "{{ jetbrains.space.automation.test.enable }}" == "true" ]; then
                    ./scripts/test.sh
                fi
            """.trimIndent()
        }
    }

    container("\uD83D\uDD2C Sonar continuous inspection of code quality and security", "{{ jetbrains.space.automation.run.env }}") {
        // The only way to get a secret in a shell script is an env variable
        env["JB_SPACE_GRADLE_BUILD_CACHE_USERNAME"] = System.getenv("JETBRAINS_SPACE_CLIENT_ID")
        env["JB_SPACE_GRADLE_BUILD_CACHE_PASSWORD"] = System.getenv("JETBRAINS_SPACE_CLIENT_SECRET")
        env["SONAR_TOKEN"] = "{{ project:sonar.token }}"
        shellScript {
            interpreter = "/bin/bash"
            content = """
                if [[ "{{ jetbrains.space.automation.quality.check.enable }}" == "true" ]; then
                    ./scripts/quality-check.sh
                fi
            """.trimIndent()
        }
    }

    parallel {
        container(
            "\uD83D\uDE80 Publish to Space Packages",
            "{{ jetbrains.space.automation.run.env }}",
        ) {
            // The only way to get a secret in a shell script is an env variable
            env["JB_SPACE_GRADLE_BUILD_CACHE_USERNAME"] = System.getenv("JETBRAINS_SPACE_CLIENT_ID")
            env["JB_SPACE_GRADLE_BUILD_CACHE_PASSWORD"] = System.getenv("JETBRAINS_SPACE_CLIENT_SECRET")
            env["JB_SPACE_RELEASES_USERNAME"] = System.getenv("JETBRAINS_SPACE_CLIENT_ID")
            env["JB_SPACE_RELEASES_PASSWORD"] = System.getenv("JETBRAINS_SPACE_CLIENT_SECRET")
            env["JB_SPACE_SNAPSHOTS_USERNAME"] = System.getenv("JETBRAINS_SPACE_CLIENT_ID")
            env["JB_SPACE_SNAPSHOTS_PASSWORD"] = System.getenv("JETBRAINS_SPACE_CLIENT_SECRET")
            env["SINGING_GNUPG_KEY_ID"] = "{{ project:signing.gnupg.key.id }}"
            env["SIGNING_GNUPG_KEY_PASSPHRASE"] = "{{ project:signing.gnupg.key.passphrase }}"
            env["SINGING_GNUPG_KEY"] = "{{ project:signing.gnupg.key }}"
            shellScript {
                interpreter = "/bin/bash"
                location = """
                     if [[ "{{ jetbrains.space.automation.publish.space.packages.enable }}" == "true" ]; then
                        ./scripts/publish-space-packages.sh
                     fi
                """.trimIndent()
            }
        }

        container(
            "\uD83D\uDE80 Publish to Maven Central",
            "{{ jetbrains.space.automation.run.env }}",
        ) {
            // The only way to get a secret in a shell script is an env variable
            env["JB_SPACE_GRADLE_BUILD_CACHE_USERNAME"] = System.getenv("JETBRAINS_SPACE_CLIENT_ID")
            env["JB_SPACE_GRADLE_BUILD_CACHE_PASSWORD"] = System.getenv("JETBRAINS_SPACE_CLIENT_SECRET")
            env["SONATYPE_RELEASES_USERNAME"] = "{{ project:sonatype.releases.username }}"
            env["SONATYPE_RELEASES_PASSWORD"] = "{{ project:sonatype.releases.password }}"
            env["SONATYPE_SNAPSHOTS_USERNAME"] = "{{ project:sonatype.snapshots.username }}"
            env["SONATYPE_SNAPSHOTS_PASSWORD"] = "{{ project:sonatype.snapshots.password }}"
            env["SINGING_GNUPG_KEY_ID"] = "{{ project:signing.gnupg.key.id }}"
            env["SIGNING_GNUPG_KEY_PASSPHRASE"] = "{{ project:signing.gnupg.key.passphrase }}"
            env["SINGING_GNUPG_KEY"] = "{{ project:signing.gnupg.key }}"
            shellScript {
                interpreter = "/bin/bash"
                location = """
                    if [[ "{{ jetbrains.space.automation.publish.maven.enable }}" == "true" ]; then
                        ./scripts/publish-maven.sh
                    fi
                """.trimIndent()
            }
        }
    }
}
