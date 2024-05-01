#!/bin/bash

. scripts/util.sh

. scripts/export-gpg-key.sh

gradle_properties_file="./gradle.properties"
local_properties_file="./local.properties"

echo PUBLISHING TO MAVEN...

export ORG_GRADLE_PROJECT_mavenCentralUsername
export ORG_GRADLE_PROJECT_mavenCentralPassword

if [[ "$(property "project.version.snapshot" "$gradle_properties_file")" == "true" ]]; then
ORG_GRADLE_PROJECT_mavenCentralUsername="${SONATYPE_USERNAME:=$(property "sonatype.snapshots.username" "$local_properties_file")}"
ORG_GRADLE_PROJECT_mavenCentralPassword="${SONATYPE_PASSWORD:=$(property "sonatype.snapshots.password" "$local_properties_file")}"
else
ORG_GRADLE_PROJECT_mavenCentralUsername="${SONATYPE_USERNAME:=$(property "sonatype.releases.username" "$local_properties_file")}"
ORG_GRADLE_PROJECT_mavenCentralPassword="${SONATYPE_PASSWORD:=$(property "sonatype.releases.password" "$local_properties_file")}"
fi

./gradlew publishAndReleaseToMavenCentral --no-configuration-cache
