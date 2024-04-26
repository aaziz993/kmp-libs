#!/bin/bash

. scripts/util.sh

. scripts/export-gpg-key.sh

local_properties_file="./local.properties"

echo PUBLISHING TO MAVEN...

export ORG_GRADLE_PROJECT_mavenCentralUsername
export ORG_GRADLE_PROJECT_mavenCentralPassword

if [[ -n "$SONATYPE_USERNAME" ]]; then
    ORG_GRADLE_PROJECT_mavenCentralUsername="$SONATYPE_USERNAME"
else
    ORG_GRADLE_PROJECT_mavenCentralUsername="$(property "sonatype.username" "$local_properties_file")"
fi

if [[ -n "$SONATYPE_PASSWORD" ]]; then
    ORG_GRADLE_PROJECT_mavenCentralPassword="$SONATYPE_PASSWORD"
else
    ORG_GRADLE_PROJECT_mavenCentralPassword="$(property "sonatype.password" "$local_properties_file")"
fi

./gradlew publishAndReleaseToMavenCentral --no-configuration-cache
