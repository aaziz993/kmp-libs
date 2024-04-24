#!/bin/bash

echo PUBLISHING TO MAVEN...
. ./gen-gpg-key.sh
echo GPG KeyId "$ORG_GRADLE_PROJECT_signingInMemoryKeyId"
echo GPG Key "$ORG_GRADLE_PROJECT_signingInMemoryKey"
echo Sonatype Username "$ORG_GRADLE_PROJECT_mavenCentralUsername"
echo Sonatype Password "$ORG_GRADLE_PROJECT_mavenCentralPassword"
#. ./gradlew publishAndReleaseToMavenCentral --no-configuration-cache
