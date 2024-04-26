#!/bin/bash

gpg --list-keys --with-colons | awk -F: '$1 == "fpr"  { print $10 }' | xargs gpg --batch --yes --delete-secret-keys &&
gpg --list-keys --with-colons | awk -F: '$1 == "pub"  { print $5 }' | xargs gpg --batch --yes --delete-keys
