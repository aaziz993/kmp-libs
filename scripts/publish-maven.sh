#!/bin/bash

echo PUBLISHING TO MAVEN...
. ./gen-gpg-key.sh
export GPG KeyId "$ORG_GRADLE_PROJECT_signingInMemoryKeyId"
export GPG Key "$ORG_GRADLE_PROJECT_signingInMemoryKey"
export Sonatype Username "$ORG_GRADLE_PROJECT_mavenCentralUsername"
export Sonatype Password "$ORG_GRADLE_PROJECT_mavenCentralPassword"
#. ./gradlew publishAndReleaseToMavenCentral --no-configuration-cache
