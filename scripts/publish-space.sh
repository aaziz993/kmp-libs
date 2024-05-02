#!/bin/bash

echo Publish to Space Packages

. scripts/export-gpg-key.sh

./gradlew publishAllPublicationsToSpacePackagesRepository --no-configuration-cache
