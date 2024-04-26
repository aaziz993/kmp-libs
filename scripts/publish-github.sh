#!/bin/bash

. scripts/export-gpg-key.sh

echo PUBLISHING TO GITHUB PACKAGES... "$SIGNING_GNUPG_KEY_PASSPHRASE"

./gradlew publishAllPublicationsToGithubPackagesRepository --no-configuration-cache
