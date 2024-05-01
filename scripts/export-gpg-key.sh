#!/bin/bash

. scripts/util.sh

local_properties_file="./local.properties"

export ORG_GRADLE_PROJECT_signingInMemoryKeyId
export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword
export ORG_GRADLE_PROJECT_signingInMemoryKey

ORG_GRADLE_PROJECT_signingInMemoryKeyId="${SIGNING_GNUPG_KEY_ID:=$(property "signing.gnupg.key.id" "$local_properties_file")}"
ORG_GRADLE_PROJECT_signingInMemoryKeyPassword="${SIGNING_GNUPG_KEY_PASSPHRASE:=$(property "signing.gnupg.key.passphrase" "$local_properties_file")}"
ORG_GRADLE_PROJECT_signingInMemoryKey="${SIGNING_GNUPG_KEY:=$(property "signing.gnupg.key" "$local_properties_file")}"

if ! is_gpg_key_in_keyserver keyserver.ubuntu.com "$ORG_GRADLE_PROJECT_signingInMemoryKeyId"; then
   gpg --keyserver keyserver.ubuntu.com --send-keys "$ORG_GRADLE_PROJECT_signingInMemoryKeyId"
fi

if ! is_gpg_key_in_keyserver keys.openpgp.org "$ORG_GRADLE_PROJECT_signingInMemoryKeyId"; then
   gpg --keyserver keys.openpgp.org --send-keys "$ORG_GRADLE_PROJECT_signingInMemoryKeyId"
fi

if ! is_gpg_key_in_keyserver pgp.mit.edu "$ORG_GRADLE_PROJECT_signingInMemoryKeyId"; then
   gpg --keyserver pgp.mit.edu --send-keys "$ORG_GRADLE_PROJECT_signingInMemoryKeyId"
fi
