#!/bin/bash

echo PUBLISHING TO SPACE PACKAGES...

. scripts/export-gpg-key.sh

./gradlew publishAllPublicationsToSpacePackagesRepository --no-configuration-cache
