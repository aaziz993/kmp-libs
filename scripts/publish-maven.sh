#!/bin/bash

echo PUBLISHING TO MAVEN...

. scripts/util.sh

. scripts/export-gpg-key.sh

gradle_properties_file="./gradle.properties"
local_properties_file="./local.properties"

version_infix="$(
        [ "$(property "project.version.snapshot" "$gradle_properties_file")" == "true" ] &&
        echo "snapshots" ||
        echo "releases"
    )"

sonatype_username_env_var_name=SONATYPE_${version_infix^^}_USERNAME
sonatype_password_env_var_name=SONATYPE_${version_infix^^}_PASSWORD

export ORG_GRADLE_PROJECT_mavenCentralUsername
export ORG_GRADLE_PROJECT_mavenCentralPassword
ORG_GRADLE_PROJECT_mavenCentralUsername="${!sonatype_username_env_var_name:=$(property "sonatype.$version_infix.username" "$local_properties_file")}"
ORG_GRADLE_PROJECT_mavenCentralPassword="${!sonatype_password_env_var_name:=$(property "sonatype.$version_infix.password" "$local_properties_file")}"

./gradlew publishAndReleaseToMavenCentral --no-configuration-cache
