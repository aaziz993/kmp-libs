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
import kotlin.io.path.fileVisitor
import space.jetbrains.api.runtime.resources.projects.Params
import space.jetbrains.api.runtime.resources.projects.automation.stepExecutions.usedParameters.Param

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

    var enable = false

    container("Read gradle.properties", "amazoncorretto:17-alpine") {
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
            enable = api.parameters["jetbrains.space.automation.enable"].toBoolean()
        }
    }

    println("ENABLED: $enable")
//
//    container("Spotless code format check", "{{ jetbrains.space.automation.run.env }}") {
//        shellScript {
//            interpreter = "/bin/bash"
//            location = "./scripts/format-check.sh"
//        }
//    }
//
//    container("Test and generate code coverage report with Kover", "{{ jetbrains.space.automation.run.env }}") {
//        shellScript {
//            interpreter = "/bin/bash"
//            location = "./scripts/test.sh"
//        }
//    }
//
//    container("Sonar continuous inspection of code quality and security", "{{ jetbrains.space.automation.run.env }}") {
//        env["SONAR_TOKEN"] = "{{ project:sonar.token }}"
//        shellScript {
//            interpreter = "/bin/bash"
//            content = "./scripts/quality-check.sh"
//        }
//    }
//
//    parallel {
//        container(
//            "Publish to Space Packages",
//            "{{ jetbrains.space.automation.run.env }}",
//        ) {
//            // The only way to get a secret in a shell script is an env variable
//            env["JB_SPACE_RELEASES_USERNAME"] = System.getenv("JETBRAINS_SPACE_CLIENT_ID")
//            env["JB_SPACE_RELEASES_PASSWORD"] = System.getenv("JETBRAINS_SPACE_CLIENT_SECRET")
//            env["JB_SPACE_SNAPSHOTS_USERNAME"] = System.getenv("JETBRAINS_SPACE_CLIENT_ID")
//            env["JB_SPACE_SNAPSHOTS_PASSWORD"] = System.getenv("JETBRAINS_SPACE_CLIENT_SECRET")
//            env["SINGING_GNUPG_KEY_ID"] = "{{ project:signing.gnupg.key.id }}"
//            env["SIGNING_GNUPG_KEY_PASSPHRASE"] = "{{ project:signing.gnupg.key.passphrase }}"
//            env["SINGING_GNUPG_KEY"] = "{{ project:signing.gnupg.key }}"
//            shellScript {
//                interpreter = "/bin/bash"
//                location = "./scripts/publish-space.sh"
//            }
//        }
//
//        container(
//            "Publish to Maven Central",
//            "{{ jetbrains.space.automation.run.env }}",
//        ) {
//            // The only way to get a secret in a shell script is an env variable
//            env["SONATYPE_RELEASES_USERNAME"] = "{{ project:sonatype.releases.username }}"
//            env["SONATYPE_RELEASES_PASSWORD"] = "{{ project:sonatype.releases.password }}"
//            env["SONATYPE_SNAPSHOTS_USERNAME"] = "{{ project:sonatype.snapshots.username }}"
//            env["SONATYPE_SNAPSHOTS_PASSWORD"] = "{{ project:sonatype.snapshots.password }}"
//            env["SINGING_GNUPG_KEY_ID"] = "{{ project:signing.gnupg.key.id }}"
//            env["SIGNING_GNUPG_KEY_PASSPHRASE"] = "{{ project:signing.gnupg.key.passphrase }}"
//            env["SINGING_GNUPG_KEY"] = "{{ project:signing.gnupg.key }}"
//            shellScript {
//                interpreter = "/bin/bash"
//                location = "./scripts/publish-maven.sh"
//            }
//        }
//    }
}
