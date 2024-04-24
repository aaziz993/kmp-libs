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

job("Code format check, analysis and publish") {
    startOn {
        gitPush { enabled = true }
    }

    container("Spotless code format check", "gradle") {
        kotlinScript { api ->
            api.gradlew("spotlessCheck")
        }
    }

    container("Sonar continuous inspection of code quality and security", "gradle") {
        env["SONAR_TOKEN"] = "{{ project:sonar.token }}"
        kotlinScript { api ->
            api.gradlew("sonar")
        }
    }

    parallel {
        container(
            "Publish to Space Packages",
            "aaziz93.registry.jetbrains.space/p/aaziz-93/containers/env-os:latest",
        ) {
            // The only way to get a secret in a shell script is an env variable
            env["SIGNING_GNUPG_PASSPHRASE"] = "{{ project:signing.gnupg.passphrase }}"
            shellScript {
                interpreter = "/bin/bash"
                content = """
                    make publish-space
                """
            }
        }

        container(
            "Publish to Maven Central",
            "aaziz93.registry.jetbrains.space/p/aaziz-93/containers/env-os:latest",
        ) {
            // The only way to get a secret in a shell script is an env variable
            env["SONATYPE_USERNAME"] = "{{ project:sonatype.username }}"
            env["SONATYPE_PASSWORD"] = "{{ project:sonatype.password }}"
            env["SIGNING_GNUPG_PASSPHRASE"] = "{{ project:signing.gnupg.passphrase }}"
            shellScript {
                interpreter = "/bin/bash"
                content = """
                    make publish-maven
                """
            }
        }
    }
}
