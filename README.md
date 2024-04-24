[![official project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)

# Kotlin Multiplatform Project

## What is it?

It is the barebones library project intended to quickly bootstrap a Kotlin Multiplatform project, that is deployable to Space Packages, Github Packages and Maven Central.

## How do I publish it locally?
1. - Install ```make```.  See [Makefile](Makefile) publish commands.
2. - Run ```make publish-space``` to publish to Space Packages
3. - Run ```make publish-github``` to publish to Github Packages
4. - Run ```make publish-maven``` to publish to Maven Central
5. - Run ```make publish``` to publish to Space Packages, Github Packages and Maven Central

## How do I publish it with CI/CD?
1. - With <b>Github Actions</b> add ```sonar_token and signing_gnupg_passphrase``` secrets in Repository -> Settings -> Security -> Secrets and variables -> Actions -> New repository secret. See [publish.yml](.github/workflows/publish.yml) publish script.
2. - With <b>Jetbrains Space Automation</b> add ```sonar.token, signing.gnupg.passphrase, sonatype.username and sonatype.password``` secrets in Project -> Settings -> Secrets & Parameters -> Create -> Secret.  See [.space.kts](.space.kts) publish script.
