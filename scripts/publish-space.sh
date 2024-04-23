#!/bin/bash

echo PUBLISHING TO SPACE PACKAGES...
. ./gen-gpg-key.sh
. ./gradlew publishAllPublicationsToSpacePackagesRepository --no-configuration-cache
