![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)

[![official project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)

![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)

![GitHub Actions](https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white)

![SonarQube](https://img.shields.io/badge/SonarQube-black?style=for-the-badge&logo=sonarqube&logoColor=4E9BCD)

![Apache Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)

# Kotlin Multiplatform Project Libraries.

# Publish to Github Packages, Space Packages and Maven Central.

## How do I publish it manually?
1. Install ```gpg```
2. Install ```make```.  See [Makefile](Makefile) publish commands.
3. Run ```make publish-space``` to publish to Space Packages.
4. Run ```make publish-github``` to publish to Github Packages.
5. Run ```make publish-maven``` to publish to Maven Central.
6. Run ```make publish``` to publish to Space Packages, Github Packages and Maven Central.

## How do I publish it with CI/CD?
### Github Setup
_1. Share project to Github.
__1.1. sdf 
- ### Jetbrains Space Setup
1. - In <b>Github Actions</b> add ```sonar_token and signing_gnupg_passphrase``` secrets in Repository -> Settings -> Security -> Secrets and variables -> Actions -> New repository secret. It publishes to Github Packages. See [publish.yml](.github/workflows/publish.yml) publish script.
2. - In <b>Jetbrains Space Automation</b> add ```sonar.token, signing.gnupg.passphrase, sonatype.username and sonatype.password``` secrets in Project -> Settings -> Secrets & Parameters -> Create -> Secret. It publishes to Space Packages and Maven Central. See [.space.kts](.space.kts) publish script.
