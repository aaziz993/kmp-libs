#!/bin/bash

. scripts/util.sh

gradle_properties_file="./gradle.properties"
local_properties_file="./local.properties"

if [[ -z "$(gpg --list-keys)" ]]; then
gpg --gen-key --batch << EOF
Key-Type:$(property "signing.gnupg.key.type" "$gradle_properties_file")
Key-Length:$(property "signing.gnupg.key.length" "$gradle_properties_file" )
Subkey-Type:$(property "signing.gnupg.subkey.type" "$gradle_properties_file" )
Subkey-Length:$(property "signing.gnupg.subkey.length" "$gradle_properties_file" )
Name-Real:$(property "signing.gnupg.name.real" "$gradle_properties_file" )
Name-Comment:$(property "signing.gnupg.name.comment" "$gradle_properties_file" )
Name-Email:$(property "signing.gnupg.name.email" "$gradle_properties_file" )
Expire-Date:$(property "signing.gnupg.expire.date" "$gradle_properties_file" )
Passphrase:$(property "signing.gnupg.key.passphrase" "$local_properties_file" )
%commit
%echo done
EOF
fi

echo GPG short key-id:"$(gpg_short_key_id "$(property "signing.gnupg.name.real" "$gradle_properties_file" )")"
echo GPG long key-id:"$(gpg_long_key_id "$(property "signing.gnupg.name.real" "$gradle_properties_file" )")"
echo GPG key:"$(gpg_key "$(property "signing.gnupg.name.real" "$gradle_properties_file" )" \
                        "$(property "signing.gnupg.key.passphrase" "$local_properties_file" )")"

if [[ -n "$2" ]];then
    gpg --pinentry-mode=loopback --passphrase="$(property "signing.gnupg.key.passphrase" "$local_properties_file" )" \
    --output "$2" --armor --export-secret-key "$(property "signing.gnupg.name.real" "$gradle_properties_file" )"
fi
