/**
 * JetBrains Space Automation
 * This Kotlin-script file lets you automate build activities
 * For more info, see https://www.jetbrains.com/help/space/automation.html
 */

job("Code analysis, clean, test, build and publish") {
    startOn {
        gitPush { enabled = true }
    }

    container(
        "Sonar continuous inspection of code quality and security",
        "aaziz93.registry.jetbrains.space/p/aaziz-93/containers/env-os:latest"
    ) {
        env["SONAR_TOKEN"] = "{{ project:sonar_token }}"
        kotlinScript { api ->
            api.gradlew("sonar")
        }
    }

    parallel {
        container(
            "Gradle test, build and publish to Space Packages",
            "aaziz93.registry.jetbrains.space/p/aaziz-93/containers/env-os:latest"
        ) {

            shellScript {
                interpreter = "/bin/bash"
                content = """
                    chmod 777 -R scripts/
                    make publish-space
                """
            }
        }

        container(
            "Gradle test, build and publish to Maven Central",
            "aaziz93.registry.jetbrains.space/p/aaziz-93/containers/env-os:latest"
        ) {
            shellScript {
                interpreter = "/bin/bash"
                content = """
                    chmod 777 -R scripts/
                    make publish-maven
                """
            }
        }
    }
}