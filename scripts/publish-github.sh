#!/bin/bash

echo PUBLISHING TO GITHUB PACKAGES...

. scripts/export-gpg-key.sh

./gradlew publishAllPublicationsToGithubPackagesRepository --no-configuration-cache --stacktrace

