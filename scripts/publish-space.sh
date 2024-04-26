#!/bin/bash

. scripts/export-gpg-key.sh

echo PUBLISHING TO SPACE PACKAGES...

./gradlew publishAllPublicationsToSpacePackagesRepository --no-configuration-cache
