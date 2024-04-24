#!/bin/bash

echo PUBLISHING TO MAVEN...
. ./gen-gpg-key.sh
echo "$ORG_GRADLE_PROJECT_signingInMemoryKey"
#. ./gradlew publishAndReleaseToMavenCentral --no-configuration-cache
