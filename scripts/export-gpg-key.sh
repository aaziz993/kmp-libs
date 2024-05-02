#!/bin/bash

. scripts/util.sh

local_properties_file="./local.properties"

gpg_key_id="${SIGNING_GNUPG_KEY_ID:=$(property "signing.gnupg.key.id" "$local_properties_file")}"

export ORG_GRADLE_PROJECT_signingInMemoryKeyId
export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword
export ORG_GRADLE_PROJECT_signingInMemoryKey

ORG_GRADLE_PROJECT_signingInMemoryKeyId="${gpg_key_id: -8}"
ORG_GRADLE_PROJECT_signingInMemoryKeyPassword="${SIGNING_GNUPG_KEY_PASSPHRASE:=$(property "signing.gnupg.key.passphrase" "$local_properties_file")}"
ORG_GRADLE_PROJECT_signingInMemoryKey="${SIGNING_GNUPG_KEY:=$(property "signing.gnupg.key" "$local_properties_file")}"

if ! is_gpg_key_in_keyserver keyserver.ubuntu.com "$gpg_key_id"; then
   gpg --keyserver keyserver.ubuntu.com --send-keys "$gpg_key_id"
fi

if ! is_gpg_key_in_keyserver keys.openpgp.org "$gpg_key_id"; then
   gpg --keyserver keys.openpgp.org --send-keys "$gpg_key_id"
fi

if ! is_gpg_key_in_keyserver pgp.mit.edu "$gpg_key_id"; then
   gpg --keyserver pgp.mit.edu --send-keys "$gpg_key_id"
fi
