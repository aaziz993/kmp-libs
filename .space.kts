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

job("Code format, analysis and publish") {
    startOn {
        gitPush { enabled = true }
    }

//    container(
//        "Sonar continuous inspection of code quality and security",
//        "aaziz93.registry.jetbrains.space/p/aaziz-93/containers/env-os:latest",
//    ) {
//        env["SONAR_TOKEN"] = "{{ project:sonar_token }}"
//        kotlinScript { api ->
//            api.gradlew("sonar")
//        }
//    }
//
//    container(
//        "Spotless code format",
//        "aaziz93.registry.jetbrains.space/p/aaziz-93/containers/env-os:latest",
//    ) {
//        kotlinScript { api ->
//            api.gradlew("spotlessApply")
//        }
//    }

//    parallel {
//        container(
//            "Gradle test, build and publish to Space Packages",
//            "aaziz93.registry.jetbrains.space/p/aaziz-93/containers/env-os:latest",
//        ) {
//
//            shellScript {
//                interpreter = "/bin/bash"
//                content = """
//                    make publish-space
//                """
//            }
//        }

    container(
        "Gradle test, build and publish to Maven Central",
        "aaziz93.registry.jetbrains.space/p/aaziz-93/containers/env-os:latest",
    ) {
        env["SONATYPE_PASSWORD"] = "{{ project:SONATYPE_PASSWORD }}"
        shellScript {
            interpreter = "/bin/bash"
            content = """
                    make publish-maven
                """
        }
    }
//    }
}
