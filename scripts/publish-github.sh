#!/bin/bash

. scripts/export-gpg-key.sh

echo PUBLISHING TO GITHUB PACKAGES...

./gradlew publishAllPublicationsToGithubPackagesRepository --no-configuration-cache
