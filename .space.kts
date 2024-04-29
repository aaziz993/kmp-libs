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
import java.io.File
import java.util.*

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

    container("Read gradle.properties", "{{ jetbrains.space.automation.env.os }}") {
        kotlinScript { api ->
            // Do not use workDir to get the path to the working directory in a shellScript or kotlinScript.
            // Instead, use the JB_SPACE_WORK_DIR_PATH environment variable.
            File("${System.getenv("JB_SPACE_WORK_DIR_PATH")}/gradle.properties").let { file ->
                Properties().apply {
                    if (file.exists()) {
                        load(file.reader())
                    }
                }.entries.forEach {
                    println("${it.key}=${it.value}")
                    api.parameters[it.key.toString()] = it.value.toString()
                }
            }
        }
    }

    container("Spotless code format check", "{{ jetbrains.space.automation.env.os }}") {
        shellScript {
            content = "apt install -y make && make format-check"
        }
    }

    container("Sonar continuous inspection of code quality and security", "{{ jetbrains.space.automation.env.os }}") {
        env["SONAR_TOKEN"] = "{{ project:sonar.token }}"
        shellScript {
            content = "apt install -y make && make quality-check"
        }
    }

    container("Test", "{{ jetbrains.space.automation.env.os }}") {
        shellScript {
            content = "apt install -y make && make test"
        }
    }

    parallel {
        container(
            "Publish to Space Packages",
            "{{ jetbrains.space.automation.env.os }}",
        ) {
            // The only way to get a secret in a shell script is an env variable
            env["SINGING_GNUPG_KEY_ID"] = "{{ project:signing.gnupg.key.id }}"
            env["SIGNING_GNUPG_KEY_PASSPHRASE"] = "{{ project:signing.gnupg.key.passphrase }}"
            env["SINGING_GNUPG_KEY"] = "{{ project:signing.gnupg.key }}"
            shellScript {
                interpreter = "/bin/bash"
                content = """
                    make publish-space
                """
            }
        }

        container(
            "Publish to Maven Central",
            "{{ jetbrains.space.automation.env.os }}",
        ) {
            // The only way to get a secret in a shell script is an env variable
            env["SONATYPE_USERNAME"] = "{{ project:sonatype.username }}"
            env["SONATYPE_PASSWORD"] = "{{ project:sonatype.password }}"
            env["SINGING_GNUPG_KEY_ID"] = "{{ project:signing.gnupg.key.id }}"
            env["SIGNING_GNUPG_KEY_PASSPHRASE"] = "{{ project:signing.gnupg.key.passphrase }}"
            env["SINGING_GNUPG_KEY"] = "{{ project:signing.gnupg.key }}"
            shellScript {
                interpreter = "/bin/bash"
                content = """
                    make publish-maven
                """
            }
        }
    }
}
