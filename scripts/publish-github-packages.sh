#!/bin/bash

echo Publish to GitHub Packages

. scripts/export-gpg-key.sh

./gradlew publishAllPublicationsToGithubPackagesRepository --no-configuration-cache
