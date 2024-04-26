#!/bin/bash

. scripts/util.sh

gradle_properties_file="./gradle.properties"
local_properties_file="./local.properties"

export ORG_GRADLE_PROJECT_signingInMemoryKeyId
export ORG_GRADLE_PROJECT_signingInMemoryKey

export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword
ORG_GRADLE_PROJECT_signingInMemoryKeyPassword="${SIGNING_GNUPG_PASSPHRASE:=$(property "signing.gnupg.passphrase" "$local_properties_file")}"

# GPG VARIABLES PROVIDED IN ENVIRONMENT VARIABLES
if [[ -n "$SINGING_GNUGPG_KEY_ID" && -n "$SINGING_GNUGPG_KEY" ]];then
    ORG_GRADLE_PROJECT_signingInMemoryKeyId=SINGING_GNUGPG_KEY_ID
    ORG_GRADLE_PROJECT_signingInMemoryKey=SINGING_GNUGPG_KEY
else

    signing_gnupg_key_file="${SIGNING_GNUPG_FILE:=$(property "signing.gnupg.key.file" "$gradle_properties_file")}"
    # GPG VARIABLES PROVIDED IN KEY FILE
    if [[ -n "$signing_gnupg_key_file" ]];then
        echo GET IN FILE GPG KEY
            if [[ -f "$signing_gnupg_key_file" ]]; then
                ORG_GRADLE_PROJECT_signingInMemoryKeyId="$(gpg --list-keys --keyid-format short "$(property "signing.gnupg.name.real" "$gradle_properties_file")" "$(property "signing.gnupg.key.file" "$gradle_properties_file")" | awk '$1 == "pub" { print $2 }' | cut -d'/' -f2)"
                ORG_GRADLE_PROJECT_signingInMemoryKey="$(gpg --pinentry-mode=loopback --passphrase="$ORG_GRADLE_PROJECT_signingInMemoryKeyPassword" --export-secret-keys --armor "$ORG_GRADLE_PROJECT_signingInMemoryKeyId" "$(property "signing.gnupg.key.file" "$gradle_properties_file")" | grep -v '\-\-' | grep -v '^=.' | tr -d '\n')"
            fi
    # GPG VARIABLES PROVIDED IN MEMORY
    else
        echo GET IN MEMORY GPG KEY
        if [[ -z "$(gpg --list-keys)" ]]; then
            . gen-gpg-key.sh "$ORG_GRADLE_PROJECT_signingInMemoryKeyPassword"
        fi
        ORG_GRADLE_PROJECT_signingInMemoryKeyId="$(gpg --list-keys --keyid-format short "$(property "signing.gnupg.name.real" "$gradle_properties_file" )" | awk '$1 == "pub" { print $2 }' | cut -d'/' -f2)"
        ORG_GRADLE_PROJECT_signingInMemoryKey="$(gpg --pinentry-mode=loopback --passphrase="$ORG_GRADLE_PROJECT_signingInMemoryKeyPassword" --export-secret-keys --armor "$ORG_GRADLE_PROJECT_signingInMemoryKeyId" | grep -v '\-\-' | grep -v '^=.' | tr -d '\n')"
    fi
fi

