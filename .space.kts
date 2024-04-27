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
    // Users will be able to redefine these parameters in custom job run.
    // See the 'Customize job run' section
    parameters {
        text("env.os", value = "aaziz93.registry.jetbrains.space/p/aaziz-93/containers/env-os:latest")
        text("gradlew.option", value = "--no-configuration-cache")
    }

    startOn {
        gitPush { enabled = true }
    }

    container("TEST", "ubuntu") {
        env["SINGING_GNUPG_KEY_ID"] = "{{ project:signing.gnupg.key.id }}"
        env["SIGNING_GNUPG_KEY_PASSPHRASE"] = "{{ project:signing.gnupg.key.passphrase }}"
        env["SINGING_GNUPG_KEY"] = "{{ project:signing.gnupg.key }}"
        shellScript {
            interpreter = "/bin/bash"
            content = """
                echo 234324${'$'}{SINGING_GNUPG_KEY_ID}345345
                if [ ${'$'}SINGING_GNUPG_KEY_ID != "2923E8CD3" ]; then
                    echo "Wrong secret!"
                    else
                    echo "True secret!"
                fi
                """
        }
    }

//    container(
//        "Spotless code format check",
//        "{{ env.os }}",
//    ) {
//        kotlinScript { api ->
//            api.gradlew("spotlessCheck", "{{ gradlew.option }}")
//        }
//    }
//
//    container(
//        "Sonar continuous inspection of code quality and security",
//        "{{ env.os }}",
//    ) {
//        env["SONAR_TOKEN"] = "{{ project:sonar.token }}"
//        kotlinScript { api ->
//            api.gradlew("sonar", "{{ gradlew.option }}")
//        }
//    }
//
//    parallel {
//        container(
//            "Publish to Space Packages",
//            "{{ env.os }}",
//        ) {
//            // The only way to get a secret in a shell script is an env variable
//            env["SINGING_GNUPG_KEY_ID"] = "{{ project:signing.gnupg.key.id }}"
//            env["SIGNING_GNUPG_KEY_PASSPHRASE"] = "{{ project:signing.gnupg.key.passphrase }}"
//            env["SINGING_GNUPG_KEY"] = "{{ project:signing.gnupg.key }}"
//            shellScript {
//                interpreter = "/bin/bash"
//                content = """
//                    make publish-space
//                """
//            }
//        }
//
//        container(
//            "Publish to Maven Central",
//            "{{ env.os }}",
//        ) {
//            // The only way to get a secret in a shell script is an env variable
//            env["SONATYPE_USERNAME"] = "{{ project:sonatype.username }}"
//            env["SONATYPE_PASSWORD"] = "{{ project:sonatype.password }}"
//            env["SINGING_GNUPG_KEY_ID"] = "{{ project:signing.gnupg.key.id }}"
//            env["SIGNING_GNUPG_KEY_PASSPHRASE"] = "{{ project:signing.gnupg.key.passphrase }}"
//            env["SINGING_GNUPG_KEY"] = "{{ project:signing.gnupg.key }}"
//            shellScript {
//                interpreter = "/bin/bash"
//                content = """
//                    make publish-maven
//                """
//            }
//        }
//    }
}
