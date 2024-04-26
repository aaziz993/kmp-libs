#!/bin/bash

. scripts/util.sh

. scripts/export-gpg-key.sh

local_properties_file="./local.properties"

echo PUBLISHING TO MAVEN...

export ORG_GRADLE_PROJECT_mavenCentralUsername
export ORG_GRADLE_PROJECT_mavenCentralPassword
ORG_GRADLE_PROJECT_mavenCentralUsername="${SONATYPE_USERNAME:=$(property "sonatype.username" "$local_properties_file")}"
ORG_GRADLE_PROJECT_mavenCentralPassword="${SONATYPE_PASSWORD:=$(property "sonatype.password" "$local_properties_file")}"

./gradlew publishAndReleaseToMavenCentral --no-configuration-cache
