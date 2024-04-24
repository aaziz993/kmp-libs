# Gradle Command-Line

## Base project info

* Display all submodules `./gradlew projects`

* Different tasks for current module `./gradlew tasks`

## List available build parameters

* List all build parameters `./gradlew parameters`

## Cleaning build directories

Technically `clean` should not be required, every time it is required it might be a bug.
However, it might be useful to perform a "clean" build:

* Cleans current project (submodule) `./gradlew clean`

* Cleans the specified project `./gradlew :src:core:clean`

## Dependencies

* Displays dependencies. Gradle's "configurations" are something like different classpaths. `./gradlew dependencies`

* Displays dependencies for all projects `./gradlew allDependencies`

* Analyze why the project depends on `org.ow2.asm:asm` `./gradlew dependencyInsight --dependency org.ow2.asm:asm`


#### Fine Grained Formatting Commands

* Run checkstyle for main (non-test) code `./gradlew checkstyleMain`
* 
* Run checkstyle for test code `./gradlew checkstyleTest`

* Run Spotless checks `./gradlew spotlessCheck`

* Fix any issues found by Spotless `./gradlew spotlessApply`

## Build Project

* Just build jar (see build/libs/*.jar) `./gradlew jar`

* "build" is a default task to "execute all the actions" `./gradlew build`

* Test might be skipped by `-x test` (Gradle's default way to skip task by name) `./gradlew -x test build`

* Build project in parallel `./gradlew build --parallel`

## Tests

Gradle automatically tracks task dependencies, so if you modify a file in `/src/*/*`,
then you can invoke `./gradlew check` at project level or in `core`, and Gradle will automatically
build only the required jars and files.

* Runs all the tests (unit tests, checkstyle, etc) `./gradlew check`

* Runs just unit tests `./gradlew test`

* Runs just core tests `./gradlew :src:core:test`

## Coverage

* Generates code coverage report for the test task to build/reports/jacoco/test/html `./gradlew jacocoTestReport -Pcoverage`

* Generate combined coverage report `./gradlew jacocoReport -Pcoverage`

## Generate Javadocs

* Builds javadoc to build/docs/javadoc subfolder `./gradlew javadoc`

* Builds javadoc jar to build/libs/jorphan-javadoc.jar `./gradlew javadocJar`

## Release Artifacts

* Builds ZIP and TGZ artifacts for the release `./gradlew :src:dist:assemble`
