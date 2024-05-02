#!/bin/bash

function property {
    key_value=$(grep "^${1}=.*$" "${2}")
    echo "${key_value#*=}"
}

function gpg_long_key_id() {
    gpg --list-keys "$1" | sed -n 2p | xargs
}

function gpg_short_key_id() {
    gpg --list-keys --keyid-format short "$1" | awk '$1 == "pub" { print $2 }' | cut -d'/' -f2
}

function gpg_key() {
    gpg --pinentry-mode=loopback --passphrase="$2" --export-secret-keys --armor "$1" | grep -v '\-\-' | grep -v '^=.' | tr -d '\n'
}

function is_gpg_key_in_keyserver() {
    if [[ "$(gpg --batch --keyserver "$1" --search-key "$2" 2>&1)" == *"not found on keyserver"* ]]; then
        false
    else
        true
    fi
}
