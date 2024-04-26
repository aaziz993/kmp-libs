#!/bin/bash

. scripts/util.sh

gradle_properties_file="./gradle.properties"
local_properties_file="./local.properties"

export ORG_GRADLE_PROJECT_signingInMemoryKeyId
export ORG_GRADLE_PROJECT_signingInMemoryKey

export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword
ORG_GRADLE_PROJECT_signingInMemoryKeyPassword="${SIGNING_GNUPG_PASSPHRASE:=$(property "signing.gnupg.passphrase" "$local_properties_file")}"

# GPG VARIABLES PROVIDED IN ENVIRONMENT VARIABLES
if [[ -n "$SINGING_GNUGPG_KEYID" && -n "$SINGING_GNUGPG_KEY" ]];then
    ORG_GRADLE_PROJECT_signingInMemoryKeyId=SINGING_GNUGPG_KEYID
    ORG_GRADLE_PROJECT_signingInMemoryKey=SINGING_GNUGPG_KEY
else
    # GPG VARIABLES PROVIDED IN KEY FILE
    if [[ -n "$(property "signing.gnupg.key.file" "$gradle_properties_file")" ]];then
        echo GET IN FILE GPG KEY
            if [[ -f "$(property "signing.gnupg.key.file" "$gradle_properties_file")" ]]; then
                ORG_GRADLE_PROJECT_signingInMemoryKeyId="$(gpg --list-keys --keyid-format short "$(property "signing.gnupg.name.real" "$gradle_properties_file")" "$(property "signing.gnupg.key.file" "$gradle_properties_file")" | awk '$1 == "pub" { print $2 }' | cut -d'/' -f2)"
                ORG_GRADLE_PROJECT_signingInMemoryKey="$(gpg --pinentry-mode=loopback --passphrase="$ORG_GRADLE_PROJECT_signingInMemoryKeyPassword" --export-secret-keys --armor "$(property "signing.gnupg.name.real" "$gradle_properties_file")" "$(property "signing.gnupg.key.file" "$gradle_properties_file")" | grep -v '\-\-' | grep -v '^=.' | tr -d '\n')"
            fi
    # GPG VARIABLES PROVIDED IN MEMORY
    else
        echo GET IN MEMORY GPG KEY
        if [[ -z "$(gpg --list-keys)" ]]; then
            . gen-gpg-key.sh "$ORG_GRADLE_PROJECT_signingInMemoryKeyPassword"
        fi
        ORG_GRADLE_PROJECT_signingInMemoryKeyId="$(gpg --list-keys --keyid-format short "$(property "signing.gnupg.name.real" "$gradle_properties_file" )" | awk '$1 == "pub" { print $2 }' | cut -d'/' -f2)"
        ORG_GRADLE_PROJECT_signingInMemoryKey="$(gpg --pinentry-mode=loopback --passphrase="$ORG_GRADLE_PROJECT_signingInMemoryKeyPassword" --export-secret-keys --armor "$(property "signing.gnupg.name.real" "$gradle_properties_file" )" | grep -v '\-\-' | grep -v '^=.' | tr -d '\n')"
    fi
fi

