#!/bin/bash

. scripts/util.sh

gradle_properties_file="./gradle.properties"

gpg --gen-key --batch << EOF
Key-Type:$(property "signing.gnupg.key.type" "$gradle_properties_file")
Key-Length:$(property "signing.gnupg.key.length" "$gradle_properties_file" )
Subkey-Type:$(property "signing.gnupg.subkey.type" "$gradle_properties_file" )
Subkey-Length:$(property "signing.gnupg.subkey.length" "$gradle_properties_file" )
Name-Real:$(property "signing.gnupg.name.real" "$gradle_properties_file" )
Name-Comment:$(property "signing.gnupg.name.comment" "$gradle_properties_file" )
Name-Email:$(property "signing.gnupg.name.email" "$gradle_properties_file" )
Expire-Date:$(property "signing.gnupg.expire.date" "$gradle_properties_file" )
Passphrase:$1
%commit
%echo done
EOF

if [[ -n "$2" ]];then
gpg --pinentry-mode=loopback --passphrase="$1" --output "$2" --armor --export-secret-key "$(property "signing.gnupg.name.real" "$gradle_properties_file" )"
fi
