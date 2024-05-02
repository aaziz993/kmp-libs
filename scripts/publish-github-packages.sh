#!/bin/bash

echo Publish to Github Packages

. scripts/export-gpg-key.sh

./gradlew publishAllPublicationsToGithubPackagesRepository --no-configuration-cache
