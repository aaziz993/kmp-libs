#!/bin/bash

cd ../

# array declaration
declare -A properties

readPropertyFile () {
  if [[ -f "$1" ]]; then
  # Read with:
  # IFS (Field Separator) =
  # -d (Record separator) newline
  # first field before separator as k (key)
  # second field after separator and reminder of record as v (value)
  while IFS='=' read -d $'\n' -r k v; do
    # Skip lines starting with sharp
    # or lines containing only space or empty lines
    [[ "$k" =~ ^([[:space:]]*|[[:space:]]*#.*)$ ]] && continue
    # Store key value into assoc array
    properties[$k]="$(echo "$v" | sed ':a;N;$!ba;s/\n//g')"
    # stdin the properties file
  done < "$1"
  fi
}

readPropertyFile "./gradle.properties"
readPropertyFile "./local.properties"

export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword
ORG_GRADLE_PROJECT_signingInMemoryKeyPassword="${SIGNING_GNUPG_PASSPHRASE:=${properties["signing.gnupg.passphrase"]}}"

# SIGNING
if [[ -n $(gpg --list-keys) ]]; then
  echo "Gpg key already exists"
else
gpg --gen-key --batch << EOF
Key-Type:${properties["signing.gnupg.key.type"]}
Key-Length:${properties["signing.gnupg.key.length"]}
Subkey-Type:${properties["signing.gnupg.subkey.type"]}
Subkey-Length:${properties["signing.gnupg.subkey.length"]}
Name-Real:${properties["signing.gnupg.name.real"]}
Name-Comment:${properties["signing.gnupg.name.comment"]}
Name-Email:${properties["signing.gnupg.name.email"]}
Expire-Date:${properties["signing.gnupg.expire.date"]}
Passphrase:$ORG_GRADLE_PROJECT_signingInMemoryKeyPassword
%commit
%echo done
EOF
fi
export ORG_GRADLE_PROJECT_signingInMemoryKeyId
export ORG_GRADLE_PROJECT_signingInMemoryKey
ORG_GRADLE_PROJECT_signingInMemoryKeyId="$(gpg --list-keys --keyid-format short "${properties["signing.gnupg.name.real"]}" | awk '$1 == "pub" { print $2 }' | cut -d'/' -f2)"
ORG_GRADLE_PROJECT_signingInMemoryKey="$(gpg --pinentry-mode=loopback --passphrase="$ORG_GRADLE_PROJECT_signingInMemoryKeyPassword" --export-secret-keys --armor "${properties["signing.gnupg.name.real"]}" | grep -v '\-\-' | grep -v '^=.' | tr -d '\n')"

export ORG_GRADLE_PROJECT_mavenCentralUsername
export ORG_GRADLE_PROJECT_mavenCentralPassword
ORG_GRADLE_PROJECT_mavenCentralUsername="${SONATYPE_USERNAME:=${properties["sonatype.username"]}}"
ORG_GRADLE_PROJECT_mavenCentralPassword="${SONATYPE_PASSWORD:=${properties["sonatype.password"]}}"
