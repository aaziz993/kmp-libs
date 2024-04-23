#!/bin/bash

echo PUBLISHING TO GITHUB PACKAGES...
. ./gen-gpg-key.sh
. ./gradlew publishAllPublicationsToGithubPackagesRepository --no-configuration-cache