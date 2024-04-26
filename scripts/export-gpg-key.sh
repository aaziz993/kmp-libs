#!/bin/bash

. scripts/util.sh

gradle_properties_file="./gradle.properties"
local_properties_file="./local.properties"

export ORG_GRADLE_PROJECT_signingInMemoryKeyId
export ORG_GRADLE_PROJECT_signingInMemoryKey

export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword
ORG_GRADLE_PROJECT_signingInMemoryKeyPassword="${SIGNING_GNUPG_KEY_PASSPHRASE:=$(property "signing.gnupg.key.passphrase" "$local_properties_file")}"

# GPG VARIABLES PROVIDED IN ENVIRONMENT VARIABLES
if [[ -n "$SINGING_GNUPG_KEY_ID" && -n "$SINGING_GNUPG_KEY" ]];then
    ORG_GRADLE_PROJECT_signingInMemoryKeyId="$SINGING_GNUPG_KEY_ID"
    ORG_GRADLE_PROJECT_signingInMemoryKey="$SINGING_GNUPG_KEY"
else
    if [[ -z "$(gpg --list-keys)" ]]; then
        ./scripts/gen-gpg-key.sh "$ORG_GRADLE_PROJECT_signingInMemoryKeyPassword"
    fi
    ORG_GRADLE_PROJECT_signingInMemoryKeyId="$(gpg --list-keys --keyid-format short "$(property "signing.gnupg.name.real" "$gradle_properties_file" )" | awk '$1 == "pub" { print $2 }' | cut -d'/' -f2)"
    ORG_GRADLE_PROJECT_signingInMemoryKey="$(gpg --pinentry-mode=loopback --passphrase="$ORG_GRADLE_PROJECT_signingInMemoryKeyPassword" --export-secret-keys --armor "$ORG_GRADLE_PROJECT_signingInMemoryKeyId" | grep -v '\-\-' | grep -v '^=.' | tr -d '\n')"
fi

echo SIGNING KEY ID = "$ORG_GRADLE_PROJECT_signingInMemoryKeyId"
echo SIGNING KEY PASSPHRASE =  "$ORG_GRADLE_PROJECT_signingInMemoryKeyPassword"
echo SIGNING KEY = "$ORG_GRADLE_PROJECT_signingInMemoryKey"

